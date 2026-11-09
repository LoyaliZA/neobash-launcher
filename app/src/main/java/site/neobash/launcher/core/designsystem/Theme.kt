package site.neobash.launcher.core.designsystem

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Dark = darkColorScheme(primary = Color.White, onPrimary = Color.Black,
    background = Color.Black, onBackground = Color.White, surface = Color(0xFF101010),
    onSurface = Color.White, surfaceVariant = Color(0xFF242424), onSurfaceVariant = Color(0xFFBDBDBD), outline = Color(0xFF777777))
private val Light = lightColorScheme(primary = Color.Black, onPrimary = Color.White,
    background = Color.White, onBackground = Color.Black, surface = Color(0xFFF4F4F4),
    onSurface = Color.Black, surfaceVariant = Color(0xFFE0E0E0), onSurfaceVariant = Color(0xFF242424))
@Composable fun NeobashTheme(light: Boolean, content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = if (light) Light else Dark, content = content)
}
