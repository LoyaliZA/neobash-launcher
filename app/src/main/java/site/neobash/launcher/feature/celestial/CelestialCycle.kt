package site.neobash.launcher.feature.celestial

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.snap
import androidx.compose.runtime.getValue
import site.neobash.launcher.core.designsystem.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import site.neobash.launcher.core.model.*
import java.time.LocalTime
import kotlin.math.*

@Composable fun CelestialCycle(data: CelestialData, time: LocalTime, mode: SkyMode, arcHeight: Dp = Dp.Unspecified, blockSize: BlockSize = BlockSize.MEDIUM) {
    val baseHeight = if (arcHeight == Dp.Unspecified) 132.dp else arcHeight
    val height by animateDpAsState(when (blockSize) {
        BlockSize.COMPACT -> baseHeight * .72f
        BlockSize.MEDIUM -> baseHeight
        BlockSize.EXPANDED -> baseHeight * 1.45f
    }, if (LocalReducedMotion.current) snap() else NeobashMotion.spatial(), label = "celestial size")
    val ink = MaterialTheme.colorScheme.onBackground
    val muted = MaterialTheme.colorScheme.outline
    val displayTime = when (mode) {
        SkyMode.AUTO -> time
        SkyMode.DAY -> LocalTime.of(14, 0)
        SkyMode.NIGHT -> LocalTime.of(21, 0)
    }
    val sun = data.sun.transitProgress(displayTime)
    val moon = data.moon.transitProgress(displayTime)
    val provenance = if (data.source == CelestialSource.SIMULATED) "Astronomía simulada · sin ubicación" else "Astronomía verificada"
    val description = "$provenance. " +
        (if (sun != null) "Sol sobre el horizonte. " else "Sol fuera del arco. ") +
        (if (moon != null) "Luna sobre el horizonte, iluminación ${(data.illuminatedFraction * 100).roundToInt()} %." else "Luna fuera del arco.")
    Column(Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Text("CICLO CELESTE", style = MaterialTheme.typography.labelMedium, color = muted)
        Canvas(Modifier.fillMaxWidth().height(height).padding(16.dp).semantics { contentDescription = description }) {
            // Same radius on both axes: never stretch the semicircle into an ellipse.
            val radius = min(size.width / 2, size.height - 12.dp.toPx()).coerceAtLeast(0f)
            val center = Offset(size.width / 2, size.height - 2.dp.toPx())
            drawArc(muted.copy(alpha = .5f), 180f, 180f, false,
                Offset(center.x - radius, center.y - radius), Size(radius * 2, radius * 2), style = Stroke(1.dp.toPx()))
            drawLine(muted.copy(alpha = .2f), Offset(center.x - radius, center.y), Offset(center.x + radius, center.y))
            fun point(progress: Float): Offset = semicirclePoint(progress).let { center + Offset(it.x * radius, it.y * radius) }
            sun?.let {
                val p = point(it)
                drawCircle(ink.copy(alpha = .06f), 12.dp.toPx(), p)
                drawCircle(ink.copy(alpha = .12f), 8.dp.toPx(), p)
                drawCircle(ink, 3.dp.toPx(), p)
            }
            moon?.let {
                val p = point(it)
                val r = 10.dp.toPx()
                drawCircle(muted.copy(alpha = .25f), r, p)
                val fraction = data.illuminatedFraction.toFloat()
                val side = if (data.phase.mod(1.0) <= .5) 1f else -1f
                rotate(data.lunarOrientationDegrees ?: 0f, p) {
                    val path = Path()
                    for (i in 0..64) {
                        val y = -r + 2 * r * i / 64
                        val x = sqrt((r*r - y*y).coerceAtLeast(0f)) * side
                        if (i == 0) path.moveTo(p.x+x, p.y+y) else path.lineTo(p.x+x, p.y+y)
                    }
                    for (i in 64 downTo 0) {
                        val y = -r + 2*r*i/64
                        val x = sqrt((r*r-y*y).coerceAtLeast(0f)) * (1-2*fraction) * side
                        path.lineTo(p.x+x, p.y+y)
                    }
                    path.close()
                    drawPath(path, ink)
                }
                drawCircle(muted.copy(alpha = .5f), r, p, style = Stroke(.5.dp.toPx()))
            }
        }
        Text("Sol ↑ ${data.sun.rise ?: "—"} · ↓ ${data.sun.set ?: "—"}", style = MaterialTheme.typography.bodySmall)
        Text("Luna ↑ ${data.moon.rise ?: "—"} · ↓ ${data.moon.set ?: "—"} · ${(data.illuminatedFraction * 100).roundToInt()} %", style = MaterialTheme.typography.bodySmall)
        Text(provenance, style = MaterialTheme.typography.labelSmall, color = muted)
        if (mode != SkyMode.AUTO) Text("Vista de demostración · ${displayTime}", style = MaterialTheme.typography.labelSmall, color = muted)
    }
}
