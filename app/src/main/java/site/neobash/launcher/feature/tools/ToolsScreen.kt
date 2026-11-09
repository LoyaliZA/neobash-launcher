package site.neobash.launcher.feature.tools

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable fun ToolsScreen(onHome: () -> Unit, onSettings: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Herramientas", style = MaterialTheme.typography.headlineMedium)
        TextButton(onClick = onHome) { Text("← Inicio") }
        Text("Productividad", style = MaterialTheme.typography.titleMedium)
        Text("Este panel se reserva para futuras herramientas.")
        TextButton(onClick = onSettings) { Text("Ajustes") }
    }
}
