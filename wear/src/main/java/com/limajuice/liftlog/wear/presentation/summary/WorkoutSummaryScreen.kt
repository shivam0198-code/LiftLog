package com.limajuice.liftlog.wear.presentation.summary

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.CompactChip
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import com.limajuice.liftlog.wear.data.WearWorkoutState
import com.limajuice.liftlog.wear.presentation.theme.LiftLogBackground
import com.limajuice.liftlog.wear.presentation.theme.LiftLogError

@Composable
fun WorkoutSummaryScreen(
    state: WearWorkoutState,
    onConfirmFinish: () -> Unit,
    onBackToWorkout: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(LiftLogBackground)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Workout Summary",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colors.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Total Volume",
            fontSize = 11.sp,
            color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
        )
        Text(
            text = state.totalWeightLiftedFormatted,
            fontSize = 16.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colors.primary
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Duration",
            fontSize = 11.sp,
            color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
        )
        Text(
            text = state.workoutDurationFormatted,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colors.onSurface
        )

        Spacer(modifier = Modifier.height(10.dp))

        Chip(
            onClick = onConfirmFinish,
            label = {
                Text(
                    text = "Finish Workout",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            colors = ChipDefaults.chipColors(
                backgroundColor = LiftLogError,
                contentColor = MaterialTheme.colors.onPrimary
            ),
            modifier = Modifier.fillMaxWidth().height(36.dp)
        )

        Spacer(modifier = Modifier.height(4.dp))

        CompactChip(
            onClick = onBackToWorkout,
            label = { Text("Resume", fontSize = 11.sp) }
        )
    }
}
