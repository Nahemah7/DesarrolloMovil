# Rincón Sur — control de flujo por acceso

Aplicación Android nativa (Kotlin y Jetpack Compose) para medir cuántas personas entran a una tienda
de retail, por dónde entran y cuántas de esas personas terminan comprando.

## El problema

La tienda sabe cuánto vende, pero no cuánta gente entra ni cuánta se va sin comprar. Ese conteo se hace
hoy a mano, en una hoja que se pierde al final del turno y que nadie puede revisar. Rincón Sur reemplaza
esa hoja por una medición por bloques de una hora en los cuatro accesos de la tienda: el observador
registra a cada persona con un toque mientras está de pie en el acceso, y el supervisor revisa, valida y
lee los indicadores del día.

## Integrantes

| Integrante | Rol en el proyecto |
|---|---|
| Silvio Gonzalves | Desarrollo de la aplicación |
| Pedro Leiva | Desarrollo de la aplicación |
| Oscar Garrido | Desarrollo de la aplicación |

## Qué hace la aplicación

### Perfil observador (quien mide en el acceso)

- **Mi sesión**: las sesiones de conteo del día con su estado (programada, en curso, cerrada, validada o
  rechazada) y el botón para iniciar el conteo.
- **Registro**: una persona por toque. Se elige clasificación (tránsito o visita), tramo de edad estimado
  de un vistazo, compañía, si compró en otra tienda y, si va en tránsito, hacia dónde se dirige. No se
  escribe texto ni se piden datos personales: nunca se guarda nombre ni RUT de quien pasa.
- **Plantillas**: se guarda un perfil repetido y se aplica con un toque cuando entra la misma clase de
  persona.
- **Anular**: un registro mal tomado se anula con su motivo; no se borra, deja de contarse y queda quién
  lo anuló.
- **Cierre**: resumen de lo registrado y envío a validación. Si no llega al mínimo de 30 registros
  válidos, avisa cuántos faltan. Si el teléfono está sin señal, la sesión queda por enviar y no se pierde.

### Perfil supervisor

- **Sesiones del día**: filtros Día y Por validar, navegación día a día con calendario y atajo a hoy.
  Cada sesión muestra su estado, sus registros y la proporción de tránsito; se valida o se rechaza con
  motivos marcados en casillas. El rechazo no borra nada: los registros quedan para volver a contar.
- **Programar sesión**: acceso, fecha, horario y observador, con validación de cruces (mismo acceso u
  misma persona en el mismo tramo) y de la duración del bloque (entre 30 y 60 minutos).
- **Eventos del día**: registrar clima, feriado, evento del mall o cierre de acceso, con descripción y
  foto de evidencia desde la cámara o la galería. Un evento se anula con motivo y deja de contar para el
  tipo de día.
- **Indicadores**: el día de hoy, los días hábiles o los fines de semana, con la semana navegable con
  botones de atrás y adelante. Muestra el avance de la medición, lo que la tienda registró (ingresos,
  transacciones y conversión bruta), el tránsito medido, los ingresos por acceso y la conversión sin
  tránsito cuando el día está completo.

### Transversal

- **Perfil**: foto, cambio de nombre, cambio de clave y recuperación de la clave con preguntas de
  seguridad, sin correo.
- **Avisos en tres niveles**: error pegado al campo que hay que corregir, aviso flotante cuando una
  acción no se puede completar, y resultado de lo que sí se hizo (sesión enviada, evento guardado).

## Cómo ejecutar

Requisitos: Android Studio con JDK 21, SDK de Android y un dispositivo o emulador con Android 8.0
(API 26) o superior.

```bash
git clone https://github.com/Nahemah7/DesarrolloMovil.git
cd DesarrolloMovil
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

También se puede abrir la carpeta del proyecto en Android Studio y ejecutar desde ahí. La primera
ejecución crea la base local con los datos de ejemplo del caso, así que la aplicación funciona sin
configurar nada más.

## Usuarios de prueba

La clave de todos los usuarios de ejemplo es `rincon2026`.

| RUT | Nombre | Perfil |
|---|---|---|
| 17.654.320-5 | Daniela Pizarro | Supervisora de piso, turno mañana |
| 16.443.019-7 | Matías Contreras | Supervisor de piso, turno tarde |
| 19.876.345-4 | Tamara Rojas | Observadora |
| 20.145.678-9 | Felipe Ibarra | Observador |
| 18.322.904-4 | Javiera Morales | Observadora |
| 15.234.871-1 | Carolina Fuentes | Jefatura de tienda |
| 13.987.442-0 | Rodrigo Saavedra | Subgerencia comercial |

## Estructura del proyecto

```text
app/src/main/java/com/example/rinconsur/
  navigation/    rutas y contenedor de navegación
  ui/screens/    una pantalla por archivo
  ui/components/ barra, tarjetas, campos, avisos y filtros reutilizables
  ui/theme/      colores, tipografía y medidas
  viewmodel/     un ViewModel por pantalla, con estado inmutable (StateFlow)
  model/         modelos de datos y estados de la interfaz
  data/          acceso a SQLite, repositorio y reglas puras de negocio
app/src/test/    pruebas unitarias
```

La arquitectura es MVVM. La interfaz solo dibuja y avisa; las reglas viven en los ViewModel y en
`data/`; la base local es SQLite con migraciones versionadas, de modo que una actualización no pierde lo
que el teléfono ya tenía guardado.

## Recursos nativos del dispositivo

- **Cámara y galería**: foto de evidencia de un evento, con permiso pedido en el momento y archivo
  entregado a la cámara por `FileProvider`.
- **Estado de conectividad**: la aplicación distingue si hay red para enviar las sesiones que quedaron
  pendientes.
- **Vibración**: confirmación al guardar un registro, para no tener que mirar la pantalla entre personas.

## Pruebas

```bash
./gradlew testDebugUnitTest
```

Hay 35 pruebas unitarias sobre las reglas que no dependen de Android: formato y dígito verificador del
RUT, reglas de la clave, largo del texto libre y reglas de programación de sesiones (duración y cruces).

## Decisiones del grupo

Quedan anotadas acá porque explican el porqué de lo que se ve en pantalla.

- **El día de trabajo es el día del teléfono.** La aplicación se usa a diario: sesiones, eventos e
  indicadores miran hoy, no la última fecha que traía el anexo.
- **Anular no es borrar.** Un registro o un evento anulado conserva su motivo y quién lo anuló, deja de
  contarse y sigue visible con su marca.
- **Indicadores sin estimaciones.** Si la tienda no cargó los ingresos del día, se dice y se muestra solo
  lo medido. La conversión sin tránsito no se calcula si falta medir algún bloque, porque un número
  parcial no es el del día.
- **El consolidado lo calcula la base.** Para un período se suman las cifras y se recalculan los
  porcentajes sobre el total; nunca se promedian los porcentajes de cada día.
- **Mínimo de 30 registros válidos para validar una sesión**, y el cierre dice cuántos faltan en vez de
  dejar el botón sin respuesta.
- **Una sola regla de texto libre** (entre 10 y 200 caracteres) para la descripción de un evento, el otro
  motivo del rechazo y el motivo de una anulación.
- **El estado de la sesión se relee de la base** al volver a cada pantalla: la base decide y la interfaz
  solo avisa. Así una sesión ya enviada no se puede seguir contando.
- **Los avisos tienen tres niveles fijos** (campo, flotante y resultado) para que el usuario siempre sepa
  dónde mirar.

## Planificación y control de versiones

- Tablero del equipo: https://trello.com/b/6ad991c3d579c2651c4bb8e1/desarrollo-movil
- Issues del proyecto: https://github.com/Nahemah7/DesarrolloMovil/issues
- Ramas por bloque de trabajo y merge a `master` cuando compila y las pruebas pasan.
- El docente está agregado como colaborador del repositorio y del tablero.
