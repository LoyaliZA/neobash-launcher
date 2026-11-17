package site.neobash.launcher.core.data

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.datastore.preferences.core.*
import site.neobash.launcher.core.model.HomeComponent
import site.neobash.launcher.core.model.moveFavorite
import androidx.datastore.preferences.preferencesDataStore
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import site.neobash.launcher.core.model.LaunchableApp
import site.neobash.launcher.core.model.Preferences

private val Context.settings by preferencesDataStore("launcher")
class PreferencesRepository(context: Context) {
    private val context = context.applicationContext
    private val hiddenKey = stringSetPreferencesKey("home_hidden")
    private val countKey = intPreferencesKey("favorite_count")
    private val motionKey = booleanPreferencesKey("reduce_motion")
    private val blocksKey = stringPreferencesKey("smart_blocks_v1")
    private val lightKey = booleanPreferencesKey("light")
    private fun slot(index: Int) = stringPreferencesKey("favorite_$index")
    val preferences = context.settings.data.catch { if (it is IOException) emit(emptyPreferences()) else throw it }.map { data ->
        Preferences(List(4) { data[slot(it)] }, data[lightKey] ?: false,
            HomeComponent.entries.filter { it.name in (data[hiddenKey] ?: emptySet()) }.toSet(),
            (data[countKey] ?: 4).coerceIn(1, 4), data[motionKey] ?: false, BlockPreferencesCodec.decode(data[blocksKey]))
    }
    suspend fun setBlock(id: String, preferences: site.neobash.launcher.core.model.BlockPreferences) {
        require(id in setOf("clock", "celestial", "favorites"))
        context.settings.edit { data ->
            data[blocksKey] = BlockPreferencesCodec.encode(BlockPreferencesCodec.decode(data[blocksKey]) + (id to preferences))
        }
    }
    suspend fun setFavorite(index: Int, id: String?) {
        require(index in 0..3)
        context.settings.edit { if (id == null) it.remove(slot(index)) else it[slot(index)] = id }
    }
    suspend fun setComponent(component: HomeComponent, visible: Boolean) { context.settings.edit {
        val hidden = it[hiddenKey] ?: emptySet()
        it[hiddenKey] = if (visible) hidden - component.name else hidden + component.name
    } }
    suspend fun setCount(count: Int) { require(count in 1..4); context.settings.edit { it[countKey] = count } }
    suspend fun setReduceMotion(value: Boolean) { context.settings.edit { it[motionKey] = value } }
    suspend fun move(from: Int, to: Int) { context.settings.edit { data ->
        moveFavorite(List(4) { data[slot(it)] }, from, to).forEachIndexed { i, id ->
            if (id == null) data.remove(slot(i)) else data[slot(i)] = id
        }
    } }
    suspend fun resetLayout() { context.settings.edit { data ->
        data.remove(hiddenKey); data.remove(countKey); data.remove(blocksKey)
        repeat(4) { data.remove(slot(it)) }
    } }
    suspend fun setLight(light: Boolean) { context.settings.edit { it[lightKey] = light } }
}
class AppsRepository(context: Context) {
    private val context = context.applicationContext
    private val mutex = kotlinx.coroutines.sync.Mutex()
    private var catalog: List<LaunchableApp>? = null
    suspend fun discover(packageName: String? = null): List<LaunchableApp> = withContext(Dispatchers.IO) {
        mutex.lock()
        try {
            if (packageName == null && catalog != null) return@withContext catalog!!
            // An event after a failed initial load must rebuild the whole catalog first.
            val queryPackage = if (catalog == null) null else packageName
            val start = android.os.SystemClock.elapsedRealtimeNanos()
            android.os.Trace.beginSection("Neobash.catalog")
            try {
                // PackageManager is the single source for the current user; no duplicate LauncherApps query.
                val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER).setPackage(queryPackage)
                @Suppress("DEPRECATION")
                val resolved = context.packageManager.queryIntentActivities(intent, 0)
                val revisions = mutableMapOf<String, Long>()
                val discovered = resolved.filter { it.activityInfo.packageName != context.packageName &&
                    it.activityInfo.enabled && it.activityInfo.applicationInfo.enabled }
                    .mapNotNull {
                        val packageName = it.activityInfo.packageName
                        try {
                            val revision = revisions.getOrPut(packageName) {
                                @Suppress("DEPRECATION")
                                context.packageManager.getPackageInfo(packageName, 0).lastUpdateTime
                            }
                            LaunchableApp(ComponentName(packageName, it.activityInfo.name).flattenToString(),
                                it.loadLabel(context.packageManager).toString(), revision)
                        } catch (_: PackageManager.NameNotFoundException) { null }
                          catch (_: SecurityException) { null }
                    }
                    .distinctBy { it.id }
                catalog = ((if (queryPackage == null) emptyList() else catalog.orEmpty().filterNot {
                    ComponentName.unflattenFromString(it.id)?.packageName == queryPackage
                }) + discovered).sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.label })
                if (site.neobash.launcher.BuildConfig.DEBUG) android.util.Log.d("NeobashPerf",
                    "catalog scope=${queryPackage ?: "all"} count=${catalog!!.size} elapsedNs=${android.os.SystemClock.elapsedRealtimeNanos() - start}")
                catalog!!
            } finally { android.os.Trace.endSection() }
        } finally { mutex.unlock() }
    }
    fun launch(app: LaunchableApp): Boolean { return try {
        val component = ComponentName.unflattenFromString(app.id) ?: return false
        context.startActivity(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
            .setComponent(component).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED))
        true
    } catch (_: android.content.ActivityNotFoundException) { false }
      catch (_: SecurityException) { false }
    }
}
