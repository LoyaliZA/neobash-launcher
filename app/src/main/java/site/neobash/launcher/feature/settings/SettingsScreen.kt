package site.neobash.launcher.feature.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import site.neobash.launcher.core.model.*

val FavoriteLabels = listOf("GELIA", "Notas", "Tareas", "Agenda")
@Composable fun SettingsScreen(preferences: Preferences, apps: List<LaunchableApp>, skyMode: SkyMode,
    onSkyMode: (SkyMode) -> Unit, onLight: (Boolean) -> Unit, onFavorite: (Int) -> Unit,
    onHomeSettings: () -> Unit, onBack: () -> Unit,
    onComponent: (HomeComponent, Boolean) -> Unit, onCount: (Int) -> Unit,
    onMove: (Int, Int) -> Unit, onReset: () -> Unit, onReduceMotion: (Boolean) -> Unit,
    onBlock: (String, BlockPreferences) -> Unit = { _, _ -> }) {
    Column(Modifier.fillMaxWidth().padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        TextButton(onClick = onBack) { Text("← Inicio") }
        Text("Configuración", style = MaterialTheme.typography.headlineLarge)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Tema claro", Modifier.weight(1f))
            Switch(preferences.light, onLight, Modifier.semantics { contentDescription = "Tema claro" })
        }
        Text("Vista astronómica simulada")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SkyMode.entries.forEach { mode ->
                FilterChip(skyMode == mode, { onSkyMode(mode) }, label = { Text(when (mode) {
                    SkyMode.AUTO -> "Auto"; SkyMode.DAY -> "Día"; SkyMode.NIGHT -> "Noche"
                }) })
            }
        }
        Text("Tamaño del ciclo celeste")
        val celestial = preferences.blocks["celestial"] ?: BlockPreferences(position = 1)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            BlockSize.entries.forEach { size -> FilterChip(celestial.size == size,
                { onBlock("celestial", celestial.copy(size = size)) }, label = { Text(when (size) {
                    BlockSize.COMPACT -> "Compacto"; BlockSize.MEDIUM -> "Mediano"; BlockSize.EXPANDED -> "Expandido"
                }) }) }
        }
        if (site.neobash.launcher.BuildConfig.DEBUG) {
            Text("Laboratorio de movimiento · datos simulados", style = MaterialTheme.typography.labelMedium)
            site.neobash.launcher.feature.celestial.CelestialCycle(SimulatedCelestialProvider().snapshot(),
                java.time.LocalTime.of(14, 0), SkyMode.DAY, blockSize = celestial.size)
        }
        Text("Personalizar inicio", style = MaterialTheme.typography.titleMedium)
        HomeComponent.entries.forEach { component ->
            Row(Modifier.fillMaxWidth(), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                val label = when (component) {
                    HomeComponent.CLOCK -> "Reloj y clima simulado"
                    HomeComponent.CELESTIAL -> "Ciclo celeste"
                    HomeComponent.FAVORITES -> "Favoritos"
                }
                Text(label, Modifier.weight(1f))
                Switch(component !in preferences.hiddenComponents, { onComponent(component, it) },
                    Modifier.semantics { contentDescription = label })
            }
        }
        Text("Cantidad de favoritos")
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            (1..4).forEach { count -> FilterChip(preferences.favoriteCount == count,
                { onCount(count) }, label = { Text("$count") }) }
        }
        Row(Modifier.fillMaxWidth(), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            Text("Reducir movimiento", Modifier.weight(1f))
            Switch(preferences.reduceMotion, onReduceMotion, Modifier.semantics { contentDescription = "Reducir movimiento" })
        }
        Text("Favoritos", style = MaterialTheme.typography.titleMedium)
        FavoriteLabels.forEachIndexed { index, label ->
            val app = apps.find { it.id == preferences.favorites[index] }
            OutlinedButton(onClick = { onFavorite(index) }, modifier = Modifier.fillMaxWidth()) {
                Text("$label · ${app?.label ?: "Configurar"}")
            }
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (index > 0) TextButton(onClick = { onMove(index, index - 1) }) { Text("Subir favorito ${index + 1}") }
                if (index < 3) TextButton(onClick = { onMove(index, index + 1) }) { Text("Bajar favorito ${index + 1}") }
            }
        }
        OutlinedButton(onClick = onReset) { Text("Restaurar distribución predeterminada") }
        Button(onClick = onHomeSettings) { Text("Elegir launcher predeterminado") }
        Text("Voz: función futura. Sin permisos de micrófono ni ubicación.", style = MaterialTheme.typography.bodySmall)
    }
}
