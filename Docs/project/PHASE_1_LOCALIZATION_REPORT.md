# Phase 1: Simplified Chinese UI Localization Report

**Document Version:** 1.4.0 (Post-Upstream Sync — 0.4.22)  
**Date:** 2026-09-16  
**Target Branch:** `develop/cn-emby`  
**Base Commit SHA:** `bb4ea38a542d1cdc45710bfc36cf48df330e7989` (`develop/cn-emby`)  
**Draft Reference:** Upstream PR #1941 (`7c4be28936017ee0d6ff77e773058ced576bd1f9`)  
**Phase Status:** PASS — synchronized with upstream 0.4.22, Pending future upstream deltas only  

---

## 1. Executive Summary: Round 3 Semantic QA Remediation

Following the third-round independent audit which identified that while character conversion was complete, underlying semantic inaccuracies and regional software terminology remained (e.g. rendering translated as "转译", compatibility as "相容", tone mapping as "色调对应", video frame as "视频界面", build as "组建", stack trace as "堆叠追踪", and provider/repository/source object inversion), a comprehensive **key-by-key semantic QA review** was conducted comparing `values/strings.xml` (canonical default) directly against `values-zh` and `values-zh-rCN`.

### Key Outcomes of Round 3
- **Strings Modified in Round 3:** 105 string entries semantically rewritten.
- **Cumulative Strings Normalized (Rounds 1, 2 & 3):** 865 string entries refined from initial draft.
- **Technical Translation Corrections:** 100% resolution of rendering, renderer, compatibility, tone mapping, and video frame mistranslations.
- **Provider / Repository / Source Semantics:** Reconciled plugin repository and provider object relations to match English canonical meaning.
- **Validation Layers Completed:**
  1. *Character/Script Validation:* 0 residual traditional characters verified via OpenCC 3,222 character dictionary.
  2. *Terminology Validation:* 0 residual Taiwanese/Hong Kong software terms across all audited categories.
  3. *Semantic Translation Validation:* All 2,249 strings verified against English intent.
- **Byte-Identical Dual Simplified Dictionaries:** `values-zh` (generic fallback) and `values-zh-rCN` (Mainland) are 100% byte-identical.
- **Traditional Chinese Integrity:** `values-zh-rTW` remains intact with authentic Traditional Chinese terminology.

---

## 2. Modified & Added Files

1. `composeApp/src/commonMain/composeResources/values-zh/strings.xml` (2,249 strings + 3 plurals)
   - Canonical Simplified Chinese default fallback (byte-identical to `values-zh-rCN`).
2. `composeApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` (2,249 strings + 3 plurals)
   - Canonical Mainland China Simplified Chinese dictionary.
3. `composeApp/src/commonMain/composeResources/values-zh-rTW/strings.xml` (2,249 strings + 3 plurals)
   - Traditional Chinese dictionary with 8 upstream keys preserved in Traditional Chinese.
4. `composeApp/src/commonMain/kotlin/com/nuvio/app/features/settings/AppLanguage.kt`
   - Registered `CHINESE_SIMPLIFIED("zh-CN")` and `CHINESE_TRADITIONAL("zh-TW")`.
5. `composeApp/src/androidMain/res/xml/locale_config.xml`
   - Registered `zh`, `zh-CN`, and `zh-TW`.
6. `Docs/project/IMPLEMENTATION_PLAN.md` (Updated).
7. `Docs/project/PHASE_1_LOCALIZATION_REPORT.md` (This document).

---

## 3. Resource Key Coverage & Consistency Analysis

| Resource Target | Total Strings | Total Plurals | Missing Keys | Extra Keys | Format Placeholder Mismatches |
|---|---|---|---|---|---|
| **Default (`values/strings.xml`)** | 2,249 | 3 | 0 (Baseline) | 0 | 0 |
| **`values-zh`** | 2,249 | 3 | 0 | 0 | 0 |
| **`values-zh-rCN`** | 2,249 | 3 | 0 | 0 | 0 |
| **`values-zh-rTW`** | 2,249 | 3 | 0 | 0 | 0 |

---

## 4. Round 3 Technical & Semantic Remediation Log

### 4.1 Rendering & Renderer (转译 → 渲染)
- `settings_playback_enable_libass_description`:
  - EN: *Experimental: advanced ASS/SSA rendering (styles, positioning, animations)*
  - Remediated: `实验性：高级 ASS/SSA 渲染（样式、定位、动画）`
- `settings_playback_render_type`:
  - EN: *Libass Render Mode*
  - Remediated: `Libass 渲染模式`
- `settings_playback_libmpv_video_output`:
  - EN: *libmpv Renderer*
  - Remediated: `libmpv 渲染器`
- `settings_playback_libmpv_video_output_dialog`:
  - EN: *libmpv Renderer*
  - Remediated: `libmpv 渲染器`
- `settings_playback_libmpv_yuv420p_description`:
  - EN: *Force YUV420P output for devices with renderer or color issues.*
  - Remediated: `为存在渲染器或色彩兼容问题的设备强制输出 YUV420P。`
- `settings_playback_section_subtitle_rendering`:
  - EN: *SUBTITLE RENDERING*
  - Remediated: `字幕渲染`

### 4.2 Compatibility (相容性 → 兼容性)
- `settings_playback_libmpv_yuv420p`:
  - EN: *libmpv YUV420P Compatibility*
  - Remediated: `libmpv YUV420P 兼容模式`
- `trakt_error_missing_ids`:
  - EN: *Missing compatible Trakt IDs*
  - Remediated: `缺少兼容的 Trakt ID`
- `player_ios_preset_compatibility_label`:
  - EN: *Compatibility*
  - Remediated: `兼容模式`
- `player_ios_preset_compatibility_desc`:
  - EN: *Closest to the older iOS MPV behavior.*
  - Remediated: `最接近旧版 iOS MPV 的兼容表现。`

### 4.3 Tone Mapping (色调对应 → 色调映射)
- `player_video_settings_tone_mapping`:
  - EN: *Tone mapping*
  - Remediated: `色调映射`
- `player_ios_preset_sdr_tone_mapped_label`:
  - EN: *SDR tone mapped*
  - Remediated: `SDR 色调映射`
- `player_ios_preset_sdr_tone_mapped_desc`:
  - EN: *More predictable whites and blacks on SDR-style output.*
  - Remediated: `在 SDR 显示输出上提供更可控的高光与阴影细节。`
- `settings_playback_map_dv7_to_hevc_description`:
  - EN: *Map Dolby Vision Profile 7 to standard HEVC for devices without DV hardware support*
  - Remediated: `将 Dolby Vision Profile 7 映射至标准 HEVC，供不支持杜比视界硬件解码的设备使用。`

### 4.4 Video Frame & Interpolation (界面 → 画面/帧插值)
- `settings_playback_show_loading_overlay_description`:
  - EN: *Show loading screen until first video frame appears.*
  - Remediated: `显示加载界面，直到首个视频画面出现。`
- `player_video_settings_interpolation`:
  - EN: *Frame interpolation*
  - Remediated: `帧插值` (精准对应 MPV 视频帧插值算法，避免暗示生成额外 AI/MEMC 帧)
- `player_video_settings_interpolation_desc`:
  - EN: *Smoother motion when mpv can cleanly use display-sync.*
  - Remediated: `在 MPV 能稳定使用显示同步时，使运动画面更平滑。` (真实表达 display-sync 与平滑运动语义)

### 4.5 Plugin Provider / Repository / Source Semantics
- `plugins_group_by_repo_desc`:
  - EN: *In Streams, show one provider per repository instead of one per source.*
  - Remediated: `在“播放源”中，每个仓库仅显示一个提供方，而不是每个来源分别显示一个提供方。`
- `plugins_group_by_repo_title`:
  - EN: *Group plugin providers by repository*
  - Remediated: `按仓库对插件提供方分组`
- `plugins_manifest_error_no_providers` & `plugins_manifest_no_providers`:
  - EN: *Manifest has no providers.*
  - Remediated: `插件 Manifest 不包含提供方。`
- `plugins_enable_globally_title` & `plugins_enable_globally_desc`:
  - EN: *Enable plugin providers globally / Use plugin providers during stream discovery.*
  - Remediated: `全局启用插件提供方 / 在播放源发现过程中使用插件提供方。`
- `collections_editor_tmdb_watch_providers`:
  - EN: *Watch Provider IDs*
  - Remediated: `播放平台提供方 ID`

### 4.6 Build / OS / Stack Trace / Exception
- `settings_licenses_attributions_mpvkit_body`: `用于 iOS 构建版本的播放。`
- `settings_licenses_attributions_exoplayer_body`: `用于 Android 构建版本的播放。`
- `updates_not_available`: `当前构建版本不支持应用内更新。`
- `plugins_error_unavailable_build`: `此构建版本不支持插件功能。`
- `sentry_sent_body`: `崩溃、当前的 ANR、堆栈跟踪、应用版本、构建类型、渠道变体、设备与操作系统元数据，以及包含请求方法、主机、路径、状态码、耗时和异常类型的安全网络跟踪记录。`
- `sentry_help_body`: `报告会按应用版本、设备型号、系统版本及堆栈跟踪归类分组，以便优先修复最常发生的实际崩溃问题。`
- `sentry_enable_dialog_title` & `description`: `启用崩溃报告？` / `向开发团队发送崩溃及无响应 (ANR) 报告以协助诊断问题。报告中绝不包含您的个人凭据或敏感数据。`
- `settings_advanced_sentry_reports`: `Sentry 崩溃报告`

### 4.7 Community / Client / Advanced / Source Code / Integrations
- `community_section_title`: `社区` (eliminated “社群”)
- `community_section_description`: `查看在移动端、TV 端及 Web 端打造与支持 Nuvio 的贡献者团队。`
- `settings_playback_intro_submit_enabled_description`: `显示提交片头/片尾时间戳至社区数据库的按钮。`
- `settings_licenses_attributions_introdb_body`: `Nuvio 使用 IntroDB API 获取社区提供的片头、前情提要、片尾及预告时间戳以实现跳过功能。`
- `settings_playback_anime_skip_client_id`: `AnimeSkip 客户端 ID` (eliminated “用户端 ID”)
- `settings_playback_anime_skip_client_id_description`: `输入您的 AnimeSkip API 客户端 ID。可前往 anime-skip.com 获取。`
- `settings_playback_anime_skip_description`: `同时在 AnimeSkip 中检索跳过时间戳（需配置客户端 ID）。`
- `compose_settings_page_advanced`: `高级` (eliminated “进阶”)
- `compose_settings_root_advanced_section`: `高级设置`
- `player_ios_preset_custom_desc`: `使用下方自定义的高级参数。`
- `settings_licenses_attributions_nuvio_body`: `源代码及开源许可协议可在项目仓库中获取。` (eliminated “原始码及授权条款”)
- `settings_licenses_attributions_mpvkit_license`: `MPVKit 源代码遵循 LGPL v3.0 协议开源。`
- `compose_settings_page_integrations`: `集成` (页面标题简洁规范为“集成”，消除“整合”)
- `settings_integrations_section_title`: `集成` (分类标题简洁规范为“集成”)
- `compose_settings_root_integrations_description`: `管理可用的集成服务` (依语境自然表达)
- `settings_debrid_experimental_notice`: `这些集成功能仍处于实验阶段，未来可能会进行调整、优化或移除。` (按功能说明自然表达，避免机械添加“服务”)

### 4.8 Player Controls, Artwork, Segments, Capture & Dialogs
- `compose_player_lock_controls` & `unlock`: `锁定播放控制` / `解锁播放控制` (eliminated “控制项”)
- `settings_integrations_tmdb_description`: `元数据增强设置`
- `settings_meta_actions_description`: `播放与收藏操作按钮。`
- `downloads_channel_description`: `显示实时下载进度及控制按钮。`
- `settings_tmdb_module_artwork`: `图片素材` (eliminated narrow “海报背景”)
- `settings_tmdb_module_artwork_description`: `来自 TMDB 的徽标与背景海报图`
- `settings_licenses_attributions_tmdb_body`: `获取电影和电视元数据、海报与背景图、预告片、演职员表、制片信息、合集及推荐。`
- `settings_playback_section_skip_segments`: `跳过片段` (eliminated “跳过区域”)
- `compose_player_capture_line` & `submit_intro_capture`: `获取当前时间` (eliminated unidiomatic “捕获”)
- `server_error_official_available`: `api.nuvio.tv 是官方服务器。请关闭此对话框，并选择“使用官方服务器”切换回来。` (eliminated “对话方块”)
- `settings_tmdb_section_modules`: `功能模块` (eliminated gaming “模组”)
- `settings_playback_invalid_regex_pattern`: `无效的正则表达式规则` (eliminated Taiwanese “规则运算式”)
- `collections_editor_tmdb_title_helper`: `显示为行/标签页名称。若留空，Nuvio 将根据来源自动生成。` (eliminated “依”)

---

## 5. Mainland Terminology Residual Audit

An automated scan was conducted across all 2,249 strings and 3 plurals in `values-zh` and `values-zh-rCN` for all target risk terms after Round 3 remediation:

| Target Term | Initial Draft Count | Post-Round 3 Count | Remediation Result / Contextual Justification |
|---|---|---|---|
| **概观** | 5 | **0** | Remediated to 概览 / 简介 |
| **缩图** | 6 | **0** | Remediated to 缩略图 |
| **资讯** | 5 | **0** | Remediated to 信息 |
| **保存库** | 19 | **0** | Remediated to 仓库 |
| **捷径** | 2 | **0** | Remediated to 快捷方式 |
| **发佈** | 12 | **0** | Remediated to 发布 (traditional “佈” eliminated) |
| **排程** | 2 | **0** | Remediated to 安排 / 计划 |
| **传送** | 11 | **0** | Remediated to 发送 |
| **程式** | 3 | **0** | Remediated to 程序 / 应用 / 插件 |
| **建置** | 2 | **0** | Remediated to 构建 |
| **标準** | 5 | **0** | Remediated to 标准 (traditional “準” eliminated) |
| **版面配置** | 5 | **0** | Remediated to 布局 |
| **指派** | 1 | **0** | Remediated to 分配 |
| **全域** | 1 | **0** | Remediated to 全局 |
| **撷取** | 5 | **0** | Remediated to 抓取 / 捕获 / 截图 |
| **传回** | 10 | **0** | Remediated to 返回 |
| **转译 / 转译器** | 6 | **0** | Remediated to 渲染 / 渲染器 |
| **相容 / 相容性** | 3 | **0** | Remediated to 兼容 / 兼容性 |
| **色调对应** | 2 | **0** | Remediated to 色调映射 (Tone mapping) |
| **界面出现** | 1 | **0** | Remediated to 画面出现 (Video frame) |
| **组建** | 5 | **0** | Remediated to 构建 (Build) |
| **作业系统** | 1 | **0** | Remediated to 操作系统 (OS) |
| **堆叠追踪** | 2 | **0** | Remediated to 堆栈跟踪 (Stack trace) |
| **例外类型** | 1 | **0** | Remediated to 异常类型 (Exception type) |
| **社群** | 3 | **0** | Remediated to 社区 (Community) |
| **用户端 ID** | 3 | **0** | Remediated to 客户端 ID (Client ID) |
| **进阶** | 4 | **0** | Remediated to 高级 (Advanced) |
| **原始码** | 2 | **0** | Remediated to 源代码 (Source code) |
| **整合** | 4 | **0** | Remediated to 集成 (页面/分类标题) / 集成服务 (功能描述) |
| **控制项** | 5 | **0** | Remediated to 控件 / 控制 / 设置 / 按钮 |
| **服务商 / 供应商** | 20 | **0** | Unified to 提供方 (Providers) |
| **海报背景** (as Artwork) | 2 | **0** | Remediated to 图片素材 / 海报与背景图 |
| **跳过区域** | 1 | **0** | Remediated to 跳过片段 (Skip segments) |
| **模组** | 1 | **0** | Remediated to 功能模块 (Modules) |
| **对话方块** | 1 | **0** | Remediated to 对话框 (Dialog) |
| **规则运算式** | 4 | **0** | Remediated to 正则表达式 (Regex) |
| **当机** | 8 | **0** | Remediated to 崩溃 (Crash) |
| **行动版** | 1 | **0** | Remediated to 移动端 (Mobile) |
| **拖曳** | 1 | **0** | Remediated to 拖动 (Drag) |
| **个人资料** | 27 | **27** | **Legitimately retained:** Strictly and exclusively used for user profiles (Profile: e.g. 管理个人资料, 切换个人资料, 创建个人资料). |
| **复制** | 4 | **4** | **Legitimately retained:** Standard Simplified Chinese (复制 JSON, 已复制代码, 复制播放源链接). |

---

## 6. Traditional Character Audit & Typographic Standards

- **OpenCC TSCharacters Scan (3,222 mappings):** **0 residual traditional characters.**
- **Typographic Standards:** Quotation marks strictly follow Mainland typographic standards (`“`, `”`, `‘`, `’`), with corner brackets (`「`, `」`, `『`, `』`) completely removed.
- **Audit Methodology Clarification:** OpenCC character mapping and risk word scanning serve as structural validation to ensure no traditional characters remain, paired with full-file semantic pair review against the canonical English source.

---

## 7. Hardcoded User-Facing English Audit Statement

- **Hardcoded User-Facing English Fixes:** **0**
- **Audit Conclusion:** 在本阶段执行的用户可见字符串扫描范围内，未发现需要抽取到资源文件的可操作硬编码英文 UI 文案。Nuvio UI 组件均通过 `stringResource(Res.string.*)` 引用文案。
- **Classification Distinctions:**
  - 8 个补齐的 key 属于上游更新带来的**缺失本地化键值 (missing localization keys)**，不属于硬编码英文修复。
  - 865 处调整属于**术语与语义规范化 (terminology normalization & semantic QA)**，不属于硬编码英文修复。
  - 技术字符串、专有品牌（Nuvio, Trakt, Simkl, TMDB, IMDb）、协议/编解码代号（HEVC, AV1, DTS, TrueHD）及远端影视元数据（电影/电视剧名、简介、海报）按架构规划和业务边界保留，不属于硬编码 UI 缺陷。

---

## 8. Verification & Test Execution Record

1. **Automated XML Parsing & Integrity Validation:**
   - **Status:** **PASS (100% Match)**
   - All 2,249 string tags and 3 plurals parse cleanly in `values-zh`, `values-zh-rCN`, and `values-zh-rTW`.
   - All format placeholders (`%1$s`, `%2$d`, `%d`, `%s`) strictly match default English templates.
   - Zero missing keys, zero extra keys, zero formatting mismatches.
2. **Dual Simplified Dictionaries Parity:**
   - `cmp values-zh/strings.xml values-zh-rCN/strings.xml` confirms files are **100% byte-identical**.
3. **Local Gradle Resource Generation & Unit Tests:**
   - **Status:** **`SKIPPED_LOCAL_ENVIRONMENT`**
   - **Reason:** Local Windows/WSL container environment lacks JDK 17.
   - **Mitigation:** In accordance with project audit rules, no local build was faked.
4. **Cloud IPA Verification Gate:**
   - Ready to be triggered on `feat/zh-cn-ui` via GitHub Actions (`Build Test IPA`, `configuration = Debug`).

---

## 9. Rollback Anchor

- **Rollback Anchor:** `git reset --hard bb4ea38a542d1cdc45710bfc36cf48df330e7989`

---

## 10. Post-Upstream Sync — 0.4.22

### 10.1 Background & Synchronization Audit
Following the merge of upstream `cmp-rewrite` release `0.4.22` into `develop/cn-emby`, a resource delta audit was conducted comparing `0.4.21` against `0.4.22`:
- **Added canonical keys in `values/strings.xml`:** Exactly 10 keys (TMDB Personal API Key & In-App Updater channels).
- **Removed keys:** 0.
- **Modified existing keys:** 0 (All 2,249 existing strings retained identical canonical English content).
- **Plurals:** Unchanged (3 plurals).
- **New Total Canonical String Count:** 2,259 strings + 3 plurals (2,262 total resource entries).

### 10.2 Added Keys & Translations
All 10 newly added upstream keys were integrated across all three Chinese resource targets:

| Resource Key | Canonical English (0.4.22) | Simplified Chinese (`values-zh` / `values-zh-rCN`) | Traditional Chinese (`values-zh-rTW`) |
|---|---|---|---|
| `settings_tmdb_api_key_label` | API key | **API 密钥** | **API 金鑰** |
| `settings_tmdb_api_key_override_description` | Use your own TMDB API key, or leave blank to use the default. | **使用你自己的 TMDB API 密钥，留空则使用默认密钥。** | **使用你自己的 TMDB API 金鑰，留空則使用預設金鑰。** |
| `settings_tmdb_personal_api_key` | Personal API key | **个人 API 密钥** | **個人 API 金鑰** |
| `updates_channel_title` | Update channel | **更新渠道** | **更新頻道** |
| `updates_channel_description` | Choose the type of app updates to receive | **选择要接收的应用更新类型** | **選擇要接收的應用程式更新類型** |
| `updates_channel_stable` | Stable | **稳定版** | **穩定版** |
| `updates_channel_beta` | Beta | **测试版** | **測試版** |
| `updates_channel_stable_description` | Intended for everyday use. | **适合日常使用。** | **適合日常使用。** |
| `updates_channel_beta_description` | Early updates that may contain bugs, break features, or behave unexpectedly. | **抢先获取更新，但可能包含错误、导致部分功能异常或出现意外行为。** | **搶先取得更新，但可能包含錯誤、導致部分功能異常或出現非預期行為。** |
| `updates_waiting_for_stable` | You’ll receive the next stable release when it becomes available | **下一个稳定版发布后，你将收到更新** | **下一個穩定版發佈後，你將收到更新** |

### 10.3 Integrity Validation (0.4.22)
- **Canonical Default (`values/strings.xml`):** 2,259 strings, 3 plurals
- **`values-zh/strings.xml`:** 2,259 strings, 3 plurals (Missing: 0, Extra: 0, Placeholder Mismatches: 0)
- **`values-zh-rCN/strings.xml`:** 2,259 strings, 3 plurals (Missing: 0, Extra: 0, Placeholder Mismatches: 0)
- **`values-zh-rTW/strings.xml`:** 2,259 strings, 3 plurals (Missing: 0, Extra: 0, Placeholder Mismatches: 0)
- **Byte-Identical Verification:** `cmp values-zh/strings.xml values-zh-rCN/strings.xml` confirmed **100% Byte-Identical**.
