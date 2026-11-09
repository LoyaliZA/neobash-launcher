package site.neobash.launcher

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [28], qualifiers = "w393dp-h852dp-mdpi")
class PanelNavigationTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Test fun swipesReachBothPanelsAndStopAtOuterLimits() {
        compose.onNodeWithContentDescription("Opciones del escritorio y navegación").assertIsDisplayed()
        compose.onRoot().performTouchInput { swipeLeft() }
        compose.onNodeWithText("Aplicaciones").assertIsDisplayed()
        compose.onRoot().performTouchInput { swipeLeft() }
        compose.onNodeWithText("Aplicaciones").assertIsDisplayed()
        compose.onRoot().performTouchInput { swipeRight() }
        compose.onNodeWithContentDescription("Opciones del escritorio y navegación").assertIsDisplayed()
        compose.onRoot().performTouchInput { swipeRight() }
        compose.onNodeWithText("Herramientas").assertIsDisplayed()
        compose.onRoot().performTouchInput { swipeRight() }
        compose.onNodeWithText("Herramientas").assertIsDisplayed()
    }

    @Test fun optionsProvideNavigationWithoutHorizontalSwipe() {
        compose.onNodeWithContentDescription("Opciones del escritorio y navegación").performClick()
        compose.onNodeWithText("Herramientas").performClick()
        compose.onNodeWithContentDescription("Ajustes").assertIsDisplayed()
        compose.onNodeWithText("← Inicio").performClick()
        compose.onNodeWithContentDescription("Opciones del escritorio y navegación").assertIsDisplayed()
    }
}
