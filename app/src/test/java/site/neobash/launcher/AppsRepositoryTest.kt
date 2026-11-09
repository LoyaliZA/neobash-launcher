package site.neobash.launcher

import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.ResolveInfo
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import site.neobash.launcher.core.data.AppsRepository

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class AppsRepositoryTest {
    @Test fun metadataIsReusedAndPackageChangePreservesOtherEntries() = runBlocking {
        val context = RuntimeEnvironment.getApplication()
        val pm = shadowOf(context.packageManager)
        fun intent(pkg: String? = null) = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER).setPackage(pkg)
        fun app(pkg: String) = ResolveInfo().apply {
            activityInfo = ActivityInfo().apply {
                packageName = pkg; name = "$pkg.Main"; enabled = true
                applicationInfo = ApplicationInfo().apply { packageName = pkg; enabled = true }
            }
            nonLocalizedLabel = pkg
        }
        listOf("test.alpha", "test.beta").forEach { pkg ->
            pm.installPackage(PackageInfo().apply { packageName = pkg; lastUpdateTime = 1 })
            pm.addResolveInfoForIntent(intent(), app(pkg))
            pm.addResolveInfoForIntent(intent(pkg), app(pkg))
        }
        val repository = AppsRepository(context)
        val first = repository.discover()
        assertEquals(2, first.size)
        assertSame(first, repository.discover())
        pm.removeResolveInfosForIntent(intent("test.alpha"), "test.alpha")
        val removed = repository.discover("test.alpha")
        assertEquals(listOf("test.beta"), removed.map { it.label })
        assertSame(first.single { it.label == "test.beta" }, removed.single())
        pm.addResolveInfoForIntent(intent("test.alpha"), app("test.alpha"))
        val added = repository.discover("test.alpha")
        assertEquals(2, added.size)
        assertSame(added, repository.discover())
        pm.removeResolveInfosForIntent(intent("test.alpha"), "test.alpha")
        pm.installPackage(PackageInfo().apply { packageName = "test.alpha"; lastUpdateTime = 2 })
        val updated = app("test.alpha").apply { nonLocalizedLabel = "Actualizada" }
        pm.addResolveInfoForIntent(intent("test.alpha"), updated)
        val changed = repository.discover("test.alpha")
        assertEquals(2L, changed.single { it.label == "Actualizada" }.iconRevision)
        assertSame(first.single { it.label == "test.beta" }, changed.single { it.label == "test.beta" })
        pm.removeResolveInfosForIntent(intent("test.alpha"), "test.alpha")
        pm.addResolveInfoForIntent(intent("test.alpha"), updated.apply { activityInfo.enabled = false })
        assertEquals(listOf("test.beta"), repository.discover("test.alpha").map { it.label })
    }
}
