package site.neobash.launcher.core.model

import java.text.Normalizer

data class LaunchableApp(val id: String, val label: String, val iconRevision: Long = 0)
data class Preferences(val favorites: List<String?> = List(4) { null }, val light: Boolean = false)
enum class SkyMode { AUTO, DAY, NIGHT }
fun normalized(value: String): String = Normalizer.normalize(value, Normalizer.Form.NFD)
    .replace("\\p{M}+".toRegex(), "").lowercase(java.util.Locale.ROOT)
fun filterApps(apps: List<LaunchableApp>, query: String) = apps.filter {
    normalized(it.label).contains(normalized(query.trim()))
}
