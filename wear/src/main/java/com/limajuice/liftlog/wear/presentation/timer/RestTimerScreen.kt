package com.limajuice.liftlog.wear.presentation.timer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.CompactChip
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import com.limajuice.liftlog.wear.data.WearWorkoutState
import com.limajuice.liftlog.wear.presentation.theme.LiftLogPrimary
import com.limajuice.liftlog.wear.presentation.theme.LiftLogSecondary
import kotlinx.coroutines.delay

@Composable
fun RestTimerScreen(
    state: WearWorkoutState,
    onSkipRest: () -> Unit,
    onAdjustRest: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val hapticManager = remember { HapticFeedbackManager(context) }

    var nowMs by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var warned3 by remember { mutableStateOf(false) }
    var warned2 by remember { mutableStateOf(false) }
    var warned1 by remember { mutableStateOf(false) }
    var finishedAlerted by remember { mutableStateOf(false) }

    val totalDurationMs = (state.restTimerEndMs - state.restTimerStartedMs).coerceAtLeast(1000L)
    val remainingMs = (state.restTimerEndMs - nowMs).coerceAtLeast(0L)
    val elapsedMs = (nowMs - state.restTimerStartedMs).coerceIn(0L, totalDurationMs)
    val progress = (elapsedMs.toFloat() / totalDurationMs.toFloat()).coerceIn(0f, 1f)

    val remainingSeconds = (remainingMs / 1000).toInt()
    val minutes = remainingSeconds / 60
    val seconds = remainingSeconds % 60
    val timeFormatted = String.format("%02d:%02d", minutes, seconds)

    // Ticker loop
    LaunchedEffect(state.restTimerEndMs) {
        warned3 = false
        warned2 = false
        warned1 = false
        finishedAlerted = false
        while (true) {
            nowMs = System.currentTimeMillis()
            val sec = ((state.restTimerEndMs - nowMs) / 1000).toInt()

            if (sec == 3 && !warned3) {
                warned3 = true
                hapticManager.playWarningPulse()
            } else if (sec == 2 && !warned2) {
                warned2 = true
                hapticManager.playWarningPulse()
            } else if (sec == 1 && !warned1) {
                warned1 = true
                hapticManager.playWarningPulse()
            } else if (sec <= 0 && !finishedAlerted) {
                finishedAlerted = true
                hapticManager.playFinishedAlert()
            }

            delay(250)
        }
    }

    CircularProgressRing(
        progress = progress,
        modifier = modifier.fillMaxSize(),
        ringColor = if (remainingSeconds > 0) LiftLogPrimary else LiftLogSecondary
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "REST",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f),
                letterSpacing = 1.sp
            )

            Text(
                text = timeFormatted,
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colors.onSurface,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Adjustment Row (-30s, +30s)
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CompactChip(
                    onClick = { onAdjustRest(-30) },
                    label = { Text("-30s", fontSize = 10.sp, fontWeight = FontWeight.Bold) }
                )
                Spacer(modifier = Modifier.width(6.dp))
                CompactChip(
                    onClick = { onAdjustRest(30) },
                    label = { Text("+30s", fontSize = 10.sp, fontWeight = FontWeight.Bold) }
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Skip Button
            Button(
                onClick = onSkipRest,
                colors = ButtonDefaults.primaryButtonColors(backgroundColor = MaterialTheme.colors.surface),
                modifier = Modifier.height(28.dp).width(90.dp)
            ) {
                Text(
                    text = "Skip",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colors.onSurface
                )
            }
        }
    }
}
