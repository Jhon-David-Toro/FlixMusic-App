# Arquitectura de Flix Music (Android)

Kotlin + Jetpack Compose. Diseño de referencia: [`../design/flix-design.html`](../design/flix-design.html).
Backend: [`../Flix-Backend`](../Flix-Backend) (FastAPI).

## Capas

```
UI (Composables) ──eventos──▶ ViewModel ──▶ Repository ──▶ API (red) / Room (disco)
        ▲                         │
        └───────── UiState ◀──────┘
```

- **UI**: dibuja un `UiState` y avisa de eventos (`onPlayClick`, `onQueryChange`). No decide nada, no llama a la red ni a la base de datos.
- **ViewModel**: guarda el estado de la pantalla (sobrevive a la rotación), recibe los eventos y pide datos a los repositories. Expone un único `StateFlow<UiState>`.
- **Repository**: la única puerta a los datos. Decide si algo sale de la API, de Room o de caché. Se define como **interfaz**; la UI y los ViewModels no conocen la implementación.
- **Fuentes de datos**: cliente HTTP (API) y Room (favoritos, playlists, historial, cola). Solo las usa su repository.

Los datos fluyen hacia abajo (estado) y los eventos hacia arriba. Cada dato tiene una sola fuente de verdad.

## Paquetes

Un solo módulo de Gradle (`:app`) con paquetes que imitan módulos futuros:

| Paquete | Contiene |
|---|---|
| `core/designsystem/theme` | `FlixTheme`: colores, tipografía, formas, spacing, motion. |
| `core/designsystem/component` | Componentes reutilizables sin lógica de negocio: botones, `TrackRow`, `MiniPlayer`, `StateView`… |
| `core/model` | Modelos de dominio: `Track`, `Artist`, `Album`, `Playlist`, `QueueItem`… Clases de datos puras. |
| `core/data` | DTOs de la API, cliente HTTP, DataStore/Room, mappers DTO → dominio, interfaces e implementaciones de repositories. |
| `core/navigation` | Tipos de navegación compartidos entre features (`TopLevelRoute`). |
| `core/player` | `PlayerController` (interfaz) y su implementación con Media3: servicio en segundo plano, cola, estados de reproducción y precarga de la siguiente pista. |
| `feature/<nombre>` | Una carpeta por funcionalidad (`home`, `search`, `genre`, `library`…): su pantalla, su ViewModel, su `UiState` y su ruta de navegación. |

### Navegación

- Cada pestaña es un destino de primer nivel (`TopLevelRoute`). Si una pestaña tiene pantallas de detalle, es un **grafo anidado** (`SearchGraph`) y sus detalles se registran dentro, así la pestaña sigue marcada.
- Cada feature expone su ruta, `NavGraphBuilder.xxxScreen()` y `NavController.navigateToXxx()`. Solo `FlixNavHost` (paquete de la app) las conecta entre sí.
- Las transiciones están en `FlixTransitions`: fade-through entre pestañas y "push" (24 dp + fade) hacia los detalles.

## Reglas de dependencias

1. **`core/model` no importa nada** del resto de la app.
2. **`core/designsystem` no conoce `feature` ni `data`**: solo dibuja lo que recibe por parámetros.
3. **Una feature nunca importa otra feature.** Para ir de una a otra se usa la navegación (`onArtistClick(artistId)`), no código de la otra feature.
4. **Los DTO no salen de `core/data`.** Fuera de ese paquete solo existen modelos de dominio.

```
feature/*   ──▶ core/designsystem, core/model, core/data y core/player (solo interfaces)
core/data   ──▶ core/model
core/player ──▶ core/model, core/data
```

Si un `import` rompe una de estas reglas, es un error de arquitectura aunque compile.

## Por qué los DTO no salen de `core/data`

Un DTO copia la forma exacta del JSON del backend (`duration_seconds`, `thumbnail`…). Si la app entera usara DTOs, cualquier cambio del backend (un campo renombrado, un endpoint nuevo) obligaría a tocar pantallas. Con un **mapper** DTO → dominio, el cambio se absorbe en un solo sitio (`core/data`) y la UI sigue usando `Track` sin enterarse.

Ejemplo: si el backend cambia `duration_seconds` por `duration_ms`, se modifican el DTO y su mapper. `Track`, los ViewModels y las pantallas no cambian.

## Convenciones

- Todo composable que dibuja recibe `modifier: Modifier = Modifier` como primer parámetro opcional y lo aplica a su elemento raíz.
- Los componentes reciben estado y devuelven eventos (state hoisting); no crean ViewModels.
- Ningún color, tamaño o tipografía escrito a mano en una pantalla: todo sale de `FlixTheme`.
- Los textos visibles van en `res/values/strings.xml`.
- Las versiones de librerías van en `gradle/libs.versions.toml`.
- Inyección de dependencias con Hilt (se añade antes de la capa de datos).
- Cada feature se prueba con repositories falsos (implementaciones de prueba de la interfaz).
- **Nombres descriptivos siempre**: nada de variables de una letra (`c`, `t`, `e`, `q`, `x`). Cada nombre dice qué contiene: `flixColors`, `searchQuery`, `exception`. El código lo lee alguien que no lo escribió.
  - **Excepción: `it`** está permitido en lambdas cortas de una línea donde es obvio qué es (`tracks.map { it.toTrack() }`, `key = { it.id }`). Si la lambda ocupa varias líneas, hay lambdas anidadas o no es evidente qué contiene, se nombra: `{ track -> … }`.
- **Sin comentarios en línea (`//`)**. Si algo necesita documentación, se usa **KDoc** (`/** … */`) en **inglés**, sobre la clase, función o propiedad: qué es y por qué, no una narración del código.
- **Fidelidad al diseño**: cada pantalla y componente sigue `../design/flix-design.html` (valores, estados y animaciones). Si algo se desvía por una limitación, se documenta como pendiente.
- **Commits**: una sola línea en estilo convencional (`feat: …`, `fix: …`, `refactor: …`, `test: …`, `docs: …`), sin cuerpo.

## Cuándo separar en módulos de Gradle

Cuando el tiempo de compilación moleste o haga falta reutilizar código (por ejemplo, otra app para tablet o Wear). Si se respetan las reglas de dependencias, cada paquete de `core/` y `feature/` pasa a ser un módulo moviendo su carpeta y creando su `build.gradle.kts`.
