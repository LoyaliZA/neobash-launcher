package site.neobash.launcher.core.designsystem

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween

/** One finite panel movement. Clickable/Material controls provide bounded press ripples. */
object NeobashMotion {
    const val PanelDurationMillis = 240
    fun panel() = tween<Float>(PanelDurationMillis, easing = FastOutSlowInEasing)
}
