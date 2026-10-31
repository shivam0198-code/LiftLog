package com.limajuice.liftlog.wear.presentation.timer

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.limajuice.liftlog.wear.presentation.theme.LiftLogPrimary
import com.limajuice.liftlog.wear.presentation.theme.LiftLogSurface

@Composable
fun CircularProgressRing(
    progress: Float, // 0.0f to 1.0f
    modifier: Modifier = Modifier,
    ringColor: Color = LiftLogPrimary,
    trackColor: Color = LiftLogSurface,
    strokeWidth: Float = 14f,
    content: @Composable () -> Unit = {}
) {
    Box(
        modifier = modifier.fillMaxSize().padding(6.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Background track
            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = strokeWidth)
            )

            // Foreground progress arc
            drawArc(
                color = ringColor,
                startAngle = -90f,
                sweepAngle = (progress.coerceIn(0f, 1f)) * 360f,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }

        content()
    }
}
