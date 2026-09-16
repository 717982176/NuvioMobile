# Official Binary vs Public Source Feature Gap Audit

**Document Version:** 1.1.0 (Independent Audit Remediation)  
**Date:** 2026-09-16  
**Audit Target:** Nuvio Mobile 0.4.22 Baseline  
**Upstream Baseline SHA:** `95347544858e31d8cf36569a3b154961276ed46e` (`upstream/cmp-rewrite`)  
**Fork Working Branch:** `audit/official-feature-gap`  
**Base Commit SHA:** `7f8cf1fdd369acde29be25f6c0e021304b828f25` (`develop/cn-emby`)  
**Status:** Phase 1.5: COMPLETE — Independent Audit Approved  

---

## 1. Executive Summary

This audit establishes a definitive, evidence-based feature baseline comparing the **official binary release** (as observed in user-confirmed official iOS device execution), the **public open-source upstream repository** (`NuvioMedia/NuvioMobile` branch `cmp-rewrite` at version 0.4.22), and the reference fork **`NuvioMobile-Enhanced`** (`luqmanfadlli/NuvioMobile-Enhanced`).

### Key Findings
1. **Confirmed Binary / Public Source Gaps:**
   - On the official iOS release binary (confirmed via user device screenshot of Settings > Integrations), five items are present: `Connected Services`, `TMDB Enrichment`, `MDBList Ratings`, `OMDb Ratings`, and `Live TV`.
   - In public upstream `cmp-rewrite` (`0.4.22`), only three items are implemented: `TMDB Enrichment`, `MDBList Ratings`, and `Connected Services` (Debrid).
   - **`OMDb Ratings`** and **`Live TV`** are classified as **`PUBLIC_SOURCE_MISSING`** in the audited open-source `cmp-rewrite` 0.4.22 source tree. No dedicated implementation exists in public upstream.
   - Reference implementations for both features exist in `NuvioMobile-Enhanced` (OMDb in commit `86dd225c` by Alberto Vinaroz; Live TV in commits `931c2ac6` and `ed2693fd`).
2. **0.4.21 → 0.4.22 Upstream Deltas (19 Commits):**
   - 0.4.22 added: Personal TMDB API Key override, Stable/Beta update channels, Rotten Tomatoes status icons, addon episode runtime parsing, and addon IMDb ID fallback for non-IMDb content. These are **publicly implemented in source** and already verified.
3. **Architecture & Scope Preservation:**
   - This audit is strictly observational and informational. Zero business, player, or integration code is modified. OMDb Ratings and Live TV are categorized as `REIMPLEMENT_LATER` to ensure that our core roadmap (Phase 2 Chinese Metadata & Phase 3–8 Emby integration) remains focused and uncompromised.

---

## 2. Audit Scope

The scope of this audit encompasses:
1. **Public Source Verification:** Full recursive search across routes, composables, repositories, storage models, and resource dictionaries in `composeApp/` and `iosApp/`.
2. **Git History & Branch Tracking:** All remote branches, tags, and commit trees in `upstream` (`NuvioMedia/NuvioMobile`) and `enhanced` (`luqmanfadlli/NuvioMobile-Enhanced`).
3. **Physical Binary Evidence:** User-confirmed iOS device screenshot of Settings > Integrations.
4. **Feature Categories Examined:** Integrations, Playback, Appearance, Metadata/Ratings, Accounts/Tracking, and Build Feature Policies.

---

## 3. Compared Versions

| Stream | Repository / Origin | Branch / Tag | Exact Commit SHA |
|---|---|---|---|
| **Upstream Baseline** | `https://github.com/NuvioMedia/NuvioMobile` | `cmp-rewrite` / `0.4.22` | `95347544858e31d8cf36569a3b154961276ed46e` |
| **Fork Baseline** | `https://github.com/717982176/NuvioMobile` | `develop/cn-emby` | `7f8cf1fdd369acde29be25f6c0e021304b828f25` |
| **Reference Fork** | `https://github.com/luqmanfadlli/NuvioMobile-Enhanced` | `enhanced` | Latest HEAD (`b842c1d8`) |
| **Official Device Binary** | Official Release Distribution (`com.nuvio.media`) | 0.4.22 | Real iOS device execution |

---

## 4. Evidence Rules & Classification System

Every audited feature is strictly assigned exactly **one primary classification**:

- **`PUBLIC_SOURCE_PRESENT_VISIBLE`:** The implementation is present in public upstream source and actively exposed to users in the UI.
- **`PUBLIC_SOURCE_PRESENT_HIDDEN`:** The implementation exists in public source, but its UI entry point or route is gated behind feature flags or hardcoded disabled states.
- **`PUBLIC_SOURCE_PARTIAL`:** Shared abstractions or partial models exist, but no complete platform implementation is assembled in public source.
- **`PUBLIC_SOURCE_MISSING`:** No dedicated implementation was found in the audited public `cmp-rewrite` 0.4.22 source tree.
- **`CONFIG_REQUIRED`:** The code exists in public source, but requires API keys, OAuth credentials, or `local.properties` values to function.
- **`PLATFORM_SPECIFIC`:** Implementation is constrained to specific target platforms (iOS, Android, or Desktop).
- **`BUILD_FLAVOR_SPECIFIC`:** Gated by build variants (`iosFull` vs `iosAppStore`, `androidFull` vs `androidPlaystore`).
- **`ENHANCED_ONLY`:** Confirmed exclusively in `NuvioMobile-Enhanced`; absent from both public source and official binary evidence.
- **`UNKNOWN`:** Insufficient evidence.

---

## 5. Confirmed Official Binary / Public Source Gaps

The primary functional delta confirmed between the official iOS release binary and public upstream source occurs within the **Settings > Integrations** screen:

| Feature | Primary Classification | Official Binary Evidence | Public Source Evidence | Enhanced Evidence | Platform | Risk | Recommended Action |
|---|---|---|---|---|---|---|---|
| **OMDb Ratings** | **PUBLIC_SOURCE_MISSING** | **PRESENT** (User-confirmed official iOS binary screenshot) | No dedicated implementation was found in the audited public cmp-rewrite 0.4.22 source tree. Missing from `IntegrationsSettingsPage.kt`, `Routes.kt`, and `strings.xml`. | **PRESENT** (Implemented in commit `86dd225c` by Alberto Vinaroz: `OmdbSettingsPage.kt`, `OmdbEpisodeRatingsService.kt`). | All | LOW | `REIMPLEMENT_LATER` (Phase 9) |
| **Live TV / IPTV** | **PUBLIC_SOURCE_MISSING** | **PRESENT** (User-confirmed official iOS binary screenshot) | No dedicated implementation was found in the audited public cmp-rewrite 0.4.22 source tree. (Ordinary `.m3u8` handling in public source belongs to standard HLS stream playback, which is not equivalent to Live TV / M3U playlist / IPTV portal functionality.) | **PRESENT** (Implemented in commits `931c2ac6` and `ed2693fd`: `LiveTvScreen.kt`, `LiveTvRepository.kt`, `LiveTvSettingsPage.kt`, `LiveTvPortalProviders.kt`). | All | MEDIUM | `REIMPLEMENT_LATER` (Post-MVP) |

### Detailed Analysis of the Gaps
1. **OMDb Ratings:**
   - In `NuvioMobile-Enhanced`, OMDb is utilized to provide personal API key support for fetching IMDb per-episode ratings (circumventing rate-limits on public scrapers).
   - In official public source `cmp-rewrite`, episode ratings are handled through `ImdbEpisodeRatingsRepository.kt` via bundled tapframe/IMDb proxy endpoints, without an OMDb settings page.
2. **Live TV:**
   - In `NuvioMobile-Enhanced`, Live TV supports M3U playlists, Xtream Codes, and Stalker portals.
   - In official public source `cmp-rewrite`, Nuvio is architected strictly around Stremio-compatible catalog/stream addons, with zero native IPTV portal plumbing.
3. **Provenance & Identifier Guidance:**
   - *Informational Note:* `NuvioMobile-Enhanced` retains the same product identifiers (bundle identifier `com.nuvio.media` and app name `Nuvio` in `store.json`) as official upstream.
   - *Provenance Boundary:* Identical bundle identifiers or application names do **not** constitute valid proof of code origin, release distribution provenance, or official feature authorization. Official binary feature presence is established strictly through user-provided device execution evidence and official release materials.

---

## 6. Public Source Partial, Hidden & Config-Gated Features

| Feature | Primary Classification | Official Binary Evidence | Public Source Evidence | Enhanced Evidence | Platform | Risk | Recommended Action |
|---|---|---|---|---|---|---|---|
| **iOS Native Picture-in-Picture** | **PUBLIC_SOURCE_PARTIAL** | **UNKNOWN** | `commonMain` defines `expect fun rememberIsInPictureInPicture(): Boolean` (consumed in UI). In `iosMain`, `PlayerPlatformEffects.ios.kt` provides `actual fun rememberIsInPictureInPicture(): Boolean = false` stub. Native MPV layer lacks AVSampleBuffer frame-grabbing. | **PRESENT** (Commits `ce510201`, `e7f49411` via `AVSampleBufferDisplayLayer` and frame-capture bridge). | iOS | MEDIUM | `REFERENCE_ENHANCED` (Post-MVP) |
| **Nuvio Account Sync (Supabase)** | **CONFIG_REQUIRED** | **PRESENT** | Code fully assembled in `SupabaseProvider.kt`; requires `NUVIO_SUPABASE_URL` and `NUVIO_SUPABASE_ANON_KEY` in `local.properties`. | Equivalent | All | LOW | `CONFIGURE_ONLY` |
| **Trakt Scrobble & Watchlist** | **CONFIG_REQUIRED** | **PRESENT** | Code fully assembled in `TraktAuthRepository.kt`; requires `TRAKT_CLIENT_ID` and `TRAKT_CLIENT_SECRET`. | Equivalent | All | LOW | `KEEP` |
| **Simkl Tracking** | **CONFIG_REQUIRED** | **PRESENT** | Code fully assembled in `SimklAuthRepository.kt`; requires `SIMKL_CLIENT_ID`. | Equivalent | All | LOW | `KEEP` |
| **MDBList Ratings** | **CONFIG_REQUIRED** | **PRESENT** | Code fully assembled in `MdbListMetadataService.kt`; requires user to supply personal MDBList API key in settings. | Equivalent | All | LOW | `KEEP` |
| **IMDb Episode Ratings Proxy** | **CONFIG_REQUIRED** | **PRESENT** | Code fully assembled in `ImdbEpisodeRatingsRepository.kt`; relies on `IMDB_RATINGS_API_BASE_URL` or `IMDB_TAPFRAME_API_BASE_URL`. | Replaced by OMDb in Enhanced | All | LOW | `KEEP` |

---

## 7. Platform & Build-Flavor Specific Capabilities

| Feature | iOS Full | iOS AppStore | Android Full | Android PlayStore | Classification |
|---|---|---|---|---|---|
| **QuickJS Plugins Runtime** | Enabled (`pluginsEnabled=true`) | Disabled (`pluginsEnabled=false`) | Enabled | Disabled | **BUILD_FLAVOR_SPECIFIC** |
| **P2P Torrent Streaming** | Enabled (`p2pEnabled=true`) | Disabled (`p2pEnabled=false`) | Enabled | Disabled | **BUILD_FLAVOR_SPECIFIC** |
| **In-App Updater Channels** | Disabled (`inAppUpdaterEnabled=false`) | Disabled | Enabled | Disabled | **PLATFORM_SPECIFIC** / **BUILD_FLAVOR_SPECIFIC** |
| **Liquid Glass Native Tab Bar** | Enabled (iOS 26+) | Enabled (iOS 26+) | N/A | N/A | **PLATFORM_SPECIFIC** |
| **Native Player Engine** | `libmpv` (MPVKit + Metal) | `libmpv` (MPVKit + Metal) | Media3 / ExoPlayer + mpv | Media3 / ExoPlayer | **PLATFORM_SPECIFIC** |
| **Android PiP** | N/A | N/A | Enabled (`enterPictureInPictureMode`) | Enabled | **PLATFORM_SPECIFIC** |
| **Background Download Service** | Disabled on iOS | Disabled on iOS | Enabled (Foreground Service) | Disabled (Play Store policy) | **BUILD_FLAVOR_SPECIFIC** |

---

## 8. Upstream 0.4.21 → 0.4.22 Delta Audit (19 Commits)

The delta between `0.4.21` (`9bc77bc4`) and `0.4.22` (`95347544`) comprises 19 commits categorized below:

```
0.4.21 (9bc77bc4) ───[ 19 Commits ]───> 0.4.22 (95347544)
```

| Commit SHA | Author | Classification | Subject & Description |
|---|---|---|---|
| `12111fd8` | tapframe | **FEATURE** | `feat(updater): add stable and beta update channels` — Introduced stable and beta update stream selection. |
| `df589078` | tapframe | **FEATURE** / **METADATA** | `feat(tmdb): add personal API key override with credential sync` — Added personal TMDB API key setting with multi-device sync. |
| `48bf5ed3` | tapframe | **FEATURE** / **METADATA** | `feat(details): parse addon episode runtimes` — Enables episode card duration parsing from addon video metadata. |
| `90054b7b` | skoruppa | **METADATA** / **FIX** | `fix: use addon imdb_id as fallback for non-IMDB content enrichment` (PR #1966) — Improves TMDB metadata resolution for non-IMDb content. |
| `177f4b5c` | tapframe | **FEATURE** / **UI** | `feat(ratings): add rotten tomatoes status icons` — Added Rotten Tomatoes status icons and fresh/rotten indicators. |
| `3312374e` | tapframe | **FIX** / **UI** | `fix(settings): keep page content visible during navigation` — Prevented settings page content flashing during transition. |
| `8ac70e59` | skoruppa | **FIX** | `Make anime id preference only for content with anime ids` (PR #1955) — Prevented non-anime content from triggering TVDB anime routing. |
| `a50f29b5` | blueocean2308 | **I18N** | `Add files via upload` (PR #1959) — Updated Vietnamese string resources. |
| `dd6a27c1` | blueocean2308 | **I18N** | `update latest Vietnamese strings` |
| `75c415e7` | blueocean2308 | **I18N** | `update latest Vietnamese strings` |
| `278b48aa` | blueocean2308 | **I18N** | `update latest Vietnamese strings` |
| `814cf2ae` | skoruppa | **BUILD** | `Merge branch 'NuvioMedia:cmp-rewrite' into cmp-rewrite` |
| `9c075db6` | tapframe | **BUILD** | `Merge branch 'cmp-rewrite' of https://github.com/NuvioMedia/NuvioMobile into cmp-rewrite` |
| `09c3e9ec` | tapframe | **BUILD** | `Merge branch 'cmp-rewrite' of https://github.com/NuvioMedia/NuvioMobile into cmp-rewrite` |
| `3a2c8813` | Nayif | **BUILD** | `Merge pull request #1966 from skoruppa/fix/imdb-id-fallback` |
| `e04788ba` | Nayif | **BUILD** | `Merge pull request #1955 from skoruppa/cmp-rewrite` |
| `127ef2a3` | Nayif | **BUILD** | `Merge pull request #1959 from blueocean2308/cmp-rewrite` |
| `db34bad9` | tapframe | **RELEASE_METADATA** | `bump version` (Updated version metadata to 0.4.22, build 127). |
| `95347544` | github-actions | **RELEASE_METADATA** | `chore(store): publish 0.4.22` (Updated `store.json` download URLs and hashes). |

---

## 9. NuvioMobile-Enhanced Feature Matrix & Disposition

| Feature | Primary Classification | Commit in Enhanced | Upstream Status | Official Binary Evidence | Disposition for Fork |
|---|---|---|---|---|---|
| **CJK Subtitle Font Resolver** | **ENHANCED_ONLY** | `bcedb582` | Absent in upstream | UNKNOWN | **REUSE / CHERRY-PICK LATER** (Phase 7). Fixes iOS FreeType sandboxing font loading bug for Chinese subtitles. |
| **Audio Language Preferences Bridge** | **PUBLIC_SOURCE_PRESENT_VISIBLE** | `012d663a` | Merged in upstream (`4f79bfe0`) | PRESENT | **IGNORE** (Already in upstream codebase). |
| **OMDb Ratings Integration** | **PUBLIC_SOURCE_MISSING** | `86dd225c` | Absent in upstream | PRESENT | **REIMPLEMENT_LATER** (Phase 9). Optional personal API key for episode ratings. |
| **Live TV / IPTV Portals** | **PUBLIC_SOURCE_MISSING** | `931c2ac6`, `ed2693fd` | Absent in upstream | PRESENT | **REIMPLEMENT_LATER** (Post-MVP). Significant scope; defer until Emby core is stable. |
| **iOS Picture-in-Picture** | **PUBLIC_SOURCE_PARTIAL** | `ce510201`, `e7f49411` | Partial stub in upstream | UNKNOWN | **REFERENCE_ENHANCED** (Post-MVP). Frame-grabber approach has memory/thermal overhead. |
| **Playback Info Modal ("Stats for Nerds")** | **ENHANCED_ONLY** | `ce510201` | Absent in upstream | UNKNOWN | **REFERENCE_ENHANCED** (Phase 9 / Debugging). High diagnostic utility. |
| **Manual Stream Quality Clamping** | **ENHANCED_ONLY** | `ce510201` | Absent in upstream | UNKNOWN | **REFERENCE_ENHANCED** (Phase 6 / 9). Bandwidth limiter. |
| **Volume Boost (>100%)** | **ENHANCED_ONLY** | `feat-volume-boost` | Absent in upstream | UNKNOWN | **IGNORE** (Software audio digital pre-amp risk). |
| **Swipe Calendar in Library** | **ENHANCED_ONLY** | `ac2921b7`, `1c1650db` | Absent in upstream | UNKNOWN | **REFERENCE_ENHANCED** (Post-MVP). Nice-to-have UI enhancement. |

---

## 10. Metric Summary

| Metric Category | Count | Detailed Breakdown |
|---|---|---|
| **Confirmed Binary / Public-Source Gaps** | **2** | `OMDb Ratings`, `Live TV` (Confirmed present on official iOS release binary, missing from public source) |
| **PUBLIC_SOURCE_PRESENT_HIDDEN** | **0** | No feature in public source is fully assembled and hidden behind non-build-flavor flags |
| **PUBLIC_SOURCE_PARTIAL** | **1** | `iOS Native PiP` (Common abstraction and UI logic present, but iOS actual is a hardcoded false stub) |
| **PUBLIC_SOURCE_MISSING** | **2** | `OMDb Ratings`, `Live TV` |
| **CONFIG_REQUIRED** | **5** | `Nuvio Account/Supabase`, `Trakt`, `Simkl`, `MDBList`, `IMDb Episode Ratings Proxy` |
| **PLATFORM_SPECIFIC** | **4** | `Overall PiP (Android native, iOS partial)`, `iOS MPV Metal Layer`, `iOS Liquid Glass Tab Bar`, `Android App Updater` |
| **BUILD_FLAVOR_SPECIFIC** | **3** | `QuickJS Plugins Runtime`, `P2P Torrent Streaming`, `Background Download Service` |
| **ENHANCED_ONLY** | **5** | `CJK Subtitle Font Resolver`, `Playback Info Modal`, `Manual Stream Quality Clamping`, `Volume Boost (>100%)`, `Library Swipe Calendar` |
| **UNKNOWN** | **0** | All audited features have concrete evidence anchors |

---

## 11. Risk Assessment

| Risk Item | Severity | Likelihood | Impact on develop/cn-emby | Mitigation |
|---|---|---|---|---|
| **User Expectation Mismatch on OMDb & Live TV** | LOW | HIGH | Users assuming all binary features exist in open-source `cmp-rewrite` may report missing UI. | Document clearly in release notes that OMDb and Live TV are scheduled for Phase 9 / post-MVP. |
| **Upstream Code Divergence from Enhanced Features** | MEDIUM | MEDIUM | Merging complex Enhanced features (like Live TV) prematurely will cause massive upstream merge conflicts. | Keep `develop/cn-emby` strictly aligned with upstream `cmp-rewrite` architecture. Implement Emby cleanly in isolated packages. |
| **iOS MPV Font Loading Bug** | HIGH | HIGH | Without `MPVSubtitleFontResolver`, Chinese subtitles render as empty boxes on iOS. | Already scheduled for Phase 7 cherry-pick. |

---

## 12. Recommendations & Phase Roadmap Integration

1. **Maintain Current Upstream Alignment:**
   Continue using `upstream/cmp-rewrite` 0.4.22 as the authoritative baseline. Do NOT attempt to pull large foreign modules (Live TV) during early phases.
2. **Roadmap Execution:**
   - Proceed to **Phase 2: Chinese Metadata Enrichment (TMDB zh-CN)** as scheduled.
   - Schedule **OMDb Ratings** under Phase 9 (Settings & UX Integration) if user demand warrants it.
   - Evaluate **Live TV** strictly post-Phase 10, after Emby media playback and session synchronization are fully verified.
