
CREATE TABLE rol (
    id_rol      INT          NOT NULL,
    codigo      VARCHAR(5)   NOT NULL,
    nombre      VARCHAR(40)  NOT NULL,
    descripcion VARCHAR(200) NULL,
    CONSTRAINT pk_rol PRIMARY KEY (id_rol),
    CONSTRAINT uq_rol_codigo UNIQUE (codigo),
    CONSTRAINT uq_rol_nombre UNIQUE (nombre),
    CONSTRAINT ck_rol_codigo CHECK (codigo IN ('P-01','P-02','P-03','P-04','P-05'))
);;

CREATE TABLE usuario (
    id_usuario INTEGER PRIMARY KEY AUTOINCREMENT,
    id_rol         INT          NOT NULL,
    rut            VARCHAR(12)  NOT NULL,
    nombres        VARCHAR(60)  NOT NULL,
    apellido_paterno VARCHAR(40) NOT NULL,
    apellido_materno VARCHAR(40) NULL,
    genero         CHAR(1)      NOT NULL DEFAULT 'F',
    cargo          VARCHAR(60)  NOT NULL,
    correo         VARCHAR(100) NOT NULL,
    telefono       VARCHAR(20)  NULL,
    password_hash  VARCHAR(255) NOT NULL,
    debe_cambiar_clave BOOLEAN  NOT NULL DEFAULT 0,
    codigo_vence   DATETIME     NULL,
    activo         BOOLEAN      NOT NULL DEFAULT 1,
    foto           BLOB         NULL,
    fecha_creacion DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_usuario_rut UNIQUE (rut),
    CONSTRAINT uq_usuario_correo UNIQUE (correo),
    CONSTRAINT ck_usuario_genero CHECK (genero IN ('F', 'M')),
    CONSTRAINT fk_usuario_rol FOREIGN KEY (id_rol) REFERENCES rol (id_rol)
);;

CREATE TABLE acceso (
    id_acceso   INT          NOT NULL,
    codigo      CHAR(2)      NOT NULL,
    nombre      VARCHAR(40)  NOT NULL,
    ubicacion   VARCHAR(120) NOT NULL,
    observacion VARCHAR(200) NULL,
    CONSTRAINT pk_acceso PRIMARY KEY (id_acceso),
    CONSTRAINT uq_acceso_codigo UNIQUE (codigo)
);;

CREATE TABLE bloque_horario (
    id_bloque   INT  NOT NULL,
    hora_inicio TIME NOT NULL,
    hora_fin    TIME NOT NULL,
    CONSTRAINT pk_bloque_horario PRIMARY KEY (id_bloque),
    CONSTRAINT uq_bloque_inicio UNIQUE (hora_inicio),
    CONSTRAINT ck_bloque_orden CHECK (hora_fin > hora_inicio)
);;

CREATE TABLE conteo_ingreso (
    id_conteo INTEGER PRIMARY KEY AUTOINCREMENT,
    id_acceso            INT      NULL,
    fecha                DATE     NOT NULL,
    id_bloque            INT      NOT NULL,
    cantidad             INT      NOT NULL,
    id_usuario_carga     INT      NOT NULL,
    fecha_carga          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    id_usuario_correccion INT     NULL,
    fecha_correccion     DATETIME NULL,
    CONSTRAINT ck_conteo_cantidad CHECK (cantidad >= 0),
    CONSTRAINT ck_conteo_correccion CHECK ((id_usuario_correccion IS NULL) = (fecha_correccion IS NULL)),
    CONSTRAINT fk_conteo_acceso FOREIGN KEY (id_acceso) REFERENCES acceso (id_acceso),
    CONSTRAINT fk_conteo_bloque FOREIGN KEY (id_bloque) REFERENCES bloque_horario (id_bloque),
    CONSTRAINT fk_conteo_carga FOREIGN KEY (id_usuario_carga) REFERENCES usuario (id_usuario),
    CONSTRAINT fk_conteo_corrige FOREIGN KEY (id_usuario_correccion) REFERENCES usuario (id_usuario)
);;

CREATE TABLE transaccion_bloque (
    id_transaccion INTEGER PRIMARY KEY AUTOINCREMENT,
    fecha            DATE     NOT NULL,
    id_bloque        INT      NOT NULL,
    cantidad         INT      NOT NULL,
    id_usuario_carga INT      NOT NULL,
    fecha_carga      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    id_usuario_correccion INT     NULL,
    fecha_correccion DATETIME NULL,
    CONSTRAINT uq_transaccion_bloque UNIQUE (fecha, id_bloque),
    CONSTRAINT fk_transaccion_corrige FOREIGN KEY (id_usuario_correccion) REFERENCES usuario (id_usuario),
    CONSTRAINT ck_transaccion_cantidad CHECK (cantidad >= 0),
    CONSTRAINT fk_transaccion_bloque FOREIGN KEY (id_bloque) REFERENCES bloque_horario (id_bloque),
    CONSTRAINT fk_transaccion_carga FOREIGN KEY (id_usuario_carga) REFERENCES usuario (id_usuario)
);;

CREATE TABLE estado_sesion (
    id_estado INT         NOT NULL,
    nombre    VARCHAR(30) NOT NULL,
    CONSTRAINT pk_estado_sesion PRIMARY KEY (id_estado),
    CONSTRAINT uq_estado_nombre UNIQUE (nombre)
);;

CREATE TABLE sesion_observacion (
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
    id_remoto       INT          NULL,
    CONSTRAINT uq_sesion_codigo UNIQUE (codigo),
    CONSTRAINT ck_sesion_orden CHECK (hora_fin_prog > hora_inicio_prog),
    CONSTRAINT ck_sesion_revisor CHECK (id_revisor IS NULL OR id_revisor <> id_observador),
    CONSTRAINT ck_sesion_motivo CHECK (id_estado <> 5 OR motivo_rechazo IS NOT NULL),
    CONSTRAINT fk_sesion_acceso FOREIGN KEY (id_acceso) REFERENCES acceso (id_acceso),
    CONSTRAINT fk_sesion_estado FOREIGN KEY (id_estado) REFERENCES estado_sesion (id_estado),
    CONSTRAINT fk_sesion_observador FOREIGN KEY (id_observador) REFERENCES usuario (id_usuario),
    CONSTRAINT fk_sesion_programador FOREIGN KEY (id_programador) REFERENCES usuario (id_usuario),
    CONSTRAINT fk_sesion_revisor FOREIGN KEY (id_revisor) REFERENCES usuario (id_usuario)
);;

CREATE TABLE clasificacion (
    id_clasificacion INT         NOT NULL,
    nombre           VARCHAR(30) NOT NULL,
    CONSTRAINT pk_clasificacion PRIMARY KEY (id_clasificacion),
    CONSTRAINT uq_clasificacion_nombre UNIQUE (nombre)
);;

CREATE TABLE rango_etario (
    id_rango INT         NOT NULL,
    nombre   VARCHAR(30) NOT NULL,
    edad_min INT         NOT NULL,
    edad_max INT         NULL,
    orden    INT         NOT NULL,
    CONSTRAINT pk_rango PRIMARY KEY (id_rango),
    CONSTRAINT uq_rango_nombre UNIQUE (nombre),
    CONSTRAINT uq_rango_orden UNIQUE (orden),
    CONSTRAINT ck_rango_edad CHECK (edad_max IS NULL OR edad_max >= edad_min)
);;

CREATE TABLE tipo_compania (
    id_tipo_compania INT         NOT NULL,
    nombre           VARCHAR(40) NOT NULL,
    CONSTRAINT pk_tipo_compania PRIMARY KEY (id_tipo_compania),
    CONSTRAINT uq_tipo_compania_nombre UNIQUE (nombre)
);;

CREATE TABLE registro_observacion (
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
    enviado             BOOLEAN      NOT NULL DEFAULT 0,
    id_remoto           INT          NULL,
    CONSTRAINT ck_registro_anulacion CHECK (anulado = 0 OR (id_usuario_anula IS NOT NULL AND motivo_anulacion IS NOT NULL)),
    CONSTRAINT fk_registro_sesion FOREIGN KEY (id_sesion) REFERENCES sesion_observacion (id_sesion),
    CONSTRAINT fk_registro_clasificacion FOREIGN KEY (id_clasificacion) REFERENCES clasificacion (id_clasificacion),
    CONSTRAINT fk_registro_rango FOREIGN KEY (id_rango) REFERENCES rango_etario (id_rango),
    CONSTRAINT fk_registro_compania FOREIGN KEY (id_tipo_compania) REFERENCES tipo_compania (id_tipo_compania),
    CONSTRAINT fk_registro_destino FOREIGN KEY (id_acceso_destino) REFERENCES acceso (id_acceso),
    CONSTRAINT fk_registro_anula FOREIGN KEY (id_usuario_anula) REFERENCES usuario (id_usuario)
);;

CREATE TABLE tipo_evento (
    id_tipo_evento INT         NOT NULL,
    nombre         VARCHAR(40) NOT NULL,
    CONSTRAINT pk_tipo_evento PRIMARY KEY (id_tipo_evento),
    CONSTRAINT uq_tipo_evento_nombre UNIQUE (nombre)
);;

CREATE TABLE evento_dia (
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
    anulado             INTEGER      NOT NULL DEFAULT 0,
    motivo_anulacion    VARCHAR(200) NULL,
    id_usuario_anula    INT          NULL,
    fecha_anulacion     DATETIME     NULL,
    CONSTRAINT uq_evento_codigo UNIQUE (codigo),
    CONSTRAINT ck_evento_fechas CHECK (fecha_fin >= fecha_inicio),
    CONSTRAINT fk_evento_tipo FOREIGN KEY (id_tipo_evento) REFERENCES tipo_evento (id_tipo_evento),
    CONSTRAINT fk_evento_acceso FOREIGN KEY (id_acceso) REFERENCES acceso (id_acceso),
    CONSTRAINT fk_evento_usuario FOREIGN KEY (id_usuario_registra) REFERENCES usuario (id_usuario)
);;

CREATE TABLE meta_conversion (
    id_meta INTEGER PRIMARY KEY AUTOINCREMENT,
    anio               SMALLINT     NOT NULL,
    mes                TINYINT      NOT NULL,
    porcentaje         DECIMAL(5,2) NOT NULL,
    id_usuario_registra INT         NOT NULL,
    CONSTRAINT uq_meta_periodo UNIQUE (anio, mes),
    CONSTRAINT ck_meta_mes CHECK (mes BETWEEN 1 AND 12),
    CONSTRAINT ck_meta_porcentaje CHECK (porcentaje BETWEEN 0 AND 100),
    CONSTRAINT fk_meta_usuario FOREIGN KEY (id_usuario_registra) REFERENCES usuario (id_usuario)
);;

CREATE TABLE pregunta_seguridad (
    id_pregunta INTEGER PRIMARY KEY AUTOINCREMENT,
    texto       VARCHAR(120) NOT NULL,
    activa      BOOLEAN      NOT NULL DEFAULT 1,
    CONSTRAINT uq_pregunta_texto UNIQUE (texto)
);;

CREATE TABLE respuesta_seguridad (
    id_usuario     INT          NOT NULL,
    id_pregunta    INT          NOT NULL,
    respuesta_hash VARCHAR(255) NOT NULL,
    CONSTRAINT pk_respuesta_seguridad PRIMARY KEY (id_usuario, id_pregunta),
    CONSTRAINT fk_respuesta_usuario FOREIGN KEY (id_usuario) REFERENCES usuario (id_usuario),
    CONSTRAINT fk_respuesta_pregunta FOREIGN KEY (id_pregunta) REFERENCES pregunta_seguridad (id_pregunta)
);;

CREATE UNIQUE INDEX ux_conteo_con_detalle
    ON conteo_ingreso (id_acceso, fecha, id_bloque) WHERE id_acceso IS NOT NULL;;

CREATE UNIQUE INDEX ux_conteo_sin_detalle
    ON conteo_ingreso (fecha, id_bloque) WHERE id_acceso IS NULL;;

CREATE TABLE configuracion (
    clave          VARCHAR(40)  NOT NULL PRIMARY KEY,
    valor          VARCHAR(120) NOT NULL,
    actualizado_en DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
);;
