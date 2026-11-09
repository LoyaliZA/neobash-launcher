package site.neobash.launcher.core.data

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.util.LruCache
import androidx.core.graphics.createBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import site.neobash.launcher.core.model.LaunchableApp

data class AppIconResult(val bitmap: Bitmap? = null, val available: Boolean = true)

/** Application-scoped, bounded raster cache. Adaptive/vector/bitmap drawables use the same path. */
class AppIconsRepository(context: Context) {
    private val context = context.applicationContext
    private val mutex = Mutex()
    private val cache = object : LruCache<String, Bitmap>(4 * 1024 * 1024) {
        override fun sizeOf(key: String, value: Bitmap) = value.allocationByteCount
    }

    suspend fun invalidate(packageName: String) = withContext(Dispatchers.IO) {
        mutex.withLock {
            cache.snapshot().keys.filter { it.substringBefore('/').equals(packageName) }.forEach(cache::remove)
        }
    }

    suspend fun load(app: LaunchableApp, sizePx: Int): AppIconResult = withContext(Dispatchers.IO) {
        val config = context.resources.configuration
        val key = "${app.id}:${app.iconRevision}:$sizePx:${config.densityDpi}:${config.uiMode}"
        cache.get(key)?.let { return@withContext AppIconResult(it) }
        mutex.withLock {
            cache.get(key)?.let { return@withLock AppIconResult(it) }
            val start = android.os.SystemClock.elapsedRealtimeNanos()
            android.os.Trace.beginSection("Neobash.icon.miss")
            try {
                val component = ComponentName.unflattenFromString(app.id) ?: return@withLock AppIconResult(available = false)
                val manager = context.packageManager
                @Suppress("DEPRECATION")
                val info = manager.getActivityInfo(component, 0)
                if (!info.enabled || !info.applicationInfo.enabled) return@withLock AppIconResult(available = false)
                val drawable = info.loadIcon(manager)
                val bitmap = createBitmap(sizePx, sizePx)
                // Preserve the full original drawable, including its adaptive background and mask.
                val bounds = android.graphics.Rect(drawable.bounds)
                drawable.setBounds(0, 0, sizePx, sizePx)
                drawable.draw(Canvas(bitmap))
                drawable.bounds = bounds
                cache.put(key, bitmap)
                AppIconResult(bitmap)
            } catch (_: PackageManager.NameNotFoundException) { AppIconResult(available = false) }
              catch (_: SecurityException) { AppIconResult(available = false) }
              catch (_: android.content.res.Resources.NotFoundException) { AppIconResult() }
            finally {
                android.os.Trace.endSection()
                if (site.neobash.launcher.BuildConfig.DEBUG) android.util.Log.d("NeobashPerf",
                    "iconMiss component=${app.id} sizePx=$sizePx elapsedNs=${android.os.SystemClock.elapsedRealtimeNanos() - start}")
            }
        }
    }
}
