package com.example.rinconsur.data

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.rinconsur.model.*

/**
 * Base de datos de la app, SQL escrito a mano,
 * `onCreate` que crea y precarga, y metodos que devuelven modelos (no cursores).
 *
 * El esquema es el modelo C01 v2 (15 tablas y 6 vistas), el mismo que quedo validado
 * contra los anexos del caso. El original MySQL/MariaDB y la traduccion a SQLite estan
 * documentados en `docs/base_tecnica.md` y en `docs/rincon_sur_sqlite.sql`.
 *
 * Las vistas hacen el trabajo pesado (conversion del dia, proporcion de transito de la
 * sesion, transito por bloque). La app las consulta en vez de recalcular: asi no puede
 * desviarse de lo que dice el modelo.
 */
class FlujoDbHelper(context: Context) :
    SQLiteOpenHelper(context, NOMBRE_BD, null, VERSION_BD) {

    companion object {
        private const val NOMBRE_BD = "rinconsur.db"
        private const val VERSION_BD = 4

        /** Estado con el que se abre y se cierra la sesion de conteo. */
        const val ESTADO_PROGRAMADA = 1L
    const val ESTADO_EN_CURSO = 2L
        const val ESTADO_FINALIZADA = 3L
        const val ESTADO_VALIDADA = 4L
        const val ESTADO_RECHAZADA = 5L

        /** Minimo de registros validos para poder validar una sesion (Anexo 8). */
        const val MINIMO_REGISTROS = 30

        private const val SQL_USUARIO =
            "SELECT u.*, r.codigo AS rol_codigo FROM usuario u JOIN rol r ON r.id_rol = u.id_rol "

        private const val SQL_SESIONES =
            "SELECT s.*, u.nombres || ' ' || u.apellido_paterno AS nombre_observador, " +
                "e.nombre AS estado_nombre, COALESCE(v.registros_validos, 0) AS registros_validos, " +
                "COALESCE(v.transito, 0) AS transito, COALESCE(v.visita, 0) AS visita, " +
                "COALESCE(v.no_determinado, 0) AS no_determinado, v.pct_transito " +
                "FROM sesion_observacion s " +
                "JOIN usuario u ON u.id_usuario = s.id_observador " +
                "JOIN estado_sesion e ON e.id_estado = s.id_estado " +
                "LEFT JOIN v_sesion_proporcion v ON v.id_sesion = s.id_sesion "

        // ---- esquema, vistas y datos de ejemplo del caso ----
        private val SQL_ESQUEMA: List<String> = listOf(
        // 15 tablas del modelo C01 v2
        """CREATE TABLE rol (
    id_rol      INT          NOT NULL,
    codigo      VARCHAR(5)   NOT NULL,
    nombre      VARCHAR(40)  NOT NULL,
    descripcion VARCHAR(200) NULL,
    CONSTRAINT pk_rol PRIMARY KEY (id_rol),
    CONSTRAINT uq_rol_codigo UNIQUE (codigo),
    CONSTRAINT uq_rol_nombre UNIQUE (nombre),
    CONSTRAINT ck_rol_codigo CHECK (codigo IN ('P-01','P-02','P-03','P-04'))
);""",
        """CREATE TABLE usuario (
    id_usuario INTEGER PRIMARY KEY AUTOINCREMENT,
    id_rol         INT          NOT NULL,
    rut            VARCHAR(12)  NOT NULL,
    nombres        VARCHAR(60)  NOT NULL,
    apellido_paterno VARCHAR(40) NOT NULL,
    apellido_materno VARCHAR(40) NULL,
    cargo          VARCHAR(60)  NOT NULL,
    correo         VARCHAR(100) NOT NULL,
    telefono       VARCHAR(20)  NULL,
    password_hash  VARCHAR(255) NOT NULL,
    activo         BOOLEAN      NOT NULL DEFAULT 1,
    fecha_creacion DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_usuario_rut UNIQUE (rut),
    CONSTRAINT uq_usuario_correo UNIQUE (correo),
    CONSTRAINT fk_usuario_rol FOREIGN KEY (id_rol) REFERENCES rol (id_rol)
);""",
        """CREATE TABLE acceso (
    id_acceso   INT          NOT NULL,
    codigo      CHAR(2)      NOT NULL,
    nombre      VARCHAR(40)  NOT NULL,
    ubicacion   VARCHAR(120) NOT NULL,
    observacion VARCHAR(200) NULL,
    CONSTRAINT pk_acceso PRIMARY KEY (id_acceso),
    CONSTRAINT uq_acceso_codigo UNIQUE (codigo)
);""",
        """CREATE TABLE bloque_horario (
    id_bloque   INT  NOT NULL,
    hora_inicio TIME NOT NULL,
    hora_fin    TIME NOT NULL,
    CONSTRAINT pk_bloque_horario PRIMARY KEY (id_bloque),
    CONSTRAINT uq_bloque_inicio UNIQUE (hora_inicio),
    CONSTRAINT ck_bloque_orden CHECK (hora_fin > hora_inicio)
);""",
        """CREATE TABLE conteo_ingreso (
    id_conteo INTEGER PRIMARY KEY AUTOINCREMENT,
    id_acceso            INT      NOT NULL,
    fecha                DATE     NOT NULL,
    id_bloque            INT      NOT NULL,
    cantidad             INT      NOT NULL,
    id_usuario_carga     INT      NOT NULL,
    fecha_carga          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    id_usuario_correccion INT     NULL,
    fecha_correccion     DATETIME NULL,
    CONSTRAINT uq_conteo_bloque UNIQUE (id_acceso, fecha, id_bloque),
    CONSTRAINT ck_conteo_cantidad CHECK (cantidad >= 0),
    CONSTRAINT ck_conteo_correccion CHECK ((id_usuario_correccion IS NULL) = (fecha_correccion IS NULL)),
    CONSTRAINT fk_conteo_acceso FOREIGN KEY (id_acceso) REFERENCES acceso (id_acceso),
    CONSTRAINT fk_conteo_bloque FOREIGN KEY (id_bloque) REFERENCES bloque_horario (id_bloque),
    CONSTRAINT fk_conteo_carga FOREIGN KEY (id_usuario_carga) REFERENCES usuario (id_usuario),
    CONSTRAINT fk_conteo_corrige FOREIGN KEY (id_usuario_correccion) REFERENCES usuario (id_usuario)
);""",
        """CREATE TABLE transaccion_bloque (
    id_transaccion INTEGER PRIMARY KEY AUTOINCREMENT,
    fecha            DATE     NOT NULL,
    id_bloque        INT      NOT NULL,
    cantidad         INT      NOT NULL,
    id_usuario_carga INT      NOT NULL,
    fecha_carga      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_transaccion_bloque UNIQUE (fecha, id_bloque),
    CONSTRAINT ck_transaccion_cantidad CHECK (cantidad >= 0),
    CONSTRAINT fk_transaccion_bloque FOREIGN KEY (id_bloque) REFERENCES bloque_horario (id_bloque),
    CONSTRAINT fk_transaccion_carga FOREIGN KEY (id_usuario_carga) REFERENCES usuario (id_usuario)
);""",
        """CREATE TABLE estado_sesion (
    id_estado INT         NOT NULL,
    nombre    VARCHAR(30) NOT NULL,
    CONSTRAINT pk_estado_sesion PRIMARY KEY (id_estado),
    CONSTRAINT uq_estado_nombre UNIQUE (nombre)
);""",
        """CREATE TABLE sesion_observacion (
    id_sesion INTEGER PRIMARY KEY AUTOINCREMENT,
    codigo          VARCHAR(10)  NOT NULL,
    id_acceso       INT          NOT NULL,
    fecha           DATE         NOT NULL,
    hora_inicio_prog TIME        NOT NULL,
    hora_fin_prog    TIME        NOT NULL,
    inicio_real     DATETIME     NULL,
    fin_real        DATETIME     NULL,
    id_estado       INT          NOT NULL,
    id_observador   INT          NOT NULL,
    id_programador  INT          NOT NULL,
    id_revisor      INT          NULL,
    fecha_revision  DATETIME     NULL,
    motivo_rechazo  VARCHAR(200) NULL,
    CONSTRAINT uq_sesion_codigo UNIQUE (codigo),
    CONSTRAINT ck_sesion_orden CHECK (hora_fin_prog > hora_inicio_prog),
    CONSTRAINT ck_sesion_revisor CHECK (id_revisor IS NULL OR id_revisor <> id_observador),
    CONSTRAINT ck_sesion_motivo CHECK (id_estado <> 5 OR motivo_rechazo IS NOT NULL),
    CONSTRAINT fk_sesion_acceso FOREIGN KEY (id_acceso) REFERENCES acceso (id_acceso),
    CONSTRAINT fk_sesion_estado FOREIGN KEY (id_estado) REFERENCES estado_sesion (id_estado),
    CONSTRAINT fk_sesion_observador FOREIGN KEY (id_observador) REFERENCES usuario (id_usuario),
    CONSTRAINT fk_sesion_programador FOREIGN KEY (id_programador) REFERENCES usuario (id_usuario),
    CONSTRAINT fk_sesion_revisor FOREIGN KEY (id_revisor) REFERENCES usuario (id_usuario)
);""",
        """CREATE TABLE clasificacion (
    id_clasificacion INT         NOT NULL,
    nombre           VARCHAR(30) NOT NULL,
    CONSTRAINT pk_clasificacion PRIMARY KEY (id_clasificacion),
    CONSTRAINT uq_clasificacion_nombre UNIQUE (nombre)
);""",
        """CREATE TABLE rango_etario (
    id_rango INT         NOT NULL,
    nombre   VARCHAR(30) NOT NULL,
    edad_min INT         NOT NULL,
    edad_max INT         NULL,
    orden    INT         NOT NULL,
    CONSTRAINT pk_rango PRIMARY KEY (id_rango),
    CONSTRAINT uq_rango_nombre UNIQUE (nombre),
    CONSTRAINT uq_rango_orden UNIQUE (orden),
    CONSTRAINT ck_rango_edad CHECK (edad_max IS NULL OR edad_max >= edad_min)
);""",
        """CREATE TABLE tipo_compania (
    id_tipo_compania INT         NOT NULL,
    nombre           VARCHAR(40) NOT NULL,
    CONSTRAINT pk_tipo_compania PRIMARY KEY (id_tipo_compania),
    CONSTRAINT uq_tipo_compania_nombre UNIQUE (nombre)
);""",
        """CREATE TABLE registro_observacion (
    id_registro INTEGER PRIMARY KEY AUTOINCREMENT,
    id_sesion           INT          NOT NULL,
    fecha_hora          DATETIME     NOT NULL,
    id_clasificacion    INT          NOT NULL,
    id_rango            INT          NOT NULL,
    id_tipo_compania    INT          NOT NULL,
    compras_otra_tienda BOOLEAN      NOT NULL DEFAULT 0,
    id_acceso_destino   INT          NULL,
    anulado             BOOLEAN      NOT NULL DEFAULT 0,
    id_usuario_anula    INT          NULL,
    fecha_anulacion     DATETIME     NULL,
    motivo_anulacion    VARCHAR(200) NULL,
    CONSTRAINT ck_registro_anulacion CHECK (anulado = 0 OR (id_usuario_anula IS NOT NULL AND motivo_anulacion IS NOT NULL)),
    CONSTRAINT fk_registro_sesion FOREIGN KEY (id_sesion) REFERENCES sesion_observacion (id_sesion),
    CONSTRAINT fk_registro_clasificacion FOREIGN KEY (id_clasificacion) REFERENCES clasificacion (id_clasificacion),
    CONSTRAINT fk_registro_rango FOREIGN KEY (id_rango) REFERENCES rango_etario (id_rango),
    CONSTRAINT fk_registro_compania FOREIGN KEY (id_tipo_compania) REFERENCES tipo_compania (id_tipo_compania),
    CONSTRAINT fk_registro_destino FOREIGN KEY (id_acceso_destino) REFERENCES acceso (id_acceso),
    CONSTRAINT fk_registro_anula FOREIGN KEY (id_usuario_anula) REFERENCES usuario (id_usuario)
);""",
        """CREATE TABLE tipo_evento (
    id_tipo_evento INT         NOT NULL,
    nombre         VARCHAR(40) NOT NULL,
    CONSTRAINT pk_tipo_evento PRIMARY KEY (id_tipo_evento),
    CONSTRAINT uq_tipo_evento_nombre UNIQUE (nombre)
);""",
        """CREATE TABLE evento_dia (
    id_evento INTEGER PRIMARY KEY AUTOINCREMENT,
    codigo          VARCHAR(6)   NOT NULL,
    fecha_inicio    DATE         NOT NULL,
    fecha_fin       DATE         NOT NULL,
    id_tipo_evento  INT          NOT NULL,
    id_acceso       INT          NULL,
    descripcion     VARCHAR(200) NOT NULL,
    id_usuario_registra INT      NOT NULL,
    fecha_registro  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
        foto                BLOB         NULL,
    CONSTRAINT uq_evento_codigo UNIQUE (codigo),
    CONSTRAINT ck_evento_fechas CHECK (fecha_fin >= fecha_inicio),
    CONSTRAINT fk_evento_tipo FOREIGN KEY (id_tipo_evento) REFERENCES tipo_evento (id_tipo_evento),
    CONSTRAINT fk_evento_acceso FOREIGN KEY (id_acceso) REFERENCES acceso (id_acceso),
    CONSTRAINT fk_evento_usuario FOREIGN KEY (id_usuario_registra) REFERENCES usuario (id_usuario)
);""",
        """CREATE TABLE meta_conversion (
    id_meta INTEGER PRIMARY KEY AUTOINCREMENT,
    anio               SMALLINT     NOT NULL,
    mes                TINYINT      NOT NULL,
    porcentaje         DECIMAL(5,2) NOT NULL,
    id_usuario_registra INT         NOT NULL,
    CONSTRAINT uq_meta_periodo UNIQUE (anio, mes),
    CONSTRAINT ck_meta_mes CHECK (mes BETWEEN 1 AND 12),
    CONSTRAINT ck_meta_porcentaje CHECK (porcentaje BETWEEN 0 AND 100),
    CONSTRAINT fk_meta_usuario FOREIGN KEY (id_usuario_registra) REFERENCES usuario (id_usuario)
);""",
        """CREATE TABLE pregunta_seguridad (
    id_pregunta INTEGER PRIMARY KEY AUTOINCREMENT,
    texto       VARCHAR(120) NOT NULL,
    activa      BOOLEAN      NOT NULL DEFAULT 1,
    CONSTRAINT uq_pregunta_texto UNIQUE (texto)
);""",
        """CREATE TABLE respuesta_seguridad (
    id_usuario     INT          NOT NULL,
    id_pregunta    INT          NOT NULL,
    respuesta_hash VARCHAR(255) NOT NULL,
    CONSTRAINT pk_respuesta_seguridad PRIMARY KEY (id_usuario, id_pregunta),
    CONSTRAINT fk_respuesta_usuario FOREIGN KEY (id_usuario) REFERENCES usuario (id_usuario),
    CONSTRAINT fk_respuesta_pregunta FOREIGN KEY (id_pregunta) REFERENCES pregunta_seguridad (id_pregunta)
);""",

        )

        private val SQL_VISTAS: List<String> = listOf(
        // 6 vistas: el calculo vive en SQL
        """CREATE VIEW v_tipo_dia AS
SELECT d.fecha,
       CASE WHEN (CAST(strftime('%w', d.fecha) AS INTEGER) + 1) IN (1, 7)
                 OR EXISTS (SELECT 1 FROM evento_dia e
                            JOIN tipo_evento t ON t.id_tipo_evento = e.id_tipo_evento
                            WHERE t.nombre = 'Feriado'
                              AND d.fecha BETWEEN e.fecha_inicio AND e.fecha_fin)
            THEN 'Fin de semana' ELSE 'Habil' END AS tipo_dia
FROM (SELECT fecha FROM conteo_ingreso UNION SELECT fecha FROM sesion_observacion) d;""",
        """CREATE VIEW v_sesion_proporcion AS
SELECT s.id_sesion, s.codigo, s.id_acceso, s.fecha, CAST(strftime('%H', s.hora_inicio_prog) AS INTEGER) AS hora_bloque,
       e.nombre AS estado,
       SUM(CASE WHEN r.anulado = 0 THEN 1 ELSE 0 END) AS registros_validos,
       SUM(CASE WHEN r.anulado = 0 AND c.id_clasificacion = 1 THEN 1 ELSE 0 END) AS transito,
       SUM(CASE WHEN r.anulado = 0 AND c.id_clasificacion = 2 THEN 1 ELSE 0 END) AS visita,
       SUM(CASE WHEN r.anulado = 0 AND c.id_clasificacion = 3 THEN 1 ELSE 0 END) AS no_determinado,
       ROUND(100.0 * SUM(CASE WHEN r.anulado = 0 AND c.id_clasificacion = 1 THEN 1 ELSE 0 END)
             / NULLIF(SUM(CASE WHEN r.anulado = 0 AND c.id_clasificacion <> 3 THEN 1 ELSE 0 END), 0), 1) AS pct_transito
FROM sesion_observacion s
JOIN estado_sesion e ON e.id_estado = s.id_estado
LEFT JOIN registro_observacion r ON r.id_sesion = s.id_sesion
LEFT JOIN clasificacion c ON c.id_clasificacion = r.id_clasificacion
GROUP BY s.id_sesion, s.codigo, s.id_acceso, s.fecha, CAST(strftime('%H', s.hora_inicio_prog) AS INTEGER), e.nombre;""",
        """CREATE VIEW v_transito_bloque AS
SELECT ci.fecha, td.tipo_dia, a.codigo AS acceso, bh.id_bloque, bh.hora_inicio, ci.cantidad AS ingresos,
       MAX(CASE WHEN sv.fecha = ci.fecha THEN sv.pct_transito END) AS pct_sesion,
       ROUND(AVG(CASE WHEN sv.fecha < ci.fecha THEN sv.pct_transito END), 1) AS pct_promedio,
       CASE WHEN MAX(CASE WHEN sv.fecha = ci.fecha THEN sv.pct_transito END) IS NOT NULL THEN 'medido'
            WHEN AVG(CASE WHEN sv.fecha < ci.fecha THEN sv.pct_transito END) IS NOT NULL THEN 'estimado'
            ELSE 'sin información' END AS origen,
       ROUND(ci.cantidad * COALESCE(MAX(CASE WHEN sv.fecha = ci.fecha THEN sv.pct_transito END),
                                    AVG(CASE WHEN sv.fecha < ci.fecha THEN sv.pct_transito END)) / 100) AS transito_estimado
FROM conteo_ingreso ci
JOIN acceso a ON a.id_acceso = ci.id_acceso
JOIN bloque_horario bh ON bh.id_bloque = ci.id_bloque
JOIN v_tipo_dia td ON td.fecha = ci.fecha
LEFT JOIN v_sesion_proporcion sv
       ON sv.id_acceso = ci.id_acceso AND sv.estado = 'Validada'
      AND ( (sv.fecha = ci.fecha AND sv.hora_bloque = CAST(strftime('%H', bh.hora_inicio) AS INTEGER))
         OR (sv.fecha < ci.fecha AND sv.fecha >= date(ci.fecha, '-28 day')
             AND EXISTS (SELECT 1 FROM v_tipo_dia t2 WHERE t2.fecha = sv.fecha AND t2.tipo_dia = td.tipo_dia)) )
GROUP BY ci.fecha, td.tipo_dia, a.codigo, bh.id_bloque, bh.hora_inicio, ci.cantidad;""",
        """CREATE VIEW v_conversion_dia AS
SELECT i.fecha, td.tipo_dia, i.ingresos, t.transacciones,
       ROUND(100.0 * t.transacciones / NULLIF(i.ingresos, 0), 1) AS conversion_bruta,
       CASE WHEN b.bloques = 44 AND b.accesos = 4 AND tb.bloques_tx = 11 THEN 'completo' ELSE 'incompleto' END AS dia,
       tr.transito, i.ingresos - tr.transito AS flujo_sin_transito,
       CASE WHEN tr.sin_informacion = 0
            THEN ROUND(100.0 * t.transacciones / NULLIF(i.ingresos - tr.transito, 0), 1) END AS conversion_sin_transito,
       tr.medidos, tr.estimados, tr.sin_informacion
FROM (SELECT fecha, SUM(cantidad) AS ingresos FROM conteo_ingreso GROUP BY fecha) i
JOIN (SELECT fecha, SUM(cantidad) AS transacciones FROM transaccion_bloque GROUP BY fecha) t ON t.fecha = i.fecha
JOIN (SELECT fecha, COUNT(*) AS bloques, COUNT(DISTINCT id_acceso) AS accesos FROM conteo_ingreso GROUP BY fecha) b ON b.fecha = i.fecha
LEFT JOIN (SELECT fecha, COUNT(*) AS bloques_tx FROM transaccion_bloque GROUP BY fecha) tb ON tb.fecha = i.fecha
LEFT JOIN (SELECT fecha, SUM(transito_estimado) AS transito,
                  SUM(origen = 'medido') AS medidos, SUM(origen = 'estimado') AS estimados,
                  SUM(origen = 'sin información') AS sin_informacion
           FROM v_transito_bloque GROUP BY fecha) tr ON tr.fecha = i.fecha
LEFT JOIN v_tipo_dia td ON td.fecha = i.fecha;""",
        """CREATE VIEW v_perfil_transito AS
SELECT a.codigo AS acceso, s.fecha, td.tipo_dia, rg.nombre AS rango_etario, tc.nombre AS compania,
       r.compras_otra_tienda, COUNT(*) AS personas
FROM registro_observacion r
JOIN sesion_observacion s ON s.id_sesion = r.id_sesion
JOIN estado_sesion e ON e.id_estado = s.id_estado
JOIN acceso a ON a.id_acceso = s.id_acceso
JOIN clasificacion c ON c.id_clasificacion = r.id_clasificacion
JOIN rango_etario rg ON rg.id_rango = r.id_rango
JOIN tipo_compania tc ON tc.id_tipo_compania = r.id_tipo_compania
LEFT JOIN v_tipo_dia td ON td.fecha = s.fecha
WHERE r.anulado = 0 AND e.id_estado = 4 AND c.id_clasificacion = 'Transito'
GROUP BY a.codigo, s.fecha, td.tipo_dia, rg.nombre, tc.nombre, r.compras_otra_tienda;""",
        """CREATE VIEW v_pendiente_medir AS
SELECT a.codigo AS acceso, ci.fecha, bh.hora_inicio, ult.ultima AS ultima_sesion_valida,
       CASE WHEN ult.ultima IS NULL THEN NULL ELSE CAST(julianday(ci.fecha) - julianday(ult.ultima) AS INTEGER) END AS dias_sin_medir,
       CASE WHEN ult.ultima IS NULL THEN NULL ELSE ROUND(CAST(julianday(ci.fecha) - julianday(ult.ultima) AS INTEGER) / 7, 1) END AS semanas_sin_medir
FROM conteo_ingreso ci
JOIN acceso a ON a.id_acceso = ci.id_acceso
JOIN bloque_horario bh ON bh.id_bloque = ci.id_bloque
LEFT JOIN (SELECT s.id_acceso, CAST(strftime('%H', s.hora_inicio_prog) AS INTEGER) AS hb, MAX(s.fecha) AS ultima
           FROM sesion_observacion s JOIN estado_sesion e ON e.id_estado = s.id_estado
           WHERE e.nombre = 'Validada'
           GROUP BY s.id_acceso, CAST(strftime('%H', s.hora_inicio_prog) AS INTEGER)) ult
       ON ult.id_acceso = ci.id_acceso AND ult.hb = CAST(strftime('%H', bh.hora_inicio) AS INTEGER)
WHERE NOT EXISTS (SELECT 1 FROM sesion_observacion s2 JOIN estado_sesion e2 ON e2.id_estado = s2.id_estado
                  WHERE s2.id_acceso = ci.id_acceso AND s2.fecha = ci.fecha
                    AND CAST(strftime('%H', s2.hora_inicio_prog) AS INTEGER) = CAST(strftime('%H', bh.hora_inicio) AS INTEGER) AND e2.nombre = 'Validada');""",
        )

        private val SQL_DATOS: List<String> = listOf(
        // datos de ejemplo: 23 INSERT, el UPDATE de anulados y los 7 UPDATE del hash de la clave
        """INSERT INTO rol (id_rol, codigo, nombre, descripcion) VALUES
  (1, 'P-01', 'Jefa de tienda', 'Consulta indicadores, historial y perfil; registra la meta'),
  (2, 'P-02', 'Subgerente comercial', 'Administra sesiones, valida, carga ingresos y transacciones, gestiona colaboradores'),
  (3, 'P-03', 'Supervisor de piso', 'Programa y valida sesiones, registra eventos, puede observar'),
  (4, 'P-04', 'Colaborador observador', 'Registra y anula sus propios registros en sesion en curso');""",
        """INSERT INTO usuario (id_usuario, id_rol, rut, nombres, apellido_paterno, apellido_materno, cargo, correo, telefono, password_hash, activo) VALUES
  (1, 1, '15.234.871-1', 'Carolina', 'Fuentes', 'Araya', 'Jefa de tienda', 'carolina.fuentes@rinconsur-ficticio.ejemplo', '+56 9 0000 0011', '${'$'}2a${'$'}10${'$'}semilla.demo.no.usar.en.produccion', 1),
  (2, 2, '13.987.442-0', 'Rodrigo', 'Saavedra', 'Munoz', 'Subgerente comercial', 'rodrigo.saavedra@rinconsur-ficticio.ejemplo', '+56 9 0000 0012', '${'$'}2a${'$'}10${'$'}semilla.demo.no.usar.en.produccion', 1),
  (3, 3, '17.654.320-5', 'Daniela', 'Pizarro', 'Toro', 'Supervisora de piso (turno manana)', 'daniela.pizarro@rinconsur-ficticio.ejemplo', '+56 9 0000 0013', '${'$'}2a${'$'}10${'$'}semilla.demo.no.usar.en.produccion', 1),
  (4, 3, '16.443.019-7', 'Matias', 'Contreras', 'Vidal', 'Supervisor de piso (turno tarde)', 'matias.contreras@rinconsur-ficticio.ejemplo', '+56 9 0000 0014', '${'$'}2a${'$'}10${'$'}semilla.demo.no.usar.en.produccion', 1),
  (5, 4, '19.876.345-4', 'Tamara', 'Rojas', 'Espinoza', 'Vendedora, Vestuario Mujer', 'tamara.rojas@rinconsur-ficticio.ejemplo', '+56 9 0000 0015', '${'$'}2a${'$'}10${'$'}semilla.demo.no.usar.en.produccion', 1),
  (6, 4, '20.145.678-9', 'Felipe', 'Ibarra', 'Soto', 'Vendedor, Hogar y Deco', 'felipe.ibarra@rinconsur-ficticio.ejemplo', '+56 9 0000 0016', '${'$'}2a${'$'}10${'$'}semilla.demo.no.usar.en.produccion', 1),
  (7, 4, '18.322.904-4', 'Javiera', 'Morales', 'Caceres', 'Cajera, Cajas Centrales', 'javiera.morales@rinconsur-ficticio.ejemplo', '+56 9 0000 0017', '${'$'}2a${'$'}10${'$'}semilla.demo.no.usar.en.produccion', 1);""",
        """INSERT INTO acceso (id_acceso, codigo, nombre, ubicacion, observacion) VALUES
  (1, 'A1', 'Acceso Principal', 'Frente al pasillo central del mall', 'Entrada natural de quienes vienen del mall'),
  (2, 'A2', 'Acceso Lateral', 'Costado del sector patio de comidas', 'Flujo alto a la hora de almuerzo'),
  (3, 'A3', 'Acceso Estacionamiento', 'Conecta con el estacionamiento subterraneo', 'Zona con senal de red debil'),
  (4, 'A4', 'Acceso Conexión', 'Comunica con la tienda vecina de mejoramiento del hogar', 'Principal punto de transito entre la tienda vecina y el mall');""",
        """INSERT INTO bloque_horario (id_bloque, hora_inicio, hora_fin) VALUES
  (1, '10:00:00', '10:59:59'),
  (2, '11:00:00', '11:59:59'),
  (3, '12:00:00', '12:59:59'),
  (4, '13:00:00', '13:59:59'),
  (5, '14:00:00', '14:59:59'),
  (6, '15:00:00', '15:59:59'),
  (7, '16:00:00', '16:59:59'),
  (8, '17:00:00', '17:59:59'),
  (9, '18:00:00', '18:59:59'),
  (10, '19:00:00', '19:59:59'),
  (11, '20:00:00', '20:59:59');""",
        """INSERT INTO conteo_ingreso (id_acceso, fecha, id_bloque, cantidad, id_usuario_carga) VALUES
  (1, '2026-09-15', 1, 95, 2),
  (1, '2026-09-15', 2, 140, 2),
  (1, '2026-09-15', 3, 180, 2),
  (1, '2026-09-15', 4, 260, 2),
  (1, '2026-09-15', 5, 240, 2),
  (1, '2026-09-15', 6, 190, 2),
  (1, '2026-09-15', 7, 175, 2),
  (1, '2026-09-15', 8, 200, 2),
  (1, '2026-09-15', 9, 230, 2),
  (1, '2026-09-15', 10, 210, 2),
  (1, '2026-09-15', 11, 120, 2),
  (2, '2026-09-15', 1, 40, 2),
  (2, '2026-09-15', 2, 60, 2),
  (2, '2026-09-15', 3, 85, 2),
  (2, '2026-09-15', 4, 150, 2),
  (2, '2026-09-15', 5, 170, 2),
  (2, '2026-09-15', 6, 110, 2),
  (2, '2026-09-15', 7, 90, 2),
  (2, '2026-09-15', 8, 95, 2),
  (2, '2026-09-15', 9, 120, 2),
  (2, '2026-09-15', 10, 115, 2),
  (2, '2026-09-15', 11, 70, 2),
  (3, '2026-09-15', 1, 30, 2),
  (3, '2026-09-15', 2, 45, 2),
  (3, '2026-09-15', 3, 60, 2),
  (3, '2026-09-15', 4, 70, 2),
  (3, '2026-09-15', 5, 65, 2),
  (3, '2026-09-15', 6, 60, 2),
  (3, '2026-09-15', 7, 70, 2),
  (3, '2026-09-15', 8, 85, 2),
  (3, '2026-09-15', 9, 110, 2),
  (3, '2026-09-15', 10, 95, 2),
  (3, '2026-09-15', 11, 40, 2),
  (4, '2026-09-15', 1, 70, 2),
  (4, '2026-09-15', 2, 110, 2),
  (4, '2026-09-15', 3, 160, 2),
  (4, '2026-09-15', 4, 210, 2),
  (4, '2026-09-15', 5, 200, 2),
  (4, '2026-09-15', 6, 180, 2),
  (4, '2026-09-15', 7, 170, 2),
  (4, '2026-09-15', 8, 190, 2),
  (4, '2026-09-15', 9, 240, 2),
  (4, '2026-09-15', 10, 220, 2),
  (4, '2026-09-15', 11, 110, 2);""",
        """INSERT INTO transaccion_bloque (fecha, id_bloque, cantidad, id_usuario_carga) VALUES
  ('2026-09-15', 1, 18, 2),
  ('2026-09-15', 2, 30, 2),
  ('2026-09-15', 3, 42, 2),
  ('2026-09-15', 4, 55, 2),
  ('2026-09-15', 5, 60, 2),
  ('2026-09-15', 6, 48, 2),
  ('2026-09-15', 7, 44, 2),
  ('2026-09-15', 8, 50, 2),
  ('2026-09-15', 9, 62, 2),
  ('2026-09-15', 10, 58, 2),
  ('2026-09-15', 11, 30, 2);""",
        """INSERT INTO estado_sesion (id_estado, nombre) VALUES
  (1, 'Programada'),
  (2, 'En curso'),
  (3, 'Finalizada'),
  (4, 'Validada'),
  (5, 'Rechazada'),
  (6, 'Cancelada');""",
        """INSERT INTO sesion_observacion (id_sesion, codigo, id_acceso, fecha, hora_inicio_prog, hora_fin_prog, inicio_real, fin_real, id_estado, id_observador, id_programador, id_revisor, fecha_revision, motivo_rechazo) VALUES
  (1, 'S-0001', 4, '2026-09-15', '13:05:00', '13:50:00', '2026-09-15 13:05:00', '2026-09-15 13:50:00', 4, 5, 3, 3, '2026-09-15 20:00:00', NULL),
  (2, 'S-0002', 1, '2026-09-15', '18:10:00', '18:55:00', '2026-09-15 18:10:00', '2026-09-15 18:55:00', 4, 6, 3, 4, '2026-09-15 20:00:00', NULL),
  (3, 'S-0003', 4, '2026-09-12', '17:00:00', '17:45:00', '2026-09-12 17:00:00', '2026-09-12 17:45:00', 4, 7, 3, 4, '2026-09-12 20:00:00', NULL),
  (4, 'S-0004', 2, '2026-09-12', '13:00:00', '13:40:00', '2026-09-12 13:00:00', '2026-09-12 13:40:00', 5, 6, 3, 3, '2026-09-12 20:00:00', 'Muestra insuficiente: 14 registros, el minimo es 30'),
  (5, 'S-0005', 3, '2026-09-22', '11:00:00', '11:45:00', '2026-09-22 11:00:00', '2026-09-22 11:45:00', 3, 5, 3, NULL, NULL, NULL),
  (6, 'S-0006', 4, '2026-10-08', '12:00:00', '12:45:00', NULL, NULL, 1, 7, 3, NULL, NULL, NULL);""",
        """INSERT INTO clasificacion (id_clasificacion, nombre) VALUES
  (1, 'Tránsito'),
  (2, 'Visita'),
  (3, 'No determinado');""",
        """INSERT INTO rango_etario (id_rango, nombre, edad_min, edad_max, orden) VALUES
  (1, 'Menor de 18 años', 0, 17, 1),
  (2, 'De 18 a 25 años', 18, 25, 2),
  (3, 'De 26 a 40 años', 26, 40, 3),
  (4, 'De 41 a 60 años', 41, 60, 4),
  (5, '61 años o más', 61, NULL, 5);""",
        """INSERT INTO tipo_compania (id_tipo_compania, nombre) VALUES
  (1, 'Solo'),
  (2, 'Pareja'),
  (3, 'Con niños'),
  (4, 'En grupo de 3 o más');""",
        """INSERT INTO tipo_evento (id_tipo_evento, nombre) VALUES
  (1, 'Clima'),
  (2, 'Feriado'),
  (3, 'Evento en el mall'),
  (4, 'Cierre de acceso'),
  (5, 'Campaña comercial'),
  (6, 'Otro');""",
        """INSERT INTO evento_dia (id_evento, codigo, fecha_inicio, fecha_fin, id_tipo_evento, id_acceso, descripcion, id_usuario_registra) VALUES
  (1, 'E-01', '2026-09-10', '2026-09-17', 5, NULL, 'Campana de Fiestas Patrias en tienda', 2),
  (2, 'E-02', '2026-09-12', '2026-09-12', 3, 1, 'Activacion de marca en el pasillo central del mall, 16:00 a 19:00', 2),
  (3, 'E-03', '2026-09-18', '2026-09-19', 2, NULL, 'Feriados irrenunciables: tienda cerrada', 2),
  (4, 'E-04', '2026-09-22', '2026-09-22', 1, NULL, 'Lluvia intensa durante toda la tarde', 4),
  (5, 'E-05', '2026-10-01', '2026-10-01', 4, 3, 'Acceso cerrado de 10:00 a 13:00 por mantencion del estacionamiento', 4),
  (6, 'E-06', '2026-10-03', '2026-10-03', 5, NULL, 'Inicio de liquidacion de temporada', 2);""",
        """INSERT INTO meta_conversion (id_meta, anio, mes, porcentaje, id_usuario_registra) VALUES
  (1, 2026, 8, 9.5, 1),
  (2, 2026, 9, 10.0, 1),
  (3, 2026, 10, 10.0, 2);""",
        """INSERT INTO registro_observacion (id_sesion, fecha_hora, id_clasificacion, id_rango, id_tipo_compania, compras_otra_tienda, id_acceso_destino) VALUES
  (1, '2026-09-15 13:05:00', 1, 3, 1, 1, 1),
  (1, '2026-09-15 13:05:15', 1, 4, 2, 0, 2),
  (1, '2026-09-15 13:05:30', 1, 2, 3, 1, 3),
  (1, '2026-09-15 13:05:45', 1, 5, 4, 0, 1),
  (1, '2026-09-15 13:06:00', 1, 1, 1, 1, 2),
  (1, '2026-09-15 13:06:15', 1, 3, 2, 0, 3),
  (1, '2026-09-15 13:06:30', 1, 4, 3, 1, 1),
  (1, '2026-09-15 13:06:45', 1, 2, 4, 0, 2),
  (1, '2026-09-15 13:07:00', 1, 5, 1, 1, 3),
  (1, '2026-09-15 13:07:15', 1, 1, 2, 0, 1),
  (1, '2026-09-15 13:07:30', 1, 3, 3, 1, 2),
  (1, '2026-09-15 13:07:45', 1, 4, 4, 0, 3),
  (1, '2026-09-15 13:08:00', 1, 2, 1, 1, 1),
  (1, '2026-09-15 13:08:15', 1, 5, 2, 0, 2),
  (1, '2026-09-15 13:08:30', 1, 1, 3, 1, 3),
  (1, '2026-09-15 13:08:45', 1, 3, 4, 0, 1),
  (1, '2026-09-15 13:09:00', 1, 4, 1, 1, 2),
  (1, '2026-09-15 13:09:15', 1, 2, 2, 0, 3),
  (1, '2026-09-15 13:09:30', 1, 5, 3, 1, 1),
  (1, '2026-09-15 13:09:45', 1, 1, 4, 0, 2),
  (1, '2026-09-15 13:10:00', 1, 3, 1, 1, 3),
  (1, '2026-09-15 13:10:15', 1, 4, 2, 0, 1),
  (1, '2026-09-15 13:10:30', 1, 2, 3, 1, 2),
  (1, '2026-09-15 13:10:45', 1, 5, 4, 0, 3),
  (1, '2026-09-15 13:11:00', 1, 1, 1, 1, 1),
  (1, '2026-09-15 13:11:15', 1, 3, 2, 0, 2),
  (1, '2026-09-15 13:11:30', 1, 4, 3, 1, 3),
  (1, '2026-09-15 13:11:45', 1, 2, 4, 0, 1),
  (1, '2026-09-15 13:12:00', 1, 5, 1, 1, 2),
  (1, '2026-09-15 13:12:15', 1, 1, 2, 0, 3),
  (1, '2026-09-15 13:12:30', 1, 3, 3, 1, 1),
  (1, '2026-09-15 13:12:45', 1, 4, 4, 0, 2),
  (1, '2026-09-15 13:13:00', 1, 2, 1, 1, 3),
  (1, '2026-09-15 13:13:15', 1, 5, 2, 0, 1),
  (1, '2026-09-15 13:13:30', 1, 1, 3, 1, 2),
  (1, '2026-09-15 13:13:45', 1, 3, 4, 0, 3),
  (1, '2026-09-15 13:14:00', 1, 4, 1, 1, 1),
  (1, '2026-09-15 13:14:15', 1, 2, 2, 0, 2),
  (1, '2026-09-15 13:14:30', 1, 5, 3, 1, 3),
  (1, '2026-09-15 13:14:45', 1, 1, 4, 0, 1),
  (1, '2026-09-15 13:15:00', 1, 3, 1, 1, 2),
  (1, '2026-09-15 13:15:15', 1, 4, 2, 0, 3),
  (1, '2026-09-15 13:15:30', 1, 2, 3, 1, 1),
  (1, '2026-09-15 13:15:45', 1, 5, 4, 0, 2),
  (1, '2026-09-15 13:16:00', 1, 1, 1, 1, 3),
  (1, '2026-09-15 13:16:15', 1, 3, 2, 0, 1),
  (1, '2026-09-15 13:16:30', 1, 4, 3, 1, 2),
  (1, '2026-09-15 13:16:45', 1, 2, 4, 0, 3),
  (1, '2026-09-15 13:17:00', 1, 5, 1, 1, 1),
  (1, '2026-09-15 13:17:15', 1, 1, 2, 0, 2);""",
        """INSERT INTO registro_observacion (id_sesion, fecha_hora, id_clasificacion, id_rango, id_tipo_compania, compras_otra_tienda, id_acceso_destino) VALUES
  (1, '2026-09-15 13:17:30', 1, 3, 3, 1, 3),
  (1, '2026-09-15 13:17:45', 1, 4, 4, 0, 1),
  (1, '2026-09-15 13:18:00', 1, 2, 1, 1, 2),
  (1, '2026-09-15 13:18:15', 1, 5, 2, 0, 3),
  (1, '2026-09-15 13:18:30', 1, 1, 3, 1, 1),
  (1, '2026-09-15 13:18:45', 1, 3, 4, 0, 2),
  (1, '2026-09-15 13:19:00', 1, 4, 1, 1, 3),
  (1, '2026-09-15 13:19:15', 1, 2, 2, 0, 1),
  (1, '2026-09-15 13:19:30', 1, 5, 3, 1, 2),
  (1, '2026-09-15 13:19:45', 1, 1, 4, 0, 3),
  (1, '2026-09-15 13:20:00', 1, 3, 1, 1, 1),
  (1, '2026-09-15 13:20:15', 2, 4, 2, 0, NULL),
  (1, '2026-09-15 13:20:30', 2, 2, 3, 0, NULL),
  (1, '2026-09-15 13:20:45', 2, 5, 4, 0, NULL),
  (1, '2026-09-15 13:21:00', 2, 1, 1, 0, NULL),
  (1, '2026-09-15 13:21:15', 2, 3, 2, 0, NULL),
  (1, '2026-09-15 13:21:30', 2, 4, 3, 0, NULL),
  (1, '2026-09-15 13:21:45', 2, 2, 4, 0, NULL),
  (1, '2026-09-15 13:22:00', 2, 5, 1, 0, NULL),
  (1, '2026-09-15 13:22:15', 2, 1, 2, 0, NULL),
  (1, '2026-09-15 13:22:30', 2, 3, 3, 0, NULL),
  (1, '2026-09-15 13:22:45', 2, 4, 4, 0, NULL),
  (1, '2026-09-15 13:23:00', 2, 2, 1, 0, NULL),
  (1, '2026-09-15 13:23:15', 2, 5, 2, 0, NULL),
  (1, '2026-09-15 13:23:30', 2, 1, 3, 0, NULL),
  (1, '2026-09-15 13:23:45', 2, 3, 4, 0, NULL),
  (1, '2026-09-15 13:24:00', 2, 4, 1, 0, NULL),
  (1, '2026-09-15 13:24:15', 2, 2, 2, 0, NULL),
  (1, '2026-09-15 13:24:30', 2, 5, 3, 0, NULL),
  (1, '2026-09-15 13:24:45', 2, 1, 4, 0, NULL),
  (1, '2026-09-15 13:25:00', 2, 3, 1, 0, NULL),
  (1, '2026-09-15 13:25:15', 2, 4, 2, 0, NULL),
  (1, '2026-09-15 13:25:30', 2, 2, 3, 0, NULL),
  (1, '2026-09-15 13:25:45', 2, 5, 4, 0, NULL),
  (1, '2026-09-15 13:26:00', 2, 1, 1, 0, NULL),
  (1, '2026-09-15 13:26:15', 2, 3, 2, 0, NULL),
  (1, '2026-09-15 13:26:30', 2, 4, 3, 0, NULL),
  (1, '2026-09-15 13:26:45', 2, 2, 4, 0, NULL),
  (1, '2026-09-15 13:27:00', 2, 5, 1, 0, NULL),
  (1, '2026-09-15 13:27:15', 2, 1, 2, 0, NULL),
  (1, '2026-09-15 13:27:30', 2, 3, 3, 0, NULL),
  (1, '2026-09-15 13:27:45', 2, 4, 4, 0, NULL),
  (1, '2026-09-15 13:28:00', 2, 2, 1, 0, NULL),
  (1, '2026-09-15 13:28:15', 2, 5, 2, 0, NULL),
  (1, '2026-09-15 13:28:30', 2, 1, 3, 0, NULL),
  (1, '2026-09-15 13:28:45', 2, 3, 4, 0, NULL),
  (1, '2026-09-15 13:29:00', 2, 4, 1, 0, NULL),
  (1, '2026-09-15 13:29:15', 2, 2, 2, 0, NULL),
  (1, '2026-09-15 13:29:30', 2, 5, 3, 0, NULL),
  (1, '2026-09-15 13:29:45', 2, 1, 4, 0, NULL);""",
        """INSERT INTO registro_observacion (id_sesion, fecha_hora, id_clasificacion, id_rango, id_tipo_compania, compras_otra_tienda, id_acceso_destino) VALUES
  (1, '2026-09-15 13:30:00', 2, 3, 1, 0, NULL),
  (1, '2026-09-15 13:30:15', 2, 4, 2, 0, NULL),
  (1, '2026-09-15 13:30:30', 2, 2, 3, 0, NULL),
  (1, '2026-09-15 13:30:45', 2, 5, 4, 0, NULL),
  (1, '2026-09-15 13:31:00', 2, 1, 1, 0, NULL),
  (1, '2026-09-15 13:31:15', 2, 3, 2, 0, NULL),
  (1, '2026-09-15 13:31:30', 2, 4, 3, 0, NULL),
  (1, '2026-09-15 13:31:45', 2, 2, 4, 0, NULL),
  (1, '2026-09-15 13:32:00', 2, 5, 1, 0, NULL),
  (1, '2026-09-15 13:32:15', 2, 1, 2, 0, NULL),
  (1, '2026-09-15 13:32:30', 2, 3, 3, 0, NULL),
  (1, '2026-09-15 13:32:45', 2, 4, 4, 0, NULL),
  (1, '2026-09-15 13:33:00', 2, 2, 1, 0, NULL),
  (1, '2026-09-15 13:33:15', 3, 5, 2, 0, NULL),
  (1, '2026-09-15 13:33:30', 3, 1, 3, 0, NULL),
  (1, '2026-09-15 13:33:45', 3, 3, 4, 0, NULL),
  (1, '2026-09-15 13:34:00', 3, 4, 1, 0, NULL),
  (1, '2026-09-15 13:34:15', 3, 2, 2, 0, NULL),
  (2, '2026-09-15 18:10:00', 1, 3, 1, 1, 2),
  (2, '2026-09-15 18:10:15', 1, 4, 2, 0, 3),
  (2, '2026-09-15 18:10:30', 1, 2, 3, 1, 4),
  (2, '2026-09-15 18:10:45', 1, 5, 4, 0, 2),
  (2, '2026-09-15 18:11:00', 1, 1, 1, 1, 3),
  (2, '2026-09-15 18:11:15', 1, 3, 2, 0, 4),
  (2, '2026-09-15 18:11:30', 1, 4, 3, 1, 2),
  (2, '2026-09-15 18:11:45', 1, 2, 4, 0, 3),
  (2, '2026-09-15 18:12:00', 1, 5, 1, 1, 4),
  (2, '2026-09-15 18:12:15', 1, 1, 2, 0, 2),
  (2, '2026-09-15 18:12:30', 1, 3, 3, 1, 3),
  (2, '2026-09-15 18:12:45', 1, 4, 4, 0, 4),
  (2, '2026-09-15 18:13:00', 1, 2, 1, 1, 2),
  (2, '2026-09-15 18:13:15', 1, 5, 2, 0, 3),
  (2, '2026-09-15 18:13:30', 1, 1, 3, 1, 4),
  (2, '2026-09-15 18:13:45', 1, 3, 4, 0, 2),
  (2, '2026-09-15 18:14:00', 1, 4, 1, 1, 3),
  (2, '2026-09-15 18:14:15', 1, 2, 2, 0, 4),
  (2, '2026-09-15 18:14:30', 1, 5, 3, 1, 2),
  (2, '2026-09-15 18:14:45', 1, 1, 4, 0, 3),
  (2, '2026-09-15 18:15:00', 1, 3, 1, 1, 4),
  (2, '2026-09-15 18:15:15', 1, 4, 2, 0, 2),
  (2, '2026-09-15 18:15:30', 1, 2, 3, 1, 3),
  (2, '2026-09-15 18:15:45', 1, 5, 4, 0, 4),
  (2, '2026-09-15 18:16:00', 1, 1, 1, 1, 2),
  (2, '2026-09-15 18:16:15', 1, 3, 2, 0, 3),
  (2, '2026-09-15 18:16:30', 1, 4, 3, 1, 4),
  (2, '2026-09-15 18:16:45', 1, 2, 4, 0, 2),
  (2, '2026-09-15 18:17:00', 1, 5, 1, 1, 3),
  (2, '2026-09-15 18:17:15', 1, 1, 2, 0, 4),
  (2, '2026-09-15 18:17:30', 1, 3, 3, 1, 2),
  (2, '2026-09-15 18:17:45', 1, 4, 4, 0, 3);""",
        """INSERT INTO registro_observacion (id_sesion, fecha_hora, id_clasificacion, id_rango, id_tipo_compania, compras_otra_tienda, id_acceso_destino) VALUES
  (2, '2026-09-15 18:18:00', 1, 2, 1, 1, 4),
  (2, '2026-09-15 18:18:15', 1, 5, 2, 0, 2),
  (2, '2026-09-15 18:18:30', 1, 1, 3, 1, 3),
  (2, '2026-09-15 18:18:45', 1, 3, 4, 0, 4),
  (2, '2026-09-15 18:19:00', 1, 4, 1, 1, 2),
  (2, '2026-09-15 18:19:15', 1, 2, 2, 0, 3),
  (2, '2026-09-15 18:19:30', 2, 5, 3, 0, NULL),
  (2, '2026-09-15 18:19:45', 2, 1, 4, 0, NULL),
  (2, '2026-09-15 18:20:00', 2, 3, 1, 0, NULL),
  (2, '2026-09-15 18:20:15', 2, 4, 2, 0, NULL),
  (2, '2026-09-15 18:20:30', 2, 2, 3, 0, NULL),
  (2, '2026-09-15 18:20:45', 2, 5, 4, 0, NULL),
  (2, '2026-09-15 18:21:00', 2, 1, 1, 0, NULL),
  (2, '2026-09-15 18:21:15', 2, 3, 2, 0, NULL),
  (2, '2026-09-15 18:21:30', 2, 4, 3, 0, NULL),
  (2, '2026-09-15 18:21:45', 2, 2, 4, 0, NULL),
  (2, '2026-09-15 18:22:00', 2, 5, 1, 0, NULL),
  (2, '2026-09-15 18:22:15', 2, 1, 2, 0, NULL),
  (2, '2026-09-15 18:22:30', 2, 3, 3, 0, NULL),
  (2, '2026-09-15 18:22:45', 2, 4, 4, 0, NULL),
  (2, '2026-09-15 18:23:00', 2, 2, 1, 0, NULL),
  (2, '2026-09-15 18:23:15', 2, 5, 2, 0, NULL),
  (2, '2026-09-15 18:23:30', 2, 1, 3, 0, NULL),
  (2, '2026-09-15 18:23:45', 2, 3, 4, 0, NULL),
  (2, '2026-09-15 18:24:00', 2, 4, 1, 0, NULL),
  (2, '2026-09-15 18:24:15', 2, 2, 2, 0, NULL),
  (2, '2026-09-15 18:24:30', 2, 5, 3, 0, NULL),
  (2, '2026-09-15 18:24:45', 2, 1, 4, 0, NULL),
  (2, '2026-09-15 18:25:00', 2, 3, 1, 0, NULL),
  (2, '2026-09-15 18:25:15', 2, 4, 2, 0, NULL),
  (2, '2026-09-15 18:25:30', 2, 2, 3, 0, NULL),
  (2, '2026-09-15 18:25:45', 2, 5, 4, 0, NULL),
  (2, '2026-09-15 18:26:00', 2, 1, 1, 0, NULL),
  (2, '2026-09-15 18:26:15', 2, 3, 2, 0, NULL),
  (2, '2026-09-15 18:26:30', 2, 4, 3, 0, NULL),
  (2, '2026-09-15 18:26:45', 2, 2, 4, 0, NULL),
  (2, '2026-09-15 18:27:00', 2, 5, 1, 0, NULL),
  (2, '2026-09-15 18:27:15', 2, 1, 2, 0, NULL),
  (2, '2026-09-15 18:27:30', 2, 3, 3, 0, NULL),
  (2, '2026-09-15 18:27:45', 2, 4, 4, 0, NULL),
  (2, '2026-09-15 18:28:00', 2, 2, 1, 0, NULL),
  (2, '2026-09-15 18:28:15', 2, 5, 2, 0, NULL),
  (2, '2026-09-15 18:28:30', 2, 1, 3, 0, NULL),
  (2, '2026-09-15 18:28:45', 2, 3, 4, 0, NULL),
  (2, '2026-09-15 18:29:00', 2, 4, 1, 0, NULL),
  (2, '2026-09-15 18:29:15', 2, 2, 2, 0, NULL),
  (2, '2026-09-15 18:29:30', 2, 5, 3, 0, NULL),
  (2, '2026-09-15 18:29:45', 2, 1, 4, 0, NULL),
  (2, '2026-09-15 18:30:00', 2, 3, 1, 0, NULL),
  (2, '2026-09-15 18:30:15', 2, 4, 2, 0, NULL);""",
        """INSERT INTO registro_observacion (id_sesion, fecha_hora, id_clasificacion, id_rango, id_tipo_compania, compras_otra_tienda, id_acceso_destino) VALUES
  (2, '2026-09-15 18:30:30', 2, 2, 3, 0, NULL),
  (2, '2026-09-15 18:30:45', 2, 5, 4, 0, NULL),
  (2, '2026-09-15 18:31:00', 2, 1, 1, 0, NULL),
  (2, '2026-09-15 18:31:15', 2, 3, 2, 0, NULL),
  (2, '2026-09-15 18:31:30', 2, 4, 3, 0, NULL),
  (2, '2026-09-15 18:31:45', 2, 2, 4, 0, NULL),
  (2, '2026-09-15 18:32:00', 2, 5, 1, 0, NULL),
  (2, '2026-09-15 18:32:15', 2, 1, 2, 0, NULL),
  (2, '2026-09-15 18:32:30', 2, 3, 3, 0, NULL),
  (2, '2026-09-15 18:32:45', 2, 4, 4, 0, NULL),
  (2, '2026-09-15 18:33:00', 2, 2, 1, 0, NULL),
  (2, '2026-09-15 18:33:15', 2, 5, 2, 0, NULL),
  (2, '2026-09-15 18:33:30', 2, 1, 3, 0, NULL),
  (2, '2026-09-15 18:33:45', 2, 3, 4, 0, NULL),
  (2, '2026-09-15 18:34:00', 2, 4, 1, 0, NULL),
  (2, '2026-09-15 18:34:15', 2, 2, 2, 0, NULL),
  (2, '2026-09-15 18:34:30', 2, 5, 3, 0, NULL),
  (2, '2026-09-15 18:34:45', 2, 1, 4, 0, NULL),
  (2, '2026-09-15 18:35:00', 2, 3, 1, 0, NULL),
  (2, '2026-09-15 18:35:15', 3, 4, 2, 0, NULL),
  (2, '2026-09-15 18:35:30', 3, 2, 3, 0, NULL),
  (2, '2026-09-15 18:35:45', 3, 5, 4, 0, NULL),
  (3, '2026-09-12 17:01', 1, 3, 1, 1, 1),
  (3, '2026-09-12 17:01', 1, 4, 2, 1, 1),
  (3, '2026-09-12 17:02', 2, 3, 3, 0, NULL),
  (3, '2026-09-12 17:03', 1, 2, 4, 0, 2),
  (3, '2026-09-12 17:04', 2, 5, 2, 0, NULL),
  (3, '2026-09-12 17:04', 1, 4, 1, 1, 3),
  (3, '2026-09-12 17:05', 3, 3, 1, 0, NULL),
  (3, '2026-09-12 17:06', 1, 3, 3, 1, 1),
  (3, '2026-09-12 17:07', 2, 2, 1, 0, NULL),
  (3, '2026-09-12 17:08', 1, 1, 3, 0, 1),
  (3, '2026-09-12 17:02:30', 1, 3, 3, 1, 2),
  (3, '2026-09-12 17:02:45', 1, 4, 4, 0, 3),
  (3, '2026-09-12 17:03:00', 1, 2, 1, 1, 1),
  (3, '2026-09-12 17:03:15', 1, 5, 2, 0, 2),
  (3, '2026-09-12 17:03:30', 1, 1, 3, 1, 3),
  (3, '2026-09-12 17:03:45', 1, 3, 4, 0, 1),
  (3, '2026-09-12 17:04:00', 1, 4, 1, 1, 2),
  (3, '2026-09-12 17:04:15', 1, 2, 2, 0, 3),
  (3, '2026-09-12 17:04:30', 1, 5, 3, 1, 1),
  (3, '2026-09-12 17:04:45', 1, 1, 4, 0, 2),
  (3, '2026-09-12 17:05:00', 1, 3, 1, 1, 3),
  (3, '2026-09-12 17:05:15', 1, 4, 2, 0, 1),
  (3, '2026-09-12 17:05:30', 1, 2, 3, 1, 2),
  (3, '2026-09-12 17:05:45', 1, 5, 4, 0, 3),
  (3, '2026-09-12 17:06:00', 1, 1, 1, 1, 1),
  (3, '2026-09-12 17:06:15', 1, 3, 2, 0, 2),
  (3, '2026-09-12 17:06:30', 1, 4, 3, 1, 3),
  (3, '2026-09-12 17:06:45', 1, 2, 4, 0, 1);""",
        """INSERT INTO registro_observacion (id_sesion, fecha_hora, id_clasificacion, id_rango, id_tipo_compania, compras_otra_tienda, id_acceso_destino) VALUES
  (3, '2026-09-12 17:07:00', 1, 5, 1, 1, 2),
  (3, '2026-09-12 17:07:15', 1, 1, 2, 0, 3),
  (3, '2026-09-12 17:07:30', 1, 3, 3, 1, 1),
  (3, '2026-09-12 17:07:45', 1, 4, 4, 0, 2),
  (3, '2026-09-12 17:08:00', 1, 2, 1, 1, 3),
  (3, '2026-09-12 17:08:15', 1, 5, 2, 0, 1),
  (3, '2026-09-12 17:08:30', 1, 1, 3, 1, 2),
  (3, '2026-09-12 17:08:45', 1, 3, 4, 0, 3),
  (3, '2026-09-12 17:09:00', 1, 4, 1, 1, 1),
  (3, '2026-09-12 17:09:15', 1, 2, 2, 0, 2),
  (3, '2026-09-12 17:09:30', 1, 5, 3, 1, 3),
  (3, '2026-09-12 17:09:45', 1, 1, 4, 0, 1),
  (3, '2026-09-12 17:10:00', 1, 3, 1, 1, 2),
  (3, '2026-09-12 17:10:15', 1, 4, 2, 0, 3),
  (3, '2026-09-12 17:10:30', 1, 2, 3, 1, 1),
  (3, '2026-09-12 17:10:45', 1, 5, 4, 0, 2),
  (3, '2026-09-12 17:11:00', 1, 1, 1, 1, 3),
  (3, '2026-09-12 17:11:15', 1, 3, 2, 0, 1),
  (3, '2026-09-12 17:11:30', 1, 4, 3, 1, 2),
  (3, '2026-09-12 17:11:45', 1, 2, 4, 0, 3),
  (3, '2026-09-12 17:12:00', 1, 5, 1, 1, 1),
  (3, '2026-09-12 17:12:15', 1, 1, 2, 0, 2),
  (3, '2026-09-12 17:12:30', 1, 3, 3, 1, 3),
  (3, '2026-09-12 17:12:45', 1, 4, 4, 0, 1),
  (3, '2026-09-12 17:13:00', 1, 2, 1, 1, 2),
  (3, '2026-09-12 17:13:15', 1, 5, 2, 0, 3),
  (3, '2026-09-12 17:13:30', 1, 1, 3, 1, 1),
  (3, '2026-09-12 17:13:45', 1, 3, 4, 0, 2),
  (3, '2026-09-12 17:14:00', 1, 4, 1, 1, 3),
  (3, '2026-09-12 17:14:15', 1, 2, 2, 0, 1),
  (3, '2026-09-12 17:14:30', 1, 5, 3, 1, 2),
  (3, '2026-09-12 17:14:45', 1, 1, 4, 0, 3),
  (3, '2026-09-12 17:15:00', 1, 3, 1, 1, 1),
  (3, '2026-09-12 17:15:15', 1, 4, 2, 0, 2),
  (3, '2026-09-12 17:15:30', 1, 2, 3, 1, 3),
  (3, '2026-09-12 17:15:45', 1, 5, 4, 0, 1),
  (3, '2026-09-12 17:16:00', 1, 1, 1, 1, 2),
  (3, '2026-09-12 17:16:15', 1, 3, 2, 0, 3),
  (3, '2026-09-12 17:16:30', 1, 4, 3, 1, 1),
  (3, '2026-09-12 17:16:45', 1, 2, 4, 0, 2),
  (3, '2026-09-12 17:17:00', 1, 5, 1, 1, 3),
  (3, '2026-09-12 17:17:15', 1, 1, 2, 0, 1),
  (3, '2026-09-12 17:17:30', 1, 3, 3, 1, 2),
  (3, '2026-09-12 17:17:45', 1, 4, 4, 0, 3),
  (3, '2026-09-12 17:18:00', 1, 2, 1, 1, 1),
  (3, '2026-09-12 17:18:15', 1, 5, 2, 0, 2),
  (3, '2026-09-12 17:18:30', 1, 1, 3, 1, 3),
  (3, '2026-09-12 17:18:45', 1, 3, 4, 0, 1),
  (3, '2026-09-12 17:19:00', 1, 4, 1, 1, 2),
  (3, '2026-09-12 17:19:15', 1, 2, 2, 0, 3);""",
        """INSERT INTO registro_observacion (id_sesion, fecha_hora, id_clasificacion, id_rango, id_tipo_compania, compras_otra_tienda, id_acceso_destino) VALUES
  (3, '2026-09-12 17:19:30', 1, 5, 3, 1, 1),
  (3, '2026-09-12 17:19:45', 1, 1, 4, 0, 2),
  (3, '2026-09-12 17:20:00', 1, 3, 1, 1, 3),
  (3, '2026-09-12 17:20:15', 1, 4, 2, 0, 1),
  (3, '2026-09-12 17:20:30', 1, 2, 3, 1, 2),
  (3, '2026-09-12 17:20:45', 2, 5, 4, 0, NULL),
  (3, '2026-09-12 17:21:00', 2, 1, 1, 0, NULL),
  (3, '2026-09-12 17:21:15', 2, 3, 2, 0, NULL),
  (3, '2026-09-12 17:21:30', 2, 4, 3, 0, NULL),
  (3, '2026-09-12 17:21:45', 2, 2, 4, 0, NULL),
  (3, '2026-09-12 17:22:00', 2, 5, 1, 0, NULL),
  (3, '2026-09-12 17:22:15', 2, 1, 2, 0, NULL),
  (3, '2026-09-12 17:22:30', 2, 3, 3, 0, NULL),
  (3, '2026-09-12 17:22:45', 2, 4, 4, 0, NULL),
  (3, '2026-09-12 17:23:00', 2, 2, 1, 0, NULL),
  (3, '2026-09-12 17:23:15', 2, 5, 2, 0, NULL),
  (3, '2026-09-12 17:23:30', 2, 1, 3, 0, NULL),
  (3, '2026-09-12 17:23:45', 2, 3, 4, 0, NULL),
  (3, '2026-09-12 17:24:00', 2, 4, 1, 0, NULL),
  (3, '2026-09-12 17:24:15', 2, 2, 2, 0, NULL),
  (3, '2026-09-12 17:24:30', 2, 5, 3, 0, NULL),
  (3, '2026-09-12 17:24:45', 2, 1, 4, 0, NULL),
  (3, '2026-09-12 17:25:00', 2, 3, 1, 0, NULL),
  (3, '2026-09-12 17:25:15', 2, 4, 2, 0, NULL),
  (3, '2026-09-12 17:25:30', 2, 2, 3, 0, NULL),
  (3, '2026-09-12 17:25:45', 2, 5, 4, 0, NULL),
  (3, '2026-09-12 17:26:00', 2, 1, 1, 0, NULL),
  (3, '2026-09-12 17:26:15', 2, 3, 2, 0, NULL),
  (3, '2026-09-12 17:26:30', 2, 4, 3, 0, NULL),
  (3, '2026-09-12 17:26:45', 2, 2, 4, 0, NULL),
  (3, '2026-09-12 17:27:00', 2, 5, 1, 0, NULL),
  (3, '2026-09-12 17:27:15', 2, 1, 2, 0, NULL),
  (3, '2026-09-12 17:27:30', 2, 3, 3, 0, NULL),
  (3, '2026-09-12 17:27:45', 2, 4, 4, 0, NULL),
  (3, '2026-09-12 17:28:00', 2, 2, 1, 0, NULL),
  (3, '2026-09-12 17:28:15', 2, 5, 2, 0, NULL),
  (3, '2026-09-12 17:28:30', 2, 1, 3, 0, NULL),
  (3, '2026-09-12 17:28:45', 2, 3, 4, 0, NULL),
  (3, '2026-09-12 17:29:00', 2, 4, 1, 0, NULL),
  (3, '2026-09-12 17:29:15', 2, 2, 2, 0, NULL),
  (3, '2026-09-12 17:29:30', 2, 5, 3, 0, NULL),
  (3, '2026-09-12 17:29:45', 2, 1, 4, 0, NULL),
  (3, '2026-09-12 17:30:00', 2, 3, 1, 0, NULL),
  (3, '2026-09-12 17:30:15', 2, 4, 2, 0, NULL),
  (3, '2026-09-12 17:30:30', 2, 2, 3, 0, NULL),
  (3, '2026-09-12 17:30:45', 2, 5, 4, 0, NULL),
  (3, '2026-09-12 17:31:00', 2, 1, 1, 0, NULL),
  (3, '2026-09-12 17:31:15', 2, 3, 2, 0, NULL),
  (3, '2026-09-12 17:31:30', 2, 4, 3, 0, NULL),
  (3, '2026-09-12 17:31:45', 2, 2, 4, 0, NULL);""",
        """INSERT INTO registro_observacion (id_sesion, fecha_hora, id_clasificacion, id_rango, id_tipo_compania, compras_otra_tienda, id_acceso_destino) VALUES
  (3, '2026-09-12 17:32:00', 2, 5, 1, 0, NULL),
  (3, '2026-09-12 17:32:15', 2, 1, 2, 0, NULL),
  (3, '2026-09-12 17:32:30', 2, 3, 3, 0, NULL),
  (3, '2026-09-12 17:32:45', 2, 4, 4, 0, NULL),
  (3, '2026-09-12 17:33:00', 2, 2, 1, 0, NULL),
  (3, '2026-09-12 17:33:15', 2, 5, 2, 0, NULL),
  (3, '2026-09-12 17:33:30', 2, 1, 3, 0, NULL),
  (3, '2026-09-12 17:33:45', 2, 3, 4, 0, NULL),
  (3, '2026-09-12 17:34:00', 2, 4, 1, 0, NULL),
  (3, '2026-09-12 17:34:15', 2, 2, 2, 0, NULL),
  (3, '2026-09-12 17:34:30', 3, 5, 3, 0, NULL),
  (3, '2026-09-12 17:34:45', 3, 1, 4, 0, NULL),
  (3, '2026-09-12 17:35:00', 3, 3, 1, 0, NULL),
  (3, '2026-09-12 17:35:15', 3, 4, 2, 0, NULL),
  (4, '2026-09-12 13:00:00', 1, 3, 1, 1, 1),
  (4, '2026-09-12 13:00:15', 1, 4, 2, 0, 3),
  (4, '2026-09-12 13:00:30', 1, 2, 3, 1, 4),
  (4, '2026-09-12 13:00:45', 1, 5, 4, 0, 1),
  (4, '2026-09-12 13:01:00', 2, 1, 1, 0, NULL),
  (4, '2026-09-12 13:01:15', 2, 3, 2, 0, NULL),
  (4, '2026-09-12 13:01:30', 2, 4, 3, 0, NULL),
  (4, '2026-09-12 13:01:45', 2, 2, 4, 0, NULL),
  (4, '2026-09-12 13:02:00', 2, 5, 1, 0, NULL),
  (4, '2026-09-12 13:02:15', 2, 1, 2, 0, NULL),
  (4, '2026-09-12 13:02:30', 2, 3, 3, 0, NULL),
  (4, '2026-09-12 13:02:45', 2, 4, 4, 0, NULL),
  (4, '2026-09-12 13:03:00', 2, 2, 1, 0, NULL),
  (4, '2026-09-12 13:03:15', 3, 5, 2, 0, NULL),
  (5, '2026-09-22 11:00:00', 1, 3, 1, 1, 1),
  (5, '2026-09-22 11:00:15', 1, 4, 2, 0, 2),
  (5, '2026-09-22 11:00:30', 1, 2, 3, 1, 4),
  (5, '2026-09-22 11:00:45', 1, 5, 4, 0, 1),
  (5, '2026-09-22 11:01:00', 1, 1, 1, 1, 2),
  (5, '2026-09-22 11:01:15', 1, 3, 2, 0, 4),
  (5, '2026-09-22 11:01:30', 1, 4, 3, 1, 1),
  (5, '2026-09-22 11:01:45', 2, 2, 4, 0, NULL),
  (5, '2026-09-22 11:02:00', 2, 5, 1, 0, NULL),
  (5, '2026-09-22 11:02:15', 2, 1, 2, 0, NULL),
  (5, '2026-09-22 11:02:30', 2, 3, 3, 0, NULL),
  (5, '2026-09-22 11:02:45', 2, 4, 4, 0, NULL),
  (5, '2026-09-22 11:03:00', 2, 2, 1, 0, NULL),
  (5, '2026-09-22 11:03:15', 2, 5, 2, 0, NULL),
  (5, '2026-09-22 11:03:30', 2, 1, 3, 0, NULL),
  (5, '2026-09-22 11:03:45', 2, 3, 4, 0, NULL),
  (5, '2026-09-22 11:04:00', 2, 4, 1, 0, NULL),
  (5, '2026-09-22 11:04:15', 2, 2, 2, 0, NULL),
  (5, '2026-09-22 11:04:30', 2, 5, 3, 0, NULL),
  (5, '2026-09-22 11:04:45', 2, 1, 4, 0, NULL),
  (5, '2026-09-22 11:05:00', 2, 3, 1, 0, NULL),
  (5, '2026-09-22 11:05:15', 2, 4, 2, 0, NULL);""",
        """INSERT INTO registro_observacion (id_sesion, fecha_hora, id_clasificacion, id_rango, id_tipo_compania, compras_otra_tienda, id_acceso_destino) VALUES
  (5, '2026-09-22 11:05:30', 2, 2, 3, 0, NULL),
  (5, '2026-09-22 11:05:45', 2, 5, 4, 0, NULL),
  (5, '2026-09-22 11:06:00', 2, 1, 1, 0, NULL),
  (5, '2026-09-22 11:06:15', 2, 3, 2, 0, NULL),
  (5, '2026-09-22 11:06:30', 2, 4, 3, 0, NULL),
  (5, '2026-09-22 11:06:45', 2, 2, 4, 0, NULL),
  (5, '2026-09-22 11:07:00', 2, 5, 1, 0, NULL),
  (5, '2026-09-22 11:07:15', 2, 1, 2, 0, NULL),
  (5, '2026-09-22 11:07:30', 2, 3, 3, 0, NULL),
  (5, '2026-09-22 11:07:45', 2, 4, 4, 0, NULL),
  (5, '2026-09-22 11:08:00', 2, 2, 1, 0, NULL),
  (5, '2026-09-22 11:08:15', 2, 5, 2, 0, NULL),
  (5, '2026-09-22 11:08:30', 3, 1, 3, 0, NULL),
  (5, '2026-09-22 11:08:45', 3, 3, 4, 0, NULL),
  (1, '2026-09-15 13:20:00', 1, 3, 1, 0, 1),
  (1, '2026-09-15 13:24:00', 2, 3, 1, 0, NULL);""",
        """UPDATE registro_observacion
   SET anulado = 1, id_usuario_anula = 3, fecha_anulacion = '2026-09-15 13:35:00',
       motivo_anulacion = 'Registro duplicado: la misma persona ya estaba contada'
 WHERE id_registro IN (415, 416);""",
        """UPDATE usuario SET password_hash = 'e62b302fc482e6b882a258d3e32765076af609b882f0c98f493ebe1c0dddf8eb' WHERE rut = '13.987.442-0';""",
        """UPDATE usuario SET password_hash = 'c72033a4a579c24fa24ca3fcfae9314da208be61b33c5e85c15a9adc6ad2217b' WHERE rut = '15.234.871-1';""",
        """UPDATE usuario SET password_hash = '297de3e3c3510021fe722754a64a9a80d81b36f2f716d380c62f276c2d88ca5f' WHERE rut = '16.443.019-7';""",
        """UPDATE usuario SET password_hash = '606b61a6db5c4ec29f240888f40de8e47f367e6bf0aad85ff9dac911156bed6b' WHERE rut = '17.654.320-5';""",
        """UPDATE usuario SET password_hash = '3be196465177bea9d42580f65fd1b71b26aca25fed5e2346470148c727bcbd00' WHERE rut = '18.322.904-4';""",
        """UPDATE usuario SET password_hash = 'e1553d2b50bf5930127ad34bc182803cf351ae61de120a71d85da0fa2a1c78f1' WHERE rut = '19.876.345-4';""",
        """UPDATE usuario SET password_hash = '640c5d498a480b562da0e3bd40334b2df49a24ddca1b41f9e52449534668d5ee' WHERE rut = '20.145.678-9';""",
        """INSERT INTO pregunta_seguridad (id_pregunta, texto) VALUES
  (1, '¿Cuál es el nombre de tu primera mascota?'),
  (2, '¿En qué ciudad nació tu madre?'),
  (3, '¿Cuál fue tu primer colegio?'),
  (4, '¿Cuál fue tu primer trabajo?'),
  (5, '¿Cuál es tu comida favorita?'),
  (6, '¿Cómo se llamaba tu mejor amigo de la infancia?');""",

        )
    }

    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        db.setForeignKeyConstraintsEnabled(true)
    }

    override fun onCreate(db: SQLiteDatabase) {
        SQL_ESQUEMA.forEach { db.execSQL(it) }
        SQL_VISTAS.forEach { db.execSQL(it) }
        SQL_DATOS.forEach { db.execSQL(it) }
    }

    /**
     * Migrar conservando los datos: ALTER TABLE, nunca DROP, para no borrar lo que ya esta guardado en el telefono.
     * Cada version nueva agrega su bloque, y la app que ya estaba instalada no pierde nada.
     */
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            // Version 2: la foto de evidencia del evento (recurso nativo de camara y galeria)
            db.execSQL("ALTER TABLE evento_dia ADD COLUMN foto BLOB")
        }
        if (oldVersion < 4) {
            db.execSQL("ALTER TABLE usuario ADD COLUMN foto BLOB")
        }
        if (oldVersion < 3) {
            // Version 3: preguntas de seguridad para recuperar la clave sin correo. Se crean y se
            // siembran aca porque una base que ya existe no vuelve a pasar por onCreate.
            db.execSQL("""CREATE TABLE pregunta_seguridad (
    id_pregunta INTEGER PRIMARY KEY AUTOINCREMENT,
    texto       VARCHAR(120) NOT NULL,
    activa      BOOLEAN      NOT NULL DEFAULT 1,
    foto BLOB,
    CONSTRAINT uq_pregunta_texto UNIQUE (texto)
)""")
            db.execSQL("""CREATE TABLE respuesta_seguridad (
    id_usuario     INT          NOT NULL,
    id_pregunta    INT          NOT NULL,
    respuesta_hash VARCHAR(255) NOT NULL,
    CONSTRAINT pk_respuesta_seguridad PRIMARY KEY (id_usuario, id_pregunta),
    CONSTRAINT fk_respuesta_usuario FOREIGN KEY (id_usuario) REFERENCES usuario (id_usuario),
    CONSTRAINT fk_respuesta_pregunta FOREIGN KEY (id_pregunta) REFERENCES pregunta_seguridad (id_pregunta)
)""")
            db.execSQL("""INSERT INTO pregunta_seguridad (id_pregunta, texto) VALUES
  (1, '¿Cuál es el nombre de tu primera mascota?'),
  (2, '¿En qué ciudad nació tu madre?'),
  (3, '¿Cuál fue tu primer colegio?'),
  (4, '¿Cuál fue tu primer trabajo?'),
  (5, '¿Cuál es tu comida favorita?'),
  (6, '¿Cómo se llamaba tu mejor amigo de la infancia?')""")
        }
    }

    // ==================== catalogos ====================

    fun listarAccesos(): List<Acceso> {
        val lista = mutableListOf<Acceso>()
        readableDatabase.rawQuery("SELECT * FROM acceso ORDER BY id_acceso", null).use { c ->
            while (c.moveToNext()) {
                lista.add(
                    Acceso(
                        id = c.long("id_acceso"),
                        codigo = c.texto("codigo"),
                        nombre = c.texto("nombre"),
                        ubicacion = c.texto("ubicacion"),
                        observacion = c.texto("observacion")
                    )
                )
            }
        }
        return lista
    }

    fun listarBloques(): List<BloqueHorario> {
        val lista = mutableListOf<BloqueHorario>()
        readableDatabase.rawQuery("SELECT * FROM bloque_horario ORDER BY id_bloque", null).use { c ->
            while (c.moveToNext()) {
                lista.add(BloqueHorario(c.long("id_bloque"), c.texto("hora_inicio"), c.texto("hora_fin")))
            }
        }
        return lista
    }

    fun listarRangosEtarios(): List<Pair<RangoEtario, String>> =
        // El rango es siempre uno de los cinco del anexo: la edad no se pregunta, se estima (RN-07)
        RangoEtario.entries.map { it to it.etiqueta }

    fun listarCompanias(): List<Compania> = Compania.entries

    /**
     * La fecha de hoy en el telefono. Con esta se fechan las sesiones y los eventos: el trabajo se
     * hace el dia que se hace, no el ultimo dia que alguien cargo ingresos.
     */
    fun fechaHoy(): String =
        java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date())

    /** El dia con datos de ingresos (el anexo): es el que usan los indicadores. */
    fun fechaDeTrabajo(): String =
        readableDatabase.rawQuery("SELECT MAX(fecha) AS fecha FROM conteo_ingreso", null).use { c ->
            if (c.moveToNext()) c.texto("fecha") else ""
        }

    // ==================== sesiones ====================

    fun buscarColaboradorPorRut(rut: String): Colaborador? =
        consultarColaboradores(SQL_USUARIO + "WHERE u.rut = ?", arrayOf(rut)).firstOrNull()

    fun buscarColaboradorPorId(id: Long): Colaborador? =
        consultarColaboradores(SQL_USUARIO + "WHERE u.id_usuario = ?", arrayOf(id.toString())).firstOrNull()

    /**
     * Cambia el nombre de la persona. Son los dos campos que la app muestra: el nombre en la barra,
     * en el perfil y en la lista de observadores, y el apellido junto al nombre en la lista de
     * sesiones del supervisor. El RUT y el rol no se tocan: identifican la cuenta.
     */
    fun cambiarNombre(idUsuario: Long, nombres: String, apellidoPaterno: String): Int {
        val valores = ContentValues().apply {
            put("nombres", nombres)
            put("apellido_paterno", apellidoPaterno)
        }
        return writableDatabase.update("usuario", valores, "id_usuario = ?", arrayOf(idUsuario.toString()))
    }

    fun sesionEnCursoDe(idObservador: Long): SesionResumen? =
        consultarSesiones(SQL_SESIONES + "WHERE s.id_observador = ? AND s.id_estado = ?", arrayOf(idObservador.toString(), ESTADO_EN_CURSO.toString())).firstOrNull()

    fun sesionesDeFecha(fecha: String): List<SesionResumen> =
        consultarSesiones(SQL_SESIONES + "WHERE s.fecha = ? ORDER BY s.hora_inicio_prog", arrayOf(fecha))

    /** Una sesion por su id, sin importar la fecha (la pantalla de rechazo la recibe por la ruta). */
    fun buscarSesion(idSesion: Long): SesionResumen? =
        consultarSesiones(SQL_SESIONES + "WHERE s.id_sesion = ?", arrayOf(idSesion.toString())).firstOrNull()

    /** Los colaboradores que pueden contar: el rol de observador, sin numeros fijos. */
    // ==================== seguridad de la clave ====================

    /** Preguntas del catalogo, para que cada persona elija dos en su perfil. */
    fun listarPreguntas(): List<PreguntaSeguridad> {
        val lista = mutableListOf<PreguntaSeguridad>()
        readableDatabase.rawQuery(
            "SELECT id_pregunta, texto FROM pregunta_seguridad WHERE activa = 1 ORDER BY id_pregunta", null
        ).use { c -> while (c.moveToNext()) lista.add(PreguntaSeguridad(id = c.getLong(0), texto = c.getString(1))) }
        return lista
    }

    /** Las preguntas que esa persona ya tiene guardadas, para mostrarlas al recuperar la clave. */
    fun listarPreguntasDe(idUsuario: Long): List<PreguntaSeguridad> {
        val lista = mutableListOf<PreguntaSeguridad>()
        readableDatabase.rawQuery(
            """SELECT p.id_pregunta, p.texto FROM respuesta_seguridad r
               JOIN pregunta_seguridad p ON p.id_pregunta = r.id_pregunta
               WHERE r.id_usuario = ? ORDER BY p.id_pregunta""",
            arrayOf(idUsuario.toString())
        ).use { c -> while (c.moveToNext()) lista.add(PreguntaSeguridad(id = c.getLong(0), texto = c.getString(1))) }
        return lista
    }

    /** Si ya eligio sus preguntas: sin esto no puede recuperar la clave. */
    fun tienePreguntas(idUsuario: Long): Boolean =
        readableDatabase.rawQuery(
            "SELECT COUNT(*) FROM respuesta_seguridad WHERE id_usuario = ?", arrayOf(idUsuario.toString())
        ).use { c -> c.moveToFirst() && c.getInt(0) >= 1 }

    /**
     * Guarda las respuestas elegidas: se guarda el hash, nunca el texto. La respuesta se normaliza
     * (sin espacios al borde, en minusculas) para que "Firulais" y "firulais" valgan igual.
     */
    fun guardarRespuestas(idUsuario: Long, rut: String, respuestas: List<Pair<Long, String>>) {
        writableDatabase.beginTransaction()
        try {
            writableDatabase.delete("respuesta_seguridad", "id_usuario = ?", arrayOf(idUsuario.toString()))
            respuestas.forEach { (idPregunta, respuesta) ->
                val valores = ContentValues().apply {
                    put("id_usuario", idUsuario)
                    put("id_pregunta", idPregunta)
                    put("respuesta_hash", Clave.hash(rut, normalizarRespuesta(respuesta)))
                }
                writableDatabase.insert("respuesta_seguridad", null, valores)
            }
            writableDatabase.setTransactionSuccessful()
        } finally {
            writableDatabase.endTransaction()
        }
    }

    /** Las respuestas coinciden con las guardadas. */
    fun verificarRespuestas(idUsuario: Long, rut: String, respuestas: List<Pair<Long, String>>): Boolean {
        if (respuestas.isEmpty()) return false
        var correctas = 0
        respuestas.forEach { (idPregunta, respuesta) ->
            readableDatabase.rawQuery(
                "SELECT respuesta_hash FROM respuesta_seguridad WHERE id_usuario = ? AND id_pregunta = ?",
                arrayOf(idUsuario.toString(), idPregunta.toString())
            ).use { c ->
                if (c.moveToFirst() && c.getString(0) == Clave.hash(rut, normalizarRespuesta(respuesta))) correctas++
            }
        }
        return correctas == respuestas.size
    }

    /** Cambia la clave. Recibe el hash ya calculado: la clave en texto no pasa por aca. */
    /** Guarda la foto de perfil ya recortada y reducida. Pasar null la borra. */
    fun guardarFoto(idUsuario: Long, foto: ByteArray?): Boolean {
        val valores = ContentValues().apply {
            if (foto == null || foto.isEmpty()) putNull("foto") else put("foto", foto)
        }
        return writableDatabase.update(
            "usuario", valores, "id_usuario = ?", arrayOf(idUsuario.toString())
        ) == 1
    }

    /** La foto de perfil guardada, o null si esa persona no tiene. */
    fun fotoDe(idUsuario: Long): ByteArray? {
        readableDatabase.query(
            "usuario", arrayOf("foto"), "id_usuario = ?", arrayOf(idUsuario.toString()),
            null, null, null
        ).use { c ->
            if (c.moveToFirst() && !c.isNull(0)) return c.getBlob(0)
        }
        return null
    }

    fun cambiarClave(idUsuario: Long, nuevoHash: String): Boolean =
        writableDatabase.update(
            "usuario", ContentValues().apply { put("password_hash", nuevoHash) },
            "id_usuario = ?", arrayOf(idUsuario.toString())
        ) == 1

    /** Normalizacion de una respuesta, igual al guardar que al verificar. */
    private fun normalizarRespuesta(respuesta: String): String = respuesta.trim().lowercase()

    fun listarObservadores(): List<Colaborador> =
        consultarColaboradores(
            SQL_USUARIO + "WHERE u.id_rol = (SELECT id_rol FROM rol WHERE nombre LIKE '%observador%')",
            arrayOf()
        )

    /**
     * Programa una sesion de conteo. Queda en estado Programada con sus horas previstas y quien la
     * programo; las horas reales las escribe el observador cuando la inicia.
     */
    fun programarSesion(
        idAcceso: Long,
        fecha: String,
        horaInicio: String,
        horaFin: String,
        idObservador: Long,
        idProgramador: Long
    ): String {
        val siguiente = readableDatabase
            .rawQuery("SELECT COALESCE(MAX(id_sesion), 0) + 1 AS siguiente FROM sesion_observacion", null)
            .use { c -> if (c.moveToNext()) c.long("siguiente") else 1L }
        val codigo = "S-" + siguiente.toString().padStart(4, '0')
        val valores = ContentValues().apply {
            put("id_sesion", siguiente)
            put("codigo", codigo)
            put("id_acceso", idAcceso)
            put("fecha", fecha)
            put("hora_inicio_prog", horaInicio)
            put("hora_fin_prog", horaFin)
            put("id_estado", ESTADO_PROGRAMADA)
            put("id_observador", idObservador)
            put("id_programador", idProgramador)
        }
        writableDatabase.insert("sesion_observacion", null, valores)
        return codigo
    }

    /** Todas las sesiones cerradas que esperan revision, de cualquier fecha. */
    fun sesionesPorValidarTodas(): List<SesionResumen> =
        consultarSesiones(SQL_SESIONES + "WHERE s.id_estado = ? ORDER BY s.fecha DESC, s.hora_inicio_prog",
            arrayOf(ESTADO_FINALIZADA.toString()))

    /** Las sesiones desde una fecha hacia atras (para el filtro Semana). */
    fun sesionesDeLosUltimosDias(desde: String, dias: Int): List<SesionResumen> =
        consultarSesiones(
            SQL_SESIONES + "WHERE s.fecha <= ? AND s.fecha >= date(?, ?) ORDER BY s.fecha DESC, s.hora_inicio_prog",
            arrayOf(desde, desde, "-" + dias + " day")
        )

    fun sesionesPorValidar(fecha: String): List<SesionResumen> =
        consultarSesiones(SQL_SESIONES + "WHERE s.fecha = ? AND s.id_estado = ?", arrayOf(fecha, ESTADO_FINALIZADA.toString()))

    fun resumenDeSesion(idSesion: Long): ResumenSesion {
        val sesion = consultarSesiones(SQL_SESIONES + "WHERE s.id_sesion = ?", arrayOf(idSesion.toString())).firstOrNull()
            ?: return ResumenSesion(codigo = "")
        return ResumenSesion(
            codigo = sesion.codigo,
            transito = sesion.transito,
            visita = sesion.visita,
            noDeterminado = sesion.noDeterminado
        )
    }

    /** Arranca la sesion programada: queda En curso y con su hora real de inicio. */
    fun iniciarSesion(idSesion: Long, hora: String): Int =
        writableDatabase.update(
            "sesion_observacion",
            ContentValues().apply {
                put("inicio_real", hora)
                put("id_estado", ESTADO_EN_CURSO)
            },
            "id_sesion = ?", arrayOf(idSesion.toString())
        )

    fun cerrarSesionReal(idSesion: Long, hora: String): Int =
        writableDatabase.update(
            "sesion_observacion",
            ContentValues().apply {
                put("fin_real", hora)
                put("id_estado", ESTADO_FINALIZADA)
            },
            "id_sesion = ?", arrayOf(idSesion.toString())
        )

    /** Validar o rechazar (el que revisa nunca es el observador: lo garantiza el CHECK del esquema). */
    fun revisarSesion(idSesion: Long, estado: EstadoSesion, idRevisor: Long, motivo: String? = null): Int =
        writableDatabase.update(
            "sesion_observacion",
            ContentValues().apply {
                put("id_estado", estado.idEstado)
                put("id_revisor", idRevisor)
                put("fecha_revision", ahora())
                put("motivo_rechazo", motivo)
            },
            "id_sesion = ?", arrayOf(idSesion.toString())
        )

    // ==================== registros del observador ====================

    fun insertarRegistro(
        idSesion: Long,
        clasificacion: Clasificacion,
        rangoEtario: RangoEtario,
        compania: Compania,
        comprasOtraTienda: Boolean,
        seDirige: Long? = null
    ): Long {
        val valores = ContentValues().apply {
            put("id_sesion", idSesion)
            put("fecha_hora", ahora())
            put("id_clasificacion", clasificacion.idClasificacion)
            put("id_rango", rangoEtario.idRango)
            put("id_tipo_compania", compania.idTipo)
            put("compras_otra_tienda", if (comprasOtraTienda) 1 else 0)
            put("id_acceso_destino", seDirige)
            put("anulado", 0)
        }
        return writableDatabase.insert("registro_observacion", null, valores)
    }

    /** Anular no borra: deja el registro con su motivo y quien lo anulo (RN-11). */
    fun anularRegistro(idRegistro: Long, motivo: String, idColaborador: Long): Int =
        writableDatabase.update(
            "registro_observacion",
            ContentValues().apply {
                put("anulado", 1)
                put("motivo_anulacion", motivo)
                put("id_usuario_anula", idColaborador)
                put("fecha_anulacion", ahora())
            },
            "id_registro = ?", arrayOf(idRegistro.toString())
        )

    fun ultimosRegistros(idSesion: Long, limite: Int = 8): List<Registro> {
        val lista = mutableListOf<Registro>()
        readableDatabase.rawQuery(
            "SELECT r.*, c.nombre AS clasificacion_nombre, s.codigo AS codigo_sesion FROM registro_observacion r " +
                "JOIN clasificacion c ON c.id_clasificacion = r.id_clasificacion " +
                "JOIN sesion_observacion s ON s.id_sesion = r.id_sesion " +
                "WHERE r.id_sesion = ? ORDER BY r.id_registro DESC LIMIT ?",
            arrayOf(idSesion.toString(), limite.toString())
        ).use { c ->
            while (c.moveToNext()) lista.add(leerRegistro(c))
        }
        return lista
    }

    // ==================== indicadores ====================

    fun indicadoresDeFecha(fecha: String): IndicadoresDia? =
        readableDatabase.rawQuery("SELECT * FROM v_conversion_dia WHERE fecha = ?", arrayOf(fecha)).use { c ->
            if (!c.moveToNext()) return null
            IndicadoresDia(
                fecha = c.texto("fecha"),
                tipoDia = c.texto("tipo_dia"),
                ingresos = c.entero("ingresos"),
                transacciones = c.entero("transacciones"),
                conversionBruta = c.decimalOpcional("conversion_bruta"),
                estadoDia = c.texto("dia"),
                transito = c.decimalOpcional("transito"),
                flujoSinTransito = c.decimalOpcional("flujo_sin_transito"),
                conversionSinTransito = c.decimalOpcional("conversion_sin_transito"),
                bloquesMedidos = c.entero("medidos"),
                bloquesEstimados = c.entero("estimados"),
                bloquesSinInformacion = c.entero("sin_informacion")
            )
        }

    /** Transito estimado por acceso en la fecha: suma los bloques de la vista. */
    fun transitoPorAcceso(fecha: String): List<TransitoPorAcceso> {
        val lista = mutableListOf<TransitoPorAcceso>()
        readableDatabase.rawQuery(
            "SELECT acceso, SUM(ingresos) AS ingresos, SUM(transito_estimado) AS transito, " +
                "CASE WHEN SUM(CASE WHEN origen = 'medido' THEN 1 ELSE 0 END) > 0 THEN 'medido' " +
                "     WHEN SUM(CASE WHEN origen = 'estimado' THEN 1 ELSE 0 END) > 0 THEN 'estimado' " +
                "     ELSE 'sin información' END AS origen " +
                "FROM v_transito_bloque WHERE fecha = ? GROUP BY acceso ORDER BY acceso",
            arrayOf(fecha)
        ).use { c ->
            while (c.moveToNext()) {
                lista.add(
                    TransitoPorAcceso(
                        acceso = c.texto("acceso"),
                        ingresos = c.entero("ingresos"),
                        transitoEstimado = c.decimalOpcional("transito"),
                        origen = OrigenTransito.porNombre(c.texto("origen"))
                    )
                )
            }
        }
        return lista
    }

    fun bloquesPendientes(fecha: String): List<String> {
        val lista = mutableListOf<String>()
        readableDatabase.rawQuery(
            "SELECT acceso, hora_inicio, fecha FROM v_pendiente_medir WHERE fecha = ? ORDER BY hora_inicio",
            arrayOf(fecha)
        ).use { c ->
            while (c.moveToNext()) {
                lista.add(c.texto("acceso") + " · " + c.texto("hora_inicio").take(5) + " · " + c.texto("fecha"))
            }
        }
        return lista
    }

    fun registrarEvento(evento: EventoDia): Long =
        writableDatabase.insert(
            "evento_dia", null,
            ContentValues().apply {
                put("codigo", evento.codigo)
                put("fecha_inicio", evento.fechaInicio)
                put("fecha_fin", evento.fechaFin)
                put("id_tipo_evento", evento.tipo.idTipo)
                put("id_acceso", evento.idAcceso)
                put("descripcion", evento.descripcion)
                put("id_usuario_registra", evento.idUsuarioRegistra)
                // La foto va como bytes (BLOB). Si el evento no lleva foto, queda NULL.
                put("foto", evento.foto)
            }
        )

    /** Todos los eventos de la base: sirve para dar el codigo correlativo (E-07, E-08...). */
    fun contarTodosLosEventos(): Int =
        readableDatabase.rawQuery("SELECT COUNT(*) AS n FROM evento_dia", null).use { c ->
            if (c.moveToNext()) c.entero("n") else 0
        }

    /** Cuantos eventos hay registrados en una fecha: la pantalla lo muestra al guardar. */
    fun contarEventos(fecha: String): Int =
        readableDatabase.rawQuery("SELECT COUNT(*) AS n FROM evento_dia WHERE ? BETWEEN fecha_inicio AND fecha_fin", arrayOf(fecha)).use { c ->
            if (c.moveToNext()) c.entero("n") else 0
        }

    fun eventosDeFecha(fecha: String): List<EventoDia> {
        val lista = mutableListOf<EventoDia>()
        readableDatabase.rawQuery("SELECT * FROM evento_dia WHERE ? BETWEEN fecha_inicio AND fecha_fin", arrayOf(fecha)).use { c ->
            while (c.moveToNext()) {
                lista.add(
                    EventoDia(
                        id = c.long("id_evento"),
                        codigo = c.texto("codigo"),
                        fechaInicio = c.texto("fecha_inicio"),
                        fechaFin = c.texto("fecha_fin"),
                        tipo = TipoEvento.entries.firstOrNull { it.idTipo == c.long("id_tipo_evento") } ?: TipoEvento.OTRO,
                        idAcceso = c.longOpcional("id_acceso"),
                        descripcion = c.texto("descripcion"),
                        idUsuarioRegistra = c.long("id_usuario_registra"),
                        foto = c.blobOpcional("foto")
                    )
                )
            }
        }
        return lista
    }

    // ==================== lecturas internas ====================

    private fun consultarColaboradores(sql: String, args: Array<String>?): List<Colaborador> {
        val lista = mutableListOf<Colaborador>()
        readableDatabase.rawQuery(sql, args).use { c ->
            while (c.moveToNext()) {
                lista.add(
                    Colaborador(
                        id = c.long("id_usuario"),
                        rut = c.texto("rut"),
                        nombre = c.texto("nombres"),
                        apellido = c.texto("apellido_paterno"),
                        cargo = c.texto("cargo"),
                        perfil = PerfilUsuario.porCodigo(c.texto("rol_codigo")) ?: PerfilUsuario.OBSERVADOR,
                        activo = c.entero("activo") == 1,
                        passwordHash = c.texto("password_hash")
                    )
                )
            }
        }
        return lista
    }

    private fun consultarSesiones(sql: String, args: Array<String>?): List<SesionResumen> {
        val lista = mutableListOf<SesionResumen>()
        readableDatabase.rawQuery(sql, args).use { c ->
            while (c.moveToNext()) {
                lista.add(
                    SesionResumen(
                        id = c.long("id_sesion"),
                        codigo = c.texto("codigo"),
                        idAcceso = c.long("id_acceso"),
                        fecha = c.texto("fecha"),
                        horaInicioProg = c.texto("hora_inicio_prog"),
                        horaFinProg = c.texto("hora_fin_prog"),
                        inicioReal = c.textoOpcional("inicio_real"),
                        finReal = c.textoOpcional("fin_real"),
                        estado = EstadoSesion.porNombre(c.texto("estado_nombre")),
                        idObservador = c.long("id_observador"),
                        idProgramador = c.long("id_programador"),
                        idRevisor = c.longOpcional("id_revisor"),
                        fechaRevision = c.textoOpcional("fecha_revision"),
                        motivoRechazo = c.textoOpcional("motivo_rechazo"),
                        nombreObservador = c.texto("nombre_observador"),
                        registrosValidos = c.entero("registros_validos"),
                        transito = c.entero("transito"),
                        visita = c.entero("visita"),
                        noDeterminado = c.entero("no_determinado"),
                        pctTransito = c.decimalOpcional("pct_transito")
                    )
                )
            }
        }
        return lista
    }

    private fun leerRegistro(c: Cursor) = Registro(
        id = c.long("id_registro"),
        idSesion = c.long("id_sesion"),
        codigoSesion = c.texto("codigo_sesion"),
        fechaHora = c.texto("fecha_hora"),
        hora = c.texto("fecha_hora").takeLast(8).take(5),
        clasificacion = Clasificacion.porNombre(c.texto("clasificacion_nombre")),
        rangoEtario = RangoEtario.porId(c.long("id_rango")),
        compania = Compania.porId(c.long("id_tipo_compania")),
        comprasOtraTienda = c.entero("compras_otra_tienda") == 1,
        seDirige = c.longOpcional("id_acceso_destino"),
        anulado = c.entero("anulado") == 1,
        idUsuarioAnula = c.longOpcional("id_usuario_anula"),
        fechaAnulacion = c.textoOpcional("fecha_anulacion"),
        motivoAnulacion = c.textoOpcional("motivo_anulacion")
    )

    private fun ahora(): String =
        java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.US).format(java.util.Date())
}

// ---- lectores de cursor con nombre de columna (evitan los indices a mano) ----

private fun Cursor.texto(columna: String): String = getString(getColumnIndexOrThrow(columna)) ?: ""

private fun Cursor.textoOpcional(columna: String): String? {
    val i = getColumnIndexOrThrow(columna)
    return if (isNull(i)) null else getString(i)
}

private fun Cursor.entero(columna: String): Int = getInt(getColumnIndexOrThrow(columna))

private fun Cursor.long(columna: String): Long = getLong(getColumnIndexOrThrow(columna))

private fun Cursor.longOpcional(columna: String): Long? {
    val i = getColumnIndexOrThrow(columna)
    return if (isNull(i)) null else getLong(i)
}

private fun Cursor.blobOpcional(columna: String): ByteArray? {
    val i = getColumnIndexOrThrow(columna)
    return if (isNull(i)) null else getBlob(i)
}

private fun Cursor.decimalOpcional(columna: String): Double? {
    val i = getColumnIndexOrThrow(columna)
    return if (isNull(i)) null else getDouble(i)
}
