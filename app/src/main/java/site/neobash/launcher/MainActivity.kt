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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.PagerDefaults
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
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import site.neobash.launcher.core.designsystem.NeobashTheme
import site.neobash.launcher.core.designsystem.NeobashMotion
import site.neobash.launcher.core.designsystem.rememberReducedMotion
import site.neobash.launcher.core.designsystem.LocalReducedMotion
import site.neobash.launcher.core.data.TransitionMeasurement
import site.neobash.launcher.core.model.SkyMode
import site.neobash.launcher.feature.apps.AppsScreen
import site.neobash.launcher.feature.home.*
import site.neobash.launcher.feature.settings.SettingsScreen
import site.neobash.launcher.feature.tools.ToolsScreen

class MainActivity : ComponentActivity() {
    private val model: HomeViewModel by viewModels()
    private var homeSignal by mutableIntStateOf(0)
    private val homeRoleRequest = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { /* Role result does not invalidate installed applications. */ }

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
            val drawerScroll = rememberLazyListState()
            val settingsScroll = rememberScrollState()
            var navigationJob by remember { mutableStateOf<kotlinx.coroutines.Job?>(null) }
            var searchRequested by rememberSaveable { mutableStateOf(false) }
            val focus = LocalFocusManager.current
            val keyboard = LocalSoftwareKeyboardController.current
            var settings by rememberSaveable { mutableStateOf(false) }
            var query by rememberSaveable { mutableStateOf("") }
            var slot by rememberSaveable { mutableIntStateOf(-1) }
            var mode by rememberSaveable { mutableStateOf(SkyMode.AUTO) }
            var contextMenu by rememberSaveable { mutableStateOf(false) }
            var about by rememberSaveable { mutableStateOf(false) }
            var pickerSettings by rememberSaveable { mutableStateOf(false) }
            var pickerReturn by rememberSaveable { mutableIntStateOf(1) }
            val measurement = remember { TransitionMeasurement() }
            val reducedMotion = rememberReducedMotion(state.preferences.reduceMotion)
            fun navigate(index: Int) { navigationJob?.cancel(); navigationJob = scope.launch {
                measurement.begin(index)
                if (reducedMotion) pager.scrollToPage(index)
                else pager.animateScrollToPage(index, animationSpec = NeobashMotion.panel())
            } }
            LaunchedEffect(pager.targetPage) {
                if (pager.targetPage != pager.settledPage) measurement.begin(pager.targetPage)
            }
            LaunchedEffect(pager.settledPage, pager.isScrollInProgress) {
                if (!pager.isScrollInProgress) {
                    withFrameNanos { }
                    measurement.finish(pager.settledPage)
                }
            }
            fun home() { searchRequested = false; settings = false; slot = -1; query = ""; contextMenu = false; about = false; navigate(1) }
            fun pick(index: Int) {
                searchRequested = false; pickerSettings = settings; pickerReturn = pager.currentPage
                slot = index; query = ""; navigate(2)
            }
            fun finishPicker() {
                slot = -1; query = ""; settings = pickerSettings
                if (!settings) navigate(pickerReturn)
            }
            fun apps() { slot = -1; query = ""; searchRequested = true; navigate(2) }
            LaunchedEffect(pager.currentPage, settings) {
                focus.clearFocus()
                keyboard?.hide()
            }
            LaunchedEffect(homeSignal) { if (homeSignal > 0) {
                searchRequested = false; navigationJob?.cancel(); settings = false; query = ""; slot = -1; contextMenu = false; about = false; pager.scrollToPage(1)
            } }
            SideEffect {
                val bars = if (state.preferences.light) SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT)
                    else SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
                enableEdgeToEdge(statusBarStyle = bars, navigationBarStyle = bars)
            }
            BackHandler(enabled = !contextMenu && !about) { if (slot >= 0) finishPicker() else home() }
            CompositionLocalProvider(LocalReducedMotion provides reducedMotion) {
                NeobashTheme(state.preferences.light) {
                    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                        // Leave system gesture edges outside the pager and all pointer handlers.
                        Column(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing.union(WindowInsets.systemGestures)).imePadding()) {
                            state.error?.let { message ->
                                TextButton(onClick = model::dismissError) { Text("$message · Cerrar") }
                            }
                            Box(Modifier.weight(1f)) {
                                val panelNames = listOf("Productividad", "Inicio", "Aplicaciones")
                                HorizontalPager(pager, Modifier.fillMaxSize().semantics {
                                    paneTitle = panelNames[pager.currentPage]
                                    customActions = if (slot >= 0) emptyList() else
                                        listOf(CustomAccessibilityAction("Abrir ajustes") { settings = true; true }) +
                                            panelNames.indices.filter { it != pager.currentPage }.map { target ->
                                                CustomAccessibilityAction("Ir a ${panelNames[target]}") { navigate(target); true }
                                            }
                                }.then(if (settings && slot < 0) Modifier.clearAndSetSemantics { } else Modifier), userScrollEnabled = slot < 0 && !settings, beyondViewportPageCount = 1,
                                    flingBehavior = PagerDefaults.flingBehavior(pager, snapAnimationSpec = if (reducedMotion) androidx.compose.animation.core.snap() else NeobashMotion.panel()), key = { it }) { index ->
                                    Box(Modifier.fillMaxSize().then(if (index != pager.currentPage) Modifier.clearAndSetSemantics { } else Modifier)) {
                                        when (index) {
                                            0 -> ToolsScreen({ home() }, { settings = true })
                                            1 -> HomeScreen(state, model.celestial, mode, { apps() }, { contextMenu = true }, { index ->
                                                val app = state.apps.find { it.id == state.preferences.favorites[index] }
                                                if (app != null) model.open(app) else pick(index)
                                            })
                                            2 -> AppsScreen(state.apps, state.loading, slot >= 0, query, { query = it }, { app ->
                                                if (slot >= 0) { model.favorite(slot, app.id); finishPicker() }
                                                else model.open(app)
                                            }, { if (slot >= 0) finishPicker() else home() }, {
                                                if (slot >= 0) model.favorite(slot, null)
                                                finishPicker()
                                            }, model::icon, drawerScroll, searchRequested && pager.settledPage == 2 && !pager.isScrollInProgress && !settings, { searchRequested = false })
                                        }
                                    }
                                }
                                androidx.compose.animation.AnimatedVisibility(visible = settings && slot < 0, enter = NeobashMotion.enter(reducedMotion), exit = NeobashMotion.exit(reducedMotion)) {
                                    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).verticalScroll(settingsScroll)) {
                                        SettingsScreen(state.preferences, state.apps, mode, { mode = it }, model::light,
                                            { pick(it) }, ::chooseDefaultHome, { home() }, model::component, model::count,
                                            model::move, model::resetLayout, model::reduceMotion, model::block)
                                    }
                                }
                            }
                        }
                        if (contextMenu) ModalBottomSheet(onDismissRequest = { contextMenu = false }) {
                            Column(Modifier.verticalScroll(rememberScrollState()).padding(horizontal = 24.dp).navigationBarsPadding()) {
                                Text("Escritorio", style = MaterialTheme.typography.titleLarge)
                                TextButton(onClick = { contextMenu = false; settings = true }) { Text("Personalizar inicio") }
                                TextButton(onClick = { contextMenu = false; settings = true }) { Text("Ajustes") }
                                TextButton(onClick = { contextMenu = false; about = true }) { Text("Información") }
                                Spacer(Modifier.height(16.dp))
                            }
                        }
                        if (about) AlertDialog(onDismissRequest = { about = false },
                            title = { Text("Neobash 0.3.0") },
                            text = { Text("Launcher nativo · Kotlin y Compose. Clima y astronomía simulados. El arco celeste es una representación estilizada. Gestos HyperOS pendientes de pruebas físicas.") },
                            confirmButton = { TextButton(onClick = { about = false }) { Text("Cerrar") } })
                    }
                }
            }
        }
    }
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (intent.hasCategory(Intent.CATEGORY_HOME)) { homeSignal++ }
    }
}
