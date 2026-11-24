package com.sunsoft.aura.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.sunsoft.aura.ui.theme.AuraSurface

/**
 * A frosted-glass-looking panel. It doesn't blur what's behind it (that's blurred
 * separately, e.g. the backdrop art image) - this just supplies the tinted,
 * faintly-lit "glass" fill and edge highlight that sits on top of it.
 */
@Composable
fun GlassSurface(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(28.dp),
    tint: Color = AuraSurface,
    fillAlpha: Float = 0.55f,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    listOf(
                        tint.copy(alpha = fillAlpha + 0.08f),
                        tint.copy(alpha = fillAlpha)
                    )
                )
            )
            .border(1.dp, Color.White.copy(alpha = 0.10f), shape)
    ) {
        content()
    }
}
