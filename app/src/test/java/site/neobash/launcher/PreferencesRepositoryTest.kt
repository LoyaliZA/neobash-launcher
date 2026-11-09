package site.neobash.launcher

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import site.neobash.launcher.core.data.PreferencesRepository
import site.neobash.launcher.core.model.HomeComponent

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class PreferencesRepositoryTest {
    @Test fun layoutRoundTripsAndResetKeepsThemeAndMotion() = runBlocking {
        val context = RuntimeEnvironment.getApplication()
        val repo = PreferencesRepository(context)
        repo.resetLayout()
        repo.setFavorite(0, "camera/.Main")
        repo.setFavorite(1, "calendar/.Main")
        repo.setLight(true)
        repo.setReduceMotion(true)
        repo.setCount(2)
        repo.setComponent(HomeComponent.CELESTIAL, false)
        repo.move(0, 1)
        val saved = PreferencesRepository(context).preferences.first()
        assertEquals(listOf("calendar/.Main", "camera/.Main", null, null), saved.favorites)
        assertEquals(2, saved.favoriteCount)
        assertEquals(setOf(HomeComponent.CELESTIAL), saved.hiddenComponents)
        assertTrue(saved.light)
        assertTrue(saved.reduceMotion)
        repo.setCount(1)
        assertEquals(saved.favorites, repo.preferences.first().favorites)
        repo.resetLayout()
        val reset = repo.preferences.first()
        assertEquals(4, reset.favoriteCount)
        assertTrue(reset.hiddenComponents.isEmpty())
        assertEquals(List<String?>(4) { null }, reset.favorites)
        assertTrue(reset.light)
        assertTrue(reset.reduceMotion)
    }
}
