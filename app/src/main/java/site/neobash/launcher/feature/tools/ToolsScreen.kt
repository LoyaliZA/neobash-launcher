package site.neobash.launcher.feature.tools

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Small feature boundary for the future productivity center; no placeholder apps. */
@Composable fun ToolsScreen(onHome: () -> Unit, onSettings: () -> Unit,
    onFavorites: () -> Unit, onTheme: () -> Unit, onAbout: () -> Unit, onDefaultHome: () -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            Text("Herramientas", Modifier.weight(1f), style = MaterialTheme.typography.headlineMedium)
            IconButton(onClick = onSettings, modifier = Modifier.semantics { contentDescription = "Ajustes" }) {
                Text("⚙", fontSize = 24.sp)
            }
        }
        TextButton(onClick = onHome) { Text("← Inicio") }
        Text("Tu espacio", style = MaterialTheme.typography.titleMedium)
        TextButton(onClick = onFavorites) { Text("Personalizar favoritos") }
        TextButton(onClick = onTheme) { Text("Cambiar tema") }
        HorizontalDivider()
        TextButton(onClick = onAbout) { Text("Información del launcher") }
        OutlinedButton(onClick = onDefaultHome) { Text("Elegir launcher predeterminado") }
    }
}
