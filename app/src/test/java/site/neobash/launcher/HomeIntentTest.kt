package site.neobash.launcher

import android.content.Intent
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [28], qualifiers = "w393dp-h852dp-mdpi")
class HomeIntentTest {
    // ActivityScenario tracks the original launch intent. Use ActivityController for
    // newIntent, because a HOME delivery intentionally changes Activity.getIntent().
    @get:Rule val compose = createEmptyComposeRule()

    @Test fun homeDeliveryClosesDrawerAndRestoresHome() {
        val controller = Robolectric.buildActivity(MainActivity::class.java).setup()
        try {
            compose.onNodeWithText("Buscar aplicaciones").performClick()
            compose.onNodeWithText("Aplicaciones").assertIsDisplayed()
            compose.runOnUiThread {
                controller.newIntent(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME))
            }
            compose.onNodeWithContentDescription("Zona vacía del escritorio").assertIsDisplayed()
            compose.onNodeWithText("Aplicaciones").assertDoesNotExist()
        } finally {
            controller.pause().stop().destroy()
        }
    }
}
