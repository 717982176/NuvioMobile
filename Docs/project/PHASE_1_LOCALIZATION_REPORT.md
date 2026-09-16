# Phase 1: Simplified Chinese UI Localization Report

**Document Version:** 1.2.0 (Independent Audit Remediation Round 2)  
**Date:** 2026-09-16  
**Target Branch:** `feat/zh-cn-ui`  
**Base Commit SHA:** `bb4ea38a542d1cdc45710bfc36cf48df330e7989` (`develop/cn-emby`)  
**Draft Reference:** Upstream PR #1941 (`7c4be28936017ee0d6ff77e773058ced576bd1f9`)  
**Phase Status:** Implementation Complete — Pending Independent Review / Cloud Verification  

---

## 1. Remediation Round 2 Overview

Following the second-round independent audit which identified systematic retention of secondary Taiwanese software terms (e.g., 概观, 缩图, 资讯, 保存库, 捷径, 排程, 传送, 程式, 建置, 版面配置, 指派, 全域, 撷取, 传回) and residual traditional characters (such as “準” in 標準 and “佈” in 發佈), a comprehensive full-file re-audit and remediation pass was performed across all 2,249 strings and 3 plurals in `values-zh` and `values-zh-rCN`.

### Key Outcomes of Round 2
- **Strings Modified in Round 2:** 151 string entries refined.
- **Cumulative Strings Normalized (Rounds 1 & 2):** 760 string entries updated from the initial draft.
- **Traditional Character Audit:** 未发现已知繁体字形残留（基于 OpenCC 字符映射、风险词全量扫描及语境校对）。
- **Targeted Risk Term Residual Count:** **0** for all 16 designated target terms.
- **Resource Key Parity:** 100% key parity with default English resources (2,249 strings + 3 plurals). Zero missing keys, zero extra keys, zero format placeholder discrepancies.
- **Two Simplified Dictionaries Identical:** `values-zh` (generic fallback) and `values-zh-rCN` (Mainland) are 100% identical and strictly adhere to Mainland software UI terminology.
- **Traditional Chinese Integrity:** `values-zh-rTW` remains intact with authentic Traditional Chinese terminology.

---

## 2. Modified & Added Files

1. `composeApp/src/commonMain/composeResources/values-zh/strings.xml` (2,249 strings + 3 plurals)
   - Configured as **Simplified Chinese default fallback**.
2. `composeApp/src/commonMain/composeResources/values-zh-rCN/strings.xml` (2,249 strings + 3 plurals)
   - Canonical Mainland China Simplified Chinese dictionary (100% identical to `values-zh`).
3. `composeApp/src/commonMain/composeResources/values-zh-rTW/strings.xml` (2,249 strings + 3 plurals)
   - Traditional Chinese dictionary with the 8 upstream keys added.
4. `composeApp/src/commonMain/kotlin/com/nuvio/app/features/settings/AppLanguage.kt` (Modified)
   - Registered `CHINESE_SIMPLIFIED("zh-CN")` and `CHINESE_TRADITIONAL("zh-TW")`.
   - Settings UI displays strictly **简体中文** and **繁體中文**.
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

## 4. Round 2 Terminology Remediation & Residual Audit

### 4.1 Target Risk Terms Residual Audit
An automated regex scan was executed across all 2,249 strings and 3 plurals in `values-zh` and `values-zh-rCN` for the 16 target terms specified in the Round 2 directive:

| Target Term | Initial Draft Count | Post-Round 2 Count | Remediation Action |
|---|---|---|---|
| **概观** | 5 | **0** | Remediated to **概览** (Overview UI) / **简介** (Episode synopsis) |
| **缩图** | 6 | **0** | Remediated to **缩略图** |
| **资讯** | 5 | **0** | Remediated to **信息** (e.g. “信息密集的横向卡片”) |
| **保存库** | 19 | **0** | Remediated to **仓库** (e.g. “插件仓库”, “添加仓库链接”) |
| **捷径** | 2 | **0** | Remediated to **快捷方式** (e.g. “预告片列表与播放快捷方式”) |
| **发佈** | 12 | **0** | Remediated to **发布** (eliminated traditional character “佈”) |
| **排程** | 2 | **0** | Remediated to **安排** / **计划** (e.g. “安排本地设备通知”) |
| **传送** | 11 | **0** | Remediated to **发送** (e.g. “发送测试通知”, “已发送”) |
| **程式** | 3 | **0** | Remediated to **插件** / **程序** / **应用** by context |
| **建置** | 2 | **0** | Remediated to **构建** (e.g. “请重新构建应用”) |
| **标準** | 5 | **0** | Remediated to **标准** (eliminated traditional character “準”) |
| **版面配置** | 5 | **0** | Remediated to **布局** (e.g. “首页布局”) |
| **指派** | 1 | **0** | Remediated to **分配** (e.g. “最多可分配 3 个分区”) |
| **全域** | 1 | **0** | Remediated to **全局** (e.g. “全局启用插件提供方”) |
| **撷取** | 5 | **0** | Remediated to **抓取** (Scrapers) / **捕获** (Capture) |
| **传回** | 10 | **0** | Remediated to **返回** (e.g. “未返回有效搜索结果”) |

### 4.2 Additional Terminology & Phrasing Refinements
- **规则运算式 (4 hits) → 正则表达式:** (e.g. “正则表达式规则”, “正则表达式自动匹配播放源”)
- **当机 (8 hits) → 崩溃:** (e.g. “Sentry 崩溃报告”, “启用崩溃报告？”)
- **行动版 (1 hit) → 移动端:** (e.g. “在移动端、TV 端及 Web 端打造与支持 Nuvio”)
- **拖曳 (1 hit) → 拖动:** (e.g. “向右拖动提升边缘亮度”)
- **取得 (18 hits) → 获取:** (e.g. “点击获取字幕”, “在项目仓库中获取”)
- **画面 (7 hits) → 界面 / 屏幕:** (e.g. “返回登录界面”, “Trakt 连接界面”)
- **美术图 (4 hits) → 海报背景 / 海报图:** (e.g. “海报优先的卡片展示”)
- **选集 (3 hits) → 系列合集 / 推荐列表:** (e.g. “系列合集”, “推荐内容列表”)
- **供应商 (17 hits) → 提供方 / 播放平台提供方:** (Watch Providers / Providers)
- **缓衝 (3 hits) → 缓冲:** (e.g. “缓冲中…”, “已缓冲”)
- **捲动 (1 hit) → 滚动:** (e.g. “滚动预览区域”)
- **滑桿 (1 hit) → 滑块:** (e.g. “使用滑块将光晕延伸”)
- **乾净 (1 hit) → 干净 / 正常:** (e.g. “当 MPV 能精准使用显示同步时”)

### 4.3 Legitimate Retentions
- **个人资料 (27 occurrences):** Strictly and exclusively used for Netflix-style user profiles (Profile: e.g. “管理个人资料”, “切换个人资料”, “创建个人资料”).
- **复制 (4 occurrences):** Standard Simplified Chinese (“复制 JSON”, “已复制代码”, “复制播放源链接”).

---

## 5. Traditional Character Audit & Typographic Standards

基于 OpenCC 字符映射（参考 3,222 个繁简映射关系）、专项风险词扫描与人工语境校对：

- **`values-zh`:** 未发现已知繁体字形残留。
- **`values-zh-rCN`:** 未发现已知繁体字形残留。
- **质量审查说明:** OpenCC 字符扫描用于辅助排查已知繁体字残留（如消除“佈”、“準”、“衝”、“捲”、“桿”、“乾”等），本项目同时对大陆现代软件 UI 术语进行了上下文级人工复核，避免单纯依据繁简字符转换断定本地化质量。
- **引号排版规范:** 标点符号统一遵循大陆国家标准，消除所有港台弯角引号（`「`、`」`、`『`、`』`），规范化使用双直引号（`“`、`”`、`‘`、`’`）。

---

## 6. Hardcoded User-Facing English Audit Statement

- **Hardcoded User-Facing English Fixes:** **0**
- **Audit Conclusion:** 在本阶段执行的用户可见字符串扫描范围内，未发现需要抽取到资源文件的可操作硬编码英文 UI 文案。
- **Classification Distinctions:**
  - 8 个补齐的 key 属于上游更新带来的**缺失本地化键值 (missing localization keys)**，不属于硬编码英文修复。
  - 760 处调整属于**术语与文字规范化 (terminology normalization & de-traditionalization)**，不属于硬编码英文修复。
  - 技术字符串、专有品牌（Nuvio, Trakt, Simkl, TMDB, IMDb）、协议/编解码代码（HEVC, AV1, DTS, TrueHD）及远端影视元数据（电影/电视剧名、简介、海报）按架构规划和业务边界保留，不属于硬编码 UI 缺陷。

---

## 7. Verification & Test Execution Record

1. **Automated XML Parsing & Integrity Validation:**
   - **Status:** **PASS (100% Match)**
   - All 2,249 string tags and 3 plurals parse cleanly in `values-zh`, `values-zh-rCN`, and `values-zh-rTW`.
   - All format placeholders (`%1$s`, `%2$d`, `%d`, `%s`) strictly match the default English templates.
   - Zero missing keys, zero extra keys, zero formatting mismatches.
2. **Local Gradle Resource Generation & Unit Tests:**
   - **Status:** **`SKIPPED_LOCAL_ENVIRONMENT`**
   - **Reason:** Local Windows/WSL environment lacks JDK 17.
   - **Mitigation:** In accordance with project audit rules, no local build was faked. Validation is handled via GitHub Actions.
3. **Cloud IPA Verification Gate:**
   - Ready to be triggered on `feat/zh-cn-ui` via GitHub Actions (`Build Test IPA`, `configuration = Debug`).

---

## 8. Known Issues & Rollback Anchor

- **Known Issues:** None. All primary and secondary Taiwanese terms and traditional characters fully remediated.
- **Rollback Anchor:** `git reset --hard bb4ea38a542d1cdc45710bfc36cf48df330e7989`
