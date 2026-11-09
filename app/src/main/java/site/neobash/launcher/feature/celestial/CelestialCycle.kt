package site.neobash.launcher.feature.celestial

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import site.neobash.launcher.core.model.CelestialData
import java.time.LocalTime
import kotlin.math.*

@Composable fun CelestialCycle(data: CelestialData, time: LocalTime, day: Boolean) {
    val ink = MaterialTheme.colorScheme.onBackground
    val muted = MaterialTheme.colorScheme.outline
    Column(Modifier.fillMaxWidth().padding(vertical = 24.dp)) {
        Text(if (day) "TRAYECTORIA SOLAR" else "FASE LUNAR", style = MaterialTheme.typography.labelMedium, color = muted)
        Canvas(Modifier.fillMaxWidth().height(160.dp).padding(top = 20.dp)) {
            if (day) {
                val left = size.width * .08f
                val width = size.width * .84f
                val height = size.height * .8f
                drawArc(muted, 180f, 180f, false, Offset(left, size.height - height), Size(width, height * 2), style = Stroke(1.dp.toPx()))
                drawLine(muted.copy(alpha = .4f), Offset(left, size.height), Offset(left + width, size.height))
                val angle = PI * (1 - data.solarProgress(time))
                val point = Offset(left + width / 2 + width / 2 * cos(angle).toFloat(), size.height - height * sin(angle).toFloat())
                drawCircle(ink.copy(alpha = .08f), 14.dp.toPx(), point)
                drawCircle(ink, 5.dp.toPx(), point)
            } else {
                val r = min(size.width, size.height) * .38f
                val center = Offset(size.width / 2, size.height / 2)
                drawCircle(muted.copy(alpha = .2f), r, center)
                // Visible disk: limb plus elliptical terminator, waxing lit on right.
                val phase = data.phase.mod(1.0)
                val fraction = data.illuminatedFraction.toFloat()
                val side = if (phase <= .5) 1f else -1f
                val path = Path()
                for (i in 0..100) {
                    val y = -r + 2 * r * i / 100
                    val x = sqrt((r*r - y*y).coerceAtLeast(0f))
                    if (i == 0) path.moveTo(center.x + side*x, center.y+y) else path.lineTo(center.x+side*x, center.y+y)
                }
                for (i in 100 downTo 0) {
                    val y = -r + 2*r*i/100
                    val x = sqrt((r*r-y*y).coerceAtLeast(0f)) * (1-2*fraction) * side
                    path.lineTo(center.x+x, center.y+y)
                }
                path.close()
                drawPath(path, ink)
            }
        }
        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(if (day) "↑ ${data.sunrise}" else "Iluminación ${(data.illuminatedFraction * 100).roundToInt()} %", style = MaterialTheme.typography.bodySmall)
            Text(if (day) "${data.sunset} ↓" else "Creciente · simulada", style = MaterialTheme.typography.bodySmall)
        }
        Text("Astronomía simulada · sin ubicación", style = MaterialTheme.typography.labelSmall, color = muted)
    }
}
