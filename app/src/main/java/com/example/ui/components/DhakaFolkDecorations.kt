package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.JaminiTerracotta
import com.example.ui.theme.JaminiTerracottaContainer
import com.example.ui.theme.RickshawCobalt
import com.example.ui.theme.RickshawMagenta
import com.example.ui.theme.RickshawTeal
import com.example.ui.theme.StageAmber
import com.example.ui.theme.StageCharcoal
import com.example.ui.theme.StageEmerald
import com.example.ui.theme.StageOffWhite

/**
 * Hand-crafted Dhaka Rickshaw Paint geometric ribbon inspired by rickshaw hood trim (jhalor)
 * and Jamini Roy patachitra borders. Designed to stay crisp, vibrant, and non-distracting.
 */
@Composable
fun RickshawPaintRibbon(
    modifier: Modifier = Modifier,
    height: Dp = 5.dp
) {
    val palette = listOf(
        StageAmber,
        JaminiTerracotta,
        RickshawMagenta,
        RickshawTeal,
        StageEmerald,
        RickshawCobalt
    )
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
    ) {
        val segmentWidth = 18.dp.toPx()
        val count = (size.width / segmentWidth).toInt().coerceAtLeast(1) + 1
        drawRect(color = StageCharcoal)
        for (i in 0 until count) {
            val left = i * segmentWidth
            val right = left + segmentWidth
            val color = palette[i % palette.size]
            val path = Path().apply {
                moveTo(left, size.height)
                lineTo(left + segmentWidth * 0.5f, 0f)
                lineTo(right, size.height)
                close()
            }
            drawPath(path = path, color = color)
            // Alternating inverted diamond accent in next color
            val nextColor = palette[(i + 2) % palette.size]
            val topPath = Path().apply {
                moveTo(left, 0f)
                lineTo(left + segmentWidth * 0.5f, size.height)
                lineTo(left, size.height)
                close()
            }
            drawPath(path = topPath, color = nextColor.copy(alpha = 0.85f))
            // Crisp Jamini kohl divider dot
            drawCircle(
                color = StageOffWhite.copy(alpha = 0.85f),
                radius = (size.height * 0.16f).coerceAtLeast(1.2f),
                center = Offset(left + segmentWidth * 0.5f, size.height * 0.6f)
            )
        }
    }
}

/**
 * Subtle Jamini Roy folk-modernist & alpona lotus arch motif for screen headers.
 * Kept at low opacity so foreground typography and controls stay highly legible for everyone.
 */
@Composable
fun JaminiRoyLotusHeaderBackdrop(
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val center = Offset(w * 0.88f, h * 0.52f)
        val outerRadius = h * 0.88f

        // Warm terracotta sun halo
        drawCircle(
            color = JaminiTerracotta.copy(alpha = 0.16f),
            radius = outerRadius,
            center = center
        )
        // Bold Jamini black-ink & marigold ring
        drawCircle(
            color = StageAmber.copy(alpha = 0.24f),
            radius = outerRadius * 0.72f,
            center = center,
            style = Stroke(width = 2.dp.toPx())
        )
        // Peacock teal inner arch
        drawArc(
            color = RickshawTeal.copy(alpha = 0.25f),
            startAngle = 140f,
            sweepAngle = 220f,
            useCenter = false,
            topLeft = Offset(center.x - outerRadius * 0.48f, center.y - outerRadius * 0.48f),
            size = Size(outerRadius * 0.96f, outerRadius * 0.96f),
            style = Stroke(width = 2.5.dp.toPx())
        )
        // Folk alpona scalloped border dots along the top-right arc
        val dotRadius = 2.2.dp.toPx()
        for (i in 0..5) {
            val x = w * (0.64f + i * 0.055f)
            val y = h * (0.18f + (i % 2) * 0.10f)
            drawCircle(
                color = if (i % 2 == 0) StageAmber.copy(alpha = 0.35f) else RickshawMagenta.copy(alpha = 0.3f),
                radius = dotRadius,
                center = Offset(x, y)
            )
        }
    }
}

/**
 * Compact pill identifying the Dhaka-based community & BDT pricing.
 */
@Composable
fun DhakaHeritagePill(
    text: String = "DHAKA · ৳ BDT",
    modifier: Modifier = Modifier
) {
    Surface(
        color = JaminiTerracottaContainer,
        shape = RoundedCornerShape(999.dp),
        modifier = modifier.border(1.dp, JaminiTerracotta.copy(alpha = 0.7f), RoundedCornerShape(999.dp))
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(StageAmber)
            )
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                color = StageOffWhite,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
