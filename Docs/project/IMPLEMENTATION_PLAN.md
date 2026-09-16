# Nuvio Mobile (CN + Emby) Implementation Plan

**Target Branch:** `develop/cn-emby`  
**Base Commit:** `9bc77bc48e0cc4958006657f129190169d831e33`  
**Architecture:** Kotlin Multiplatform + Compose Multiplatform + Swift/MPVKit  

---

## Phase 0: Baseline Verification & Pre-Implementation Audit (CLOSED - PASSED)
- **Files/Modules:**
  - `docs/project/PRE_IMPLEMENTATION_AUDIT.md`
  - `docs/project/IMPLEMENTATION_PLAN.md`
  - `docs/project/REFERENCE_IMPLEMENTATIONS.md`
  - `docs/project/UPSTREAM_STRATEGY.md`
- **Dependencies:** None
- **Acceptance Criteria:**
  - Git baseline verified against `upstream/cmp-rewrite`.
  - Architecture, player engine, storage, and build workflows completely audited without guessing.
  - Zero modifications to production source code.
- **Phase 0 Closure Gate (Cloud Baseline Build Status):**
  - **Build Test IPA #2 (Failure Analysis):** FAILED at `:composeApp:generateRuntimeConfigs` because GitHub forks do not inherit upstream `secrets.NUVIO_LOCAL_PROPERTIES_BASE64`, leaving `local.properties` absent for Gradle 9.4.1.
  - **Remediation Applied:** Adjusted `.github/workflows/ios-test-build.yml` to always execute `Configure runtime properties`, writing an empty `local.properties` when the secret is absent. Zero application source code modified.
  - **Build Test IPA #3 (Verification Success):**
    - Status: Success
    - Branch: `develop/cn-emby`
    - Configuration: Debug
    - Distribution: full
    - Signing: unsigned
    - Artifacts: 1 (`nuvio-0.4.21-full-debug.ipa`, 64 MB)
  - **Toolchain & Pipeline Validation:** `macos-26-arm64`, Xcode 26.6, iPhoneOS SDK, MPVKit submodule, Nuvio Engine XCFramework, and unsigned IPA packaging are all empirically **VERIFIED**.
  - **Closure Gate State:** **PASS (OFFICIALLY CLOSED)**. Pure upstream baseline + fork configuration + GitHub Actions environment successfully proven.
- **Rollback Point:** `git checkout develop/cn-emby && git reset --hard 9bc77bc4`

---

## Phase 1: Simplified Chinese UI Localization & Terminology Normalization (IMPLEMENTATION COMPLETED - PENDING INDEPENDENT REVIEW / CLOUD VERIFICATION)
- **Status:** Implementation & Independent Audit Remediation Complete on branch `feat/zh-cn-ui`. All 2,249 strings + 3 plurals normalized, traditional characters eliminated, and integrity validated. Awaiting cloud IPA build verification.
- **Files/Modules:**
  - `composeApp/src/commonMain/composeResources/values-zh/strings.xml` (Simplified Chinese fallback)
  - `composeApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` (Mainland Simplified Chinese)
  - `composeApp/src/commonMain/composeResources/values-zh-rTW/strings.xml` (Traditional Chinese)
  - `composeApp/src/androidMain/res/xml/locale_config.xml` (Registered `zh`, `zh-CN`, `zh-TW`)
  - `composeApp/src/commonMain/kotlin/com/nuvio/app/features/settings/AppLanguage.kt` (Registered `CHINESE_SIMPLIFIED` & `CHINESE_TRADITIONAL`)
- **Locale Strategy:**
  - `values-zh` = Simplified Chinese default fallback (prevents generic `zh` defaulting to Traditional).
  - `values-zh-rCN` = Mainland China Simplified Chinese.
  - `values-zh-rTW` = Traditional Chinese.
  - Settings UI displays clean options: `简体中文` (`zh-CN`) and `繁體中文` (`zh-TW`). No ambiguous generic "中文" item.
- **Mandatory Terminology Normalization:**
  - Simple character-level conversion is strictly prohibited.
  - Apply standard Mainland software terminology:
    - 自订 → 自定义
    - 快取 → 缓存
    - 汇入 → 导入
    - 汇出 → 导出
    - 专案 → 项目
    - 储存 → 保存
    - 网页面板 → Web 控制台
    - 附加元件 → 插件 / 扩展
    - 资料 → 数据 / 信息
    - 设定 → 设置
- **Resource Generation & Locale Tests:**
  - Rerun Compose Multiplatform resource generation task (`generateComposeResClass`).
  - Run locale and string format verification tests to fulfill upstream contributor requirements.
  - Add all 8 missing upstream keys from PR #1941.
- **Acceptance Criteria:**
  - 100% of keys in `values/strings.xml` translated and normalized.
  - Selecting "简体中文" applies translations across all UI screens instantly.
- **Cloud Build Check:** `Build Test IPA` (Debug) on GitHub Actions.
- **Rollback Point:** `git reset --hard phase0-baseline`

---

## Phase 1.5: Official Binary Feature Gap Audit (COMPLETE — Independent Audit Approved)
- **Status:** Complete — Informational baseline audit documented in `Docs/project/OFFICIAL_BINARY_FEATURE_GAP_AUDIT.md`.
- **Scope & Findings:**
  - Audited feature gaps between official release binary (as observed in user-confirmed iOS execution) and public `upstream/cmp-rewrite` 0.4.22.
  - Confirmed `OMDb Ratings` and `Live TV` are `PUBLIC_SOURCE_MISSING` in open-source `cmp-rewrite` and originated in `NuvioMobile-Enhanced`.
  - Confirmed iOS Native PiP is `PUBLIC_SOURCE_PARTIAL` (common abstraction present, iOS actual is a false stub).
  - Categorized for fork roadmap: OMDb Ratings scheduled for Phase 9 (Settings & UX); Live TV scheduled for post-MVP evaluation.
- **Rule:** Strictly observational and informational. No business logic, player, or feature policy code modified.

---

## Phase 2: Chinese Metadata Enrichment (TMDB zh-CN Integration)
- **Files/Modules:**
  - `composeApp/src/commonMain/kotlin/com/nuvio/app/features/tmdb/TmdbMetadataService.kt`
  - `composeApp/src/commonMain/kotlin/com/nuvio/app/features/tmdb/TmdbImages.kt`
  - `composeApp/src/commonMain/kotlin/com/nuvio/app/features/details/MetaDetailsRepository.kt`
- **Metadata Investigation & Delivery Policy:**
  - First audit and prioritize reusing Nuvio / Enhanced existing metadata backend pipelines and built-in keys.
  - Do NOT require regular users to provide personal TMDB API keys or proxy setups.
  - Fallback sequence:
    - Title & Overview: `zh-CN` -> `zh` -> `en` -> original metadata.
    - Posters: `zh-CN` -> `zh` -> neutral (`null`) -> original poster.
    - Season / Episode titles: `zh-CN` -> `en` -> original.
  - Optional custom TMDB API Key and proxy URL provided in Settings for advanced users only.
- **Acceptance Criteria:**
  - Centralized enrichment: UI composables perform zero direct metadata requests.
  - Movie and Series details load Chinese descriptions, localized titles, and Chinese posters where available.
- **Verification & Tests:**
  - Unit tests for image localization logic and fallback chains.
- **Cloud Build Check:** `Build Test IPA` (Debug) on GitHub Actions.
- **Rollback Point:** `git reset --hard phase1-complete`

---

## Phase 3: Emby Foundation (Config, Auth & Secure Storage)
- **Files/Modules:**
  - `composeApp/src/commonMain/kotlin/com/nuvio/app/core/storage/SecureStorage.kt` (`expect/actual`)
  - `composeApp/src/iosMain/kotlin/com/nuvio/app/core/storage/SecureStorage.ios.kt` (Security.framework Keychain)
  - `composeApp/src/androidMain/kotlin/com/nuvio/app/core/storage/SecureStorage.android.kt` (EncryptedSharedPreferences)
  - `composeApp/src/commonMain/kotlin/com/nuvio/app/features/emby/api/EmbyApiClient.kt`
  - `composeApp/src/commonMain/kotlin/com/nuvio/app/features/emby/api/EmbyAuthApi.kt`
  - `composeApp/src/commonMain/kotlin/com/nuvio/app/features/emby/storage/EmbyAccountStorage.kt`
- **Dependencies:** Phase 2
- **Acceptance Criteria:**
  - Emby authentication via `POST /Users/AuthenticateByName`.
  - `AccessToken` stored strictly in `SecureStorage` (Keychain / KeyStore).
  - Cleartext password discarded immediately after authentication.
  - Non-sensitive server configuration (URL, UserId, ServerId) stored in standard preferences.
- **Verification & Tests:**
  - Unit tests for authentication request/response parsing.
  - Keyring round-trip test.
- **Cloud Build Check:** `Build Test IPA` (Debug) on GitHub Actions.
- **Rollback Point:** `git reset --hard phase2-complete`

---

## Phase 4: Emby Item Matcher
- **Files/Modules:**
  - `composeApp/src/commonMain/kotlin/com/nuvio/app/features/emby/api/EmbyItemsApi.kt`
  - `composeApp/src/commonMain/kotlin/com/nuvio/app/features/emby/domain/EmbyMatcher.kt`
  - `composeApp/src/commonMain/kotlin/com/nuvio/app/features/emby/domain/EmbyItemModels.kt`
- **Dependencies:** Phase 3
- **Acceptance Criteria:**
  - Movies matched strictly by `AnyProviderIdEquals=imdb.{id}` or `tmdb.{id}`.
  - Series matched strictly by `AnyProviderIdEquals=tvdb.{id}` or `tmdb.{id}`.
  - TV episodes matched by Series ID -> `/Shows/{id}/Episodes` matching `ParentIndexNumber == season && IndexNumber == episode`.
  - Season 0 (Specials) correctly handled.
  - No false positives on similarly named titles.
- **Verification & Tests:**
  - Matcher test suite with diverse test cases (movies, regular series, specials, multi-season series).
- **Cloud Build Check:** `Build Test IPA` (Debug) on GitHub Actions.
- **Rollback Point:** `git reset --hard phase3-complete`

---

## Phase 5: Emby Stream Integration & Addon Fallback
- **Files/Modules:**
  - `composeApp/src/commonMain/kotlin/com/nuvio/app/features/emby/repository/EmbyStreamRepository.kt`
  - `composeApp/src/commonMain/kotlin/com/nuvio/app/features/streams/StreamsRepository.kt` (Integration hook)
  - `composeApp/src/commonMain/kotlin/com/nuvio/app/features/streams/StreamModels.kt`
- **Integration Principles:**
  - Preferred integration point is the **Streams repository layer** (`StreamsRepository.kt`).
  - Emby streams inject as `EMBY [Direct Play]` stream items.
  - If Emby item matches, Emby stream is prioritized for AutoPlay.
  - If Emby is offline, unreachable, or unmatched, the stream list transparently loads **existing Nuvio Addon streams** without error alerts.
  - The integration is completely provider-agnostic and coexists with any Stremio-compatible addon.
- **Verification & Tests:**
  - Stream ordering tests ensuring Emby priority when available.
  - Fallback simulation tests ensuring existing addon streams function unaffected when Emby is offline.
- **Cloud Build Check:** `Build Test IPA` (Debug) on GitHub Actions.
- **Rollback Point:** `git reset --hard phase4-complete`

---

## Phase 6: Emby PlaybackInfo, DeviceProfile & Capability Matrix
- **Files/Modules:**
  - `composeApp/src/commonMain/kotlin/com/nuvio/app/features/emby/api/EmbyPlaybackApi.kt`
  - `composeApp/src/commonMain/kotlin/com/nuvio/app/features/emby/domain/EmbyDeviceProfileBuilder.kt`
  - `composeApp/src/commonMain/kotlin/com/nuvio/app/features/emby/domain/EmbyPlaybackResolver.kt`
- **iOS MPV Capability Matrix & DeviceProfile Policy:**
  - Avoid unqualified claims of "universal Direct Play".
  - `EmbyDeviceProfileBuilder` generates `DeviceProfile` for `POST /Items/{id}/PlaybackInfo?UserId={userId}` based strictly on verified iOS capabilities.
  - Empirical verification matrix:
    - **Containers:** `mp4`, `mov`, `mkv`, `webm`, `mpegts`, `hls`
    - **Video Codecs:** `H.264`, `HEVC`, `VP9`, `AV1`
    - **Audio Codecs:** `AAC`, `AC3`, `EAC3`, `DTS`, `TrueHD`, `FLAC`
    - **Subtitle Codecs:** `SRT`, `ASS/SSA`, `WebVTT`, `PGS`
    - **Dynamic Ranges:** `SDR`, `HDR10`, `HLG`, `Dolby Vision` (Profiles 5 & 8.1)
  - Keep `DeviceProfile` conservative until specific formats are proven on device.
  - Direct Play URL generated: `/Videos/{id}/stream.{ext}?Static=true&mediaSourceId=...`.
- **Verification & Tests:**
  - PlaybackInfo query builder tests with both query and body parameters (Emby divergence safeguard).
- **Cloud Build Check:** `Build Test IPA` (Debug) on GitHub Actions.
- **Rollback Point:** `git reset --hard phase5-complete`

---

## Phase 7: Audio Tracks, Subtitles & CJK Font Integration
- **Files/Modules:**
  - `iosApp/iosApp/Player/MPVSubtitleFontResolver.swift` (Ported from Enhanced)
  - `iosApp/iosApp/SubtitleFonts/NotoSansCJKsc-Regular.otf` (Bundled font)
  - `iosApp/iosApp/Player/MPVPlayerBridge.swift`
  - `composeApp/src/commonMain/kotlin/com/nuvio/app/features/emby/domain/EmbyTrackMapper.kt`
- **Dependencies:** Phase 6
- **Acceptance Criteria:**
  - Emby audio streams correctly exposed in Nuvio Audio Track picker.
  - Emby embedded and external subtitles exposed in Subtitle picker.
  - CJK subtitles render with bundled Noto Sans CJK SC font to eliminate missing glyph boxes (□).
- **Verification & Tests:**
  - Track mapping serialization tests.
  - Subtitle font resolver property change handling tests.
- **Cloud Build Check:** `Build Test IPA` (Debug) on GitHub Actions.
- **Rollback Point:** `git reset --hard phase6-complete`

---

## Phase 8: Playback Session Reporting & Scrobble Sync
- **Files/Modules:**
  - `composeApp/src/commonMain/kotlin/com/nuvio/app/features/emby/api/EmbySessionApi.kt`
  - `composeApp/src/commonMain/kotlin/com/nuvio/app/features/emby/repository/EmbyScrobbleAdapter.kt`
  - `composeApp/src/commonMain/kotlin/com/nuvio/app/features/player/PlayerScreenRuntimePlaybackActions.kt`
- **Dependencies:** Phase 7
- **Acceptance Criteria:**
  - Emby session events reported: `Playing`, `Progress`, `Stopped`.
  - Progress updates sent every 10s and on pause/seek (`PositionTicks = ms * 10,000`).
  - Watched threshold (>= 80%) updates Emby status without causing loops or overwriting Trakt/Simkl scrobbles.
- **Verification & Tests:**
  - Session lifecycle event tests.
  - Reconciliation tests ensuring progress consistency.
- **Cloud Build Check:** `Build Test IPA` (Debug) on GitHub Actions.
- **Rollback Point:** `git reset --hard phase7-complete`

---

## Phase 9: Settings UI & User Experience Integration
- **Files/Modules:**
  - `composeApp/src/commonMain/kotlin/com/nuvio/app/features/settings/EmbySettingsPage.kt`
  - `composeApp/src/commonMain/kotlin/com/nuvio/app/features/settings/IntegrationsSettingsPage.kt`
  - `composeApp/src/commonMain/kotlin/com/nuvio/app/navigation/Routes.kt`
- **Dependencies:** Phase 8
- **Acceptance Criteria:**
  - "Emby Server" configuration page under Settings -> Integrations.
  - Server URL, Username, Password input with validation and test-connection feedback.
  - Emby server status and library summary display.
  - Prioritize Emby toggle.
- **Verification & Tests:**
  - UI interaction and navigation tests.
- **Cloud Build Check:** `Build Test IPA` (Debug) on GitHub Actions.
- **Rollback Point:** `git reset --hard phase8-complete`

---

## Phase 10: Regression Testing & Cloud IPA Release Build
- **Files/Modules:** Entire project
- **Dependencies:** Phases 1–9
- **Acceptance Criteria:**
  - Full test suite passes: `./gradlew test`.
  - Zero regression in existing Nuvio features (Trakt, Simkl, Downloads, Addons).
  - Trigger `Build Test IPA` with `configuration = Release`.
  - Unsigned Release IPA produced, verified for arm64 architecture, and downloadable via GitHub Actions Artifacts.
  - User sideloading verified via TrollStore / AltStore / Sideloadly.
- **Rollback Point:** Revert to specific failing phase.

---

## Phase 11: Branding, Versioning & Release (FINAL PHASE ONLY)
- **Rule:** Executed strictly after all functional phases pass cloud validation.
- **Files/Modules:**
  - `iosApp/Configuration/Config.xcconfig`
  - `iosApp/Configuration/Version.xcconfig`
  - `store.json`
- **Acceptance Criteria:**
  - Brand adjustments and marketing versions finalized if requested.
  - Release notes published.
