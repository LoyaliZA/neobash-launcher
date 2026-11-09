package site.neobash.launcher.feature.apps

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import site.neobash.launcher.core.model.*

@Composable fun AppsScreen(apps: List<LaunchableApp>, loading: Boolean, picking: Boolean,
    query: String, onQuery: (String) -> Unit, onSelect: (LaunchableApp) -> Unit, onBack: () -> Unit, onClear: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(horizontal = 24.dp)) {
        TextButton(onClick = onBack) { Text("← Inicio") }
        Text(if (picking) "Elegir favorito" else "Aplicaciones", style = MaterialTheme.typography.headlineLarge)
        OutlinedTextField(query, onQuery, Modifier.fillMaxWidth().padding(vertical = 16.dp),
            label = { Text("Buscar aplicaciones") }, singleLine = true)
        if (picking) TextButton(onClick = onClear) { Text("Quitar este favorito") }
        val filtered = remember(apps, query) { filterApps(apps, query) }
        if (loading) LinearProgressIndicator(Modifier.fillMaxWidth())
        else if (filtered.isEmpty()) Text("No hay aplicaciones coincidentes.")
        LazyColumn(Modifier.weight(1f)) {
            items(filtered, key = { it.id }) { app ->
                ListItem(headlineContent = { Text(app.label) },
                    supportingContent = { Text(app.id.substringBefore('/'), maxLines = 1) },
                    modifier = Modifier.clickable { onSelect(app) })
            }
        }
    }
}
