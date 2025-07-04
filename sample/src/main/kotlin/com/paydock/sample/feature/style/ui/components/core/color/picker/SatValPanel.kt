package com.paydock.sample.feature.style.ui.components.core.color.picker

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ComposeShader
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.RectF
import android.graphics.Shader
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.core.graphics.createBitmap
import androidx.core.graphics.toRect
import com.paydock.sample.core.extensions.color
import com.paydock.sample.designsystems.theme.SampleTheme
import kotlinx.coroutines.launch
import android.graphics.Color as AndroidColor

@Composable
fun SatValPanel(
    currentHSV: Triple<Float, Float, Float>,
    setSatVal: (Float, Float) -> Unit,
) {
    val interactionSource = remember {
        MutableInteractionSource()
    }
    val scope = rememberCoroutineScope()
    val pressOffset = remember {
        mutableStateOf(Offset.Zero)
    }

    Canvas(
        modifier = Modifier
            .size(300.dp)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, MaterialTheme.shapes.medium)
            .emitDragGesture(interactionSource)
            .clip(MaterialTheme.shapes.medium)
    ) {
        val cornerRadius = 12.dp.toPx()
        val panelSize = size // Size of the Compose Canvas

        if (panelSize.width <= 0 || panelSize.height <= 0) {
            return@Canvas
        }

        val bitmap = createBitmap(panelSize.width.toInt(), panelSize.height.toInt())
        val bitmapCanvas = Canvas(bitmap) // Canvas to draw onto the bitmap
        val panelRect = RectF(0f, 0f, bitmap.width.toFloat(), bitmap.height.toFloat())

        // Use the pure hue (full saturation, full value) for the saturation shader's colored endpoint
        val pureHueForSatShaderEndpoint = AndroidColor.HSVToColor(
            floatArrayOf(
                currentHSV.first, // Current Hue
                1f,               // Full Saturation
                1f                // Full Value
            )
        )

        val satShader = LinearGradient(
            panelRect.left, panelRect.top, panelRect.right, panelRect.top,
            AndroidColor.WHITE, // Explicit white for 0% saturation
            pureHueForSatShaderEndpoint,
            Shader.TileMode.CLAMP
        )

        val topColorForValShader = AndroidColor.WHITE

        val valShader = LinearGradient(
            panelRect.left, panelRect.top, panelRect.left, panelRect.bottom,
            topColorForValShader,
            AndroidColor.BLACK,
            Shader.TileMode.CLAMP
        )
        val combinedShader = ComposeShader(valShader, satShader, PorterDuff.Mode.MULTIPLY)
        val paint = Paint().apply { shader = combinedShader }
        bitmapCanvas.drawRoundRect(panelRect, cornerRadius, cornerRadius, paint)

        drawBitmap(
            bitmap = bitmap,
            panel = panelRect
        )

        // --- Selector Circle Logic ---
        // Calculate pixel position of the selector based on current Saturation and Value
        val selectorX = (panelSize.width * currentHSV.second).coerceIn(0f, panelSize.width)
        val selectorY = (panelSize.height * (1f - currentHSV.third)).coerceIn(0f, panelSize.height)
        pressOffset.value = Offset(selectorX, selectorY) // Update selector position

        // Handle user interaction (drag/click) to update Saturation and Value
        scope.collectForPress(interactionSource) { pressPosition ->
            val clampedX = pressPosition.x.coerceIn(0f, panelSize.width)
            val clampedY = pressPosition.y.coerceIn(0f, panelSize.height)

            pressOffset.value = Offset(clampedX, clampedY) // Update visual immediately

            val (newSat, newVal) = pointToSatVal(clampedX, clampedY, panelRect)
            setSatVal(newSat, newVal)
        }

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

private fun pointToSatVal(pointX: Float, pointY: Float, panelRect: RectF): Pair<Float, Float> {
    val width = panelRect.width()
    val height = panelRect.height()

    val x = when {
        pointX < panelRect.left -> 0f
        pointX > panelRect.right -> width
        else -> pointX - panelRect.left
    }

    val y = when {
        pointY < panelRect.top -> 0f
        pointY > panelRect.bottom -> height
        else -> pointY - panelRect.top
    }

    val satPoint = 1f / width * x
    val valuePoint = 1f - 1f / height * y

    return satPoint to valuePoint
}

private fun Modifier.emitDragGesture(
    interactionSource: MutableInteractionSource,
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
    panel: RectF,
) {
    Log.d("SatValPanel_Test", "drawBitmap")
    drawIntoCanvas {
        it.nativeCanvas.drawBitmap(
            bitmap,
            null,
            panel.toRect(),
            null
        )
    }
}

@PreviewLightDark
@Composable
internal fun PreviewSatValPanel() {
    val color = "#3e3e9d".color
    val hsv = floatArrayOf(0f, 0f, 0f)
    AndroidColor.colorToHSV(color!!.toArgb(), hsv)
    SampleTheme {
        SatValPanel(currentHSV = Triple(hsv[0], hsv[1], hsv[2])) { _, _ ->

        }
    }
}