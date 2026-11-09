package site.neobash.benchmark

import android.content.Intent
import androidx.benchmark.macro.*
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Physical device only. Does not change the default HOME role or animation settings. */
@RunWith(AndroidJUnit4::class)
class DrawerBenchmark {
    @get:Rule val benchmark = MacrobenchmarkRule()
    private val packageName = "site.neobash.launcher"
    @Test fun firstDrawer() = drawer(StartupMode.COLD)
    @Test fun consecutiveDrawers() = drawer(StartupMode.WARM)
    private fun drawer(startup: StartupMode) = benchmark.measureRepeated(
        packageName = packageName,
        metrics = listOf(FrameTimingMetric()),
        compilationMode = CompilationMode.None(),
        startupMode = startup,
        iterations = 10,
        setupBlock = {
            startActivityAndWait(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
                .setClassName(packageName, "$packageName.MainActivity"))
        }
    ) {
        val w = device.displayWidth
        val y = device.displayHeight / 2
        repeat(if (startup == StartupMode.COLD) 1 else 5) {
            device.swipe(w * 8 / 10, y, w * 2 / 10, y, 15)
            device.waitForIdle()
            device.swipe(w * 2 / 10, y, w * 8 / 10, y, 15)
            device.waitForIdle()
        }
    }
}
