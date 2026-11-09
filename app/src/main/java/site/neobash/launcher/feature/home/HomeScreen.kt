package site.neobash.launcher.feature.home

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onLongClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import site.neobash.launcher.core.model.*
import site.neobash.launcher.feature.celestial.CelestialCycle
import site.neobash.launcher.feature.settings.FavoriteLabels
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable fun HomeScreen(state: HomeState, celestial: CelestialData, mode: SkyMode,
    onApps: () -> Unit, onContextMenu: () -> Unit, onFavorite: (Int) -> Unit) {
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val windowWidth = maxWidth
        val windowHeight = maxHeight
        val wide = windowWidth >= 600.dp
        val compact = maxHeight < 780.dp
        val padding = if (wide) 32.dp else 16.dp
        val gap = if (compact) 8.dp else 16.dp
        val fontScale = androidx.compose.ui.platform.LocalDensity.current.fontScale
        val scroll = rememberScrollState()
        // Measure natural content, then enable scrolling only on actual overflow. Text scales
        // normally, so large fonts and short/multiwindow screens retain every control.
        Column(Modifier.fillMaxSize().verticalScroll(scroll, enabled = scroll.maxValue > 0)
            .padding(horizontal = padding, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(gap)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                FacetedN()
                IconButton(onClick = onContextMenu) {
                    Text("⚙", modifier = Modifier.semantics { contentDescription = "Opciones del escritorio y navegación" }, fontSize = 22.sp)
                }
            }
            if (wide) Row(horizontalArrangement = Arrangement.spacedBy(32.dp)) {
                Column(Modifier.weight(1f)) { ClockHeader(state, compact) }
                Column(Modifier.weight(1f)) { CelestialCycle(celestial, state.now.toLocalTime(), mode,
                    arcHeight = (windowHeight * .2f).coerceIn(88.dp, 156.dp)) }
            } else {
                ClockHeader(state, compact)
                CelestialCycle(celestial, state.now.toLocalTime(), mode,
                    arcHeight = (windowHeight * .17f).coerceIn(88.dp, 156.dp))
            }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("TU ESPACIO", color = MaterialTheme.colorScheme.outline, style = MaterialTheme.typography.labelMedium)
                val columns = if (windowWidth - padding * 2 < 320.dp || fontScale > 1.3f) 2 else 4
                FavoriteLabels.indices.chunked(columns).forEach { indices ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        indices.forEach { index ->
                            val id = state.preferences.favorites[index]
                            val app = state.apps.find { it.id == id }
                            Surface(Modifier.weight(1f).heightIn(min = 72.dp).clickable { onFavorite(index) },
                                shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.surface) {
                                Column(Modifier.padding(horizontal = 8.dp, vertical = 10.dp)) {
                                    FavoriteMark(index)
                                    Spacer(Modifier.height(6.dp))
                                    Text(app?.label ?: FavoriteLabels[index], style = MaterialTheme.typography.labelLarge,
                                        maxLines = 2, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                                    if (app == null) Text(if (id == null) "Elegir app" else "No disponible", style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }
            Box(Modifier.fillMaxWidth().height(48.dp)
                .semantics {
                    contentDescription = "Zona vacía del escritorio"
                    onLongClick("Abrir opciones del escritorio") { onContextMenu(); true }
                }
                .pointerInput(onContextMenu) { detectTapGestures(onLongPress = { onContextMenu() }) }
                .pointerInput(onApps) {
                    var drag = 0f
                    detectVerticalDragGestures(
                        onDragStart = { drag = 0f },
                        onVerticalDrag = { change, amount -> change.consume(); drag += amount },
                        onDragEnd = { if (drag < -32.dp.toPx()) onApps() },
                        onDragCancel = { drag = 0f })
                })
            Surface(Modifier.fillMaxWidth().heightIn(min = 48.dp).clickable(onClick = onApps),
                shape = MaterialTheme.shapes.extraLarge, color = MaterialTheme.colorScheme.surfaceVariant) {
                Row(Modifier.padding(horizontal = 18.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    Text("⌕")
                    Text("Buscar aplicaciones", Modifier.weight(1f))
                    Text("↑", style = MaterialTheme.typography.titleLarge)
                }
            }
        }
    }
}
@Composable private fun ClockHeader(state: HomeState, compact: Boolean) {
    BoxWithConstraints {
        val fontScale = androidx.compose.ui.platform.LocalDensity.current.fontScale
        val clockSize = minOf(if (compact) 64f else 76f, maxWidth.value / (2.8f * fontScale)).sp
        Column {
        Text(state.now.format(DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM", Locale.forLanguageTag("es"))),
            style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(state.now.format(DateTimeFormatter.ofPattern("HH:mm")), fontSize = clockSize, maxLines = 1, softWrap = false,
                fontWeight = FontWeight.Thin, letterSpacing = (-3).sp)
            Column(Modifier.padding(top = 8.dp)) {
                Text("☀ 24°", style = MaterialTheme.typography.titleMedium)
                Text("UV 3 · Moderado", style = MaterialTheme.typography.bodySmall)
                Text("Clima simulado", style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        }
    }
}
@Composable private fun FavoriteMark(index: Int) {
    val ink = MaterialTheme.colorScheme.onSurface
    Canvas(Modifier.size(24.dp)) {
        val s = size.width
        when (index) {
            0 -> { drawLine(ink, androidx.compose.ui.geometry.Offset(0f,s), androidx.compose.ui.geometry.Offset(0f,0f), 2f); drawLine(ink, androidx.compose.ui.geometry.Offset(0f,0f), androidx.compose.ui.geometry.Offset(s,s),2f); drawLine(ink, androidx.compose.ui.geometry.Offset(s,s), androidx.compose.ui.geometry.Offset(s,0f),2f) }
            1 -> { drawRect(ink, style = androidx.compose.ui.graphics.drawscope.Stroke(2f)); drawLine(ink, androidx.compose.ui.geometry.Offset(s*.2f,s*.4f), androidx.compose.ui.geometry.Offset(s*.8f,s*.4f),2f) }
            2 -> { drawLine(ink, androidx.compose.ui.geometry.Offset(0f,s*.5f), androidx.compose.ui.geometry.Offset(s*.4f,s),2f); drawLine(ink, androidx.compose.ui.geometry.Offset(s*.4f,s), androidx.compose.ui.geometry.Offset(s,0f),2f) }
            else -> { drawRect(ink, style = androidx.compose.ui.graphics.drawscope.Stroke(2f)); drawLine(ink, androidx.compose.ui.geometry.Offset(0f,s*.3f), androidx.compose.ui.geometry.Offset(s,s*.3f),2f) }
        }
    }
}
@Composable private fun FacetedN() {
    val ink = MaterialTheme.colorScheme.onSurface
    val shimmer = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        shimmer.animateTo(1f, tween(220))
        shimmer.animateTo(0f, tween(320))
    }
    Canvas(Modifier.size(24.dp).semantics { contentDescription = "Logotipo de Neobash" }) {
        val s = size.width
        fun triangle(ax: Float, ay: Float, bx: Float, by: Float, cx: Float, cy: Float, alpha: Float) {
            drawPath(Path().apply { moveTo(ax*s,ay*s); lineTo(bx*s,by*s); lineTo(cx*s,cy*s); close() }, ink.copy(alpha=alpha))
        }
        triangle(0f,1f,0f,0f,.4f,1f,1f)
        triangle(0f,0f,1f,1f,.6f,0f,.5f + .5f*shimmer.value)
        triangle(.6f,0f,1f,0f,1f,1f,1f)
    }
}
