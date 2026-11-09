package site.neobash.launcher

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import site.neobash.launcher.core.data.*
import site.neobash.launcher.core.designsystem.NeobashTheme
import site.neobash.launcher.core.model.LaunchableApp
import site.neobash.launcher.feature.apps.AppsScreen

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [28], qualifiers = "w393dp-h852dp-mdpi")
class AppsScreenTest {
    @get:Rule val compose = createComposeRule()

    @Test fun originalIconLoadsWhileOnlyLabelIsShownAndComponentIsKeptForLaunch() {
        val context = RuntimeEnvironment.getApplication()
        val app = LaunchableApp("${context.packageName}/.MainActivity", "Mi aplicación")
        val result = runBlocking { AppIconsRepository(context).load(app, 40) }
        assertNotNull(result.bitmap)
        var selected: LaunchableApp? = null
        compose.setContent {
            NeobashTheme(false) {
                AppsScreen(listOf(app), false, false, "", {}, { selected = it }, {}, {}, { _, _ -> result })
            }
        }
        compose.onNodeWithText(app.label).assertIsDisplayed().performClick()
        compose.onNodeWithText(app.id, substring = true).assertDoesNotExist()
        assertEquals(app.id, selected?.id)
    }

    @Test fun unavailableActivityShowsStateAndCannotBeLaunched() {
        var launched = false
        compose.setContent {
            NeobashTheme(false) {
                AppsScreen(listOf(LaunchableApp("removed/.Activity", "Eliminada")), false, false, "", {},
                    { launched = true }, {}, {}, { _, _ -> AppIconResult(available = false) })
            }
        }
        compose.onNodeWithText("No disponible").assertIsDisplayed()
        compose.onNodeWithText("Eliminada").assertIsNotEnabled().performClick()
        assertFalse(launched)
    }
}
