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
| **Subgerente comercial** | Carga los ingresos y las boletas del día, bloque por bloque, y responde por el reporte operativo: indicadores, comparación por mes y perfil del tránsito |
| **Jefa de tienda** | Lee y decide: reporte del mes con la meta de conversión, la comparación por mes, las horas punta y el perfil del tránsito |
| **Administrador** | Crea y edita las cuentas de la tienda, entrega el código temporal y puede borrar los datos de ejemplo para que la aplicación quede solo con lo que carga la tienda. Cada persona define su propia clave y su pregunta de seguridad al entrar por primera vez |

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
- **Indicadores**: hoy, días hábiles o fines de semana, con la semana navegable. Arriba va una sola cifra
  —las personas medidas, con los bloques que cubre y su barra de avance—, porque es lo único que depende
  del observador y se ve el mismo día, sin esperar la carga de la tienda. Debajo, en fila, la conversión y
  las transacciones cuando la tienda cargó sus números. El resto —cobertura de la medición, ingresos por
  acceso, conversión sin tránsito y el estado de las sesiones del día— vive detrás de «Ver el detalle».

### Administrar personas

- **Colaboradores**: la lista completa por perfil y estado, con la marca de quienes todavía no
  entraron nunca.
- **Nueva persona**: RUT, nombres, apellidos, correo y teléfono, el género y el perfil elegido de una
  lista. El cargo sale del perfil y del género —jefa de tienda o jefe de tienda, supervisora o supervisor—,
  no se escribe aparte.
- **Editar persona**: tocar una fila abre sus datos para corregir nombres, correo o teléfono, cambiarle el
  perfil, cambiar el género o desactivar la cuenta cuando la persona deja la tienda. Desactivar no borra
  nada: sus sesiones y registros se conservan, solo deja de poder entrar.
- **Código temporal**: la app lo genera al crear la cuenta y se muestra una sola vez; en la base queda
  su hash y su vencimiento, siete días. Si la persona lo pierde sin haber entrado nunca, el
  administrador genera uno nuevo desde la lista.

### Primer acceso

La cuenta no nace con una clave definitiva. Al entrar con el RUT y el código temporal, la aplicación no
deja pasar: pide la contraseña nueva, repetida, y **una** pregunta de seguridad con su respuesta. Recién
ahí el código deja de servir y la persona entra a su menú. Si el código venció, el login lo dice y hay
que pedir uno nuevo.

### Cargar el día

- **Carga del día**: once bloques horarios con dos datos cada uno, ingresos del local y boletas del
  punto de venta. Se escribe leyendo el reporte de caja, con el total y la conversión calculándose al pie
  mientras se avanza.
- **Reparto por acceso, opcional**: un bloque se puede repartir entre A1 y A4 cuando el dato existe; la
  suma tiene que dar el total del bloque y, si no cuadra, no deja guardar. Si no se reparte, el bloque
  queda como total del local y los informes lo dicen en vez de inventar un reparto.
- **Bloques a medias**: si un bloque tiene ingresos y le faltan boletas, la pantalla avisa, porque sin los
  dos datos no hay conversión para ese día.
- **Corrección**: volver a guardar un día ya cargado reemplaza los valores y deja marcado quién corrigió y
  cuándo, tanto en los ingresos como en las boletas.

### Leer y decidir

- **Reporte**: la pantalla de entrada de la jefatura. La meta del mes se dibuja como un anillo de avance;
  la comparación por mes y las horas punta, como barras; y el perfil del tránsito, como tres donuts con su
  leyenda (rango de edad, compañía y compra en otra tienda). Los gráficos se dibujan con Compose, sin
  librerías externas, así usan la misma paleta y el mismo peso visual que el resto de la aplicación.
- **Meta del mes**: solo la jefa la registra, entre 0 y 100 por ciento. Si el mes no tiene días cargados,
  el avance dice que no hay dato en vez de mostrar un cero.
- **Horas punta**: marca el bloque de mayor tránsito medido y distingue con texto las horas que todavía no
  tienen sesión validada, en lugar de esconderlas.

### Transversal

- **Perfil**: la foto con su botón de cámara encima, el nombre con su cargo, y una ficha de seguridad
  con tres filas —clave (con «Cambiar clave» que abre el diálogo), teléfono y cargo—. Cerrar sesión va
  arriba, como acción de borde, y no al final del scroll. La pregunta de seguridad se elige de una lista y
  se guarda con su botón.
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
    FlujoDbHelper   versión, orden de borrado y lectura de los scripts (77 líneas)
    ScriptsSql      separa un script SQL en sentencias
    SesionDao       sesiones, registros, cierre, revisión y detalle
    IndicadorDao    indicadores, tránsito y bloques pendientes
    UsuarioDao      colaboradores, preguntas de seguridad, foto y clave
    EventoDao       eventos del día
    CatalogoDao     accesos, bloques, rangos, compañías y fechas
    BaseDao         lectura de cursores y marca de tiempo
    CargaDao        carga diaria de ingresos y boletas
app/src/main/assets/sql/
    01_esquema.sql      tablas, claves y restricciones
    02_vistas.sql       las seis vistas de indicadores
    03_datos.sql        catálogos, personas y datos del anexo
    04_demo_sabado.sql  un sábado completo para mostrar el fin de semana
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

**Diez pruebas, en cuatro archivos, sobre los ViewModels.** Es lo que pide la pauta y lo que se puede
explicar una por una: cada una comprueba una sola cosa.

| Archivo | Pruebas | Qué defiende |
|---|---|---|
| `LoginViewModelTest` | 3 | RUT inválido, clave equivocada y entrada de la jefa de tienda |
| `SesionesViewModelTest` | 3 | Filtro por validar, filtro de mis sesiones y día elegido |
| `RegistroViewModelTest` | 2 | No guardar sin sesión en curso y no aplicar una plantilla que no existe |
| `IndicadoresViewModelTest` | 2 | Período de hábiles y retroceder una semana |

Se nombran `funcion_en_condicion_esperado` (`entrar_conClaveEquivocada_...`,
`onSemanaAnterior_...`) y corren con Robolectric, así que el ViewModel se instancia de verdad, con su
`Application` y su SQLite cargado desde los `.sql` de `assets`: no hay dobles de prueba que puedan
decir que sí a algo que la app real diría que no.

El reporte HTML se genera solo y queda versionado para la entrega:

```
app/build/reports/tests/testDebugUnitTest/index.html   ->   reportes/pruebas-unitarias/
```

Las reglas que antes se probaban en el teléfono (proporción de tránsito, mínimos de la sesión, carga
del día, cargo según el perfil) se comprueban hoy en el **servicio**: `mvn verify` en el repo del
backend corre 153 pruebas con la compuerta de 80 % de cobertura. La app se queda con lo que solo la
app puede decidir.

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
- **La boleta no tiene acceso; el ingreso sí.** Una venta no entra por una puerta, así que las boletas
  son once por día (una por bloque) y los ingresos, cuando hay detalle, cuatro por bloque. El reparto por
  acceso es opcional porque la mayoría de las tiendas no tiene contador en cada vano.
- **Sin ingresos y boletas del mismo bloque no hay conversión.** El día queda como incompleto y la
  pantalla lo avisa antes de guardar, en vez de mostrar un porcentaje calculado con la mitad de los datos.
- **Ninguna cuenta nace con una clave definitiva.** El administrador crea la persona y la app genera un
  código temporal que vence en siete días; el primer acceso obliga a dejar una clave propia y una
  pregunta de seguridad. En la base queda el hash del código, nunca el código.
- **El cargo no se escribe: sale del perfil.** Pedir el puesto dos veces permitía escribir «Vendedora»
  para una cuenta de administrador.
- **Los datos de ejemplo se anuncian y se pueden borrar.** Mientras la base tenga la semilla del caso,
  los informes muestran la etiqueta «Datos de ejemplo»; el administrador la quita desde
  «Datos de la aplicación → Empezar de cero», que borra mediciones, ingresos, transacciones y metas
  pero conserva personas, accesos, bloques horarios y preguntas de seguridad.
- **No se siembran respuestas de seguridad ni cuentas de relleno.** Los datos de ejemplo son los del
  anexo y las siete personas del caso; lo demás lo crea el administrador y lo configura cada persona.
- **El esquema y la semilla son archivos `.sql`**, no texto dentro de Kotlin: se editan, se revisan y se
  pueden correr con cualquier cliente. La clase de la base solo sabe de versiones y de leerlos, y el
  separador de sentencias respeta los punto y coma que van dentro de un texto.

## Lo que viene

La siguiente entrega deja la base local y pasa a la nube, con la herramienta que fijó el docente:
microservicios en Java Spring Boot con MySQL, puerta de entrada y registro de servicios, documentación
con Swagger, y el cliente Android consumiendo la API con Retrofit. La aplicación seguirá funcionando sin
señal sobre la base local, y las pantallas que faltan de cada perfil se construyen sobre ese mismo
contrato:

- Importación del archivo del punto de venta para llenar la carga del día de una vez.
- Pantallas de jefatura: meta del mes, comparación de períodos, horas punta y perfil del tránsito.
- Gestión de colaboradores: edición de datos, activación y desactivación de una cuenta.
- Recuperación de clave eligiendo la pregunta de seguridad del catálogo, y hash de clave con sal
  aleatoria por usuario.

## Planificación y control de versiones

- Tablero del equipo: https://trello.com/b/6ad991c3d579c2651c4bb8e1/desarrollo-movil
- Issues del proyecto: https://github.com/Nahemah7/DesarrolloMovil/issues
- Ramas por bloque de trabajo y merge a `master` cuando compila y las pruebas pasan.
- El docente está agregado como colaborador del repositorio y del tablero.

## Conexión con el backend

La app ya tiene el cliente HTTP (Retrofit + OkHttp) contra el gateway de los microservicios, y una
pantalla para probarlo sin recompilar: **Perfil → Probar la conexión**.

Cada paso muestra su tiempo y su resultado, así se distingue "no hay red" de "un servicio está
caído":

```
Inicio de sesión     Carolina Fuentes · P-01 · token de 237 caracteres      2282 ms
Catálogos            4 accesos · A1, A2, A3, A4                             494 ms
Sesiones del 15-09   2 sesiones · S-0001 Validada · S-0002 Validada         360 ms
Indicadores 15-09    5735 ingresos · 497 transacciones · 8.7 % conversión   315 ms
```

La dirección por defecto es `http://localhost:8090` y va por el túnel de `adb reverse tcp:8090
tcp:8090`, que funciona igual en el emulador y en un teléfono por USB sin pelear con el firewall.
Por la red local se escribe la IP del PC en la misma pantalla y queda guardada.

Las pantallas de la app todavía leen de SQLite: la conexión está probada y lista para irla
llevando pantalla por pantalla.
