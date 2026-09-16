package com.nuvio.app.features.tmdb

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

internal fun isChineseLanguage(language: String): Boolean {
    val lower = language.trim().lowercase().replace('_', '-')
    return lower == "zh" || lower.startsWith("zh-")
}

internal fun isSimplifiedChinese(language: String): Boolean {
    val lower = language.trim().lowercase().replace('_', '-')
    return lower == "zh-cn" || lower == "zh-hans" || lower == "zh-sg"
}

internal fun isTraditionalChinese(language: String): Boolean {
    val lower = language.trim().lowercase().replace('_', '-')
    return lower == "zh-tw" || lower == "zh-hant" || lower == "zh-hk"
}

internal fun tmdbImageLanguages(language: String): String {
    val normalizedLanguage = normalizeTmdbLanguage(language)
    return when {
        isSimplifiedChinese(normalizedLanguage) -> {
            listOf("zh-CN", "zh", "null", "en")
                .distinct()
                .joinToString(",")
        }
        isTraditionalChinese(normalizedLanguage) -> {
            val region = normalizedLanguage.substringAfter("-", "TW").uppercase()
            val altRegion = if (region == "TW") "HK" else "TW"
            listOf(normalizedLanguage, "zh-$altRegion", "zh", "zh-CN", "null", "en")
                .distinct()
                .joinToString(",")
        }
        normalizedLanguage.equals("zh", ignoreCase = true) -> {
            listOf("zh", "zh-CN", "zh-TW", "null", "en")
                .distinct()
                .joinToString(",")
        }
        else -> {
            listOf(normalizedLanguage.substringBefore("-"), normalizedLanguage, "en", "null")
                .distinct()
                .joinToString(",")
        }
    }
}

internal data class TmdbArtworkSelection(
    val preferredPath: String? = null,
    val fallbackPath: String? = null,
)

internal fun List<TmdbImage>.selectLocalizedArtwork(normalizedLanguage: String): TmdbArtworkSelection {
    val languageCode = normalizedLanguage.substringBefore("-").lowercase()
    val regionCode = normalizedLanguage.substringAfter("-", "").uppercase().takeIf { it.length == 2 }
        ?: defaultLanguageRegions[languageCode]

    val validImages = filter { !it.filePath.isNullOrBlank() }

    if (languageCode == "zh") {
        val isTrad = isTraditionalChinese(normalizedLanguage)
        val traditionalRegions = setOf("TW", "HK")

        val preferredList = validImages.filter { it.iso6391 == "zh" || it.iso6391 == null }
        val preferredComparator = if (isTrad) {
            compareByDescending<TmdbImage> { it.iso6391 == "zh" && it.iso31661 == regionCode }
                .thenByDescending { it.iso6391 == "zh" && it.iso31661 in traditionalRegions }
                .thenByDescending { it.iso6391 == "zh" && it.iso31661 == null }
                .thenByDescending { it.iso6391 == "zh" }
                .thenByDescending { it.iso6391 == null }
        } else {
            compareByDescending<TmdbImage> { it.iso6391 == "zh" && it.iso31661 == regionCode }
                .thenByDescending { it.iso6391 == "zh" && it.iso31661 == null }
                .thenByDescending { it.iso6391 == "zh" }
                .thenByDescending { it.iso6391 == null }
        }
        val preferred = preferredList.sortedWith(preferredComparator).firstOrNull()?.filePath?.trim()

        val fallback = validImages.firstOrNull { it.iso6391 == "en" }?.filePath?.trim()
            ?: validImages.firstOrNull { it.iso6391 != "zh" }?.filePath?.trim()

        return TmdbArtworkSelection(
            preferredPath = preferred,
            fallbackPath = if (preferred == null) fallback else null,
        )
    }

    val comparator = compareByDescending<TmdbImage> { it.iso6391 == languageCode && it.iso31661 == regionCode }
        .thenByDescending { it.iso6391 == languageCode && it.iso31661 == null }
        .thenByDescending { it.iso6391 == languageCode }
        .thenByDescending { it.iso6391 == "en" }
        .thenByDescending { it.iso6391 == null }

    val selected = validImages.sortedWith(comparator).firstOrNull()?.filePath?.trim()
    return TmdbArtworkSelection(preferredPath = selected, fallbackPath = null)
}

internal fun List<TmdbImage>.selectBestLocalizedImagePath(normalizedLanguage: String): String? {
    val selection = selectLocalizedArtwork(normalizedLanguage)
    return selection.preferredPath ?: selection.fallbackPath
}

private val defaultLanguageRegions = mapOf(
    "pt" to "PT",
    "es" to "ES",
)

@Serializable
internal data class TmdbImagesResponse(
    val logos: List<TmdbImage> = emptyList(),
    val backdrops: List<TmdbImage> = emptyList(),
    val posters: List<TmdbImage> = emptyList(),
)

@Serializable
internal data class TmdbImage(
    @SerialName("file_path") val filePath: String? = null,
    @SerialName("iso_639_1") val iso6391: String? = null,
    @SerialName("iso_3166_1") val iso31661: String? = null,
)
