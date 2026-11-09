package site.neobash.launcher.feature.apps

import site.neobash.launcher.core.data.AppIconResult
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import site.neobash.launcher.core.model.*

@Composable fun AppsScreen(apps: List<LaunchableApp>, loading: Boolean, picking: Boolean,
    query: String, onQuery: (String) -> Unit, onSelect: (LaunchableApp) -> Unit, onBack: () -> Unit, onClear: () -> Unit, loadIcon: suspend (LaunchableApp, Int) -> AppIconResult) {
    Column(Modifier.fillMaxSize().padding(horizontal = 24.dp)) {
        TextButton(onClick = onBack) { Text("← Inicio") }
        Text(if (picking) "Elegir favorito" else "Aplicaciones", style = MaterialTheme.typography.headlineLarge)
        OutlinedTextField(query, onQuery, Modifier.fillMaxWidth().padding(vertical = 16.dp),
            placeholder = { Text("Buscar aplicaciones") }, shape = MaterialTheme.shapes.extraLarge, singleLine = true)
        if (picking) TextButton(onClick = onClear) { Text("Quitar este favorito") }
        val filtered = remember(apps, query) { filterApps(apps, query) }
        if (loading) LinearProgressIndicator(Modifier.fillMaxWidth())
        else if (filtered.isEmpty()) Text("No hay aplicaciones coincidentes.")
        LazyColumn(Modifier.weight(1f)) {
            items(filtered, key = { it.id }) { app ->
                val sizePx = with(LocalDensity.current) { 40.dp.roundToPx() }
                val configuration = LocalConfiguration.current
                val icon by produceState<AppIconResult?>(null, app, sizePx, configuration) {
                    value = loadIcon(app, sizePx)
                }
                // The label is the accessible name; the adjacent original icon is decorative.
                ListItem(headlineContent = { Text(app.label) },
                    leadingContent = {
                        val bitmap = icon?.bitmap
                        if (bitmap != null) Image(bitmap.asImageBitmap(), contentDescription = null, modifier = Modifier.size(40.dp))
                        else Surface(Modifier.size(40.dp), shape = MaterialTheme.shapes.small,
                            color = MaterialTheme.colorScheme.surfaceVariant) {
                            Box(contentAlignment = androidx.compose.ui.Alignment.Center) { Text(app.label.take(1)) }
                        }
                    },
                    supportingContent = if (icon?.available == false) ({ Text("No disponible") }) else null,
                    modifier = Modifier.clickable(enabled = icon?.available != false) { onSelect(app) })
            }
        }
    }
}
