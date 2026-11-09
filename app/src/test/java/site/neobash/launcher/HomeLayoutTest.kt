package site.neobash.launcher

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import site.neobash.launcher.core.designsystem.NeobashTheme
import site.neobash.launcher.core.model.*
import site.neobash.launcher.feature.home.*

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [28], qualifiers = "w393dp-h852dp-mdpi")
class HomeLayoutTest {
    @get:Rule val compose = createComposeRule()

    private fun home(fontScale: Float, height: Int = 760) {
        compose.setContent {
            CompositionLocalProvider(LocalDensity provides Density(1f, fontScale)) {
                NeobashTheme(false) {
                    Box(Modifier.requiredSize(393.dp, height.dp)) {
                        HomeScreen(HomeState(loading = false), SimulatedCelestialProvider().snapshot(), SkyMode.DAY, {}, {}, {})
                    }
                }
            }
        }
    }

    @Test fun normalPhoneShowsAllContentWithoutScrolling() {
        home(1f)
        compose.onNodeWithText("Buscar aplicaciones").assertIsDisplayed()
        compose.onNodeWithText("CICLO CELESTE").assertIsDisplayed()
        compose.onAllNodes(hasScrollAction()).assertCountEquals(0)
    }

    @Test fun doubleFontKeepsSearchReachableByOverflowScroll() {
        home(2f)
        compose.onNodeWithText("Buscar aplicaciones").performScrollTo().assertIsDisplayed()
        compose.onAllNodes(hasScrollAction()).assertCountEquals(1)
    }

    @Test fun shortWindowRetainsEveryControlThroughScroll() {
        home(1f, height = 480)
        compose.onNodeWithText("Buscar aplicaciones").performScrollTo().assertIsDisplayed()
        compose.onAllNodes(hasScrollAction()).assertCountEquals(1)
    }
}
