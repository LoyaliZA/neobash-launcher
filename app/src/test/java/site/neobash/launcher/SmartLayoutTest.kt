package site.neobash.launcher

import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import site.neobash.launcher.core.data.BlockPreferencesCodec
import site.neobash.launcher.core.model.*

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class SmartLayoutTest {
    @Test fun legacyPreferencesProduceStableVisibleBlocks() {
        val blocks = Preferences(hiddenComponents = setOf(HomeComponent.CELESTIAL)).smartBlocks()
        assertEquals(listOf("clock", "celestial", "favorites"), blocks.map { it.id })
        assertFalse(blocks[1].visible)
        assertEquals(BlockSize.entries.toSet(), blocks[1].supportedSizes)
    }
    @Test fun codecRoundTripsOptionsAndLogicalPosition() {
        val blocks = mapOf("favorites" to BlockPreferences(position = 2, options = mapOf("alias_0" to "Cámara ✨")),
            "celestial" to BlockPreferences(BlockSize.COMPACT, 0))
        assertEquals(blocks, BlockPreferencesCodec.decode(BlockPreferencesCodec.encode(blocks)))
        val preferences = Preferences(blocks = blocks)
        assertEquals("celestial", preferences.smartBlocks().first().id)
        assertEquals("Cámara ✨", preferences.favoriteLabel(0, "GELIA"))
        assertEquals("Notas", preferences.favoriteLabel(1, "Notas"))
    }
    @Test fun malformedOrFuturePayloadFallsBackWithoutLosingLegacyFavorites() {
        assertTrue(BlockPreferencesCodec.decode("broken").isEmpty())
        assertTrue(BlockPreferencesCodec.decode("{\"version\":2}").isEmpty())
        assertEquals(BlockSize.MEDIUM, BlockPreferencesCodec.decode(
            "{\"version\":1,\"blocks\":{\"celestial\":{\"size\":\"future\",\"position\":-1}}}")["celestial"]?.size)
    }
}
