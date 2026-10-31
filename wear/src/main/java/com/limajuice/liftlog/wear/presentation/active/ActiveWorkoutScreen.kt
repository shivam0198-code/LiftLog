package com.limajuice.liftlog.wear.presentation.active

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.CompactChip
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import com.limajuice.liftlog.wear.data.WearWorkoutState
import com.limajuice.liftlog.wear.presentation.theme.LiftLogBackground
import com.limajuice.liftlog.wear.presentation.theme.LiftLogPrimary

@Composable
fun ActiveWorkoutScreen(
    state: WearWorkoutState,
    onCompleteSet: (weight: Double, reps: Int) -> Unit,
    onNavigateSet: (delta: Int) -> Unit,
    onOpenSummary: () -> Unit,
    modifier: Modifier = Modifier
) {
    var editedWeight by remember(state.targetWeight, state.setIndex) {
        mutableDoubleStateOf(state.targetWeight)
    }
    var editedReps by remember(state.targetReps, state.setIndex) {
        mutableIntStateOf(state.targetReps)
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(LiftLogBackground)
            .verticalScroll(scrollState)
            .padding(horizontal = 12.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        // Exercise Name & Set Counter
        Text(
            text = state.exerciseName,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colors.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )

        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Set ${state.setIndex} of ${state.totalSets}",
                fontSize = 12.sp,
                color = MaterialTheme.colors.primary,
                fontWeight = FontWeight.SemiBold
            )

            state.liveHeartRateBpm?.let { bpm ->
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "❤️ $bpm",
                    fontSize = 11.sp,
                    color = MaterialTheme.colors.error,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Weight Stepper
        SetStepperControl(
            valueText = if (editedWeight % 1.0 == 0.0) editedWeight.toInt().toString() else "%.1f".format(editedWeight),
            labelText = state.weightUnit,
            onDecrement = {
                val step = state.weightIncrement.coerceAtLeast(0.5)
                editedWeight = (editedWeight - step).coerceAtLeast(0.0)
            },
            onIncrement = {
                val step = state.weightIncrement.coerceAtLeast(0.5)
                editedWeight += step
            },
            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
        )

        // Reps Stepper
        SetStepperControl(
            valueText = editedReps.toString(),
            labelText = "reps",
            onDecrement = {
                editedReps = (editedReps - 1).coerceAtLeast(1)
            },
            onIncrement = {
                editedReps += 1
            },
            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Complete Set Primary Button
        Chip(
            onClick = { onCompleteSet(editedWeight, editedReps) },
            label = {
                Text(
                    text = "✓ Complete Set",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            colors = ChipDefaults.chipColors(
                backgroundColor = LiftLogPrimary,
                contentColor = MaterialTheme.colors.onPrimary
            ),
            modifier = Modifier.fillMaxWidth().height(40.dp)
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Navigation & Summary row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            CompactChip(
                onClick = { onNavigateSet(-1) },
                label = { Text("◀ Prev", fontSize = 10.sp) },
                enabled = state.setIndex > 1
            )

            CompactChip(
                onClick = onOpenSummary,
                label = { Text("Done", fontSize = 10.sp, fontWeight = FontWeight.Bold) }
            )

            CompactChip(
                onClick = { onNavigateSet(1) },
                label = { Text("Next ▶", fontSize = 10.sp) },
                enabled = state.setIndex < state.totalSets
            )
        }
    }
}
