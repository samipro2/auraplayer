package com.sunsoft.aura.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private fun scheme(amoled: Boolean) = darkColorScheme(
    primary = AuraAccent,
    onPrimary = AuraBackground,
    secondary = AuraGlowTeal,
    onSecondary = AuraBackground,
    background = if (amoled) AuraBackgroundAmoled else AuraBackground,
    onBackground = AuraTextPrimary,
    surface = AuraSurface,
    onSurface = AuraTextPrimary,
    surfaceVariant = AuraSurfaceVariant,
    onSurfaceVariant = AuraTextSecondary,
    outline = AuraDivider,
    error = AuraError,
    onError = AuraTextPrimary
)

@Composable
fun AuraTheme(amoledBlack: Boolean = false, content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = scheme(amoledBlack),
        typography = AuraTypography,
        content = content
    )
}
