package site.neobash.launcher.core.designsystem

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer

/** System duration scaling is also applied by Compose's animation clock. */
val LocalReducedMotion = staticCompositionLocalOf { false }
object NeobashMotion {
    fun panel() = spring<Float>(dampingRatio = 1f, stiffness = 400f)
    fun <T> spatial() = spring<T>(dampingRatio = 1f, stiffness = 500f)
    fun enter(reduced: Boolean): EnterTransition = if (reduced) EnterTransition.None
        else fadeIn(tween(140)) + slideInVertically(spatial()) { it / 24 }
    fun exit(reduced: Boolean): ExitTransition = if (reduced) ExitTransition.None
        else fadeOut(tween(100))
}

/** The click is dispatched immediately; release/cancel returns the same spring to rest. */
fun Modifier.fluidClickable(onClick: () -> Unit): Modifier = composed {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val reduced = LocalReducedMotion.current
    val scale by animateFloatAsState(if (pressed && !reduced) .97f else 1f,
        if (reduced) snap() else NeobashMotion.spatial(), label = "press")
    graphicsLayer { scaleX = scale; scaleY = scale }
        .clickable(interactionSource = interaction, indication = androidx.compose.material3.ripple(), onClick = onClick)
}

/** Observe runtime changes too, including returning from Android's developer settings. */
@Composable
fun rememberReducedMotion(preference: Boolean): Boolean {
    val resolver = androidx.compose.ui.platform.LocalContext.current.contentResolver
    fun disabled() = android.provider.Settings.Global.getFloat(resolver,
        android.provider.Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f
    var systemDisabled by remember(resolver) { mutableStateOf(disabled()) }
    DisposableEffect(resolver) {
        val observer = object : android.database.ContentObserver(android.os.Handler(android.os.Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean) { systemDisabled = disabled() }
        }
        resolver.registerContentObserver(android.provider.Settings.Global.getUriFor(
            android.provider.Settings.Global.ANIMATOR_DURATION_SCALE), false, observer)
        onDispose { resolver.unregisterContentObserver(observer) }
    }
    return preference || systemDisabled
}
