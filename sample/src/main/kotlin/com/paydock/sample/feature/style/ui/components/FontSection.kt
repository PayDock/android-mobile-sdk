package com.paydock.sample.feature.style.ui.components

import android.content.Context
import android.graphics.fonts.SystemFonts
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontListFontFamily
import androidx.compose.ui.text.font.ResourceFont
import androidx.compose.ui.text.font.toFontFamily
import com.paydock.ThemeFont
import com.paydock.sample.R
import com.paydock.sample.designsystems.components.containers.SectionContainer
import com.paydock.sample.designsystems.components.fields.DropdownListField
import com.paydock.sample.designsystems.theme.typography.AcidGroteskFontFamily
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.regex.Pattern

const val DEFAULT_FONT_NAME = "Acid-Grotesk*.otf"

@Composable
fun FontSection(fontTheme: ThemeFont, onFontUpdated: (FontFamily) -> Unit) {
    val context: Context = LocalContext.current
    var selectedFontFamily by remember { mutableStateOf(fontTheme.familyName) }
    var selectedFontName by remember { mutableStateOf("") }

    // State to hold the list of fonts
    val fontsState = remember { mutableStateListOf<String>() }
    // Fetch the list of fonts using a coroutine
    LaunchedEffect(Unit) {
        val fonts: List<String> = listOf(DEFAULT_FONT_NAME) + getSystemFontNames()
        // Update the fontsState with the fetched list of fonts
        fontsState.addAll(fonts)
        selectedFontName = context.getResourceFontName(selectedFontFamily)
            ?: if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                getSelectedSystemFontName(selectedFontFamily) ?: ""
            } else ""
    }
    SectionContainer(title = stringResource(R.string.label_font)) {
        DropdownListField(
            modifier = Modifier.fillMaxWidth(),
            items = fontsState,
            selected = selectedFontName
        ) {
            selectedFontName = it
            val selectedFont = getSelectedSystemFont(it)
            selectedFontFamily = selectedFont?.toFontFamily() ?: AcidGroteskFontFamily
            onFontUpdated(selectedFontFamily)
        }

        Text(fontFamily = selectedFontFamily, text = "Sample Text")
    }

}

fun getSelectedSystemFont(fontName: String): Font? =
    getSystemFontFiles().find { it.name == fontName }?.let { Font(it) }

@RequiresApi(Build.VERSION_CODES.Q)
fun getSystemFonts(): List<android.graphics.fonts.Font> {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        // Then we get any device system fonts
        SystemFonts.getAvailableFonts().mapNotNull {
            it
        }
    } else {
        val path = "/system/fonts"
        val file = File(path)
        val fontFamily: Array<out File>? = file.listFiles()

        fontFamily?.map { fontFile ->
            android.graphics.fonts.Font.Builder(fontFile).build()
        } ?: listOf()
    }
}

fun getSystemFontFiles(): List<File> {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        // Then we get any device system fonts
        SystemFonts.getAvailableFonts().mapNotNull {
            it.file
        }
    } else {
        val path = "/system/fonts"
        val file = File(path)
        val fontFamily: Array<out File>? = file.listFiles()

        fontFamily?.map { fontFile ->
            fontFile
        } ?: listOf()
    }
}

suspend fun getSystemFontNames(): List<String> = withContext(Dispatchers.Default) {
    return@withContext getSystemFontFiles().map { it.name }.sorted().distinct()
}

fun Context.getResourceFontName(fontFamily: FontFamily): String? {
    val fontList = (fontFamily as FontListFontFamily).fonts
    if (fontList.isEmpty()) return null
    val firstFont: Font = fontList[0]
    if (firstFont !is ResourceFont) return null
    return mapResourceToStringName(resources.getResourceEntryName(firstFont.resId))
}

@RequiresApi(Build.VERSION_CODES.Q)
fun getSelectedSystemFontName(fontFamily: FontFamily?): String? {
    val font = (fontFamily as FontListFontFamily).fonts.firstOrNull()
    val filePath = extractFilePath(font.toString())
    val systemFonts: List<android.graphics.fonts.Font> = getSystemFonts()
    systemFonts.forEach {
        if (it.file?.path == filePath) {
            return it.file?.name
        }
    }
    return null
}

fun extractFilePath(input: String): String? {
    val pattern = Pattern.compile("Font\\(file=([^,]+),")
    val matcher = pattern.matcher(input)

    if (matcher.find()) {
        // Extract and return the matched file path
        return matcher.group(1)
    }

    return null // Return null if no match is found
}

fun mapResourceToStringName(resourceEntryName: String?): String? {
    return if (resourceEntryName?.equals("acid_grotesk") == true) {
        DEFAULT_FONT_NAME
    } else {
        null
    }
}
