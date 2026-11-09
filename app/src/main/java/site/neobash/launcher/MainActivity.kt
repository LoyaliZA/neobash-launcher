package site.neobash.launcher

import android.app.role.RoleManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.SystemBarStyle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import site.neobash.launcher.core.designsystem.NeobashTheme
import site.neobash.launcher.core.model.SkyMode
import site.neobash.launcher.feature.apps.AppsScreen
import site.neobash.launcher.feature.home.*
import site.neobash.launcher.feature.settings.SettingsScreen

class MainActivity : ComponentActivity() {
    private val model: HomeViewModel by viewModels()
    private var homeSignal by mutableIntStateOf(0)
    private val homeRoleRequest = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { model.refresh() }

    private fun chooseDefaultHome() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roles = getSystemService(RoleManager::class.java)
            if (roles.isRoleAvailable(RoleManager.ROLE_HOME)) {
                if (roles.isRoleHeld(RoleManager.ROLE_HOME)) {
                    Toast.makeText(this, "Neobash ya es tu launcher predeterminado", Toast.LENGTH_SHORT).show()
                    return
                }
                try {
                    homeRoleRequest.launch(roles.createRequestRoleIntent(RoleManager.ROLE_HOME))
                    return
                } catch (_: android.content.ActivityNotFoundException) { /* Public settings fallback below. */ }
            }
        }
        try { startActivity(Intent(Settings.ACTION_HOME_SETTINGS)) }
        catch (_: android.content.ActivityNotFoundException) {
            Toast.makeText(this, "Abre Ajustes > Aplicaciones > Aplicaciones predeterminadas > Inicio", Toast.LENGTH_LONG).show()
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val state by model.state.collectAsStateWithLifecycle()
            val pager = rememberPagerState(initialPage = 1, pageCount = { 3 })
            val scope = rememberCoroutineScope()
            val focus = LocalFocusManager.current
            val keyboard = LocalSoftwareKeyboardController.current
            var settings by rememberSaveable { mutableStateOf(false) }
            var query by rememberSaveable { mutableStateOf("") }
            var slot by rememberSaveable { mutableIntStateOf(-1) }
            var mode by rememberSaveable { mutableStateOf(SkyMode.AUTO) }
            var contextMenu by rememberSaveable { mutableStateOf(false) }
            var about by rememberSaveable { mutableStateOf(false) }
            fun navigate(index: Int) { scope.launch { pager.animateScrollToPage(index, animationSpec = tween(240)) } }
            fun home() { settings = false; slot = -1; query = ""; contextMenu = false; about = false; navigate(1) }
            fun apps() { slot = -1; query = ""; navigate(2) }
            LaunchedEffect(pager.currentPage, settings) {
                focus.clearFocus()
                keyboard?.hide()
            }
            LaunchedEffect(homeSignal) { if (homeSignal > 0) {
                settings = false; query = ""; slot = -1; contextMenu = false; about = false; pager.scrollToPage(1)
            } }
            SideEffect {
                val bars = if (state.preferences.light) SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT)
                    else SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
                enableEdgeToEdge(statusBarStyle = bars, navigationBarStyle = bars)
            }
            BackHandler(enabled = !contextMenu && !about) { home() }
            NeobashTheme(state.preferences.light) {
                Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    // Leave system gesture edges outside the pager and all pointer handlers.
                    Column(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing.union(WindowInsets.systemGestures)).imePadding()) {
                        state.error?.let { message ->
                            TextButton(onClick = model::dismissError) { Text("$message · Cerrar") }
                        }
                        if (settings && slot < 0) Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                            SettingsScreen(state.preferences, state.apps, mode, { mode = it }, model::light,
                                { slot = it; query = ""; navigate(2) }, ::chooseDefaultHome, { home() })
                        } else {
                            HorizontalPager(pager, Modifier.weight(1f), userScrollEnabled = slot < 0, key = { it }) { index ->
                                when (index) {
                                    0 -> Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                        Text("Herramientas", style = MaterialTheme.typography.headlineLarge)
                                        TextButton(onClick = { settings = true }) { Text("⚙  Ajustes") }
                                        TextButton(onClick = { settings = true }) { Text("Personalizar favoritos") }
                                        TextButton(onClick = { model.light(!state.preferences.light) }) { Text("Cambiar tema") }
                                        TextButton(onClick = { about = true }) { Text("Información del launcher") }
                                        OutlinedButton(onClick = ::chooseDefaultHome) { Text("Elegir launcher predeterminado") }
                                    }
                                    1 -> HomeScreen(state, model.celestial, mode, { apps() }, { contextMenu = true }, { index ->
                                        val app = state.apps.find { it.id == state.preferences.favorites[index] }
                                        if (app != null) model.open(app) else { slot = index; query = ""; navigate(2) }
                                    })
                                    2 -> AppsScreen(state.apps, state.loading, slot >= 0, query, { query = it }, { app ->
                                        if (slot >= 0) { model.favorite(slot, app.id); slot = -1; settings = true }
                                        else model.open(app)
                                    }, { if (slot >= 0) { slot = -1; settings = true } else home(); query = "" }, {
                                        if (slot >= 0) model.favorite(slot, null)
                                        slot = -1; settings = true
                                    })
                                }
                            }
                            if (slot < 0) {
                                // Explicit accessible routes complement swipes, including access to settings.
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                    listOf("Herramientas", "Inicio", "Apps").forEachIndexed { index, label ->
                                        TextButton(onClick = { navigate(index) }, modifier = Modifier.semantics {
                                            contentDescription = "$label, panel ${index + 1} de 3"
                                            selected = pager.currentPage == index
                                        }) { Text(label, color = if (pager.currentPage == index) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.outline) }
                                    }
                                }
                            }
                        }
                    }
                    if (contextMenu) ModalBottomSheet(onDismissRequest = { contextMenu = false }) {
                        Column(Modifier.padding(horizontal = 24.dp).navigationBarsPadding()) {
                            Text("Escritorio", style = MaterialTheme.typography.titleLarge)
                            TextButton(onClick = { contextMenu = false; settings = true }) { Text("⚙  Ajustes") }
                            TextButton(onClick = { contextMenu = false; settings = true }) { Text("Personalizar favoritos") }
                            TextButton(onClick = { contextMenu = false; model.light(!state.preferences.light) }) { Text("Cambiar tema") }
                            TextButton(onClick = { contextMenu = false; about = true }) { Text("Información del launcher") }
                            Spacer(Modifier.height(16.dp))
                        }
                    }
                    if (about) AlertDialog(onDismissRequest = { about = false },
                        title = { Text("Neobash 0.2") },
                        text = { Text("Launcher nativo · Kotlin y Compose. Clima y astronomía simulados. El arco celeste es una representación estilizada. Gestos HyperOS pendientes de pruebas físicas.") },
                        confirmButton = { TextButton(onClick = { about = false }) { Text("Cerrar") } })
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
