package site.neobash.launcher.core.data

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.preferences.core.emptyPreferences
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import site.neobash.launcher.core.model.LaunchableApp
import site.neobash.launcher.core.model.Preferences

private val Context.settings by preferencesDataStore("launcher")
class PreferencesRepository(private val context: Context) {
    private val lightKey = booleanPreferencesKey("light")
    private fun slot(index: Int) = stringPreferencesKey("favorite_$index")
    val preferences = context.settings.data.catch { if (it is IOException) emit(emptyPreferences()) else throw it }.map { data ->
        Preferences(List(4) { data[slot(it)] }, data[lightKey] ?: false)
    }
    suspend fun setFavorite(index: Int, id: String?) {
        require(index in 0..3)
        context.settings.edit { if (id == null) it.remove(slot(index)) else it[slot(index)] = id }
    }
    suspend fun setLight(light: Boolean) { context.settings.edit { it[lightKey] = light } }
}
class AppsRepository(private val context: Context) {
    suspend fun discover(): List<LaunchableApp> = withContext(Dispatchers.IO) {
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        @Suppress("DEPRECATION")
        val resolved = context.packageManager.queryIntentActivities(intent, 0)
        val revisions = mutableMapOf<String, Long>()
        resolved.filter { it.activityInfo.packageName != context.packageName &&
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
            .distinctBy { it.id }.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.label })
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
