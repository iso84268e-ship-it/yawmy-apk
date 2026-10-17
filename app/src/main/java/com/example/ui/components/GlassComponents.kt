package com.example.ui.components

import android.os.Build
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BgGradientEnd
import com.example.ui.theme.BgGradientMid
import com.example.ui.theme.BgGradientStart
import com.example.ui.theme.GlassBorder70
import com.example.ui.theme.GlassInk82
import com.example.ui.theme.GlassWhite34
import com.example.ui.theme.InkPrimary
import com.example.ui.theme.RadialHighlightBottom
import com.example.ui.theme.RadialHighlightTop
import kotlin.math.cos
import kotlin.math.sin

/**
 * Top-level background with 165° linear gradient and radial lighting highlights.
 */
@Composable
fun YawmiBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(BgGradientStart, BgGradientMid, BgGradientEnd),
                    start = Offset(0f, 0f),
                    end = Offset(
                        x = cos(Math.toRadians(165.0)).toFloat() * 1200f + 600f,
                        y = sin(Math.toRadians(165.0)).toFloat() * 2400f + 1200f
                    )
                )
            )
    ) {
        // Radial highlight top-right
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(RadialHighlightTop.copy(alpha = 0.45f), Color.Transparent),
                        center = Offset(800f, 150f),
                        radius = 900f
                    )
                )
        )
        // Radial highlight bottom-left
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(RadialHighlightBottom.copy(alpha = 0.4f), Color.Transparent),
                        center = Offset(100f, 1800f),
                        radius = 800f
                    )
                )
        )

        content()
    }
}

/**
 * Standard Glass Card: White 55%, border White 70% 1dp, corner radius 24dp.
 * Does not blur children so text and icons remain 100% sharp and legible.
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(24.dp),
    backgroundColor: Color = com.example.ui.theme.GlassWhite55,
    borderColor: Color = GlassBorder70,
    borderWidth: Dp = 1.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val clickModifier = if (onClick != null) {
        Modifier.clickable(onClick = onClick)
    } else Modifier

    Box(
        modifier = modifier
            .clip(shape)
            .border(BorderStroke(borderWidth, borderColor), shape)
            .background(backgroundColor, shape)
            .then(clickModifier),
        content = content
    )
}

/**
 * Dark Ink Glass Card: #1B2340 with 82% opacity, 24dp corners.
 */
@Composable
fun InkGlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(24.dp),
    borderColor: Color = Color(0x33FFFFFF),
    borderWidth: Dp = 1.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val clickModifier = if (onClick != null) {
        Modifier.clickable(onClick = onClick)
    } else Modifier

    Box(
        modifier = modifier
            .clip(shape)
            .border(BorderStroke(borderWidth, borderColor), shape)
            .background(GlassInk82, shape)
            .then(clickModifier),
        content = content
    )
}

/**
 * Full-width or compact Ink action button with minimum 48dp height.
 */
@Composable
fun PrimaryInkButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: (@Composable () -> Unit)? = null
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = CircleShape,
        color = if (enabled) InkPrimary else InkPrimary.copy(alpha = 0.5f),
        contentColor = Color.White,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 14.dp)
        ) {
            androidx.compose.foundation.layout.Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Center
            ) {
                if (leadingIcon != null) {
                    leadingIcon()
                    androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(end = 8.dp))
                }
                Text(
                    text = text,
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
                )
            }
        }
    }
}

/**
 * Rounded fully circular activity chips.
 */
@Composable
fun ActivityChip(
    text: String,
    color: Color,
    selected: Boolean = false,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val chipModifier = if (onClick != null) {
        modifier.clickable(onClick = onClick)
    } else modifier

    val background = if (selected) color else color.copy(alpha = 0.65f)
    val border = if (selected) BorderStroke(1.5.dp, InkPrimary) else BorderStroke(1.dp, Color(0x40FFFFFF))

    Box(
        modifier = chipModifier
            .clip(CircleShape)
            .border(border, CircleShape)
            .background(background, CircleShape)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge.copy(
                color = InkPrimary,
                fontWeight = FontWeight.Bold
            )
        )
    }
}
