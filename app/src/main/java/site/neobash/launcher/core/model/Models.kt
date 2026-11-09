package site.neobash.launcher.core.model

import java.text.Normalizer
import java.time.LocalTime
import kotlin.math.PI
import kotlin.math.cos

data class LaunchableApp(val id: String, val label: String)
data class Preferences(val favorites: List<String?> = List(4) { null }, val light: Boolean = false)
enum class SkyMode { AUTO, DAY, NIGHT }
data class CelestialData(val sunrise: LocalTime, val sunset: LocalTime, val phase: Double) {
    val illuminatedFraction: Double get() = (1 - cos(2 * PI * phase)) / 2
    fun isDay(time: LocalTime) = time >= sunrise && time < sunset
    fun solarProgress(time: LocalTime): Float = ((time.toSecondOfDay() - sunrise.toSecondOfDay()).toFloat() /
        (sunset.toSecondOfDay() - sunrise.toSecondOfDay())).coerceIn(0f, 1f)
}
interface CelestialProvider { fun snapshot(): CelestialData }
class SimulatedCelestialProvider : CelestialProvider {
    override fun snapshot() = CelestialData(LocalTime.of(6, 30), LocalTime.of(18, 45), 0.35)
}
fun normalized(value: String): String = Normalizer.normalize(value, Normalizer.Form.NFD)
    .replace("\\p{M}+".toRegex(), "").lowercase(java.util.Locale.ROOT)
fun filterApps(apps: List<LaunchableApp>, query: String) = apps.filter {
    normalized(it.label).contains(normalized(query.trim()))
}
