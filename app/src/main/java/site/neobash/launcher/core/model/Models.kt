package site.neobash.launcher.core.model

import java.text.Normalizer

data class LaunchableApp(val id: String, val label: String, val iconRevision: Long = 0) {
    val searchLabel: String = normalized(label)
}
enum class HomeComponent { CLOCK, CELESTIAL, FAVORITES }
data class Preferences(val favorites: List<String?> = List(4) { null }, val light: Boolean = false,
    val hiddenComponents: Set<HomeComponent> = emptySet(), val favoriteCount: Int = 4,
    val reduceMotion: Boolean = false, val blocks: Map<String, BlockPreferences> = emptyMap())
fun moveFavorite(favorites: List<String?>, from: Int, to: Int): List<String?> {
    require(from in favorites.indices && to in favorites.indices)
    return favorites.toMutableList().apply { add(to, removeAt(from)) }
}
enum class SkyMode { AUTO, DAY, NIGHT }
private val combiningMarks = "\\p{M}+".toRegex()
fun normalized(value: String): String = Normalizer.normalize(value, Normalizer.Form.NFD)
    .replace(combiningMarks, "").lowercase(java.util.Locale.ROOT)
fun filterApps(apps: List<LaunchableApp>, query: String): List<LaunchableApp> {
    val needle = normalized(query.trim())
    if (needle.isEmpty()) return apps
    return apps.filter { it.searchLabel.contains(needle) }
}
