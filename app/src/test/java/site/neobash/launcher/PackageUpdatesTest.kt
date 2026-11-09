package site.neobash.launcher

import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Looper
import androidx.lifecycle.ViewModelStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import site.neobash.launcher.feature.home.HomeViewModel
import java.time.Duration

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class PackageUpdatesTest {
    @Test fun packageRemovalUpdatesVisibleStateWithoutRecreatingViewModel() {
        val application = RuntimeEnvironment.getApplication()
        val pm = shadowOf(application.packageManager)
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        pm.installPackage(PackageInfo().apply { packageName = "test.dynamic"; lastUpdateTime = 1 })
        pm.addResolveInfoForIntent(intent, ResolveInfo().apply {
            nonLocalizedLabel = "Dynamic"
            activityInfo = ActivityInfo().apply {
                packageName = "test.dynamic"; name = "test.dynamic.Main"; enabled = true
                applicationInfo = ApplicationInfo().apply { packageName = "test.dynamic"; enabled = true }
            }
        })
        val model = HomeViewModel(application)
        val store = ViewModelStore().apply { put("home", model) }
        val collector = CoroutineScope(Dispatchers.Unconfined).launch { model.state.collect {} }
        fun await(condition: () -> Boolean) {
            repeat(200) {
                shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(25))
                if (condition()) return
                Thread.sleep(10) // Let real Dispatchers.IO complete its package/DataStore calls.
            }
            fail("The package update did not reach HomeState")
        }
        try {
            await { !model.state.value.loading }
            assertEquals(listOf("Dynamic"), model.state.value.apps.map { it.label })
            pm.removeResolveInfosForIntent(intent, "test.dynamic")
            application.sendBroadcast(Intent(Intent.ACTION_PACKAGE_REMOVED, Uri.parse("package:test.dynamic")))
            await { model.state.value.apps.isEmpty() }
        } finally {
            collector.cancel()
            store.clear()
        }
    }
}
