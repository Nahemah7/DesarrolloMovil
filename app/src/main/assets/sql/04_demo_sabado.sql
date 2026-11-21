-- Rincón Sur · script de la base local
-- Se ejecuta al crear la base (FlujoDbHelper.onCreate). Es SQL estándar de SQLite,
-- así que también se puede correr con cualquier cliente para revisarlo.

DELETE FROM registro_observacion WHERE id_sesion IN (21, 22);;

DELETE FROM sesion_observacion WHERE id_sesion IN (21, 22);;

DELETE FROM conteo_ingreso WHERE fecha = '2026-10-24';;

DELETE FROM transaccion_bloque WHERE fecha = '2026-10-24';;

INSERT INTO conteo_ingreso (id_acceso, fecha, id_bloque, cantidad, id_usuario_carga) VALUES
  (1, '2026-10-24', 1, 83, 2),
  (2, '2026-10-24', 1, 65, 2),
  (3, '2026-10-24', 1, 52, 2),
  (4, '2026-10-24', 1, 124, 2),
  (1, '2026-10-24', 2, 102, 2),
  (2, '2026-10-24', 2, 78, 2),
  (3, '2026-10-24', 2, 63, 2),
  (4, '2026-10-24', 2, 149, 2),
  (1, '2026-10-24', 3, 130, 2),
  (2, '2026-10-24', 3, 100, 2),
  (3, '2026-10-24', 3, 80, 2),
  (4, '2026-10-24', 3, 190, 2),
  (1, '2026-10-24', 4, 153, 2),
  (2, '2026-10-24', 4, 117, 2),
  (3, '2026-10-24', 4, 94, 2),
  (4, '2026-10-24', 4, 223, 2),
  (1, '2026-10-24', 5, 119, 2),
  (2, '2026-10-24', 5, 91, 2),
  (3, '2026-10-24', 5, 73, 2),
  (4, '2026-10-24', 5, 173, 2),
  (1, '2026-10-24', 6, 96, 2),
  (2, '2026-10-24', 6, 74, 2),
  (3, '2026-10-24', 6, 59, 2),
  (4, '2026-10-24', 6, 140, 2),
  (1, '2026-10-24', 7, 90, 2),
  (2, '2026-10-24', 7, 70, 2),
  (3, '2026-10-24', 7, 56, 2),
  (4, '2026-10-24', 7, 132, 2),
  (1, '2026-10-24', 8, 102, 2),
  (2, '2026-10-24', 8, 78, 2),
  (3, '2026-10-24', 8, 63, 2),
  (4, '2026-10-24', 8, 149, 2),
  (1, '2026-10-24', 9, 136, 2),
  (2, '2026-10-24', 9, 104, 2),
  (3, '2026-10-24', 9, 83, 2),
  (4, '2026-10-24', 9, 198, 2),
  (1, '2026-10-24', 10, 141, 2),
  (2, '2026-10-24', 10, 109, 2),
  (3, '2026-10-24', 10, 87, 2),
  (4, '2026-10-24', 10, 206, 2),
  (1, '2026-10-24', 11, 90, 2),
  (2, '2026-10-24', 11, 70, 2),
  (3, '2026-10-24', 11, 56, 2),
  (4, '2026-10-24', 11, 132, 2);;

INSERT INTO transaccion_bloque (fecha, id_bloque, cantidad, id_usuario_carga) VALUES
  ('2026-10-24', 1, 28, 2),
  ('2026-10-24', 2, 34, 2),
  ('2026-10-24', 3, 44, 2),
  ('2026-10-24', 4, 51, 2),
  ('2026-10-24', 5, 40, 2),
  ('2026-10-24', 6, 32, 2),
  ('2026-10-24', 7, 30, 2),
  ('2026-10-24', 8, 34, 2),
  ('2026-10-24', 9, 45, 2),
  ('2026-10-24', 10, 47, 2),
  ('2026-10-24', 11, 30, 2);;

INSERT INTO sesion_observacion (id_sesion, codigo, id_acceso, fecha, hora_inicio_prog, hora_fin_prog, inicio_real, fin_real, id_estado, id_observador, id_programador, id_revisor, fecha_revision, motivo_rechazo) VALUES
  (21, 'S-0021', 1, '2026-10-24', '13:00:00', '13:45:00', '2026-10-24 13:02:00', '2026-10-24 13:44:00', 4, 5, 3, 4, '2026-10-24 20:00:00', NULL),
  (22, 'S-0022', 2, '2026-10-24', '13:00:00', '13:45:00', '2026-10-24 13:01:00', '2026-10-24 13:45:00', 4, 6, 3, 4, '2026-10-24 20:00:00', NULL);;

INSERT INTO registro_observacion (id_sesion, fecha_hora, id_clasificacion, id_rango, id_tipo_compania, compras_otra_tienda, id_acceso_destino, anulado)
SELECT 21, datetime('2026-10-24 13:05:00', '+' || (n * 40) || ' seconds'),
       CASE WHEN n % 11 = 0 THEN 3 WHEN n % 3 = 0 THEN 2 ELSE 1 END,
       (n % 5) + 1, (n % 4) + 1, n % 2, NULL, 0
FROM (WITH RECURSIVE c(n) AS (SELECT 1 UNION ALL SELECT n + 1 FROM c WHERE n < 32) SELECT n FROM c);;

INSERT INTO registro_observacion (id_sesion, fecha_hora, id_clasificacion, id_rango, id_tipo_compania, compras_otra_tienda, id_acceso_destino, anulado)
SELECT 22, datetime('2026-10-24 13:04:00', '+' || (n * 40) || ' seconds'),
       CASE WHEN n % 11 = 0 THEN 3 WHEN n % 3 = 0 THEN 2 ELSE 1 END,
       (n % 5) + 1, (n % 4) + 1, n % 2, NULL, 0
FROM (WITH RECURSIVE c(n) AS (SELECT 1 UNION ALL SELECT n + 1 FROM c WHERE n < 32) SELECT n FROM c);;

-- Los registros del anexo son historia, no cola de salida: se marcan como ya enviados para que
-- la sincronización no los vuelva a subir al servidor (que tiene su propia copia).
UPDATE registro_observacion SET enviado = 1;;
