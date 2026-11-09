package com.graz.signalreseau.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.graz.signalreseau.ui.theme.DarkOutline
import com.graz.signalreseau.ui.theme.SignalCyan
import com.graz.signalreseau.ui.theme.SignalGreen
import com.graz.signalreseau.ui.theme.SignalOrange
import com.graz.signalreseau.ui.theme.SignalRed
import com.graz.signalreseau.ui.theme.SignalYellow
import com.graz.signalreseau.ui.theme.TextMuted
import com.graz.signalreseau.ui.theme.TextPrimary
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun SignalGauge(
    dbm: Int,
    percentage: Int,
    qualityLabel: String,
    qualityColorHex: Long,
    modifier: Modifier = Modifier,
    unitLabel: String = "dBm"
) {
    val animatedPercentage by animateFloatAsState(
        targetValue = percentage.toFloat(),
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "signal_percentage"
    )

    val activeColor = Color(qualityColorHex)

    Box(
        modifier = modifier.size(240.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(220.dp)) {
            val strokeWidth = 16.dp.toPx()
            val diameter = size.minDimension - strokeWidth
            val arcSize = Size(diameter, diameter)
            val topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f)

            val startAngle = 140f
            val maxSweepAngle = 260f

            // Background track
            drawArc(
                color = DarkOutline.copy(alpha = 0.5f),
                startAngle = startAngle,
                sweepAngle = maxSweepAngle,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Dynamic progress sweep
            val progressSweep = (animatedPercentage / 100f) * maxSweepAngle
            if (progressSweep > 0f) {
                val gaugeBrush = Brush.sweepGradient(
                    0.0f to SignalRed,
                    0.25f to SignalOrange,
                    0.5f to SignalYellow,
                    0.75f to SignalGreen,
                    1.0f to SignalCyan
                )

                drawArc(
                    brush = gaugeBrush,
                    startAngle = startAngle,
                    sweepAngle = progressSweep,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }

            // Outer Tick marks
            val radius = diameter / 2f + 14.dp.toPx()
            val centerOffset = Offset(size.width / 2f, size.height / 2f)

            for (i in 0..10) {
                val fraction = i / 10f
                val angleDeg = startAngle + fraction * maxSweepAngle
                val angleRad = Math.toRadians(angleDeg.toDouble())

                val startR = radius - 8.dp.toPx()
                val endR = radius

                val p1 = Offset(
                    (centerOffset.x + startR * cos(angleRad)).toFloat(),
                    (centerOffset.y + startR * sin(angleRad)).toFloat()
                )
                val p2 = Offset(
                    (centerOffset.x + endR * cos(angleRad)).toFloat(),
                    (centerOffset.y + endR * sin(angleRad)).toFloat()
                )

                val tickColor = if (fraction <= (animatedPercentage / 100f)) activeColor else DarkOutline
                drawLine(
                    color = tickColor,
                    start = p1,
                    end = p2,
                    strokeWidth = 2.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
        }

        // Center readout
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text(
                text = if (dbm > -130) "$dbm" else "--",
                fontSize = 42.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = TextPrimary
            )
            Text(
                text = unitLabel,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = TextMuted
            )
            Spacer(modifier = Modifier.height(6.dp))

            // Percentage & Label Pill
            Box(
                modifier = Modifier
                    .background(
                        color = activeColor.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "$percentage% • $qualityLabel",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = activeColor
                )
            }
        }
    }
}
