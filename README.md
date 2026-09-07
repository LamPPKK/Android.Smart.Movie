# Smart Movie Android 3.0 — Android, Wear OS, desktop and web

[![Android CI](https://github.com/LamPPKK/Smart-Movie-Android/actions/workflows/android-ci.yml/badge.svg?branch=main)](https://github.com/LamPPKK/Smart-Movie-Android/actions/workflows/android-ci.yml)
[![Compose Multiplatform CI](https://github.com/LamPPKK/Smart-Movie-Android/actions/workflows/multiplatform-ci.yml/badge.svg?branch=main)](https://github.com/LamPPKK/Smart-Movie-Android/actions/workflows/multiplatform-ci.yml)

SmartMovie is a cinematic TMDb catalog built natively for Android. It covers movies, television, people, collections, organizations, keywords, seasons, and episodes; adds region-aware availability and trailers; and can optionally synchronize library, ratings, recommendations, and mixed lists through browser-approved TMDb access.

The native app adapts from phones to tablets, foldables, ChromeOS, Android TV, and Android XR Home Space. A Wear OS companion mirrors the safe title or exact episode open on the paired phone and hands back to the matching detail. An isolated Compose Multiplatform app brings the same product flow to macOS, Windows, Linux, and responsive Web/Wasm.

> [!NOTE]
> SmartMovie is a catalog and trailer app. It does not stream movies or episodes, create a separate SmartMovie identity, collect a TMDb password, display advertising, or offer in-app purchases.

> [!IMPORTANT]
> SmartMovie 3.0 is under active development. Automated source gates are in place; production Worker D1/session configuration, protected signing identities, final store artwork, privacy/support URLs, and Play metadata still require release-owner configuration.

## Two repositories, one SmartMovie 3.0

| Repository | Owns | Mobile release role |
| --- | --- | --- |
| **[Smart Movie Android](https://github.com/LamPPKK/Smart-Movie-Android)** (this repository) | Native Android and Wear OS apps, Compose Multiplatform desktop/web app, checksummed contract snapshot | Google Play source of truth |
| **[Smart Movie iOS](https://github.com/LamPPKK/Smart-Movie-iOS)** | Native SwiftUI Apple apps, `SmartMovieKit`, Cloudflare Worker, canonical OpenAPI 3.1 contract and fixtures | App Store source of truth and backend owner |

Apple and Android keep native UI, lifecycle, and storage while sharing the additive `/v2` Worker contract, six locales, semantic release train, normalized errors, and decoder fixtures. `/v1` remains available for 2.0 clients. TMDb is the post-login account source of truth; each client remains local-first through a durable, account-scoped outbox.

The canonical cross-repository roadmap lives in the backend-owner repository: [Kế hoạch phát triển Smart Movie sau 3.0](https://github.com/LamPPKK/Smart-Movie-iOS/blob/main/docs/DEVELOPMENT_PLAN.md). It defines release order, parity, per-feature commit/push discipline, and the shared Definition of Done.

## What you can do

- **Explore the complete catalog** through Home, trending, pagination, retry, cancellation, and regional Discover filters for dates, language, country, certification, runtime, votes, providers, monetization, rating, and adult-PIN context.
- **Search across entities** with discriminated Movie, TV, Person, Collection, Company, and Keyword results, or resolve an IMDb, TheTVDB, Wikidata, Facebook, Instagram, or X/Twitter ID.
- **Open deep details** for titles, people, collections, organizations, keywords, seasons, episodes, and individual cast/crew credits, including navigable person/title links, role metadata, reviews, related content, and providers.
- **Browse complete TMDb media** through deduplicated image galleries and every valid YouTube trailer, teaser, clip, or featurette on Movie/TV, Season, and Episode details; season/episode pages also expose air date, runtime, production code, votes, and external identifiers when supplied.
- **Track episode progress offline** with per-episode and per-season watched controls. Progress is stored locally with the canonical `series:season:episode` key and remains independent from TMDb account Favorite/Watchlist sync.
- **Continue watching** from Season Detail opens the first unwatched episode in order and disappears after the season is complete.
- **Read catalog reviews and follow recommendations** with full review bodies, blank/duplicate removal, localized author fallbacks, optional rating/date metadata, same-media-type TMDb recommendations, current-title exclusion, a separate similar-titles shelf, and adult-title filtering unless the local PIN is unlocked on phone/tablet, Android TV, and KMP desktop/web.
- **Understand every regional edition** through production-company/network links, region-matched certification and release date, alternative titles, localized translations, and external identifiers on native Android/TV and KMP desktop/web.
- **Use TMDb account recommendations** from Profile with separate Movie/TV feeds, retry, pagination, title navigation, deduplication, and local adult-PIN filtering across phone, Android TV, and KMP desktop/web.
- **Manage mixed custom lists** by loading every list page, editing metadata, paging through Movie/TV contents, searching the catalog, and adding or removing titles with restart-safe optimistic synchronization across native Android/TV and KMP. Canonical `list_id` acknowledgements survive idempotent create replay, so a lost response cannot strand a pending list.
- **See where to watch** in a device or chosen region, open only TMDb URLs, and retain required JustWatch attribution.
- **Keep adult content private by default** behind local confirmation, a six-digit PIN, and five-attempt lockout. The gate partitions Search, External ID, Title, Person, Collection, Company, Network, Keyword, Credit Detail, recommendations and similar titles; native Android and KMP filter again before display, while Wear/public surfaces never receive restricted titles.
- **Connect TMDb safely** in a browser or by TV QR. Rate Movie/TV/Episode titles and manage account library/lists through optimistic durable retry without exposing credentials.
- **Keep a local-first library** with independent Favorite and Watchlist records in Room or the KMP platform store.
- **Use an interface built for each screen**: bottom navigation on phones, rail/list-detail layouts on larger windows, a dedicated 10-foot TV composition, and compact Wear OS actions.
- **Navigate without touch** using keyboard shortcuts on ChromeOS/desktop and focus-preserving D-pad navigation on Android TV.
- **Use the app in six languages**: English, Vietnamese, Japanese, Korean, Simplified Chinese, and Traditional Chinese.

## Latest screenshots

The gallery combines local Web/Wasm preview captures with native Android component goldens. Abstract artwork is generated demo imagery, not actual TMDb posters or portraits. Native goldens intentionally have no network images; they do not prove native image loading. No image contains an account token, personal list, adult title, or production credential.

Missing thumbnails? See the shared [image loading diagnosis and production checklist](https://github.com/LamPPKK/Smart-Movie-iOS/blob/main/docs/IMAGE_LOADING.md). Local preview image delivery has regression tests; staging/production Worker DNS remains a separate release blocker as checked on 2026-08-28.

### Native Android phone, tablet, TV and Wear OS

<table>
  <tr>
    <td width="34%" align="center"><strong>Phone</strong><br><sub>Compact cinematic Home composition</sub></td>
    <td width="66%" align="center"><strong>Tablet · foldable · ChromeOS · XR Home Space</strong><br><sub>Expanded layout and denser shelves</sub></td>
  </tr>
  <tr>
    <td align="center"><img src="https://raw.githubusercontent.com/LamPPKK/Smart-Movie-Android/main/app/src/screenshotTestDebug/reference/com/lamndt/smartmovie/CinematicGoldenPreviewsKt/PhoneHomeGolden_phone_compact_8dbe636a_0.png" alt="Smart Movie Android native Home on a compact phone" width="330"></td>
    <td align="center"><img src="https://raw.githubusercontent.com/LamPPKK/Smart-Movie-Android/main/app/src/screenshotTestDebug/reference/com/lamndt/smartmovie/CinematicGoldenPreviewsKt/TabletHomeGolden_tablet_expanded_97f2797e_0.png" alt="Smart Movie Android native Home on an expanded tablet" width="760"></td>
  </tr>
</table>

<table>
  <tr>
    <td width="72%" align="center"><strong>Android TV</strong><br><sub>Dedicated 10-foot layout and D-pad focus</sub></td>
    <td width="28%" align="center"><strong>Wear OS</strong><br><sub>Safe title/episode remote and phone handoff</sub></td>
  </tr>
  <tr>
    <td align="center"><img src="https://raw.githubusercontent.com/LamPPKK/Smart-Movie-Android/main/app/src/screenshotTestDebug/reference/com/lamndt/smartmovie/CinematicGoldenPreviewsKt/TvHomeGolden_tv_1080p_b8c175df_0.png" alt="Smart Movie Android Home on Android TV" width="760"></td>
    <td align="center"><img src="https://raw.githubusercontent.com/LamPPKK/Smart-Movie-Android/main/wear/src/screenshotTestDebug/reference/com/lamndt/smartmovie/wear/WearRemoteGoldenPreviewKt/WearRemoteGolden_wear_round_remote_a6040ff4_0.png" alt="Smart Movie Android companion remote on Wear OS" width="280"></td>
  </tr>
</table>

### Compose Multiplatform — desktop and Web/Wasm

The isolated KMP app shares its Kotlin data and Compose UI across macOS, Windows, Linux, JavaScript, and Wasm. The latest captures expose the same five destinations as the native clients.

<table>
  <tr>
    <td width="50%" align="center"><strong>Expanded desktop/web</strong><br><sub>Navigation rail and responsive catalog</sub></td>
    <td width="25%" align="center"><strong>Detail</strong><br><sub>Typed `/v2` metadata and actions</sub></td>
    <td width="25%" align="center"><strong>Profile</strong><br><sub>TMDb auth, region and adult-content PIN</sub></td>
  </tr>
  <tr>
    <td align="center"><img src="https://raw.githubusercontent.com/LamPPKK/Smart-Movie-Android/main/docs/images/screenshots/multiplatform-home-desktop.png" alt="Smart Movie Compose Multiplatform Home on desktop" width="720"></td>
    <td align="center"><img src="https://raw.githubusercontent.com/LamPPKK/Smart-Movie-Android/main/docs/images/screenshots/multiplatform-detail-phone.png" alt="Smart Movie Compose Multiplatform title detail" width="300"></td>
    <td align="center"><img src="https://raw.githubusercontent.com/LamPPKK/Smart-Movie-Android/main/docs/images/screenshots/multiplatform-profile-phone.png" alt="Smart Movie Compose Multiplatform Profile" width="300"></td>
  </tr>
</table>

See the complete [screen gallery](docs/SCREENSHOTS.md) for Home, Explore, Search, Detail, Library, Profile and capture provenance. [Platform support](docs/PLATFORM_SUPPORT.md) defines the exact release boundary for every device class, including why Android Auto is intentionally not declared.

## App and platform matrix

| App / device | Minimum / target | Experience | Release artifact |
| --- | ---: | --- | --- |
| Android phone | Android 8 / API 26+ | Five-destination navigation, entity details, account Profile, and compact detail flow | Main Android AAB |
| Tablet and foldable | Android 8 / API 26+ | Adaptive rail, expanded shelves, and list-detail layouts | Main Android AAB |
| ChromeOS | Android 8 / API 26+ | Resizable windows, pointer, and Ctrl/Cmd shortcuts | Main Android AAB |
| Android TV | Android 8 / API 26+ | Dedicated 10-foot UI, D-pad focus, TV search, retained navigation state | Main Android AAB |
| Android XR | Home Space panel | Resizable adaptive 2D experience; no immersive scene | Main Android AAB |
| Wear OS | Paired companion | Safe title/episode context and phone handoff over the Play services Data Layer | Wear companion AAB |
| Desktop | macOS 13+, Windows 10+, Ubuntu 20.04-compatible Linux | Shared Compose app with local library and native installers | DMG/PKG, MSI/EXE, DEB/RPM |
| Web | Modern JS/Wasm browser | Responsive Compose app with browser-local library; beta | Static JS/Wasm distribution |

The main and Wear AABs use the same Play application identity, semantic version, version code, and signing key. Desktop JVM, JavaScript, and Wasm are also release blockers for the coordinated 3.0 train.

## Architecture

```mermaid
flowchart LR
    subgraph Native["Native Android"]
        Phone["Phone · tablet · ChromeOS · XR"]
        TV["Android TV"]
        Wear["Wear OS remote"]
        Features["Feature modules"]
        Core["Core data · network · Room"]
        Phone --> Features
        TV --> Features
        Features --> Core
        Wear <-->|"Data Layer"| Phone
    end

    subgraph KMP["Compose Multiplatform"]
        Desktop["macOS · Windows · Linux"]
        Web["JS · Wasm"]
        Shared["Shared Compose UDF · Worker client"]
        Desktop --> Shared
        Web --> Shared
    end

    Core -->|"HTTPS /v2"| Worker["SmartMovie Cloudflare Worker"]
    Shared -->|"HTTPS /v2"| Worker
    Worker -->|"server-side v3/v4 credentials"| TMDb["TMDb API"]
    Worker --> Sessions["D1 session broker"]
    Snapshot["Versioned OpenAPI + fixtures snapshot"] -. conformance .-> Core
    Snapshot -. conformance .-> Shared
```

The native UI uses unidirectional data flow with immutable `UiState`, `StateFlow`, and screen-level ViewModels. Constructor-injected repositories keep UI modules independent from Retrofit, Room, and transport details. The KMP project has its own shared UDF state, Worker client, persistence adapters, and adaptive Compose UI under `multiplatform/`.

### Module map

| Module | Responsibility |
| --- | --- |
| `app` | Mobile entry point, dedicated TV activity, Navigation 3, adaptive navigation, and application container |
| `core:model` | Immutable domain models and repository contracts |
| `core:network` | Retrofit, Kotlin Serialization, installation identity, retry/cancellation policy |
| `core:database` | Room library/account outboxes, lossless migrations, and committed schemas |
| `core:data` | Repository implementations, durable mutation delivery, image URL policy, Paging sources |
| `core:remote` | Versioned phone/watch commands and state serialization |
| `core:designsystem` | Cinematic palette, offline fonts, and shared accessible components |
| `feature:*` | Home, Explore, Search, Library, Detail, and About |
| `wear` | Compose for Wear OS companion and Data Layer remote |
| `catalog-contract` | Checksummed snapshot of the canonical Worker contract and fixtures |
| `multiplatform/composeApp` | Desktop/web catalog + account state, persistent outboxes, adaptive UI, and platform entry points |

## Repository layout

```text
Smart-Movie-Android/
├── app/                 # Native Android phone/tablet/TV/XR application
├── wear/                # Wear OS companion application
├── core/                # Model, network, database, data, remote, design system
├── feature/             # Product feature modules
├── catalog-contract/    # Versioned snapshot from the canonical Worker contract
├── multiplatform/       # Desktop JVM plus JS/Wasm Compose application
├── release/             # Shared SmartMovie 3.0 release-train manifest
├── scripts/             # Read-only Doctor and scoped release checks
├── docs/                # Screenshots, testing, privacy, platform support, release docs
└── .github/workflows/   # Native CI, emulator smoke tests, KMP builds, signed releases
```

## Getting started

### Requirements

- JDK 17 for native Android
- JDK 21 for Compose desktop/web
- Android SDK 36 and Build Tools 36.0.0
- Android Studio with an API 35 phone image for instrumentation tests
- An Android TV emulator image for D-pad smoke testing

Run the read-only environment check before Gradle:

```sh
./scripts/doctor.sh
```

Clone and build the native apps:

```sh
git clone https://github.com/LamPPKK/Smart-Movie-Android.git
cd Smart-Movie-Android
./gradlew --dependency-verification=strict :app:assembleDebug :wear:assembleDebug
```

The native debug build calls `https://staging-catalog.smartmovie.app/`; release calls `https://catalog.smartmovie.app/`. Both support legacy `/v1` and additive `/v2`. `/v2/capabilities` keeps account and Advanced Discover UI disabled until the deployed Worker advertises support; phone requires `browser_auth`, Android TV requires `tv_qr_auth`, and missing/false flags prevent profile/auth requests while showing a localized unavailable state. Explore remains available through the basic `/v1` route during rollout or rollback. No TMDb or Cloudflare credential belongs in a client.

### Run desktop or web

The multiplatform project is deliberately isolated so its JDK 21 and Compose toolchain cannot destabilize the locked Android/Play build.

```sh
cd multiplatform
./gradlew :composeApp:run
./gradlew :composeApp:wasmJsBrowserDevelopmentRun
```

See [`multiplatform/README.md`](multiplatform/README.md) for JS, Wasm, desktop packaging, and platform-specific local storage.

## Tests and quality gates

Run the native source gates from the repository root:

```sh
./gradlew --dependency-verification=strict \
  :core:model:test \
  :core:remote:test \
  testDebugUnitTest \
  validateDebugScreenshotTest \
  lintDebug \
  :app:assembleDebug \
  :wear:assembleDebug \
  :app:assembleDebugAndroidTest \
  :app:bundleRelease \
  :wear:bundleRelease

./scripts/verify-release.sh mobile
```

Native tests cover model/contract/network/data/Room migrations, durable account/library outboxes, feature state, deterministic catalog-review/recommendation and media-gallery presentation, non-empty editorial and Movie/TV/Season/Episode fixtures, remote protocol, and screenshot validation. CI also runs API 35 phone instrumentation and a dedicated Android TV launch/D-pad smoke with Linux KVM enabled.

Run KMP verification independently with JDK 21:

```sh
cd multiplatform
./gradlew --dependency-verification=strict \
  :composeApp:desktopTest \
  :composeApp:compileKotlinDesktop \
  :composeApp:jsBrowserDevelopmentExecutableDistribution \
  :composeApp:wasmJsBrowserDistribution

cd ..
./scripts/verify-release.sh kmp
```

Read [Testing](docs/TESTING.md) for the complete suite, emulator setup, golden update policy, and reports.

## Contract compatibility

`catalog-contract/` is a checksummed snapshot of the canonical OpenAPI document and fixtures from the companion Apple/backend repository. Native Android and KMP conformance tests decode the same success and error fixtures, including unknown fields, missing nullable values, half-step/unrated Episode account state, and the canonical list identifier returned by first and replayed account mutations.

The manifest records the contract version, upstream commit, OpenAPI SHA-256, and fixture SHA-256. A protected cross-repository workflow opens or refreshes the Android snapshot PR whenever the canonical contract changes. Production Worker promotion is blocked until Android `main` matches all four release inputs.

Run `./scripts/verify-release.sh mobile`, `./scripts/verify-release.sh kmp`, or omit the scope to check the full repository. A store candidate also requires a real 40-character upstream commit in the manifest; the local bootstrap marker is development-only.

The backend repository's canonical [TMDb coverage matrix](https://github.com/LamPPKK/Smart-Movie-iOS/blob/main/docs/TMDB_COVERAGE.md) distinguishes user-facing, backend-only, intentionally excluded, and still-blocking endpoint groups. A green contract checksum alone does not mean every product surface is complete.

## Privacy and security

- TMDb application credentials and encrypted upstream account tokens exist only in the companion repository's protected Worker services.
- The Android library is stored in Room and included in Google Auto Backup; HTTP cache and the installation UUID are excluded.
- Native Android stores only the opaque SmartMovie session in Keystore-backed storage; Web uses secure cookie auth.
- Favorite, Watchlist, rating, and custom-list mutations update optimistically and remain in account-scoped durable outboxes until the Worker acknowledges the same idempotency key.
- The adult PIN and lockout state remain local per device and are excluded from backup/account transport.
- Wear OS commands and safe title/episode state travel only through the paired-device Data Layer; trailer and library mutations remain title-only.
- KMP caches/outboxes remain in Java Preferences on desktop and `localStorage` on web.
- There is no separate SmartMovie identity, analytics SDK, advertising SDK, or in-app purchase flow.

## Release status

The shared release manifest pins SmartMovie `3.0.0` and contract `2.0.0`. Android `versionName` matches the Apple marketing version; Android `versionCode` and Apple build numbers increase independently.

The protected `production` GitHub environment must provide `ANDROID_KEYSTORE_BASE64`, `ANDROID_KEYSTORE_PASSWORD`, `ANDROID_KEY_ALIAS`, and `ANDROID_KEY_PASSWORD`. The signed release workflow produces both the main and Wear companion AABs for the same Play listing. Desktop signing/notarization identities are separate and do not block Play delivery.

Before Play submission, configure Worker D1/session secrets and callback domains, pass catalog + protected account smoke tests, configure signing, run Play Internal Testing on phone/tablet/TV/Wear devices, build desktop/JS/Wasm candidates, and complete artwork, screenshots, privacy, age rating, attribution, and support metadata.

## Attribution

This product uses the TMDB API but is not endorsed or certified by TMDB. Movie and television metadata and artwork are supplied by [The Movie Database](https://www.themoviedb.org/). Availability data is supplied by JustWatch through TMDb and is attributed wherever shown.
