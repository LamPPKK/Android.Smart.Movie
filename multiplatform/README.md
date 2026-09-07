# SmartMovie Compose Multiplatform

This project delivers SmartMovie to the remaining production Compose Multiplatform targets without changing the locked Android build in the repository root.

## Targets

- macOS: macOS 13+ on Apple silicon
- Windows: Windows 10+ on 64-bit systems
- Linux: Ubuntu 20.04-compatible 64-bit distributions
- Web: WasmGC production bundle plus a JavaScript fallback bundle

The separate `SmartMovie` repository owns the native SwiftUI clients for iPhone, iPad, Mac, Apple TV, Apple Watch, and Apple Vision Pro. Compose Multiplatform intentionally does not produce an Apple mobile framework or host application.

Product work follows the single cross-repository [Smart Movie development plan](https://github.com/LamPPKK/Smart-Movie-iOS/blob/main/docs/DEVELOPMENT_PLAN.md). Every KMP feature must keep contract behavior, six-locale copy, privacy and release version aligned with the native apps.

## Architecture

`composeApp` owns the shared `/v2` catalog/account contracts, Ktor Worker client, entity and External ID search, navigable Credit Detail flows, paginated Movie/TV account recommendations, retry/cancellation behavior, anonymous installation ID, local-first Favorite/Watchlist library, account-scoped durable mutation outbox, explicit adult age confirmation plus local PIN/lockout state, six-locale copy, UDF controller, and adaptive cinematic UI. Platform source sets provide storage, secure session behavior, URL handoff, API base URL, and entry points.

Desktop conformance tests decode the repository-level `catalog-contract` fixtures. The vendored snapshot is shared with native Android and is updated by an automated cross-repository pull request whenever the Worker contract changes.

Title detail uses typed contract models for production companies/networks, region-matched certification and release dates, alternative titles, localized translations, and external identifiers. The same six-locale summary is also documented in the in-app Profile/About surface.

All catalog/account traffic goes through `https://catalog.smartmovie.app/v2`; `/v1` remains for deterministic legacy preview and 2.0 compatibility. Desktop development can override the origin with `SMARTMOVIE_CATALOG_BASE_URL`; web development can use `?api=https://…`. The `?preview=1` switch is reserved for the deterministic local preview server.

## Build

Use JDK 21 or newer. Run commands from this directory:

```bash
./gradlew --dependency-verification=strict :composeApp:desktopTest :composeApp:compileKotlinDesktop
./gradlew :composeApp:jsBrowserDevelopmentExecutableDistribution
./gradlew :composeApp:wasmJsBrowserDistribution
```

Launch desktop with `./gradlew :composeApp:run`. Launch web during development with `./gradlew :composeApp:wasmJsBrowserDevelopmentRun`.

Desktop package formats are configured in `composeApp/build.gradle.kts`. `./gradlew :composeApp:createDistributable` creates a portable image for the current OS, and `./gradlew :composeApp:packageDistributionForCurrentOS` creates its configured installers when the OS packaging prerequisites are installed.

## Local deterministic preview

First build the Wasm distribution, then run:

```bash
python3 tools/preview_server.py
```

Open `http://127.0.0.1:8099/?preview=1`. The server supplies deterministic, contract-backed `/v1` and `/v2` catalog responses without a TMDb token and is intended only for UI QA and documentation captures. Account requests remain unauthorized so screenshots cannot include a real session.

The preview's `/artwork/` configuration points to generated abstract demo images, not actual TMDb posters/portraits. Nested fixture paths are mapped to available local assets; canonical fixtures are unchanged. Run `python3 -B -m unittest discover -s tools -p 'test_*.py' -v` to verify complete PNG responses for every advertised size, missing-image 404 and account 401. These checks are part of Compose Multiplatform CI and do not prove production Worker/CDN availability.

To exercise the Web client with live Worker responses and TMDb artwork, configure `TMDB_BEARER_TOKEN` in the Worker development environment, start `npm run dev` from `backend/worker`, build the Wasm distribution, then run `python3 tools/live_catalog_server.py` from this directory. Open `http://127.0.0.1:8100/?preview=1`. The live server proxies public `/v1` and `/v2` responses without rewriting image paths; it intentionally rejects account/auth routes and returns a clear 502 when the Worker is unavailable. Never commit the token or use this mode as a production deployment.

The current Home, Explore, Search, Detail, Library, Profile and expanded-layout captures are documented in the repository [screen gallery](../docs/SCREENSHOTS.md).

## Release boundaries

- Web hosting must serve `.wasm` with `application/wasm`; production must configure CORS, browser callback allowlist, secure cookies, and CSRF.
- Notarized macOS and signed Windows installers require protected signing identities.
- There is no separate SmartMovie identity. Optional TMDb approval synchronizes account content while local caches/outboxes remain platform-specific.
- Desktop JVM, JavaScript, and Wasm are release blockers for the coordinated 3.0 train.
