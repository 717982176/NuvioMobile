# Phase 1: Simplified Chinese UI Localization Report

**Date:** 2026-09-16  
**Target Branch:** `feat/zh-cn-ui`  
**Base Commit SHA:** `bb4ea38a542d1cdc45710bfc36cf48df330e7989` (`develop/cn-emby`)  
**Draft Reference:** Upstream PR #1941 (`7c4be28936017ee0d6ff77e773058ced576bd1f9`)  
**Phase Status:** Implementation Complete — Pending Independent Review / Cloud Verification  

---

## 1. Executive Summary & Remediation Overview

Following the independent audit which identified systematic retention of Taiwanese terminology, residual traditional Chinese characters, and un-normalized software terminology in the initial draft, a complete, full-file remediation pass was conducted across all 2,249 strings and 3 plurals in both `values-zh` and `values-zh-rCN`.

### Key Outcomes
- **Total Strings Modified in Remediation:** 609 string entries updated to comply with Mainland China software conventions.
- **Traditional Character Pollution:** 0 residual traditional Chinese characters.
- **Terminology Normalization:** All 24 identified risk terms remediated; zero un-normalized terms remain.
- **Resource Integrity:** 100% key parity with default English resources (2,249 strings + 3 plurals). Zero missing keys, zero extra keys, zero format placeholder discrepancies.
- **Traditional Chinese Support:** `values-zh-rTW` preserved and completed with the 8 upstream keys, leaving authentic Taiwanese terminology intact for `zh-TW`.

---

## 2. Modified & Added Files

1. `composeApp/src/commonMain/composeResources/values-zh/strings.xml` (Added: 2,249 strings + 3 plurals)
   - Configured as **Simplified Chinese default fallback** (ensures generic `zh` never falls back to Traditional Chinese).
2. `composeApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` (Added: 2,249 strings + 3 plurals)
   - Complete Mainland China Simplified Chinese dictionary with full terminology normalization (100% identical to `values-zh`).
3. `composeApp/src/commonMain/composeResources/values-zh-rTW/strings.xml` (Added: 2,249 strings + 3 plurals)
   - Complete Traditional Chinese dictionary with the 8 upstream keys added.
4. `composeApp/src/commonMain/kotlin/com/nuvio/app/features/settings/AppLanguage.kt` (Modified)
   - Registered `CHINESE_SIMPLIFIED("zh-CN", Res.string.lang_chinese_simplified)`.
   - Registered `CHINESE_TRADITIONAL("zh-TW", Res.string.lang_chinese_traditional)`.
   - `fromCode` maps `zh-CN`, `zh-Hans`, `zh` to `CHINESE_SIMPLIFIED`, and `zh-TW`, `zh-Hant`, `zh-HK` to `CHINESE_TRADITIONAL`.
   - UI displays strictly two clean options: **简体中文** and **繁體中文**.
5. `composeApp/src/androidMain/res/xml/locale_config.xml` (Modified)
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

### Upstream Missing Keys Added & Translated
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

## 4. Mainland Terminology Residual Audit

A rigorous automated regex scan was executed across all 2,249 strings and 3 plurals in `values-zh` and `values-zh-rCN` to track the 24 high-risk terminology categories identified during audit:

| Scanned Term / Category | Initial Count (PR #1941) | Post-Remediation Count | Disposition / Contextual Justification |
|---|---|---|---|
| **设定** | 32 | **0** | Remediated to 设置 / 配置 / 配置文件 depending on syntax |
| **资料来源** | 1 | **0** | Remediated to 数据来源 |
| **可设定** | 1 | **0** | Remediated to 可配置 |
| **资料夹** | 20 | **0** | Remediated to 文件夹 |
| **彙整 / 汇整** | 1 | **0** | Remediated to 汇总 |
| **複製** | 4 | **0** | Remediated to 复制 |
| **复制** | 0 | **4** | **Legitimately retained:** Standard Simplified Chinese ("复制 JSON", "已复制代码", "复制播放源链接") |
| **清单** | 89 | **0** | Remediated to 列表 (watchlists/lists) or Manifest (plugin manifests) |
| **载入** | 57 | **0** | Remediated to 加载 |
| **检视** | 6 | **0** | Remediated to 查看 / 视图 |
| **製作公司 / 制作公司** | 7 | **0** | Remediated to 制片公司 |
| **範例 / 范例** | 9 | **0** | Remediated to 示例 |
| **贴上** | 5 | **0** | Remediated to 粘贴 |
| **钉选** | 6 | **0** | Remediated to 置顶 / 固定 |
| **外掛 / 外挂** | 29 | **0** | Remediated to 插件 |
| **隐私权政策 / 隐私权** | 1 | **0** | Remediated to 隐私政策 / 隐私 |
| **标籤列 / 标籤** | 18 | **0** | Remediated to 标签栏 / 标签 |
| **金钥** | 15 | **0** | Remediated to 密钥 |
| **营运者 / 营运** | 2 | **0** | Remediated to 运营方 / 运营 |
| **工作阶段** | 4 | **0** | Remediated to 会话 (session replay, session) |
| **自架服务器 / 自架** | 1 | **0** | Remediated to 自托管服务器 |
| **串流** | 58 | **0** | Remediated to 播放源 (Streams UI), 流媒体, or P2P 流传输 |
| **帐号** | 35 | **0** | Remediated to 账号 |
| **连结** | 26 | **0** | Remediated to 链接 (URL) or 连接 (network connection) |
| **资料** (Total) | 106 | **27** | **27 occurrences legitimately retained:** All 27 occurrences are strictly and exclusively "个人资料" (Netflix-style user Profile: e.g. 管理个人资料, 切换个人资料, 创建个人资料). All other instances of 资料 were remediated to 数据 (数据来源, 统计数据), 详情 (详细资料 -> 详情), or 凭据 (认证资料 -> 认证凭据). |

---

## 5. Traditional Character Pollution Audit

An exhaustive character scan was executed across `values-zh` and `values-zh-rCN` checking all CJK glyphs with distinct traditional forms:

- **Scan Pattern:** `複`, `製`, `範`, `彙`, `併`, `籤`, `檔`, `網`, `訊`, `錄`, `啟`, `儲`, `匯`, `軟`, `體`, `號`, `連`, `線`, `帳`, `頁`, `夾`, `應`, `權`, `覽`, `選`, `載`, `點`, `時`, `間`, `條`, `項`, `類`, `個`, `這`, `說`, `來`, `開`, `關`, `結`, `經`, `過`, `與`, `並`, `為`, `處`, `動`, `數`, `畫`, `聲`, `標`, `題`, `視`, `驗`, `證`, `極`, `屬`, `後`, `於`, `週`, `隻`, `掛`, `暱`, `讚`, `鑑`, `賞`, `獲`, `獎`, `註`, `憑`, `麽`, `繪`
- **Result for `values-zh`:** **0 traditional characters found.**
- **Result for `values-zh-rCN`:** **0 traditional characters found.**
- **Quotation Marks:** Traditional bracket quotation marks (`「`, `」`, `『`, `』`) normalized to standard Mainland Chinese quotation marks (`“`, `”`, `‘`, `’`).

---

## 6. Hardcoded User-Facing English Audit Result

- **Hardcoded User-Facing English Fixes:** **0**
- **Audit Result:** Codebase scan confirmed **no actionable user-facing hardcoded English strings** in UI composables. Nuvio systematically binds UI elements using `stringResource(Res.string.*)`.
- **Classification Clarifications:**
  - The 8 newly added keys are **missing localization keys** (an upstream PR #1941 gap), not hardcoded English fixes.
  - The 609 modified keys are **terminology normalization** and **character standardization**, not hardcoded English fixes.
  - Media metadata (titles, overviews, season/episode labels) is remote data strictly deferred to Phase 2 (TMDB Enrichment).
  - Raw addon stream identifiers ("Torrentio", "RD+") and technical codec abbreviations ("HEVC", "DTS", "AV1") are intentionally preserved.

---

## 7. Verification & Test Execution Record

1. **Automated XML Integrity & Placeholder Verification:**
   - **Status:** **PASS (100% Match)**
   - All 2,249 string tags and 3 plurals parse cleanly in `values-zh`, `values-zh-rCN`, and `values-zh-rTW`.
   - All format placeholders (`%1$s`, `%2$d`, `%d`, `%s`) strictly match the default English templates.
   - Zero missing keys, zero extra keys, zero formatting mismatches.
2. **Local Gradle Resource Generation & Unit Tests:**
   - **Status:** **`SKIPPED_LOCAL_ENVIRONMENT`**
   - **Reason:** Local Windows/WSL container environment lacks a local JDK 17 installation.
   - **Mitigation:** In accordance with project audit rules, no local build was faked. Definitive validation is handled via the cloud CI gate.
3. **Cloud IPA Verification Gate:**
   - Ready to be triggered on `feat/zh-cn-ui` via GitHub Actions (`Build Test IPA`, `configuration = Debug`).

---

## 8. Known Issues & Rollback Anchor

- **Known Issues:** None identified. All risk terms and traditional character contamination fully remediated.
- **Rollback Anchor:** `git reset --hard bb4ea38a542d1cdc45710bfc36cf48df330e7989`
