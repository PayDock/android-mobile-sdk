package com.paydock.sample.feature.style.utils

import android.content.Context
import android.graphics.fonts.SystemFonts
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontListFontFamily
import androidx.compose.ui.text.font.ResourceFont
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.regex.Pattern

object FontHelper {

    data class FontInfo(val displayName: String, val fileName: String)

    // Your default display name for your app's resource font
    const val DEFAULT_APP_FONT_DISPLAY_NAME = "Acid Grotesk" // Cleaner display name
    private const val DEFAULT_APP_FONT_RESOURCE_ID = "acid_grotesk" // Internal resource name

    // --- Functions to get font details (used by StyleTitleSection) ---

    suspend fun getSystemFontFileDetails(): List<FontInfo> = withContext(Dispatchers.Default) {
        getSystemFontFilesFromSystem()
            .map { file ->
                FontInfo(
                    displayName = normalizeFontNameForDisplay(file.name) ?: file.name,
                    fileName = file.name
                )
            }
            .distinctBy { it.displayName }
            .sortedBy { it.displayName }
    }

    private fun normalizeFontNameForDisplay(fileName: String): String? {
        return fileName.substringBeforeLast('.')
            .replace('-', ' ')
            .replace(Regex("([a-z])([A-Z])"), "$1 $2")
            .trim()
            .ifEmpty { null }
    }

    // --- Functions to find/create Font objects (used by FontFamilyDropdown when selecting) ---

    fun findSystemFontByFileName(fileName: String): Font? { // androidx.compose.ui.text.font.Font
        val fontFile = getSystemFontFilesFromSystem().find { it.name == fileName }
        return fontFile?.let { Font(it) }
    }

    // Renamed to avoid confusion with Android platform Font
    private fun getSystemFontFilesFromSystem(): List<File> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            SystemFonts.getAvailableFonts().mapNotNull { it.file }
        } else {
            val fontsDir = File("/system/fonts")
            fontsDir.listFiles()?.toList() ?: emptyList()
        }
    }

    // --- Core function to get a display name for a given FontFamily ---
    @Composable
    fun getFontFamilyDisplayName(
        fontFamily: FontFamily?,
        systemFontDetails: List<FontInfo> // Needs the list of known system fonts
    ): String? {
        if (fontFamily == null) return null
        val context = androidx.compose.ui.platform.LocalContext.current

        // 1. Try resource font name first
        val resourceName = getDisplayableResourceFontName(context, fontFamily)
        if (resourceName != null) return resourceName

        // 2. Try to get system font name using the previous (fragile) parsing method
        //    This is only for display purposes if the fontFamily IS a system font.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) { // Guard the Q-specific logic
            val systemFileNameFromFamily = getSystemFileNameFromFontFamilyUsingParsing(fontFamily)
            if (systemFileNameFromFamily != null) {
                // Now find the FontInfo that matches this fileName to get its proper displayName
                val matchingFontInfo =
                    systemFontDetails.find { it.fileName == systemFileNameFromFamily }
                if (matchingFontInfo != null) {
                    return matchingFontInfo.displayName
                }
                // Fallback if somehow the parsed filename isn't in our details (shouldn't happen ideally)
                // or if its normalized name is better.
                return normalizeFontNameForDisplay(systemFileNameFromFamily)
                    ?: systemFileNameFromFamily
            }
        }

        // 3. Fallback: Heuristic comparison (as discussed, can be slow)
        //    This is an alternative if the parsing above fails or for non-Q devices if not covered.
        //    This should ideally be the last resort due to performance.
        for (fontInfo in systemFontDetails) {
            val tempSystemFile =
                File(fontInfo.fileName) // Assuming fileName is absolute path or in default font dir
            if (tempSystemFile.exists()) { // Ensure file actually exists before creating Font
                val tempFont = Font(tempSystemFile)
                val tempFontFamily = FontFamily(tempFont)
                if (fontFamily == tempFontFamily) {
                    return fontInfo.displayName
                }
            }
        }
        return null // No display name found
    }

    // --- Helper for Resource Fonts ---
    private fun getDisplayableResourceFontName(context: Context, fontFamily: FontFamily): String? {
        val fontListFamily = fontFamily as? FontListFontFamily ?: return null
        if (fontListFamily.fonts.isEmpty()) return null
        val resourceFont = fontListFamily.fonts.firstOrNull() as? ResourceFont ?: return null
        return mapResourceEntryToDisplayName(context.resources.getResourceEntryName(resourceFont.resId))
    }

    private val resourceToDisplayNameMap = mapOf(
        DEFAULT_APP_FONT_RESOURCE_ID to DEFAULT_APP_FONT_DISPLAY_NAME
        // Add other app-specific font resource mappings here
    )

    private fun mapResourceEntryToDisplayName(resourceEntryName: String?): String? {
        return resourceEntryName?.let { resourceToDisplayNameMap[it] }
    }


    // --- Helpers based on your PREVIOUS working (but fragile) logic for system fonts ---
    // This is the function that used toString() parsing.
    // It attempts to get the FILE NAME (e.g., "Roboto-Regular.ttf") from a FontFamily.
    @RequiresApi(Build.VERSION_CODES.Q)
    private fun getSystemFileNameFromFontFamilyUsingParsing(fontFamily: FontFamily?): String? {
        val firstComposeFont =
            (fontFamily as? FontListFontFamily)?.fonts?.firstOrNull() ?: return null

        // Only attempt parsing if it's not a ResourceFont (which is handled separately)
        if (firstComposeFont is ResourceFont) return null

        val filePathFromFileProvider = extractFilePath(firstComposeFont.toString()) ?: return null

        // We have a file path, now find the corresponding android.graphics.fonts.Font
        // to get its canonical file name.
        val systemPlatformFonts = SystemFonts.getAvailableFonts().filterNotNull()
        val matchingPlatformFont =
            systemPlatformFonts.find { it.file?.path == filePathFromFileProvider }

        return matchingPlatformFont?.file?.name // This is the actual file name like "Roboto-Regular.ttf"
    }

    private fun extractFilePath(fontToString: String): String? {
        // Your previous pattern: "Font(file=([^,]+),"
        // Let's make it slightly more robust to whitespace around '=' and after ','
        // Example: Font(type=FILE, path=/system/fonts/Roboto-Regular.ttf, index=0, ...
        // Example from compose: Font(file=/system/fonts/Roboto-Regular.ttf, weight=FontWeight(weight=400), style=Normal)
        val pattern = Pattern.compile("(?:file|path)\\s*=\\s*([^,)\\s]+)")
        val matcher = pattern.matcher(fontToString)
        return if (matcher.find()) {
            matcher.group(1)
        } else {
            null
        }
    }
}

val LocalFontHelper = staticCompositionLocalOf<FontHelper> {
    error("No FontHelper provided")
}