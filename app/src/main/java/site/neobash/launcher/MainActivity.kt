package site.neobash.launcher

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.SystemBarStyle
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import site.neobash.launcher.core.designsystem.NeobashTheme
import site.neobash.launcher.core.model.SkyMode
import site.neobash.launcher.feature.apps.AppsScreen
import site.neobash.launcher.feature.home.*
import site.neobash.launcher.feature.settings.SettingsScreen

class MainActivity : ComponentActivity() {
    private val model: HomeViewModel by viewModels()
    private var homeSignal by mutableIntStateOf(0)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val state by model.state.collectAsStateWithLifecycle()
            var page by rememberSaveable { mutableStateOf("home") }
            var query by rememberSaveable { mutableStateOf("") }
            var slot by rememberSaveable { mutableIntStateOf(-1) }
            var mode by rememberSaveable { mutableStateOf(SkyMode.AUTO) }
            LaunchedEffect(homeSignal) { if (homeSignal > 0) { page = "home"; query = ""; slot = -1 } }
            SideEffect {
                val bars = if (state.preferences.light) SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT)
                    else SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
                enableEdgeToEdge(statusBarStyle = bars, navigationBarStyle = bars)
            }
            BackHandler { page = "home"; query = ""; slot = -1 }
            NeobashTheme(state.preferences.light) {
                Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    Column(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing).imePadding()) {
                        state.error?.let { message ->
                            TextButton(onClick = model::dismissError) { Text("$message · Cerrar") }
                        }
                        when(page) {
                            "apps" -> AppsScreen(state.apps, state.loading, slot >= 0, query, { query = it }, { app ->
                                if (slot >= 0) { model.favorite(slot, app.id); slot = -1; page = "settings" }
                                else model.open(app)
                            }, { page = if (slot >= 0) "settings" else "home"; slot = -1; query = "" }, {
                                if (slot >= 0) model.favorite(slot, null)
                                slot = -1; page = "settings"
                            })
                            "settings" -> Column(Modifier.verticalScroll(rememberScrollState())) {
                                SettingsScreen(state.preferences, state.apps, mode, { mode = it }, model::light,
                                    { slot = it; query = ""; page = "apps" }, {
                                        try { startActivity(Intent(Settings.ACTION_HOME_SETTINGS)) }
                                        catch (_: android.content.ActivityNotFoundException) { Toast.makeText(this@MainActivity, "Abre Ajustes > Aplicaciones > Aplicaciones predeterminadas > Inicio", Toast.LENGTH_LONG).show() }
                                    }, { page = "home" })
                            }
                            else -> HomeScreen(state, model.celestial, mode, { slot = -1; query = ""; page = "apps" },
                                { page = "settings" }, { index ->
                                    val app = state.apps.find { it.id == state.preferences.favorites[index] }
                                    if (app != null) model.open(app) else { slot = index; query = ""; page = "apps" }
                                })
                        }
                    }
                }
            }
        }
    }
    override fun onResume() { super.onResume(); model.refresh() }
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (intent.hasCategory(Intent.CATEGORY_HOME)) { homeSignal++; model.refresh() }
    }
}
