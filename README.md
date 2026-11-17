# Rincón Sur — control de flujo por acceso

Aplicación Android nativa (Kotlin y Jetpack Compose) para medir cuántas personas entran a una tienda
de retail, por dónde entran y cuántas de esas personas terminan comprando.

## El problema

La tienda sabe cuánto vende, pero no cuánta gente entra ni cuánta se va sin comprar. Ese conteo se hace
hoy a mano, en una hoja que se pierde al final del turno y que nadie puede revisar. Rincón Sur reemplaza
esa hoja por una medición por bloques de una hora en los cuatro accesos de la tienda: el observador
registra a cada persona con un toque mientras está de pie en el acceso, el supervisor revisa y valida, y
la jefatura lee los indicadores del período.

## Integrantes

| Integrante | Rol en el proyecto |
|---|---|
| Silvio Gonzalves | Desarrollo de la aplicación |
| Pedro Leiva | Desarrollo de la aplicación |
| Oscar Garrido | Desarrollo de la aplicación |

## Los cinco perfiles

La aplicación entra con RUT y clave, y cada perfil ve solo lo que le corresponde.

| Perfil | Qué hace en la aplicación |
|---|---|
| **Colaborador observador** | Mide en el acceso: registra a cada persona con un toque, anula un registro mal tomado y cierra la sesión para enviarla a validación |
| **Supervisor de piso** | Opera el día: programa sesiones, entra por la lista «Por validar», valida o rechaza con motivo, registra eventos y consulta los indicadores |
| **Subgerente comercial** | Mira los indicadores del período y responde por el reporte operativo. La carga de ingresos y boletas del día está en desarrollo |
| **Jefa de tienda** | Lee y decide: indicadores y, más adelante, meta del mes, comparación de períodos, horas punta y perfil del tránsito |
| **Administrador** | Gestiona personas: hoy consulta la lista de colaboradores por rol y estado; el alta, la edición y el reseteo de clave están en desarrollo |

## Qué hace la aplicación

### Medir en el acceso

- **Mi sesión**: las sesiones del día con su estado y el botón para iniciar el conteo.
- **Registro**: una persona por toque. Se elige clasificación (tránsito o visita), tramo de edad estimado
  de un vistazo, compañía, si compró en otra tienda y, si va en tránsito, hacia dónde se dirige. No se
  escribe texto ni se piden datos personales: nunca se guarda nombre ni RUT de quien pasa.
- **Plantillas**: se guarda un perfil repetido y se aplica con un toque cuando entra la misma clase de
  persona.
- **Anular**: un registro mal tomado se anula con su motivo; no se borra, deja de contarse y queda quién
  lo anuló.
- **Cierre**: resumen de lo registrado y envío a validación. Si no llega al mínimo de 30 registros
  válidos, avisa cuántos faltan.

### Revisar y validar

- **Por validar** es la pantalla de entrada del supervisor. Cada fila muestra acceso, fecha y horario,
  observador, registros y proporción de tránsito, más el estado; las acciones están en la propia fila.
- **Filtros** por acceso, por observador y «solo las mías», además de los ámbitos Por validar, Día y
  Semana. Los filtros se mantienen al ir a otra pantalla y volver.
- **Validar** pide una confirmación breve y responde con un aviso de resultado. Si la sesión cambió de
  estado mientras tanto, lo dice en vez de dar por hecho el cambio.
- **Rechazar** abre los motivos frecuentes como casillas; el rechazo no borra nada, los registros quedan
  para volver a contar.
- **Detalle de sesión**: resumen de la medición y la segmentación completa — clasificación, rango etario,
  compañía, compras en otra tienda y destino del tránsito — con la cantidad, el porcentaje y una barra
  por grupo. Los porcentajes los calcula la base sobre los registros válidos de esa sesión.
- **Programar sesión**: acceso, fecha, horario y observador, con validación de cruces (mismo acceso u
  misma persona en el mismo tramo) y de la duración del bloque (entre 30 y 60 minutos).
- **Eventos del día**: clima, feriado, evento del mall o cierre de acceso, con descripción y foto de
  evidencia desde la cámara o la galería.
- **Indicadores**: hoy, días hábiles o fines de semana, con la semana navegable. Muestra el avance de la
  medición, lo que la tienda registró (ingresos, transacciones y conversión bruta), el tránsito medido,
  los ingresos por acceso y la conversión sin tránsito cuando el día está completo.

### Transversal

- **Perfil**: foto, cambio de nombre y cambio de clave.
- **Avisos en tres niveles**: error pegado al campo que hay que corregir, aviso flotante cuando una
  acción no se puede completar, y resultado de lo que sí se hizo, como aviso al pie.

## Identidad visual

Paleta propia **Marino y Aqua**, aplicada desde el tema: marino `#0E2A47` para marca, botones y títulos,
aqua `#12B5A6` como acento, tono suave `#DFF3F0` para tarjetas y secciones, y fondo `#F7F8F6`. El acento
nunca lleva texto blanco encima (no alcanza el contraste mínimo); sobre aqua el texto va en marino.

La marca es un vano de acceso — dos jambas, un arco y el umbral — dibujado con `Canvas`, y la pantalla de
entrada usa una tarjeta centrada sobre un fondo marino con corte diagonal.

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
| 20.543.219-1 | Paula Contreras | Administradora |

## Estructura del proyecto

```text
app/src/main/java/com/example/rinconsur/
  navigation/    rutas y contenedor de navegación
  ui/screens/    una pantalla por archivo
  ui/components/ barra, tarjetas, campos, avisos, filtros y marca
  ui/theme/      paleta, tipografía y medidas
  ui/styles/     estilos de botón, campo y tarjeta
  viewmodel/     un ViewModel por pantalla, con estado inmutable (StateFlow)
  model/         modelos de datos y estados de la interfaz
  data/          base, repositorio y reglas puras de negocio
    FlujoDbHelper   esquema, semilla y migraciones (1.031 líneas)
    SesionDao       sesiones, registros, cierre, revisión y detalle
    IndicadorDao    indicadores, tránsito y bloques pendientes
    UsuarioDao      colaboradores, preguntas de seguridad, foto y clave
    EventoDao       eventos del día
    CatalogoDao     accesos, bloques, rangos, compañías y fechas
    BaseDao         lectura de cursores y marca de tiempo
app/src/test/    pruebas unitarias
```

La arquitectura es MVVM. La interfaz solo dibuja y avisa; las reglas viven en los ViewModel y en
`data/`; la base local es SQLite con migraciones versionadas. El acceso a datos está separado por
dominio: las pantallas hablan con `FlujoRepository`, y el repositorio con el DAO que corresponde, así
que el esquema y la semilla no se mezclan con las consultas.

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

Hay **47 pruebas unitarias** sobre reglas que no dependen de Android: formato y dígito verificador del
RUT, reglas de la clave, largo del texto libre, reglas de programación de sesiones (duración y cruces),
filtros del listado de sesiones y armado del detalle de una sesión.

## Decisiones del grupo

Quedan anotadas acá porque explican el porqué de lo que se ve en pantalla.

- **El día de trabajo es el día del teléfono.** La aplicación se usa a diario: sesiones, eventos e
  indicadores miran hoy, no la última fecha que traía el anexo.
- **Anular no es borrar.** Un registro o un evento anulado conserva su motivo y quién lo anuló, deja de
  contarse y sigue visible con su marca.
- **Indicadores sin estimaciones.** Si la tienda no cargó los ingresos del día, se dice y se muestra solo
  lo medido. La conversión sin tránsito no se calcula si falta medir algún bloque, porque un número
  parcial no es el del día.
- **El consolidado y los porcentajes los calcula la base.** Para un período se suman las cifras y se
  recalculan los porcentajes sobre el total; nunca se promedian los porcentajes de cada día. Lo mismo en
  el detalle de una sesión: cada grupo trae su porcentaje calculado en SQL.
- **Mínimo de 30 registros válidos para validar una sesión**, y el cierre dice cuántos faltan en vez de
  dejar el botón sin respuesta.
- **Una sola regla de texto libre** (entre 10 y 200 caracteres) para la descripción de un evento, el otro
  motivo del rechazo y el motivo de una anulación.
- **El estado de la sesión se relee de la base** al volver a cada pantalla: la base decide y la interfaz
  solo avisa. Así una sesión ya enviada no se puede seguir contando.
- **Los avisos tienen tres niveles fijos** (campo, flotante y resultado) para que el usuario siempre sepa
  dónde mirar.
- **«Solo las mías» son las sesiones que medí yo**, no las que programé: el supervisor programa casi
  todas, así que filtrar por programador no acotaría nada.

## Lo que viene

La siguiente entrega deja la base local y pasa a la nube, con la herramienta que fijó el docente:
microservicios en Java Spring Boot con MySQL, puerta de entrada y registro de servicios, documentación
con Swagger, y el cliente Android consumiendo la API con Retrofit. La aplicación seguirá funcionando sin
señal sobre la base local, y las pantallas que faltan de cada perfil se construyen sobre ese mismo
contrato:

- Carga diaria de ingresos y boletas, con importación del archivo del punto de venta.
- Pantallas de jefatura: meta del mes, comparación de períodos, horas punta y perfil del tránsito.
- Gestión de colaboradores desde la aplicación: alta, edición, activación y reseteo de clave.
- Recuperación de clave eligiendo la pregunta de seguridad del catálogo, y hash de clave con sal
  aleatoria por usuario.

## Planificación y control de versiones

- Tablero del equipo: https://trello.com/b/6ad991c3d579c2651c4bb8e1/desarrollo-movil
- Issues del proyecto: https://github.com/Nahemah7/DesarrolloMovil/issues
- Ramas por bloque de trabajo y merge a `master` cuando compila y las pruebas pasan.
- El docente está agregado como colaborador del repositorio y del tablero.
