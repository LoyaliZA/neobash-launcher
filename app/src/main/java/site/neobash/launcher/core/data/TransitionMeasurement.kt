package site.neobash.launcher.core.data

import android.os.SystemClock
import android.os.Trace
import android.util.Log
import site.neobash.launcher.BuildConfig

/** Debug-only request/drag-target to first frame after pager settlement, not input-to-photon. */
class TransitionMeasurement {
    private var target: Int? = null
    private var started = 0L
    fun begin(page: Int) {
        if (!BuildConfig.DEBUG || target == page) return
        target = page
        started = SystemClock.elapsedRealtimeNanos()
    }
    fun finish(page: Int) {
        if (!BuildConfig.DEBUG) return
        if (target != page) { target = null; return }
        val elapsed = SystemClock.elapsedRealtimeNanos() - started
        Trace.beginSection("Neobash.panel.settled")
        try {
            Log.d("NeobashPerf", "panel=$page requestToSettledFrameNs=$elapsed")
        } finally { Trace.endSection() }
        target = null
    }
}
