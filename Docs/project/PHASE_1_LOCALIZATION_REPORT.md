# Phase 1: Simplified Chinese UI Localization Report

**Date:** 2026-09-16  
**Target Branch:** `feat/zh-cn-ui`  
**Base Commit SHA:** `bb4ea38a542d1cdc45710bfc36cf48df330e7989` (`develop/cn-emby`)  
**Draft Reference:** Upstream PR #1941 (`7c4be28936017ee0d6ff77e773058ced576bd1f9`)  
**Phase Status:** Implementation Complete — Ready for Cloud IPA Verification  

---

## 1. Summary of Changes

Phase 1 completes the comprehensive Simplified Chinese (zh-CN) localization of the Nuvio Mobile UI while preserving and completing Traditional Chinese (zh-TW) support.

### Files Modified & Added
1. `composeApp/src/commonMain/composeResources/values-zh/strings.xml` (Added: 2,249 strings + 3 plurals)
   - Configured as **Simplified Chinese default fallback** (prevents unregioned `zh` from displaying Traditional Chinese).
2. `composeApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` (Added: 2,249 strings + 3 plurals)
   - Complete Mainland China Simplified Chinese resource dictionary with full terminology normalization.
3. `composeApp/src/commonMain/composeResources/values-zh-rTW/strings.xml` (Added: 2,249 strings + 3 plurals)
   - Complete Traditional Chinese resource dictionary with the 8 newly added upstream keys.
4. `composeApp/src/commonMain/kotlin/com/nuvio/app/features/settings/AppLanguage.kt` (Modified)
   - Added `CHINESE_SIMPLIFIED("zh-CN", Res.string.lang_chinese_simplified)`.
   - Added `CHINESE_TRADITIONAL("zh-TW", Res.string.lang_chinese_traditional)`.
   - Updated `fromCode` resolver to cleanly map `zh-CN`, `zh-Hans`, `zh` to `CHINESE_SIMPLIFIED`, and `zh-TW`, `zh-Hant`, `zh-HK` to `CHINESE_TRADITIONAL`.
   - UI displays only two clean options: **简体中文** and **繁體中文** (no ambiguous generic "中文" menu entry).
5. `composeApp/src/androidMain/res/xml/locale_config.xml` (Modified)
   - Registered `zh`, `zh-CN`, and `zh-TW` without modifying existing locales.
6. `Docs/project/IMPLEMENTATION_PLAN.md` (Updated Phase 1 status).
7. `Docs/project/PHASE_1_LOCALIZATION_REPORT.md` (This document).

---

## 2. Resource Key Coverage & Consistency Analysis

| Resource Target | Total Strings | Total Plurals | Missing Keys | Extra Keys | Format Placeholder Mismatches |
|---|---|---|---|---|---|
| **Default (`values/strings.xml`)** | 2,249 | 3 | 0 (Baseline) | 0 | 0 |
| **`values-zh`** | 2,249 | 3 | 0 | 0 | 0 |
| **`values-zh-rCN`** | 2,249 | 3 | 0 | 0 | 0 |
| **`values-zh-rTW`** | 2,249 | 3 | 0 | 0 | 0 |

### Missing Upstream Keys Added & Translated
1. `player_seek_position`:
   - zh / zh-CN: `播放进度`
   - zh-TW: `播放進度`
2. `profile_already_active`:
   - zh / zh-CN: `当前已在“%1$s”个人资料`
   - zh-TW: `目前已在「%1$s」個人資料`
3. `settings_stream_background_title`:
   - zh / zh-CN: `背景`
   - zh-TW: `背景`
4. `settings_stream_background_description`:
   - zh / zh-CN: `选择播放源页面的背景样式。`
   - zh-TW: `選擇串流來源頁面的背景樣式。`
5. `settings_tracking_anime_id_tvdb`:
   - zh / zh-CN: `优先使用 TVDB`
   - zh-TW: `優先使用 TVDB`
6. `settings_tracking_anime_id_tvdb_description`:
   - zh / zh-CN: `使用 TVDB ID 进行剧季分组。对于在 IMDb 上按季拆分条目的动漫，比 IMDb 更稳定。`
   - zh-TW: `使用 TVDB ID 進行季數分組。對於在 IMDb 上按季拆分條目的動畫，比 IMDb 更穩定。`
7. `trailer_enter_fullscreen`:
   - zh / zh-CN: `进入全屏`
   - zh-TW: `進入全螢幕`
8. `trailer_exit_fullscreen`:
   - zh / zh-CN: `退出全屏`
   - zh-TW: `結束全螢幕`

---

## 3. Mainland China Terminology Normalization

PR #1941 draft contained numerous Taiwanese and Hong Kong software terms resulting from automatic character conversion. The following professional normalization rules were systematically applied across `values-zh` and `values-zh-rCN`:

| PR #1941 Wording (Taiwanese / Draft) | Normalized Mainland Chinese Term | Scope / Context |
|---|---|---|
| 自订 / 自訂 | **自定义** | User-configured settings, themes, and servers |
| 快取 | **缓存** | Image and watch progress caches |
| 汇入 / 匯入 | **导入** | Collections, settings, URLs |
| 汇出 / 匯出 | **导出** | Collections, data backups |
| 专案 / 專案 | **项目** | Open-source project attribution |
| 储存 / 儲存 | **保存** | File saving, preference persistence |
| 网页面板 | **Web 控制台** | Nuvio web dashboard references |
| 伺服器 | **服务器** | Self-hosted & official backend endpoints |
| 附加元件 | **插件** | Stremio-compatible addons across UI |
| 资讯清单 | **插件清单** | Manifest URLs and metadata files |
| 中继资料 / 中繼資料 | **元数据** | Film/TV media attributes |
| 搜寻 | **搜索** | Search bar, search results, tabs |
| 音讯 / 音讯轨 | **音频 / 音轨** | Audio tracks and playback selectors |
| 视讯 / 视讯轨 | **视频 / 视频轨** | Video tracks and playback streams |
| 播放清单 | **播放列表** | Video and episode sequences |
| 时间戳记 | **时间戳** | Intro/outro skip points |
| 影集 | **剧集** | TV show series categorizations |
| 装置 / 裝置 | **设备** | Client devices, login sessions |
| 登入 / 登出 | **登录 / 退出登录** | User accounts and tracking services |
| 档案 / 檔案 | **文件** | Downloaded media files, logs |
| 資料庫 | **数据库** | Media and caching database stores |
| 重新整理 | **刷新** | Refreshing catalogs, lists, and metadata |
| 已停用 | **已禁用** | Disabled settings or features |
| 連線 / 连线 | **连接** | Network server connectivity |
| 網路 / 网路 | **网络** | Network connectivity, Wi-Fi |
| 天後 / 稍後 | **天后 / 稍后** | Correct simplified character usage |

---

## 4. Hardcoded User-Facing String Audit

1. **Audit Methodology:**
   Scanned all Kotlin files in `composeApp/src/commonMain/kotlin/com/nuvio/app` for user-facing English string literals.
2. **Findings:**
   - Nuvio Mobile's codebase systematically uses `stringResource(Res.string.*)` for nearly 100% of user-facing UI labels, dialogs, buttons, and navigation chrome.
   - String literals identified in `PlayerModels.kt` (such as `IosVideoOutputPreset.NativeEdr`) are already wrapped with composable extension functions (`localizedLabel()`, `localizedDescription()`) pointing to `Res.string.*`.
   - String literals identified in `CommentDetailSheet.kt` and `P2pPlayerOverlays.kt` are formatted numerical speed/count interpolations (e.g. `↓ 1.2 MB/s`, `1 / 5`).
   - Animation transition labels (e.g. `label = "detail_dominant_backdrop_color"`) are internal Compose transition keys and correctly left untouched.
3. **Intentionally Untranslated Categories (Out of Phase 1 Scope):**
   - **Category B (Remote Media Metadata):** Movie/Series titles, overviews, season titles, episode summaries. (Strictly reserved for Phase 2 TMDB Enrichment).
   - **Category C (Raw Addon Stream Identifiers):** Torrent titles, file release names, provider tags ("RD+", "Torrentio"). (Preserved as raw stream data).
   - **Category D (Technical Protocol Literals):** Codec names ("HEVC", "AV1", "DTS", "TrueHD"), MIME types, and route keys.
   - **Category E (Branded Proper Names):** Nuvio, Trakt, Simkl, TMDB, IMDb, Real-Debrid, Premiumize, TorBox, WebDAV.

---

## 5. Verification & Test Execution Record

1. **Automated XML Integrity & Placeholder Verification:**
   - **Status:** **PASS (100% Match)**
   - Verified that all 2,249 string tags and 3 plurals parse cleanly in `values-zh`, `values-zh-rCN`, and `values-zh-rTW`.
   - Verified that positional placeholders (`%1$s`, `%2$d`, `%d`, `%s`) in all Chinese resources strictly match the default English templates.
   - Confirmed zero missing keys, zero extra keys, and zero formatting mismatches.
2. **Local Gradle Resource Generation & Unit Tests:**
   - **Status:** **`SKIPPED_LOCAL_ENVIRONMENT`**
   - **Reason:** Local Windows/WSL environment lacks a local JDK 17 installation.
   - **Mitigation:** In accordance with project audit guidelines, no local compilation was faked. The definitive build and packaging gate is delegated to GitHub Actions.
3. **Cloud IPA Verification Gate:**
   - To be executed on `feat/zh-cn-ui` via GitHub Actions (`Build Test IPA`, `configuration = Debug`).

---

## 6. Known Issues & Rollback Anchor

- **Known Issues:** None identified.
- **Rollback Anchor:** `git reset --hard bb4ea38a542d1cdc45710bfc36cf48df330e7989`
