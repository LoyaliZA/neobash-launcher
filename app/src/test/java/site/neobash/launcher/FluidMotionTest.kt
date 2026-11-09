package site.neobash.launcher

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import site.neobash.launcher.core.designsystem.*
import site.neobash.launcher.core.model.*
import site.neobash.launcher.core.data.AppIconResult
import site.neobash.launcher.feature.celestial.CelestialCycle
import site.neobash.launcher.feature.apps.AppsScreen
import java.time.LocalTime

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [28], qualifiers = "w393dp-h852dp-mdpi")
class FluidMotionTest {
    @get:Rule val compose = createComposeRule()
    @Test fun resizingCanReverseBeforeSettlingAndReducedMotionSnaps() {
        var size by mutableStateOf(BlockSize.COMPACT)
        var reduced by mutableStateOf(false)
        compose.setContent {
            CompositionLocalProvider(LocalReducedMotion provides reduced) {
                NeobashTheme(false) {
                    CelestialCycle(SimulatedCelestialProvider().snapshot(), LocalTime.of(14, 0),
                        SkyMode.DAY, 132.dp, size)
                }
            }
        }
        val arc = compose.onNodeWithContentDescription("Astronomía simulada", substring = true)
        val compact = arc.fetchSemanticsNode().boundsInRoot.height
        compose.mainClock.autoAdvance = false
        compose.runOnIdle { size = BlockSize.EXPANDED }
        compose.mainClock.advanceTimeByFrame()
        compose.waitForIdle()
        compose.mainClock.advanceTimeBy(160)
        compose.waitForIdle()
        val moving = arc.fetchSemanticsNode().boundsInRoot.height
        assertTrue("compact=$compact moving=$moving", moving > compact)
        compose.runOnIdle { size = BlockSize.COMPACT }
        compose.mainClock.advanceTimeByFrame()
        compose.waitForIdle()
        compose.mainClock.advanceTimeBy(1000)
        compose.waitForIdle()
        assertEquals(compact, arc.fetchSemanticsNode().boundsInRoot.height, 1f)
        compose.runOnIdle { reduced = true; size = BlockSize.EXPANDED }
        compose.mainClock.advanceTimeByFrame()
        compose.waitForIdle()
        compose.mainClock.advanceTimeBy(64)
        compose.waitForIdle()
        assertTrue(arc.fetchSemanticsNode().boundsInRoot.height > compact * 1.8f)
    }
    @Test fun pressDispatchesClickBeforeReleaseAnimationCompletes() {
        var clicks = 0
        compose.setContent { NeobashTheme(false) {
            Box(Modifier.size(80.dp).fluidClickable { clicks++ })
        } }
        compose.mainClock.autoAdvance = false
        compose.onNode(hasClickAction()).performTouchInput { down(center); advanceEventTime(100); up() }
        compose.runOnIdle { assertEquals(1, clicks) }
    }
    @Test fun drawerScrollSurvivesPanelRemovalWhenStateIsHoisted() {
        var visible by mutableStateOf(true)
        compose.setContent {
            val scroll = rememberLazyListState()
            NeobashTheme(false) { if (visible) {
                AppsScreen((0..99).map { LaunchableApp("id$it", "App $it") }, false, false,
                    "", {}, {}, {}, {}, { _, _ -> AppIconResult() }, scroll)
            } }
        }
        compose.onNode(hasScrollToIndexAction()).performScrollToIndex(50)
        compose.onNodeWithText("App 50").assertIsDisplayed()
        compose.runOnIdle { visible = false }
        compose.waitForIdle()
        compose.runOnIdle { visible = true }
        compose.onNodeWithText("App 50").assertIsDisplayed()
    }
}
