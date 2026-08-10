package com.example.vultrmanager.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.vultrmanager.data.remote.model.BandwidthData

/**
 * Lightweight bandwidth line chart drawn with Compose Canvas (no chart library dependency).
 * Plots the `incoming` and `outgoing` samples (each [unixSeconds, bytes]) as two lines.
 */
@Composable
fun BandwidthChart(bandwidth: BandwidthData, modifier: Modifier = Modifier) {
    val incoming = bandwidth.incoming.orEmpty()
    val outgoing = bandwidth.outgoing.orEmpty()

    val inPts = incoming.mapNotNull { if (it.size >= 2) it[0].toFloat() to it[1].toFloat() else null }
    val outPts = outgoing.mapNotNull { if (it.size >= 2) it[0].toFloat() to it[1].toFloat() else null }

    val primary = MaterialTheme.colorScheme.primary
    val tertiary = MaterialTheme.colorScheme.tertiary

    val hasData = inPts.size >= 2 || outPts.size >= 2

    if (!hasData) {
        Text(
            "暂无带宽数据",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = modifier.padding(vertical = 8.dp)
        )
        return
    }

    Canvas(modifier = modifier.fillMaxWidth().height(160.dp).padding(8.dp)) {
        val w = size.width
        val h = size.height
        val pad = 8.dp.toPx()

        val allX = (inPts + outPts).map { it.first }
        val allY = (inPts + outPts).map { it.second }
        val minX = allX.minOrNull() ?: 0f
        val maxX = allX.maxOrNull() ?: 1f
        val maxY = (allY.maxOrNull() ?: 1f).coerceAtLeast(1f)
        val xRange = if (maxX - minX == 0f) 1f else maxX - minX

        fun toOffset(pt: Pair<Float, Float>): Offset {
            val x = pad + (pt.first - minX) / xRange * (w - 2 * pad)
            val y = h - pad - (pt.second / maxY) * (h - 2 * pad)
            return Offset(x, y)
        }

        fun drawSeries(pts: List<Pair<Float, Float>>, color: Color) {
            if (pts.size < 2) return
            val path = Path()
            pts.forEachIndexed { i, pt ->
                val o = toOffset(pt)
                if (i == 0) path.moveTo(o.x, o.y) else path.lineTo(o.x, o.y)
            }
            drawPath(path, color, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round))
        }

        drawSeries(inPts, primary)
        drawSeries(outPts, tertiary)
    }
}
