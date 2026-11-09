package site.neobash.launcher.feature.home

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import kotlinx.coroutines.channels.Channel
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import java.time.LocalDateTime
import java.io.IOException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import site.neobash.launcher.core.data.*
import site.neobash.launcher.core.model.*

data class HomeState(val apps: List<LaunchableApp> = emptyList(), val preferences: Preferences = Preferences(),
    val now: LocalDateTime = LocalDateTime.now(), val loading: Boolean = true, val error: String? = null)
class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val appsRepository = AppsRepository(application)
    private val iconsRepository = AppIconsRepository(application)
    suspend fun icon(app: LaunchableApp, sizePx: Int) = iconsRepository.load(app, sizePx)
    private val preferencesRepository = PreferencesRepository(application)
    private val apps = MutableStateFlow<List<LaunchableApp>>(emptyList())
    private val loading = MutableStateFlow(true)
    private val error = MutableStateFlow<String?>(null)
    val celestial = SimulatedCelestialProvider().snapshot()
    private val clock = flow { while (true) { emit(LocalDateTime.now()); delay(60_000L - System.currentTimeMillis() % 60_000L) } }
    val state = combine(apps, preferencesRepository.preferences, clock, loading, error) { a, p, t, l, e ->
        HomeState(a, p, t, l, e)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeState())
    private val changes = Channel<String>(Channel.UNLIMITED)
    private val packagesReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            intent.data?.schemeSpecificPart?.let { changes.trySend(it) }
        }
    }
    init {
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_REMOVED)
            addAction(Intent.ACTION_PACKAGE_CHANGED)
            addAction(Intent.ACTION_PACKAGE_REPLACED)
            addDataScheme("package")
        }
        if (Build.VERSION.SDK_INT >= 33) application.registerReceiver(packagesReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        else {
            // Only protected system package actions; no public custom broadcast endpoint.
            @Suppress("DEPRECATION")
            application.registerReceiver(packagesReceiver, filter)
        }
        refresh()
        viewModelScope.launch {
            for (first in changes) {
                delay(100) // Coalesce replacement/remove/add bursts, never tied to panel navigation.
                val packages = mutableSetOf(first)
                while (true) packages += changes.tryReceive().getOrNull() ?: break
                packages.forEach { packageName ->
                    iconsRepository.invalidate(packageName)
                    refreshPackage(packageName)
                }
            }
        }
    }
    private suspend fun refreshPackage(packageName: String) {
        try { apps.value = appsRepository.discover(packageName); error.value = null }
        catch (_: SecurityException) { error.value = "No se pudieron cargar las aplicaciones." }
    }
    override fun onCleared() {
        getApplication<Application>().unregisterReceiver(packagesReceiver)
        changes.close()
        super.onCleared()
    }
    fun refresh() { viewModelScope.launch {
        try { apps.value = appsRepository.discover() } catch (_: SecurityException) { error.value = "No se pudieron cargar las aplicaciones." }
        finally { loading.value = false }
    } }
    fun favorite(slot: Int, id: String?) { viewModelScope.launch {
        try { preferencesRepository.setFavorite(slot, id) } catch (_: IOException) { error.value = "No se pudo guardar el favorito." }
    } }
    private fun savePreference(block: suspend () -> Unit) { viewModelScope.launch {
        try { block() } catch (_: IOException) { error.value = "No se pudo guardar la distribución." }
    } }
    fun component(component: HomeComponent, visible: Boolean) = savePreference { preferencesRepository.setComponent(component, visible) }
    fun count(count: Int) = savePreference { preferencesRepository.setCount(count) }
    fun move(from: Int, to: Int) = savePreference { preferencesRepository.move(from, to) }
    fun resetLayout() = savePreference { preferencesRepository.resetLayout() }
    fun reduceMotion(value: Boolean) = savePreference { preferencesRepository.setReduceMotion(value) }
    fun light(value: Boolean) { viewModelScope.launch {
        try { preferencesRepository.setLight(value) } catch (_: IOException) { error.value = "No se pudo guardar el tema." }
    } }
    fun open(app: LaunchableApp) {
        if (!appsRepository.launch(app)) {
            error.value = "Aplicación no disponible. Actualizando lista…"
            viewModelScope.launch {
                android.content.ComponentName.unflattenFromString(app.id)?.packageName?.let { refreshPackage(it) }
            }
        }
    }
    fun dismissError() { error.value = null }
}
