package com.paydock.sample.feature.style.ui.components.picker

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ComposeShader
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.RectF
import android.graphics.Shader
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toRect
import com.paydock.core.presentation.ui.preview.LightDarkPreview
import com.paydock.sample.core.extensions.color
import com.paydock.sample.designsystems.theme.SampleTheme
import kotlinx.coroutines.launch
import android.graphics.Color as AndroidColor

@Composable
fun SatValPanel(
    defaultHSV: Triple<Float, Float, Float>,
    setSatVal: (Float, Float) -> Unit
) {
    val interactionSource = remember {
        MutableInteractionSource()
    }
    val scope = rememberCoroutineScope()
    var sat: Float
    var value: Float

    val pressOffset = remember {
        mutableStateOf(Offset.Zero)
    }

    LaunchedEffect(defaultHSV) {
        val defaultSat = defaultHSV.second
        val defaultValue = defaultHSV.third
        sat = defaultSat
        value = defaultValue
    }

    Canvas(
        modifier = Modifier
            .size(300.dp)
            .emitDragGesture(interactionSource)
            .clip(RoundedCornerShape(12.dp))
    ) {
        val cornerRadius = 12.dp.toPx()
        val satValSize = size

        val bitmap =
            Bitmap.createBitmap(size.width.toInt(), size.height.toInt(), Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val satValPanel = RectF(0f, 0f, bitmap.width.toFloat(), bitmap.height.toFloat())

        val rgb = AndroidColor.HSVToColor(
            floatArrayOf(
                defaultHSV.first,
                defaultHSV.second,
                defaultHSV.third
            )
        )

        val satShader = LinearGradient(
            satValPanel.left, satValPanel.top, satValPanel.right, satValPanel.top,
            -0x1, rgb, Shader.TileMode.CLAMP
        )
        val valShader = LinearGradient(
            satValPanel.left, satValPanel.top, satValPanel.left, satValPanel.bottom,
            -0x1, -0x1000000, Shader.TileMode.CLAMP
        )

        canvas.drawRoundRect(
            satValPanel,
            cornerRadius,
            cornerRadius,
            Paint().apply {
                shader = ComposeShader(
                    valShader,
                    satShader,
                    PorterDuff.Mode.MULTIPLY
                )
            }
        )

        drawBitmap(
            bitmap = bitmap,
            panel = satValPanel
        )

        fun pointToSatVal(pointX: Float, pointY: Float): Pair<Float, Float> {
            val width = satValPanel.width()
            val height = satValPanel.height()

            val x = when {
                pointX < satValPanel.left -> 0f
                pointX > satValPanel.right -> width
                else -> pointX - satValPanel.left
            }

            val y = when {
                pointY < satValPanel.top -> 0f
                pointY > satValPanel.bottom -> height
                else -> pointY - satValPanel.top
            }

            val satPoint = 1f / width * x
            val valuePoint = 1f - 1f / height * y

            return satPoint to valuePoint
        }

        scope.collectForPress(interactionSource) { pressPosition ->
            val pressPositionOffset = Offset(
                pressPosition.x.coerceIn(0f..satValSize.width),
                pressPosition.y.coerceIn(0f..satValSize.height)
            )

            pressOffset.value = pressPositionOffset
            val (satPoint, valuePoint) = pointToSatVal(pressPositionOffset.x, pressPositionOffset.y)
            sat = satPoint
            value = valuePoint

            setSatVal(sat, value)
        }

        // Calculate the initial x and y positions within the satValPanel
        val initialX = satValPanel.width() * defaultHSV.second
        val initialY = satValPanel.height() * (1 - defaultHSV.third)
        pressOffset.value = Offset(initialX, initialY)

        drawCircle(
            color = Color.White,
            radius = 8.dp.toPx(),
            center = pressOffset.value,
            style = Stroke(
                width = 2.dp.toPx()
            )
        )

        drawCircle(
            color = Color.White,
            radius = 2.dp.toPx(),
            center = pressOffset.value,
        )
    }
}

private fun Modifier.emitDragGesture(
    interactionSource: MutableInteractionSource
): Modifier = composed {
    val scope = rememberCoroutineScope()

    pointerInput(Unit) {
        detectDragGestures { input, _ ->
            scope.launch {
                interactionSource.emit(PressInteraction.Press(input.position))
            }
        }
    }.clickable(interactionSource, null) {

    }
}

private fun DrawScope.drawBitmap(
    bitmap: Bitmap,
    panel: RectF
) {
    drawIntoCanvas {
        it.nativeCanvas.drawBitmap(
            bitmap,
            null,
            panel.toRect(),
            null
        )
    }
}

@LightDarkPreview
@Composable
private fun PreviewSatValPanel() {
    val color = "#3e3e9d".color
    val hsv = floatArrayOf(0f, 0f, 0f)
    AndroidColor.colorToHSV(color!!.toArgb(), hsv)
    SampleTheme {
        SatValPanel(defaultHSV = Triple(hsv[0], hsv[1], hsv[2])) { _, _ ->

        }
    }
}