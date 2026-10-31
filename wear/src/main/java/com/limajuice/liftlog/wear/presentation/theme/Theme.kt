package com.limajuice.liftlog.wear.presentation.theme

import androidx.compose.runtime.Composable
import androidx.wear.compose.material.Colors
import androidx.wear.compose.material.MaterialTheme

val WearColorPalette = Colors(
    primary = LiftLogPrimary,
    primaryVariant = LiftLogPrimaryVariant,
    secondary = LiftLogSecondary,
    background = LiftLogBackground,
    surface = LiftLogSurface,
    onPrimary = LiftLogOnPrimary,
    onSecondary = LiftLogBackground,
    onBackground = LiftLogOnSurface,
    onSurface = LiftLogOnSurface,
    onError = LiftLogOnPrimary,
    error = LiftLogError
)

@Composable
fun LiftLogWearTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colors = WearColorPalette,
        content = content
    )
}
