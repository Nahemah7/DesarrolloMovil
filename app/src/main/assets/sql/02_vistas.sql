
CREATE VIEW v_tipo_dia AS
SELECT d.fecha,
       CASE WHEN (CAST(strftime('%w', d.fecha) AS INTEGER) + 1) IN (1, 7)
                 OR EXISTS (SELECT 1 FROM evento_dia e
                            JOIN tipo_evento t ON t.id_tipo_evento = e.id_tipo_evento
                            WHERE t.nombre = 'Feriado' AND e.anulado = 0
                              AND d.fecha BETWEEN e.fecha_inicio AND e.fecha_fin)
            THEN 'Fin de semana' ELSE 'Habil' END AS tipo_dia
FROM (SELECT fecha FROM conteo_ingreso UNION SELECT fecha FROM sesion_observacion) d;;

CREATE VIEW v_sesion_proporcion AS
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
GROUP BY s.id_sesion, s.codigo, s.id_acceso, s.fecha, CAST(strftime('%H', s.hora_inicio_prog) AS INTEGER), e.nombre;;

CREATE VIEW v_transito_bloque AS
SELECT ci.fecha, td.tipo_dia, COALESCE(a.codigo, 'Sin detalle') AS acceso, bh.id_bloque, bh.hora_inicio, ci.cantidad AS ingresos,
       MAX(CASE WHEN sv.fecha = ci.fecha THEN sv.pct_transito END) AS pct_sesion,
       ROUND(AVG(CASE WHEN sv.fecha < ci.fecha THEN sv.pct_transito END), 1) AS pct_promedio,
       CASE WHEN MAX(CASE WHEN sv.fecha = ci.fecha THEN sv.pct_transito END) IS NOT NULL THEN 'medido'
            WHEN AVG(CASE WHEN sv.fecha < ci.fecha THEN sv.pct_transito END) IS NOT NULL THEN 'estimado'
            ELSE 'sin información' END AS origen,
       ROUND(ci.cantidad * COALESCE(MAX(CASE WHEN sv.fecha = ci.fecha THEN sv.pct_transito END),
                                    AVG(CASE WHEN sv.fecha < ci.fecha THEN sv.pct_transito END)) / 100) AS transito_estimado
FROM conteo_ingreso ci
LEFT JOIN acceso a ON a.id_acceso = ci.id_acceso
JOIN bloque_horario bh ON bh.id_bloque = ci.id_bloque
JOIN v_tipo_dia td ON td.fecha = ci.fecha
LEFT JOIN v_sesion_proporcion sv
       ON sv.id_acceso = ci.id_acceso AND sv.estado = 'Validada'
      AND ( (sv.fecha = ci.fecha AND sv.hora_bloque = CAST(strftime('%H', bh.hora_inicio) AS INTEGER))
         OR (sv.fecha < ci.fecha AND sv.fecha >= date(ci.fecha, '-28 day')
             AND EXISTS (SELECT 1 FROM v_tipo_dia t2 WHERE t2.fecha = sv.fecha AND t2.tipo_dia = td.tipo_dia)) )
GROUP BY ci.fecha, td.tipo_dia, COALESCE(a.codigo, 'Sin detalle'), bh.id_bloque, bh.hora_inicio, ci.cantidad;;

CREATE VIEW v_conversion_dia AS
SELECT i.fecha, td.tipo_dia, i.ingresos, t.transacciones,
       ROUND(100.0 * t.transacciones / NULLIF(i.ingresos, 0), 1) AS conversion_bruta,
       CASE WHEN b.bloques = 11 AND tb.bloques_tx = 11 THEN 'completo' ELSE 'incompleto' END AS dia,
       tr.transito, i.ingresos - tr.transito AS flujo_sin_transito,
       CASE WHEN tr.sin_informacion = 0
            THEN ROUND(100.0 * t.transacciones / NULLIF(i.ingresos - tr.transito, 0), 1) END AS conversion_sin_transito,
       tr.medidos, tr.estimados, tr.sin_informacion
FROM (SELECT fecha, SUM(cantidad) AS ingresos FROM conteo_ingreso GROUP BY fecha) i
JOIN (SELECT fecha, SUM(cantidad) AS transacciones FROM transaccion_bloque GROUP BY fecha) t ON t.fecha = i.fecha
JOIN (SELECT fecha, COUNT(DISTINCT id_bloque) AS bloques, COUNT(DISTINCT id_acceso) AS accesos FROM conteo_ingreso GROUP BY fecha) b ON b.fecha = i.fecha
LEFT JOIN (SELECT fecha, COUNT(*) AS bloques_tx FROM transaccion_bloque GROUP BY fecha) tb ON tb.fecha = i.fecha
LEFT JOIN (SELECT fecha, SUM(transito_estimado) AS transito,
                  SUM(origen = 'medido') AS medidos, SUM(origen = 'estimado') AS estimados,
                  SUM(origen = 'sin información') AS sin_informacion
           FROM v_transito_bloque GROUP BY fecha) tr ON tr.fecha = i.fecha
LEFT JOIN v_tipo_dia td ON td.fecha = i.fecha;;

CREATE VIEW v_perfil_transito AS
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
WHERE r.anulado = 0 AND e.id_estado = 4 AND c.id_clasificacion = 1
GROUP BY a.codigo, s.fecha, td.tipo_dia, rg.nombre, tc.nombre, r.compras_otra_tienda;;

CREATE VIEW v_pendiente_medir AS
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
                    AND CAST(strftime('%H', s2.hora_inicio_prog) AS INTEGER) = CAST(strftime('%H', bh.hora_inicio) AS INTEGER) AND e2.nombre = 'Validada');;

CREATE VIEW v_rendimiento_personal AS
SELECT u.id_usuario,
       u.nombres || ' ' || u.apellido_paterno AS observador,
       COUNT(DISTINCT s.id_sesion) AS sesiones,
       COUNT(DISTINCT CASE WHEN s.id_estado IN (3, 4) THEN s.id_sesion END) AS cerradas,
       COUNT(DISTINCT CASE WHEN s.id_estado = 4 THEN s.id_sesion END) AS validadas,
       COUNT(DISTINCT CASE WHEN s.id_estado = 5 THEN s.id_sesion END) AS rechazadas,
       COALESCE(SUM(CASE WHEN r.anulado = 0 THEN 1 ELSE 0 END), 0) AS registros,
       COALESCE(SUM(CASE WHEN r.anulado = 0 AND r.id_clasificacion = 1 THEN 1 ELSE 0 END), 0) AS transito,
       MAX(s.fecha) AS ultima_fecha
FROM sesion_observacion s
JOIN usuario u ON u.id_usuario = s.id_observador
LEFT JOIN registro_observacion r ON r.id_sesion = s.id_sesion
GROUP BY u.id_usuario, u.nombres, u.apellido_paterno;;

CREATE VIEW v_rendimiento_supervisor AS
SELECT x.id_usuario,
       SUM(x.programadas) AS programadas,
       SUM(x.pendientes) AS pendientes,
       SUM(x.validadas) AS validadas,
       SUM(x.rechazadas) AS rechazadas
FROM (
    SELECT id_programador AS id_usuario, 1 AS programadas,
           CASE WHEN id_estado IN (1, 2, 3) THEN 1 ELSE 0 END AS pendientes,
           0 AS validadas, 0 AS rechazadas
    FROM sesion_observacion
    UNION ALL
    SELECT id_revisor, 0, 0,
           CASE WHEN id_estado = 4 THEN 1 ELSE 0 END,
           CASE WHEN id_estado = 5 THEN 1 ELSE 0 END
    FROM sesion_observacion
    WHERE id_revisor IS NOT NULL
) x
GROUP BY x.id_usuario;;
