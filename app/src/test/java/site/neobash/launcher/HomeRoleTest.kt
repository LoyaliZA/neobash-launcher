package site.neobash.launcher

import android.provider.Settings
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.util.ReflectionHelpers

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class HomeRoleTest {
    @Test fun defaultHomeUsesSystemSettingsBeforeRoleManagerApi() {
        val controller = Robolectric.buildActivity(MainActivity::class.java).setup()
        val activity = controller.get()
        ReflectionHelpers.callInstanceMethod<Unit>(activity, "chooseDefaultHome")
        assertEquals(Settings.ACTION_HOME_SETTINGS, shadowOf(activity).nextStartedActivity.action)
        controller.pause().stop().destroy()
    }
}
