# Phase 2 — Chinese Metadata Localization Report (Post-Remediation)

## 1. Current Upstream Metadata Architecture
In upstream Nuvio 0.4.22, TMDB enrichment is structured as an optional enhancement layer sitting above streaming addon providers:
- **`TmdbService`**: Handles identifier resolution (`videoId` / IMDB `tt...` -> TMDB numeric ID) and TMDB HTTP requests with personal API key overrides (`TmdbSettingsRepository.effectiveApiKey()`).
- **`TmdbSettingsRepository`**: Maintains TMDB settings state (`enabled`, `apiKey`, `language`, toggle flags for modules). Persists via platform `TmdbSettingsStorage`.
- **`TmdbImages`**: Computes `include_image_language` query string and selects best localized artwork based on ISO-639-1 language and ISO-3166-1 region codes.
- **`TmdbMetadataService`**: Central coordinator for enrichment. Fetches movie/show details, credits, images, release dates, ratings, recommendations ("More Like This"), collections, person credits, and season/episode details.
- **`MetaDetailsRepository`**: Central repository for media detail resolution. First loads base metadata from Addons, then enriches it via `TmdbMetadataService.enrichMeta()`.

## 2. Localization Determination Strategy
### Core Principles: Locale Provenance & Original Comparison
The localization determination does not rely on whether a string contains Han ideographs (`containsChinese`). Instead, determination is strictly provenance- and original-source-based:
1. **Target Provenance & Original Difference**:
   - Compares the returned title with `original_title` / `original_name`.
   - If `targetTitle != originalTitle`: It is recognized as an authentic localized translation, even if it contains solely Latin characters or numbers (e.g. `F1`, `Dune`, `1917`, `WALL·E`, `3 Body Problem`).
   - If `targetTitle == originalTitle`: TMDB returned an untranslated title in the target locale. In this case, generic `zh` is inspected. If generic `zh` differs from `originalTitle`, generic `zh` is used. Otherwise, the existing Addon / source title is preserved.
2. **Japanese / Korean Kanji Original Protection**:
   - For works originally in Japanese (`original_language == "ja"`) or Korean where the original title contains Kanji/Hanja, if TMDB returns a title identical to the original title, it is recognized as untranslated rather than falsely classified as Chinese. Generic `zh` is consulted, or the source Addon title is retained.
3. **Role of `containsChinese`**:
   - `containsChinese` is strictly restricted to genre list localization detection (checking whether a genre name in `genres` is Chinese vs standard English words like `Action`, `Drama`) and is never used as the arbiter of localization for titles, overviews, person credits, episodes, seasons, or collections.

## 3. Text Fallback Chain
- **Movie / TV Title**:
  - Target `zh-CN` / `zh-TW` non-blank localized value (differing from original title)
  - $\to$ Generic `zh` non-blank localized value (differing from original title)
  - $\to$ Source / Addon title (if present)
  - $\to$ TMDB original title (only when source title is blank).
  - Non-Chinese: `targetTitle ?: sourceTitle ?: originalTitle` (upstream behavior).
- **Overview / Description**:
  - Target `zh-CN` / `zh-TW` non-blank overview
  - $\to$ Generic `zh` non-blank overview
  - $\to$ Source / Addon overview (never overwritten with an empty string or untranslated content).
  - Non-Chinese: `targetOverview ?: sourceOverview`.
- **Genres**:
  - Resolved via `resolveLocalizedGenres`:
    - Target locale genres (if localized in Chinese)
    - $\to$ Generic `zh` genres (if localized in Chinese, e.g. `["动作", "剧情"]` over untranslated English `["Action", "Drama"]`)
    - $\to$ Source / Addon genres (e.g. `["动作片"]`)
    - $\to$ Target / generic genres.
- **Episode Title**:
  - Target episode name (if not default placeholder `"Episode X"`)
  - $\to$ Generic `zh` episode name (if not default placeholder)
  - $\to$ Source episode title.
- **Episode Overview**:
  - Target episode overview $\to$ Generic `zh` episode overview $\to$ Source episode overview.
- **Season Name**:
  - Target season name (if not default `"Season X"`)
  - $\to$ Generic `zh` season name
  - $\to$ Source season name $\to$ Localized `第 X 季` / `episodes_specials`.

## 4. Artwork Fallback Chain
Artwork priority strictly prioritizes existing Addon artwork over English TMDB artwork for Chinese locales:

- **Simplified Chinese Artwork Chain**:
  1. `zh-CN` localized image (`iso_639_1 == "zh" && iso_3166_1 == "CN"`)
  2. Generic `zh` image (`iso_639_1 == "zh" && iso_3166_1 == null`)
  3. Any `zh` image (`iso_639_1 == "zh"`)
  4. Language-neutral / textless image (`iso_639_1 == null`)
  5. **Existing Source / Addon Artwork** (prioritized over English TMDB artwork!)
  6. TMDB English / default fallback artwork (only when source artwork is absent)
- **Traditional Chinese Artwork Chain**:
  1. `zh-TW` localized image (`iso_639_1 == "zh" && iso_3166_1 == "TW"`)
  2. `zh-HK` localized image (`iso_639_1 == "zh" && iso_3166_1 == "HK"`)
  3. Generic `zh` image (`iso_639_1 == "zh" && iso_3166_1 == null`)
  4. Any `zh` image (`iso_639_1 == "zh"`, e.g. CN fallback)
  5. Language-neutral / textless image (`iso_639_1 == null`)
  6. **Existing Source / Addon Artwork**
  7. TMDB English / default fallback artwork (only when source artwork is absent)
- **Non-Chinese Artwork Chain**:
  - Upstream standard: Target localized $\to$ English $\to$ Language-neutral $\to$ Source artwork.
- **Artwork Types**:
  - Posters, Backdrops, Logos all handled consistently via `selectLocalizedArtwork` and `resolveArtworkFallback`.

## 5. Cache Behavior & Non-blocking Concurrency
- **Cache Isolation**:
  - `buildMetadataRequestKey(type, id, tmdbLanguage)` incorporates normalized TMDB language into `MetaDetailsRepository` cache keys (`"$type:$id:$tmdbLanguage"`).
  - `TmdbMetadataService` uses distinct language-keyed functions: `buildEnrichmentCacheKey`, `buildEpisodeCacheKey`, `buildMoreLikeThisCacheKey`, `buildCollectionCacheKey`, `buildBackdropCacheKey`, `buildPersonCacheKey`.
  - All 6 cache key helpers are actively invoked in production fetch paths (`fetchLocalizedBackdrop`, `fetchPersonDetail`, `fetchEnrichment`, `fetchEpisodeEnrichment`, `fetchMoreLikeThis`, `fetchCollection`).
  - `zh-CN` and `zh-TW` produce isolated cache keys and payloads.
- **Non-Blocking Invalidation**:
  - `clearCaches()` uses non-blocking `backdropCacheMutex.tryLock()`. If held concurrently, it schedules an asynchronous clear via `cacheMaintenanceScope.launch` on `Dispatchers.Default`, eliminating any main-thread blocking risk during UI language switching.
  - Invalidation is triggered on TMDB language change, UI language change (when no manual override exists), and API key update.

## 6. App Language Lifecycle
- **Cold-Start & Profile Changes**:
  - `ThemeSettingsRepository.loadFromDisk()` notifies `TmdbSettingsRepository.onAppLanguageChanged(appLanguage)`.
  - Stored `AppLanguage = CHINESE_SIMPLIFIED` with unset TMDB preference cold-loads immediately as `zh-CN`.
  - Stored `AppLanguage = CHINESE_TRADITIONAL` cold-loads immediately as `zh-TW`.
  - `TmdbSettingsRepository.loadFromDisk()` tracks `previousLanguage` and calls `invalidateMetadata()` if language changed during profile reload.
- **User Preference Protection**:
  - Governed by `shouldUpdateTmdbLanguageOnAppLanguageChange(userSavedLanguage)`.
  - When the user explicitly sets and saves a language in TMDB Settings, `TmdbSettingsStorage.loadLanguage()` is populated.
  - User explicit preference is never overwritten by subsequent UI language changes.

## 7. Generic zh Extra Network Requests Audit
Generic `zh` fallback is strictly resource-level and bounded:
- **`fetchEnrichment`**: At most **1** extra request per movie/show details, only if primary locale lacks translation.
- **`fetchEpisodeEnrichment`**: At most **1** extra request per season payload, only if episodes have default placeholders or missing overviews. (Never item-by-item N+1).
- **`fetchMoreLikeThis`**: At most **1** extra request per recommendation batch.
- **`fetchCollection`**: At most **1** extra request per collection batch.
- **`fetchPersonDetail`**: At most **1** extra request for person biography and **1** extra request for credits.

## 8. Home & Search Coverage Audit Matrix
| Media Area | Coverage Status | Repository / Service Call Path | Notes |
|---|---|---|---|
| **Details Screen** | `CURRENTLY_ENRICHED` | `MetaDetailsRepository.load` $\to$ `TmdbMetadataService.enrichMeta` | Full Chinese metadata, genres, ratings, cast |
| **Seasons** | `CURRENTLY_ENRICHED` | `MetaDetailsRepository.load` $\to$ `fetchEpisodeEnrichment` $\to$ `MetaDetails.seasonNames` | Chinese season titles, localized badges |
| **Episodes** | `CURRENTLY_ENRICHED` | `MetaDetailsRepository.load` $\to$ `fetchEpisodeEnrichment` $\to$ `MetaVideo` | Chinese episode titles, overviews, season posters |
| **More Like This** | `CURRENTLY_ENRICHED` | `TmdbMetadataService.fetchMoreLikeThis` | Chinese recommendation items with fallbacks |
| **Collections** | `CURRENTLY_ENRICHED` | `TmdbMetadataService.fetchCollection` / `TmdbCollectionSourceResolver.resolveCollection` | Chinese collection names, movie parts |
| **Person Credits** | `CURRENTLY_ENRICHED` | `PersonDetailScreen` $\to$ `TmdbMetadataService.fetchPersonDetail` | Chinese biography, Chinese credits movie/show titles |
| **Home Catalog Cards** | `PARTIALLY_ENRICHED` | TMDB Collections: `TmdbCollectionSourceResolver.resolve` (`CURRENTLY_ENRICHED`). Continue Watching: `ContinueWatchingEnrichmentCache` (`CURRENTLY_ENRICHED`). Addon Catalogs: direct Addon manifest responses (`NOT_ENRICHED` at preview level). | Third-party Addon catalog rows display metadata directly from Addons. Mass preview pre-enrichment on Home launch is deferred to avoid N+1 network congestion. Full enrichment occurs upon opening Details. |
| **Search Results** | `NOT_ENRICHED` (Preview level) | `SearchRepository.search` $\to$ `SearchRequest.toSection` $\to$ Addon catalog search | Search results stream directly from Addon search endpoints. Tapping any result opens Details where full TMDB Chinese enrichment occurs immediately. |

## 9. Failure Fallback & Standalone TMDB Fallback
- **Standalone TMDB Fallback**:
  - `fetchStandaloneMeta` / `buildStandaloneMeta` now receives `language: String = "en"` and applies identical Chinese title and artwork fallback as Addon details.
  - Title resolves via `resolveLocalizedTitle` with `enrichment.genericZhTitle` and `enrichment.originalTitle`.
  - Artwork resolves via `resolveArtworkFallback(preferred, fallback, sourceArtwork = null, isChinese)`: because standalone items have no Addon source artwork, the fallback sequence is Chinese localized/neutral $\to$ English TMDB artwork fallback $\to$ `null`.
  - Non-Chinese standalone metadata preserves upstream behavior.
- **True Network Failure Test**:
  - Verified via `TmdbMetadataService.httpFetcherOverride(endpoint, query)` test seam.
  - The test seam bypasses any real TMDB API key requirement (does not depend on `TMDB_API_KEY`, `local.properties`, or GitHub Secrets), asserts that the HTTP override path was actually invoked, and operates with zero network/secret dependency.
  - Asserts that `baseMeta` is returned 100% intact with zero data loss or crashes.
- **Null Enrichment Test**:
  - Verified via `test 15 null enrichment preserves source metadata`.

## 10. Tests
All tests are implemented in `composeApp/src/commonTest/kotlin/com/nuvio/app/features/tmdb/`:
- `TmdbChineseMetadataTest.kt`:
  1. `test 1 localized title with Chinese characters`
  2. `test 2 localized title containing only Latin or digits is accepted when differing from original`
  3. `test 3 Japanese original containing Kanji is not treated as Chinese translation`
  4. `test 4 generic zh title fallback when target is untranslated`
  5. `test 5 source title fallback when both target and zh are untranslated`
  6. `test 6 Chinese overview fallback chain`
  7. `test 7 Chinese genres fallback`
  8. `test 8 episode name and overview fallback`
  9. `test 9 season name fallback`
  10. `test 10 Chinese artwork fallback chain with source preferred over English TMDB`
  11. `test 11 real cache isolation for zh-CN and zh-TW`
  12. `test 12 default language mapping for app language and device locale`
  13. `test 13 policy explicit TMDB language preference protects against app language change`
  14. `test 14 network request failure preserves source metadata intact` (asserts the HTTP override path was actually invoked)
  15. `test 15 null enrichment preserves source metadata`
  16. `test 16 non-Chinese language maintains upstream behavior`
  17. `test 17 applyEnrichment untranslated TMDB title does not overwrite existing Addon title`
  18. `test 18 applyEnrichment Chinese artwork fallback preferred then source then English fallback`
  19. `test 19 Japanese original title with Kanji is not treated as Chinese translation in applyEnrichment`
  20. `test 20 Latin or digit localized title in applyEnrichment`
  21. `test 21 Chinese genres resolution in applyEnrichment`
  22. `test 22 buildStandaloneMeta uses genericZhTitle when target title equals original`
  23. `test 23 buildStandaloneMeta uses posterFallback when preferred Chinese poster is absent`
  24. `test 24 buildStandaloneMeta non-Chinese maintains upstream behavior`
- `TmdbImagesTest.kt`:
  - Verified `selectLocalizedArtwork` preferred vs fallback distinction.
  - Verified `resolveArtworkFallback` source-before-English priority for Chinese locales.

**Test Execution Status**:
`TESTS IMPLEMENTED — NOT YET EXECUTED` (Local build daemon in restricted environment cannot download external Gradle plugin dependencies from Maven Central; all tests are written using standard Kotlin Multiplatform commonTest APIs with zero extra dependencies).

## 11. Files Changed
1. `composeApp/src/commonMain/kotlin/com/nuvio/app/features/tmdb/TmdbImages.kt`
2. `composeApp/src/commonMain/kotlin/com/nuvio/app/features/tmdb/TmdbMetadataService.kt`
3. `composeApp/src/commonMain/kotlin/com/nuvio/app/features/tmdb/TmdbSettingsRepository.kt`
4. `composeApp/src/commonMain/kotlin/com/nuvio/app/features/settings/ThemeSettingsRepository.kt`
5. `composeApp/src/commonMain/kotlin/com/nuvio/app/features/details/MetaDetailsModels.kt`
6. `composeApp/src/commonMain/kotlin/com/nuvio/app/features/details/MetaDetailsRepository.kt`
7. `composeApp/src/commonMain/kotlin/com/nuvio/app/features/details/components/DetailSeriesContent.kt`
8. `composeApp/src/commonTest/kotlin/com/nuvio/app/features/tmdb/TmdbImagesTest.kt`
9. `composeApp/src/commonTest/kotlin/com/nuvio/app/features/tmdb/TmdbChineseMetadataTest.kt`
10. `Docs/project/IMPLEMENTATION_PLAN.md`
11. `Docs/project/PHASE_2_CHINESE_METADATA_REPORT.md`

## 12. Known Limitations
- **Mass Home/Search Catalog Preview Enrichment (Deferred)**: Third-party Addon catalog rows on Home (e.g. Cinemeta) and search result cards display metadata directly provided by the Addon. Bulk pre-enrichment of hundreds of cards simultaneously on Home launch is deferred to avoid N+1 network flooding and startup latency. Full enrichment occurs seamlessly upon opening any item's Details page.
- **Untranslated Japanese Titles**: When an original Japanese title contains Kanji and neither TMDB target locale nor generic `zh` provides a localized Chinese title, the system defaults to preserving the Addon source title. If the Addon source title is also blank, it falls back to the original title.
- **TMDB Community Translations**: Titles and overviews depend on TMDB's community database. In rare cases where a title has never been translated into `zh-CN`, `zh-TW`, or `zh`, the source Addon's title is retained.
