package site.neobash.launcher.feature.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import site.neobash.launcher.core.model.*

val FavoriteLabels = listOf("GELIA", "Notas", "Tareas", "Agenda")
@Composable fun SettingsScreen(preferences: Preferences, apps: List<LaunchableApp>, skyMode: SkyMode,
    onSkyMode: (SkyMode) -> Unit, onLight: (Boolean) -> Unit, onFavorite: (Int) -> Unit,
    onHomeSettings: () -> Unit, onBack: () -> Unit) {
    Column(Modifier.fillMaxWidth().padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        TextButton(onClick = onBack) { Text("← Inicio") }
        Text("Configuración", style = MaterialTheme.typography.headlineLarge)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Tema claro", Modifier.weight(1f))
            Switch(preferences.light, onLight)
        }
        Text("Vista astronómica simulada")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SkyMode.entries.forEach { mode ->
                FilterChip(skyMode == mode, { onSkyMode(mode) }, label = { Text(when (mode) {
                    SkyMode.AUTO -> "Auto"; SkyMode.DAY -> "Día"; SkyMode.NIGHT -> "Noche"
                }) })
            }
        }
        Text("Favoritos", style = MaterialTheme.typography.titleMedium)
        FavoriteLabels.forEachIndexed { index, label ->
            val app = apps.find { it.id == preferences.favorites[index] }
            OutlinedButton(onClick = { onFavorite(index) }, modifier = Modifier.fillMaxWidth()) {
                Text("$label · ${app?.label ?: "Configurar"}")
            }
        }
        Button(onClick = onHomeSettings) { Text("Elegir launcher predeterminado") }
        Text("Voz: función futura. Sin permisos de micrófono ni ubicación.", style = MaterialTheme.typography.bodySmall)
    }
}
