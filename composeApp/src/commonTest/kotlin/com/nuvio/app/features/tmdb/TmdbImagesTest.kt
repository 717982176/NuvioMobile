package com.nuvio.app.features.tmdb

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class TmdbImagesTest {
    @Test
    fun selectedLanguageBackdropTakesPriorityOverEnglishAndTextlessArtwork() {
        val images = Json { ignoreUnknownKeys = true }.decodeFromString<TmdbImagesResponse>(
            """{"backdrops":[
                {"file_path":"/textless.jpg","iso_639_1":null},
                {"file_path":"/english.jpg","iso_639_1":"en"},
                {"file_path":"/malayalam.jpg","iso_639_1":"ml"}
            ],"logos":[{"file_path":"/logo.png","iso_639_1":"ml"}]}""",
        )

        assertEquals("/malayalam.jpg", images.backdrops.selectBestLocalizedImagePath("ml"))
        assertEquals("/logo.png", images.logos.selectBestLocalizedImagePath("ml"))
    }

    @Test
    fun regionalArtworkTakesPriorityOverOtherRegions() {
        val images = listOf(
            TmdbImage("/brazil.jpg", "pt", "BR"),
            TmdbImage("/portuguese.jpg", "pt"),
            TmdbImage("/portugal.jpg", "pt", "PT"),
        )

        assertEquals("/portugal.jpg", images.selectBestLocalizedImagePath("pt-PT"))
        assertEquals("/brazil.jpg", images.selectBestLocalizedImagePath("pt-BR"))
        assertEquals("/portugal.jpg", images.selectBestLocalizedImagePath("pt"))
    }

    @Test
    fun missingRegionFallsBackToSameLanguageThenEnglishThenTextless() {
        val images = listOf(
            TmdbImage("/textless.jpg"),
            TmdbImage("/english.jpg", "en"),
            TmdbImage("/brazil.jpg", "pt", "BR"),
            TmdbImage("/portuguese.jpg", "pt"),
        )

        assertEquals("/portuguese.jpg", images.selectBestLocalizedImagePath("pt-PT"))
        assertEquals("/brazil.jpg", images.dropLast(1).selectBestLocalizedImagePath("pt-PT"))
        assertEquals("/english.jpg", images.take(2).selectBestLocalizedImagePath("pt-PT"))
        assertEquals("/textless.jpg", images.take(1).selectBestLocalizedImagePath("pt-PT"))
    }

    @Test
    fun unusableLocalizedImageDoesNotHideValidFallback() {
        val images = listOf(TmdbImage(" ", "ml"), TmdbImage(null, "ml"), TmdbImage("/english.jpg", "en"))
        assertEquals("/english.jpg", images.selectBestLocalizedImagePath("ml"))
        assertNull(emptyList<TmdbImage>().selectBestLocalizedImagePath("ml"))
        assertNull(images.take(2).selectBestLocalizedImagePath("ml"))
    }

    @Test
    fun imageLanguageQueryIncludesConfiguredLanguageAndFallbacks() {
        assertEquals("ml,en,null", tmdbImageLanguages("ml"))
        assertEquals("pt,pt-BR,en,null", tmdbImageLanguages("pt_BR"))
        assertEquals("es,es-MX,en,null", tmdbImageLanguages("es-419"))
        assertEquals("en,null", tmdbImageLanguages("en"))
        assertEquals("zh-CN,zh,null,en", tmdbImageLanguages("zh-CN"))
        assertEquals("zh-TW,zh-HK,zh,zh-CN,null,en", tmdbImageLanguages("zh-TW"))
        assertEquals("zh,zh-CN,zh-TW,null,en", tmdbImageLanguages("zh"))
    }

    @Test
    fun chineseArtworkPrefersChineseThenNeutralOverEnglish() {
        val imagesWithChinese = listOf(
            TmdbImage("/english.jpg", "en"),
            TmdbImage("/textless.jpg", null),
            TmdbImage("/chinese_cn.jpg", "zh", "CN"),
        )
        assertEquals("/chinese_cn.jpg", imagesWithChinese.selectBestLocalizedImagePath("zh-CN"))

        val imagesWithoutChinese = listOf(
            TmdbImage("/english.jpg", "en"),
            TmdbImage("/textless.jpg", null),
            TmdbImage("/japanese.jpg", "ja"),
        )
        // Chinese user should get textless/neutral artwork rather than English text artwork!
        assertEquals("/textless.jpg", imagesWithoutChinese.selectBestLocalizedImagePath("zh-CN"))
        assertEquals("/textless.jpg", imagesWithoutChinese.selectBestLocalizedImagePath("zh-TW"))

        // When only English is available, it can fall back to English
        val imagesOnlyEnglish = listOf(
            TmdbImage("/english.jpg", "en"),
        )
        assertEquals("/english.jpg", imagesOnlyEnglish.selectBestLocalizedImagePath("zh-CN"))
    }

    @Test
    fun traditionalChineseArtworkRegionalFallback() {
        val images = listOf(
            TmdbImage("/english.jpg", "en"),
            TmdbImage("/textless.jpg", null),
            TmdbImage("/chinese_cn.jpg", "zh", "CN"),
            TmdbImage("/chinese_hk.jpg", "zh", "HK"),
            TmdbImage("/chinese_tw.jpg", "zh", "TW"),
        )
        assertEquals("/chinese_tw.jpg", images.selectBestLocalizedImagePath("zh-TW"))
        assertEquals("/chinese_hk.jpg", images.dropLast(1).selectBestLocalizedImagePath("zh-TW"))
        assertEquals("/chinese_cn.jpg", images.take(3).selectBestLocalizedImagePath("zh-TW"))
    }

    @Test
    fun nonChineseMaintainsUpstreamPriorityEnglishBeforeNeutral() {
        val images = listOf(
            TmdbImage("/textless.jpg", null),
            TmdbImage("/english.jpg", "en"),
        )
        assertEquals("/english.jpg", images.selectBestLocalizedImagePath("fr"))
    }

    @Test
    fun selectLocalizedArtworkDistinguishesPreferredAndFallbackForChinese() {
        val imagesWithChinese = listOf(
            TmdbImage("/english.jpg", "en"),
            TmdbImage("/chinese_cn.jpg", "zh", "CN"),
        )
        val selection1 = imagesWithChinese.selectLocalizedArtwork("zh-CN")
        assertEquals("/chinese_cn.jpg", selection1.preferredPath)
        assertNull(selection1.fallbackPath)

        val imagesOnlyEnglish = listOf(
            TmdbImage("/english.jpg", "en"),
        )
        val selection2 = imagesOnlyEnglish.selectLocalizedArtwork("zh-CN")
        assertNull(selection2.preferredPath)
        assertEquals("/english.jpg", selection2.fallbackPath)
    }

    @Test
    fun resolveArtworkFallbackPrioritizesSourceOverEnglishForChinese() {
        // 1. Chinese artwork exists -> Chinese
        assertEquals(
            "/chinese.jpg",
            resolveArtworkFallback(
                preferredArtwork = "/chinese.jpg",
                fallbackTmdbArtwork = "/english.jpg",
                sourceArtwork = "/source.jpg",
                isChinese = true,
            ),
        )

        // 2. Chinese missing + neutral exists -> neutral
        assertEquals(
            "/neutral.jpg",
            resolveArtworkFallback(
                preferredArtwork = "/neutral.jpg",
                fallbackTmdbArtwork = "/english.jpg",
                sourceArtwork = "/source.jpg",
                isChinese = true,
            ),
        )

        // 3. Chinese/neutral missing + source exists + English TMDB exists -> source
        assertEquals(
            "/source.jpg",
            resolveArtworkFallback(
                preferredArtwork = null,
                fallbackTmdbArtwork = "/english.jpg",
                sourceArtwork = "/source.jpg",
                isChinese = true,
            ),
        )

        // 4. Chinese/neutral/source all missing + English TMDB exists -> English
        assertEquals(
            "/english.jpg",
            resolveArtworkFallback(
                preferredArtwork = null,
                fallbackTmdbArtwork = "/english.jpg",
                sourceArtwork = null,
                isChinese = true,
            ),
        )

        // 5. 全部缺失 -> null
        assertNull(
            resolveArtworkFallback(
                preferredArtwork = null,
                fallbackTmdbArtwork = null,
                sourceArtwork = null,
                isChinese = true,
            ),
        )

        // Non-Chinese: upstream behavior (TMDB English preferred over source)
        assertEquals(
            "/english.jpg",
            resolveArtworkFallback(
                preferredArtwork = null,
                fallbackTmdbArtwork = "/english.jpg",
                sourceArtwork = "/source.jpg",
                isChinese = false,
            ),
        )
    }
}
