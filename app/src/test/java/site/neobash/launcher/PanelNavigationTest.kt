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
        compose.onNodeWithContentDescription("Zona vacía del escritorio").assertIsDisplayed()
        compose.onRoot().performTouchInput { swipeLeft() }
        compose.onNodeWithText("Aplicaciones").assertIsDisplayed()
        compose.onRoot().performTouchInput { swipeLeft() }
        compose.onNodeWithText("Aplicaciones").assertIsDisplayed()
        compose.onRoot().performTouchInput { swipeRight() }
        compose.onNodeWithContentDescription("Zona vacía del escritorio").assertIsDisplayed()
        compose.onRoot().performTouchInput { swipeRight() }
        compose.onNodeWithText("Herramientas").assertIsDisplayed()
        compose.onRoot().performTouchInput { swipeRight() }
        compose.onNodeWithText("Herramientas").assertIsDisplayed()
    }

    @Test fun longPressOpensSingleSettingsDestinationAndBackReturnsHome() {
        compose.onNodeWithContentDescription("Zona vacía del escritorio").performTouchInput { longClick() }
        compose.onNodeWithText("Personalizar inicio").assertIsDisplayed()
        compose.onNodeWithText("Ajustes").performClick()
        compose.onNodeWithText("Configuración").assertIsDisplayed()
        compose.onNodeWithText("← Inicio").performClick()
        compose.onNodeWithContentDescription("Zona vacía del escritorio").assertIsDisplayed()
    }
    @Test fun favoritePickerReturnsToSettingsAndHomeButtonReturnsHome() {
        compose.onNodeWithContentDescription("Zona vacía del escritorio").performTouchInput { longClick() }
        compose.onNodeWithText("Ajustes").performClick()
        compose.onNodeWithText("GELIA · Configurar").performScrollTo().performClick()
        compose.onNodeWithText("Elegir favorito").assertIsDisplayed()
        compose.onNodeWithText("Quitar este favorito").performClick()
        compose.onNodeWithText("Configuración").assertIsDisplayed()
        compose.onNodeWithText("← Inicio").performClick()
        compose.onNodeWithContentDescription("Zona vacía del escritorio").assertIsDisplayed()
        compose.onNodeWithText("Configuración").assertDoesNotExist()
    }


}
