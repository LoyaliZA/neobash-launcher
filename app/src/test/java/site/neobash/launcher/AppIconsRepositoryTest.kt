package site.neobash.launcher

import android.content.ComponentName
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import site.neobash.launcher.core.data.AppIconsRepository
import site.neobash.launcher.core.model.LaunchableApp

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class AppIconsRepositoryTest {
    private val context get() = RuntimeEnvironment.getApplication()
    private fun app() = LaunchableApp(ComponentName(context, MainActivity::class.java).flattenToString(), "Neobash")

    @Test fun loadsOriginalActivityDrawableAndReusesRaster() = runBlocking {
        val repository = AppIconsRepository(context)
        val first = repository.load(app(), 80).bitmap
        assertNotNull(first)
        assertEquals(80, first!!.width)
        assertEquals(80, first.height)
        assertSame(first, repository.load(app(), 80).bitmap)
    }

    @Test fun updateAndDensitySizeDoNotReuseStaleRaster() = runBlocking {
        val repository = AppIconsRepository(context)
        val first = repository.load(app(), 80).bitmap
        val updated = repository.load(app().copy(iconRevision = 1), 80).bitmap
        val resized = repository.load(app(), 120).bitmap
        assertNotNull(first)
        assertNotNull(updated)
        assertNotSame(first, updated)
        assertEquals(120, resized!!.width)
    }

    @Test fun packageInvalidationDiscardsCachedRaster() = runBlocking {
        val repository = AppIconsRepository(context)
        val first = repository.load(app(), 80).bitmap
        repository.invalidate(context.packageName)
        assertNotSame(first, repository.load(app(), 80).bitmap)
    }

    @Test fun removedOrInvalidActivityReturnsUnavailableWithoutThrowing() = runBlocking {
        val repository = AppIconsRepository(context)
        assertFalse(repository.load(LaunchableApp("missing.package/.Missing", "Eliminada"), 80).available)
        assertFalse(repository.load(LaunchableApp("invalid", "Inválida"), 80).available)
    }
}
