package com.paydock.sample.feature.widgets.ui.components.standalone3ds

import android.provider.Settings
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

internal val SuccessGreen = Color(0xFF2E7D32)

private const val CIRCLE_DRAW_MS = 450
private const val CHECK_DRAW_MS = 300
private const val POP_MS = 320
private const val POP_PEAK_MS = 140
private const val POP_PEAK_SCALE = 1.12f
private const val FULL_SWEEP_DEGREES = 360f
private const val START_ANGLE_DEGREES = -90f
private const val STROKE_FRACTION = 0.07f

// Checkmark points as fractions of the mark size (same shape as the web playground's `M15 27 l7 7 l15 -16` on 52)
private const val CHECK_START_X = 15f / 52f
private const val CHECK_START_Y = 27f / 52f
private const val CHECK_MID_X = 22f / 52f
private const val CHECK_MID_Y = 34f / 52f
private const val CHECK_END_X = 37f / 52f
private const val CHECK_END_Y = 18f / 52f

/**
 * `true` when the user turned animations off (Developer options / accessibility "Remove animations").
 */
@Composable
internal fun rememberReduceMotion(): Boolean {
    val resolver = LocalContext.current.contentResolver
    return remember(resolver) {
        Settings.Global.getFloat(resolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f
    }
}

/**
 * Success checkmark that draws its circle, then its tick, then "pops" — mirrors the web playground's
 * `paid-draw` / `paid-pop` animation. Renders the final state immediately when motion is reduced.
 */
@Composable
internal fun Standalone3DSSuccessMark(
    modifier: Modifier = Modifier,
    size: Dp = 72.dp,
    color: Color = SuccessGreen,
) {
    val reduceMotion = rememberReduceMotion()
    val circle = remember { Animatable(if (reduceMotion) 1f else 0f) }
    val check = remember { Animatable(if (reduceMotion) 1f else 0f) }
    val scale = remember { Animatable(1f) }

    LaunchedEffect(reduceMotion) {
        if (reduceMotion) return@LaunchedEffect
        circle.animateTo(1f, tween(CIRCLE_DRAW_MS, easing = FastOutSlowInEasing))
        check.animateTo(1f, tween(CHECK_DRAW_MS, easing = FastOutSlowInEasing))
        scale.animateTo(
            1f,
            keyframes {
                durationMillis = POP_MS
                POP_PEAK_SCALE at POP_PEAK_MS
            }
        )
    }

    Canvas(
        modifier = modifier
            .size(size)
            .scale(scale.value)
            .semantics { contentDescription = "Payment verified" }
    ) {
        val strokeWidth = this.size.minDimension * STROKE_FRACTION
        val stroke = Stroke(width = strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round)
        val inset = strokeWidth / 2
        drawArc(
            color = color,
            startAngle = START_ANGLE_DEGREES,
            sweepAngle = FULL_SWEEP_DEGREES * circle.value,
            useCenter = false,
            topLeft = Offset(inset, inset),
            size = this.size.copy(width = this.size.width - strokeWidth, height = this.size.height - strokeWidth),
            style = stroke
        )
        if (check.value > 0f) {
            val w = this.size.width
            val h = this.size.height
            val tick = Path().apply {
                moveTo(w * CHECK_START_X, h * CHECK_START_Y)
                lineTo(w * CHECK_MID_X, h * CHECK_MID_Y)
                lineTo(w * CHECK_END_X, h * CHECK_END_Y)
            }
            val measure = PathMeasure().apply { setPath(tick, false) }
            val partial = Path()
            measure.getSegment(0f, measure.length * check.value, partial, true)
            drawPath(path = partial, color = color, style = stroke)
        }
    }
}
