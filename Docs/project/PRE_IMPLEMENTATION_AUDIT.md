# NuvioMobile PRE-IMPLEMENTATION AUDIT (CN + Emby)

**Document Version:** 1.1.0 (Post-Audit Correction Pass)  
**Baseline SHA:** `9bc77bc48e0cc4958006657f129190169d831e33`  
**Upstream Target:** `https://github.com/NuvioMedia/NuvioMobile.git` (`upstream/cmp-rewrite`)  
**Development Branch:** `develop/cn-emby`  
**Origin:** `https://github.com/717982176/NuvioMobile.git`  
**Status:** Phase 0 CLOSED (Closure Gate Passed - Ready for Phase 1)  

---

## 1. Git Baseline Verification

### 1.1 Command Outputs & Exact Hashes
- **Current Branch:** `develop/cn-emby`
- **Current HEAD Commit:** `9bc77bc48e0cc4958006657f129190169d831e33`
- **Commit Subject:** `chore(store): publish 0.4.21`
- **Commit Date:** `2026-09-15 10:29:48 +0000`
- **Tag at HEAD:** `upstream-baseline-2026-09-15`
- **Remotes:**
  - `origin`: `https://github.com/717982176/NuvioMobile.git` (fetch & push)
  - `upstream`: `https://github.com/NuvioMedia/NuvioMobile.git` (fetch & push)
- **Upstream Comparison:**
  - `git rev-parse upstream/cmp-rewrite` = `9bc77bc48e0cc4958006657f129190169d831e33`
  - `git merge-base HEAD upstream/cmp-rewrite` = `9bc77bc48e0cc4958006657f129190169d831e33`
  - **Verdict:** `HEAD` is 100% aligned with the latest upstream `cmp-rewrite` branch. No merge or rebase required at baseline.

### 1.2 Git Submodule & Working Tree State
- **Submodules in `.gitmodules`:**
  - `MPVKit` -> `https://github.com/NuvioMedia/MPVKit.git`, branch `Nuvio`
  - Commit pointer: `-d5cf091c80368bbbc1bbf2d195fbc55d926df888 MPVKit`
- **Submodule Anomaly Detected:**
  - `git ls-tree HEAD libass-android` shows a committed gitlink (`mode 160000 commit c10b71ab8b4d90796d4a795f775d337c29198ad0`) without a matching entry in `.gitmodules`.
  - Running `git submodule status` emits a non-fatal warning/exit code 128 unless filtered. The iOS build script does not initialize `libass-android`, so iOS build is unaffected.
- **Working Tree Cleanliness:**
  - Local `core.autocrlf` configured to `false` to avoid false-positive diffs caused by Windows CRLF translation. Working tree is clean.

---

## 2. GitHub Actions CI/CD Audit

### 2.1 Workflow Audit: `.github/workflows/ios-test-build.yml`
- **Workflow Name:** `Build Test IPA`
- **Trigger:** `workflow_dispatch` (Manual trigger with input: `configuration` choice between `Debug` and `Release`, default `Release`).
- **Runner Configuration:** `runs-on: macos-26` (Configured in commit `be6beca2`).
- **Xcode Environment:** `DEVELOPER_DIR: /Applications/Xcode_26.6.app/Contents/Developer`.
- **Java Setup:** `actions/setup-java@v5` with `temurin` version `17`.
- **Gradle Setup:** `gradle/actions/setup-gradle@v6`.
- **Kotlin/Native Cache:** `actions/cache@v5` caching `~/.konan` keyed by `konan-${{ runner.os }}-${{ runner.arch }}-${{ hashFiles('gradle/libs.versions.toml') }}`.
- **Dependency Preparation:** `./scripts/prepare-ios-dependencies.sh`
  - Checks if `MPVKit/Package.swift` exists; if not, clones submodule with depth 1.
  - Downloads prebuilt `nuvio-engine-apple-0.1.1.zip` from `https://github.com/NuvioMedia/nuvio-engine/releases/download/v0.1.1/` (SHA-256: `24905c0484b2e5c886c2685ce03e5f5585c3dc6096c65c59948b35be56ae4dc0`).
  - Extracts `NuvioEngine.xcframework` containing `ios-arm64/libCNuvioEngine.a`.
- **Build Execution:** `./scripts/build-ios-ipa.sh`
  - Derives build parameters from `iosApp/Configuration/Version.xcconfig`.
  - Runs `xcodebuild -project iosApp/iosApp.xcodeproj -scheme iosApp -configuration Release -sdk iphoneos -destination generic/platform=iOS CODE_SIGNING_ALLOWED=NO CODE_SIGNING_REQUIRED=NO CODE_SIGN_IDENTITY= build`.
  - Packages compiled `Nuvio.app` into an unsigned IPA under `build/ios-ipa/nuvio-${version}-full-${configuration}.ipa`.
  - Validates `arm64` architecture and `DownloadsWidgetExtension.appex`.
- **Artifact Upload:** `actions/upload-artifact@v7`, retained for 7 days.
- **Audit Assessment & Runner Clarification:**
  - **REUSE EXISTING WORKFLOW (with Fork Environment Compatibility Fix).**
  - **Runner Verdict:** In the current runtime environment, GitHub Hosted Runners officially support `macos-26`, and the `macos-26` runner image includes `Xcode 26.6`.
  - **Policy:** **DO NOT** switch the runner to `macos-15`. Retain the upstream official runner configuration.
  - **Empirical CI Execution Audit (Build Test IPA #2):**
    - `macos-26-arm64` runner scheduled and executed successfully.
    - Apple toolchain verification (`xcodebuild -version`, `xcrun --sdk iphoneos`) succeeded with Xcode 26.6.
    - `./scripts/prepare-ios-dependencies.sh` succeeded (MPVKit submodule and NuvioEngine.xcframework verified).
    - DownloadsWidgetExtension Swift compilation succeeded.
    - **Failure Point:** Build terminated at `:composeApp:generateRuntimeConfigs` because Gradle 9.4.1 declared `localPropertiesFile` as an InputFile that did not exist.
    - **Root Cause:** Upstream workflow conditionally configured runtime properties via `if: env.RELEASE_PROPERTIES_BASE64 != ''`. Because GitHub forks do not inherit upstream repository secrets (`secrets.NUVIO_LOCAL_PROPERTIES_BASE64`), the step was skipped, leaving `local.properties` absent.
    - **Failure Classification:** CI fork-environment compatibility issue (NOT an application business logic, MPVKit, or localization defect).
    - **Fix Applied:** Modified `.github/workflows/ios-test-build.yml` to always run `Configure runtime properties`, creating an empty `local.properties` fallback if the secret is absent while maintaining full upstream behavior when the secret is present.
  - **Risk Classification:** **LOW / Resolved CI Configuration**.

---

## 3. Real Repository Architecture

### 3.1 Module Hierarchy
```
NuvioMobile (Root)
├── composeApp/                     # Kotlin Multiplatform Core Logic & UI
│   ├── build.gradle.kts           # KMP targets, dependencies, runtime configs
│   └── src/
│       ├── commonMain/             # Shared business logic, screens, navigation
│       │   ├── composeResources/   # Compose Multiplatform Resources (i18n, icons)
│       │   └── kotlin/com/nuvio/app/
│       │       ├── core/          # Cross-cutting concerns (auth, network, storage, time, ui)
│       │       ├── features/      # Feature modules
│       │       └── navigation/    # Navigation 3 routing & transitions
│       ├── fullCommonMain/         # Full-edition extensions (QuickJS plugins, YouTube)
│       ├── iosMain/               # iOS platform actuals (MPV bridge, NSUserDefaults, audio)
│       ├── iosFull/               # iOS full build (P2P engine, native crypto)
│       ├── iosAppStore/           # iOS AppStore build stubs
│       ├── androidMain/           # Android platform actuals (ExoPlayer, mpv, WorkManager)
│       ├── androidFull/           # Android full distribution
│       ├── androidPlaystore/      # Android Play Store distribution
│       ├── desktopMain/           # Desktop JVM target stubs
│       └── nativeInterop/cinterop # C-interop definitions (commoncrypto, appicon, nuvioengine)
├── iosApp/                        # Native iOS Wrapper & Player Engine
│   ├── iosApp/
│   │   ├── iOSApp.swift           # Application entry point
│   │   ├── ContentView.swift      # Compose UI container & navigation hosting
│   │   └── Player/                # Native MPV Player Implementation
│   │       ├── MPVPlayerBridge.swift # Swift bridge conforming to NuvioPlayerBridge
│   │       ├── MetalLayer.swift      # Metal-backed rendering layer for MPV
│   │       └── NowPlayingController.swift # MPNowPlayingInfoCenter & MPRemoteCommandCenter
│   └── DownloadsWidgetExtension/  # Live Activity Widget Extension
└── scripts/                       # Build, dependency, and release scripts
```

### 3.2 Key Architectural Realities
1. **The Player Engine on iOS is MPV, NOT AVPlayer:**
   - Upstream Nuvio uses `Libmpv` compiled inside `MPVKit` with custom Metal rendering via `MetalLayer.swift`.
   - Communication between Kotlin and Swift is cleanly abstracted via `NuvioPlayerBridge` protocol.
   - **Realistic Player Capabilities & Boundaries:** MPVKit/libmpv provides a solid architectural foundation for broad Direct Play formats. However, actual playback capability is bounded by:
     - `libmpv` build configuration and enabled demuxers/parsers;
     - `FFmpeg` codec library build flags;
     - iOS hardware decoder capabilities and chip generation constraints (e.g. A14 vs A17 Pro/M-series);
     - Audio output routing (PCM downmix vs spatial/passthrough);
     - HDR tone-mapping and EDR pipeline;
     - Dolby Vision profile support (Profile 5, Profile 7 FEL/MEL, Profile 8);
     - Subtitle codec support and libass CJK text rasterization.
   - Absolute claims of "plays all formats/containers" are prohibited. A rigorous empirical test matrix is required in Phase 6.
2. **Storage Architecture:**
   - All persistent storage currently uses `NSUserDefaults` on iOS and `SharedPreferences` on Android.
   - **No Keychain / Secure Enclave abstraction exists.** All API tokens (Trakt, Simkl, Supabase) are currently stored in plain preferences.
3. **Navigation Architecture:**
   - Uses AndroidX Navigation 3 for Compose Multiplatform (`NavKey`, `NuvioNavigator`, `MainAppContent.kt`).
   - SwiftUI hosts the root view and interacts with native tab bar via `NativeTabBridge`.

---

## 4. Full Localization Audit

### 4.1 Current Locales in `composeApp/src/commonMain/composeResources/`
Existing 23 localized resource directories:
`values` (Default / en), `values-ar`, `values-bg`, `values-cs`, `values-de`, `values-el`, `values-es`, `values-fr`, `values-he`, `values-hu`, `values-id`, `values-in`, `values-it`, `values-ja`, `values-nb`, `values-nl`, `values-pl`, `values-pt`, `values-pt-rBR`, `values-ro`, `values-ru`, `values-sk`, `values-tr`, `values-vi`.

**Chinese (`zh` / `zh-rCN` / `zh-Hans`) is 100% missing from upstream.**

### 4.2 Language Configuration (`AppLanguage.kt`)
`AppLanguage` enum contains 24 entries (`DEVICE` + 23 languages). No Chinese entry exists in upstream.

### 4.3 Classification of Strings Across the Project
| Classification | Description | Current State | Required Action |
|---|---|---|---|
| **Category A: Static UI Strings** | Strings defined in `values/strings.xml` (2,249 total keys) | English only in upstream | Supply comprehensive `values-zh-rCN/strings.xml` and `values-zh/strings.xml` |
| **Category B: Remote Metadata** | Title, overview, season/episode names, genres | Comes in English from Cinemeta/Addons | Centralized TMDB enrichment with `zh-CN` priority |
| **Category C: Addon & Stream Labels** | Addon names, stream descriptions, stream badges | Generated dynamically by community addons | Preserve original stream names; translate badge labels & UI chrome |
| **Category D: Low-level / Player Errors** | System and network errors, MPV decoder errors | Hardcoded in `LocalizedUiText.kt` & repositories | Map through `stringResource` |
| **Category E: Technical Literals** | Codec tags ("HEVC", "DTS"), MIME types, Route keys | Hardcoded in code | Must NOT be translated |

---

## 5. Audit of Existing Chinese Translations (PR #1941)

### 5.1 Overview of PR #1941
- **Source:** `https://github.com/NuvioMedia/NuvioMobile/pull/1941`
- **Branch/Ref:** `refs/pull/1941/head` (`7c4be289 Rename folder`)
- **License:** GPL-3.0 (Part of NuvioMobile)
- **Files Modified:**
  - `composeApp/src/androidMain/res/xml/locale_config.xml`
  - `composeApp/src/commonMain/kotlin/com/nuvio/app/features/settings/AppLanguage.kt`
  - `composeApp/src/commonMain/composeResources/values-zh/strings.xml`
  - `composeApp/src/commonMain/composeResources/values-zh-rCN/strings.xml`
  - `composeApp/src/commonMain/composeResources/values-zh-rTW/strings.xml`

### 5.2 Key Metrics & Defect Analysis
- **Key Count:** 2,241 translated keys out of 2,249 base keys in `strings.xml`.
- **Format Integrity:** 100% placeholder accuracy (0 formatting mismatches).
- **Plurals:** Correctly defines 3 plurals (`cw_airs_in_days`, `cw_airs_in_days_short`, `entity_browse_title_count`).
- **Defects Identified:**
  1. **Missing 8 Keys:**
     - `player_seek_position`: "Seek %1$s"
     - `profile_already_active`: "Profile is already active"
     - `settings_stream_background_description`: "Choose whether to display artwork or a solid dark background on the streams screen."
     - `settings_stream_background_title`: "Background"
     - `settings_tracking_anime_id_tvdb`: "TVDB (Preferred)"
     - `settings_tracking_anime_id_tvdb_description`: "Resolves anime IDs using TVDB mappings via Simkl."
     - `trailer_enter_fullscreen`: "Enter fullscreen"
     - `trailer_exit_fullscreen`: "Exit fullscreen"
  2. **Taiwanese Phrasing in Simplified Chinese:**
     - `values-zh-rCN` contains multiple Taiwanese terms directly converted to simplified characters (e.g., "自订", "网页面板", "快取", "汇入", "专案").
  3. **Folder Structure Ambiguity:**
     - `values-zh` contains Traditional Chinese identical to `values-zh-rTW`. Under standard Android/Compose resource resolution, an unregioned `zh` locale defaults to Traditional Chinese. For `develop/cn-emby`, `values-zh` must default to Simplified Chinese (`zh-Hans` / `zh-CN`).
  4. **Build & Resource Generation Requirement:**
     - Upstream maintainers require running Compose Multiplatform resource generation (`generateComposeResClass`) and locale validation tests. Simply cherry-picking the raw XML files without rebuilding resource classes causes build inconsistencies.
- **Verdict:** **USE WITH FIXES**.
  - Direct cherry-pick without fixes is strictly prohibited.
  - Phase 1 must import the baseline, add missing keys, normalize terminology, set `values-zh` to Simplified Chinese, and rerun resource generation and test suites.

---

## 6. Chinese Locale Strategy & Terminology Normalization

### 6.1 Directory & Locale Mapping Strategy
- Primary target: Mainland China Simplified Chinese (`zh-CN`).
- Directory structure:
  - `composeApp/src/commonMain/composeResources/values-zh/` -> **Simplified Chinese fallback** (Prevents generic `zh` falling back to Traditional Chinese).
  - `composeApp/src/commonMain/composeResources/values-zh-rCN/` -> **Mainland China Simplified Chinese** (`zh-Hans`, `zh-CN`).
  - `composeApp/src/commonMain/composeResources/values-zh-rTW/` -> **Traditional Chinese** (`zh-Hant`, `zh-TW`).
- Settings UI (`AppLanguage.kt`):
  - In v1, expose clear and unambiguous options:
    - `简体中文` (`zh-CN`)
    - `繁體中文` (`zh-TW`)
  - Do NOT expose an ambiguous generic "中文" item in UI.

### 6.2 Mandatory Terminology Normalization Rules
Simple character-level conversion is strictly prohibited. All user-facing strings must adhere to standard Mainland software terminology:
- 自订 → **自定义**
- 快取 → **缓存**
- 汇入 → **导入**
- 汇出 → **导出**
- 专案 → **项目**
- 储存 → **保存**
- 网页面板 → **Web 控制台**
- 附加元件 → **插件** 或 **扩展** (根据上下文统一)
- 资料 → **数据** 或 **信息** (根据上下文统一)
- 设定 → **设置**

---

## 7. Audit of NuvioMobile-Enhanced (`luqmanfadlli/NuvioMobile-Enhanced`)

| Feature | Relevant Commits | Modified Files | Upstream Status | Recommendation |
|---|---|---|---|---|
| **CJK Subtitle Font Resolver** | `bcedb582` | `MPVSubtitleFontResolver.swift`, `MPVPlayerBridge.swift`, `NotoSansCJKsc-Regular.otf` | Not in upstream | **CHERRY-PICK CANDIDATE / REUSE.** Solves iOS MPV sandbox limitation where system CJK fonts cannot be opened by FreeType. Bundles Noto Sans CJK SC and dynamically observes `sub-text` to apply CJK font. Critical for Chinese subtitles. |
| **Audio Language Preference Bridge** | `012d663a` | `MPVPlayerBridge.swift` | Merged in upstream (`4f79bfe0`) | **IGNORE (ALREADY MERGED).** Upstream already implements `applyAudioLanguagePreferences`. |
| **iOS Picture-in-Picture** | `ce510201`, `e7f49411`, `b740c288`, `bd055280` | `MPVPictureInPictureFrameCapture.swift`, `SampleBufferDisplayView.swift` | Not in upstream | **REFERENCE ONLY.** Uses AVSampleBufferDisplayLayer frame capture from MPV. Resource intensive. Defer to post-MVP phase. |
| **Playback Info Modal ("Stats for Nerds")** | `ce510201` | `PlaybackInfoModal.kt` | Not in upstream | **REFERENCE ONLY.** Useful diagnostic tool for video codec, resolution, and dropped frames. Can be evaluated in Phase 9. |
| **Quality Preset Selector** | `ce510201` | `PlayerQualitySelector.kt`, `PlayerQualityPanel.kt` | Not in upstream | **REFERENCE ONLY.** Allows manual stream resolution clamping. |

---

## 8. Metadata Data Flow Chain Audit

### 8.1 Pipeline Flow
```
Catalog / Search / Detail Request
  │
  ▼
AddonRepository (Resolves enabled manifests matching resource="meta")
  │
  ▼
fetchAddonResponseText(url) (Fetches Stremio Cinemeta / addon JSON)
  │
  ▼
MetaDetailsParser.parse(payload) (Constructs initial base MetaDetails)
  │
  ▼
TmdbMetadataService.enrichMeta(meta, fallbackItemId, settings)
  │  ├── Fetch TMDB details (overview, localized title, genres, ratings)
  │  ├── Fetch TMDB season/episodes (titles, episode overviews, thumbnails)
  │  └── Fetch TMDB localized images (posters, backdrops, logos)
  │
  ▼
MdbListMetadataService.enrichMeta(...) (Ratings enrichment)
  │
  ▼
MetaDetailsUiState (Presented to Compose UI)
```

### 8.2 Entity Models & Identity Mapping
- **`MetaDetails` (`MetaDetailsModels.kt`):** `id`, `type`, `name`, `description`, `poster`, `background`, `logo`, `videos: List<MetaVideo>`.
- **`MetaVideo` (`MetaDetailsModels.kt`):** `id`, `season`, `episode`, `title`, `overview`, `thumbnail`.
- **`MetaPreview` (`HomeModels.kt`):** `id`, `type`, `name`, `poster`, `banner`, `logo`, `imdbRating`.

---

## 9. Chinese Metadata Enrichment Architecture & Investigation Policy

### 9.1 Centralized Insertion Point
- Metadata enrichment MUST occur inside `TmdbMetadataService.kt` and `MetaDetailsRepository.kt`.
- Composable UI files must NEVER query TMDB or any metadata provider directly.

### 9.2 Fallback Resolution Rules
1. **Title & Overview:** Preference: `zh-CN` -> `zh` -> English (`en`) -> Original addon text.
2. **Posters:** Preference: `zh-CN` poster -> `zh` poster -> Language-neutral (`iso_639_1 == null`) -> Original addon poster.
3. **Episode Titles & Overviews:** Query TMDB TV season endpoint with `language=zh-CN`. Fall back to English if Chinese is missing.

### 9.3 Metadata Connectivity & Configuration Policy
- **No Mandatory API Keys:** Do NOT mandate that regular users supply personal TMDB API keys or proxy configurations as a prerequisite for Chinese metadata.
- **Phase 2 Investigation Order:**
  1. Audit and prioritize reusing Nuvio / Enhanced existing backend enrichment and built-in metadata configurations;
  2. Evaluate built-in fallback proxies only if direct connections encounter region blocks;
  3. Offer custom TMDB API Key and custom proxy URL as **optional advanced settings** for power users, not mandatory onboarding steps.

---

## 10. Stream & Playback Invocation Chain Audit

### 10.1 Call Chain
```
User clicks Play / Episode
  │
  ▼
MetaDetailsScreen (`onPrimaryPlayClick`)
  │
  ▼
MainAppContent (`launchPlaybackWithDownloadPreference` -> `StreamRoute`)
  │
  ▼
StreamDestination (`StreamDestination.kt`)
  │  ├── Queries StreamsRepository.load(...)
  │  └── Selects StreamItem (Manual click or AutoPlay rule)
  │
  ▼
PlayerLaunchStore.put(playerLaunch)
  │
  ▼
navController.navigate(PlayerRoute(launchId))
  │
  ▼
PlayerDestination (`PlayerDestination.kt` -> `PlayerScreenContent.kt`)
  │
  ▼
PlayerEngine (Kotlin Multiplatform expect/actual)
  │
  ▼ (On iOS)
NuvioPlayerBridge -> MPVPlayerViewController (Swift / Libmpv / Metal)
```

---

## 11. Emby Integration Architecture

### 11.1 Structural Decoupling
All Emby components are strictly isolated under:
`com.nuvio.app.features.emby`

```
com.nuvio.app.features.emby/
├── api/
│   ├── EmbyApiClient.kt           # Ktor HTTP client for Emby REST API
│   ├── EmbyAuthApi.kt             # /Users/AuthenticateByName & Ping
│   ├── EmbyItemsApi.kt            # /Users/{userId}/Items & /Shows/{id}/Episodes
│   ├── EmbyPlaybackApi.kt         # /Items/{id}/PlaybackInfo
│   └── EmbySessionApi.kt          # /Sessions/Playing, Progress, Stopped
├── domain/
│   ├── EmbyServerConfig.kt        # Server URL, ServerId, UserId
│   ├── EmbyItemModels.kt          # DTOs: MediaSource, MediaStream, ProviderIds
│   ├── EmbyMatcher.kt             # High-accuracy ID and Season/Episode matcher
│   ├── EmbyDeviceProfileBuilder.kt# Generates verified iOS MPV DeviceProfile
│   └── EmbyPlaybackResolver.kt    # Resolves PlaybackInfo to Direct Play StreamItem
├── repository/
│   ├── EmbyAccountRepository.kt   # Manages active servers & accounts
│   ├── EmbyStreamRepository.kt    # Injects Emby streams into StreamsRepository
│   └── EmbyScrobbleAdapter.kt     # Reports playhead progress to Emby session
└── storage/
    ├── EmbyAccountStorage.kt      # Account metadata (NSUserDefaults)
    └── EmbySecureTokenStorage.kt  # AccessToken storage (Keychain / KeyStore)
```

### 11.2 Injection into Nuvio Streams Pipeline
- Integration point is the **Streams repository layer** (`StreamsRepository.kt`).
- When `StreamsRepository.load(...)` runs:
  1. `EmbyStreamRepository` asynchronously queries the configured Emby server using standard item IDs.
  2. If matched, an Emby StreamItem (`EMBY [Direct Play]`) is prepended as a preferred source.
  3. If AutoPlay is enabled and Emby matches, it seamlessly plays the Emby source.
  4. If Emby is offline, unreachable, or unmatched, the stream list automatically presents **existing Nuvio Addon streams** without delay or error dialogs.
  5. The architecture is completely provider-agnostic and coexists with any Stremio-compatible addon without depending on any specific commercial service.

---

## 12. Emby Authentication & Secure Storage Design

### 12.1 Authentication Protocol
1. User enters: `Server URL`, `Username`, `Password`.
2. App sends `POST /Users/AuthenticateByName` with standard MediaBrowser authorization headers.
3. Server returns `AccessToken`, `User.Id`, `ServerId`.
4. Cleartext password is immediately discarded from memory.

### 12.2 Secure Storage Abstraction
- Minimal cross-platform abstraction (`expect`/`actual`):
  - `commonMain`: `interface SecureStorage { fun get(key: String): String?; fun set(key: String, value: String); fun remove(key: String) }`
  - `iosMain`: Implemented via `Security.framework` using `kSecClassGenericPassword` (Keychain).
  - `androidMain`: Implemented via `EncryptedSharedPreferences`.
- `AccessToken` is stored strictly in `SecureStorage`. Plain preferences are strictly prohibited for tokens.

---

## 13. Emby Matching Algorithm Design

### 13.1 Provider ID Matching Priority
- **Movies:**
  1. `AnyProviderIdEquals=imdb.{imdbId}`
  2. `AnyProviderIdEquals=tmdb.{tmdbId}`
  3. Fallback: `SearchTerm={title}&IncludeItemTypes=Movie&Years={year}`
- **Series:**
  1. `AnyProviderIdEquals=tvdb.{tvdbId}`
  2. `AnyProviderIdEquals=tmdb.{tmdbId}`
  3. `AnyProviderIdEquals=imdb.{imdbId}`
  4. Fallback: `SearchTerm={seriesName}&IncludeItemTypes=Series`
- **Episodes:**
  1. Match Series ID.
  2. Query `/Shows/{seriesId}/Episodes?UserId={userId}&Season={seasonNumber}`.
  3. Match `IndexNumber == episodeNumber && ParentIndexNumber == seasonNumber`.
  4. Support Season 0 (Specials).

---

## 14. Emby PlaybackInfo, DeviceProfile & Capability Matrix

### 14.1 DeviceProfile Builder (`EmbyDeviceProfileBuilder`)
- Phase 6 introduces `EmbyDeviceProfileBuilder` to generate the `DeviceProfile` payload for `POST /Items/{itemId}/PlaybackInfo?UserId={userId}`.
- Rather than declaring universal Direct Play for all formats, `DeviceProfile` is derived strictly from verified iOS player capabilities.

### 14.2 Phase 6 Required Testing Matrix
Before finalized profiles are enabled, the following formats must be empirically validated on target devices:
- **Containers:** `mp4`, `mov`, `mkv`, `webm`, `mpegts`, `hls`
- **Video Codecs:** `H.264`, `HEVC`, `VP9`, `AV1`
- **Audio Codecs:** `AAC`, `AC3`, `EAC3`, `DTS`, `TrueHD`, `FLAC`
- **Subtitle Codecs:** `SRT`, `ASS/SSA`, `WebVTT`, `PGS`
- **Dynamic Ranges:** `SDR`, `HDR10`, `HLG`, `Dolby Vision` (Profile 5, Profile 8.1)
- *Conservative Stance:* Until validated, formats requiring unsupported decoders (or high-level Dolby Vision Profile 7 EL) will negotiate Direct Stream (remux) or Transcode.

---

## 15. Audio Track, Subtitle & Session Synchronization

- Emby `MediaStreams` mapped to `PlayerLaunch.externalSubtitles` and MPV track selection.
- Port `MPVSubtitleFontResolver` and bundle `NotoSansCJKsc-Regular.otf` to guarantee correct CJK subtitle rendering on iOS.
- Full session sync: `POST /Sessions/Playing`, `POST /Sessions/Playing/Progress` (every 10s / seek / pause), `POST /Sessions/Playing/Stopped`.
- Watched threshold (>= 80%) marks item as Played on Emby without disrupting Trakt or Simkl scrobbles.

---

## 16. Comprehensive Risk Assessment Matrix

| ID | Risk Description | Severity | Mitigation Strategy |
|---|---|---|---|
| **R1** | **GitHub Actions Cloud Build Operational Verification** | **LOW** | Upstream uses `runs-on: macos-26` and `Xcode_26.6.app`. GitHub hosted runners support `macos-26`. Phase 0 includes a baseline cloud build gate. Only modify workflow if actual cloud build fails. |
| **R2** | **Chinese Translation Drift on Upstream Updates** | **MEDIUM** | Centralize in `values-zh-rCN`. Maintain terminology normalization rules. Run automated string comparison scripts during upstream sync. |
| **R3** | **Metadata Connectivity in Restricted Networks** | **MEDIUM** | Prioritize built-in Nuvio/Enhanced metadata pipelines. Provide optional proxy/API key configurations in advanced settings without forcing onboarding friction. |
| **R4** | **Emby Media Misidentification (False Positive)** | **HIGH** | Strict ID-first matching (`imdb`, `tmdb`, `tvdb`). Do not rely on loose title matching. Require season/episode number verification for series. |
| **R5** | **Plaintext Token Leakage** | **HIGH** | AccessToken must NEVER be stored in `NSUserDefaults` or plain SharedPreferences. Store exclusively via `Security.framework` (Keychain) and KeyStore. |
| **R6** | **Unverified Direct Play Failure** | **MEDIUM** | Avoid assuming universal Direct Play. Implement `EmbyDeviceProfileBuilder` backed by Phase 6 real capability test matrix. Keep profile conservative until verified. |
| **R7** | **CJK Subtitle Missing Glyphs on iOS** | **HIGH** | Port `MPVSubtitleFontResolver` from NuvioMobile-Enhanced with bundled Noto Sans CJK SC font. |
| **R8** | **GPL-3.0 License Compliance** | **LOW** | All reference repositories (Plozz, Labstream, nuvio-sync, Enhanced) are GPL-3.0. Proper copyright and attribution headers maintained. |

---

## 17. Phase 0 Closure Gate Status & Execution Record

- **Cloud Build Verification History:**
  - **Build Test IPA #2 (Failure Analysis):**
    - Triggered via `workflow_dispatch` on `develop/cn-emby` (`configuration = Debug`).
    - Runner `macos-26-arm64` scheduled; Xcode 26.6 toolchain verified; MPVKit submodule and NuvioEngine dependencies prepared.
    - Halted at: `:composeApp:generateRuntimeConfigs` because GitHub Forks do not inherit upstream repository secrets (`NUVIO_LOCAL_PROPERTIES_BASE64`), leaving `local.properties` absent.
    - Remediation: Updated `.github/workflows/ios-test-build.yml` to automatically generate an empty `local.properties` fallback when the secret is absent, leaving upstream secret behavior untouched and requiring zero application source modifications.
  - **Build Test IPA #3 (Verification Success):**
    - Triggered via `workflow_dispatch` on `develop/cn-emby` (`configuration = Debug`).
    - **Status:** Success
    - **Branch:** `develop/cn-emby`
    - **Configuration:** Debug
    - **Distribution:** full
    - **Signing:** unsigned
    - **Artifacts:** 1 (`nuvio-0.4.21-full-debug.ipa`, size: 64 MB)
- **Verified Platform & Toolchain Matrix:**
  - `macos-26-arm64` GitHub Hosted Runner: **VERIFIED**
  - Xcode 26.6 & iPhoneOS SDK: **VERIFIED**
  - MPVKit submodule checkout: **VERIFIED**
  - Nuvio Engine Apple XCFramework download & link: **VERIFIED**
  - KMP Kotlin/Native compilation: **VERIFIED**
  - Unsigned IPA packaging & Artifact upload: **VERIFIED**
- **Phase 0 Status:** **PASS (OFFICIALLY CLOSED)**
- **Next Phase Status:** **Phase 1 — Simplified Chinese UI Localization: READY**
