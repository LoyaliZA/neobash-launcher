package site.neobash.launcher.feature.home

import android.app.Application
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
    private val preferencesRepository = PreferencesRepository(application)
    private val apps = MutableStateFlow<List<LaunchableApp>>(emptyList())
    private val loading = MutableStateFlow(true)
    private val error = MutableStateFlow<String?>(null)
    val celestial = SimulatedCelestialProvider().snapshot()
    private val clock = flow { while (true) { emit(LocalDateTime.now()); delay(60_000L - System.currentTimeMillis() % 60_000L) } }
    val state = combine(apps, preferencesRepository.preferences, clock, loading, error) { a, p, t, l, e ->
        HomeState(a, p, t, l, e)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeState())
    fun refresh() { viewModelScope.launch {
        try { apps.value = appsRepository.discover() } catch (_: SecurityException) { error.value = "No se pudieron cargar las aplicaciones." }
        finally { loading.value = false }
    } }
    fun favorite(slot: Int, id: String?) { viewModelScope.launch {
        try { preferencesRepository.setFavorite(slot, id) } catch (_: IOException) { error.value = "No se pudo guardar el favorito." }
    } }
    fun light(value: Boolean) { viewModelScope.launch {
        try { preferencesRepository.setLight(value) } catch (_: IOException) { error.value = "No se pudo guardar el tema." }
    } }
    fun open(app: LaunchableApp) { if (!appsRepository.launch(app)) { error.value = "Aplicación no disponible. Actualizando lista…"; refresh() } }
    fun dismissError() { error.value = null }
}
