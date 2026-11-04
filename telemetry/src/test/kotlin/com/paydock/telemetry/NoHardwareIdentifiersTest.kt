package com.paydock.telemetry

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * MD-5 acceptance (R_10, principle 10): no main source reads a hardware or advertising identifier. Only random IDs
 * are used.
 *
 * The working directory of a module's unit tests is the module directory, so `src/main/kotlin` is this module's
 * main source set.
 */
internal class NoHardwareIdentifiersTest {

    private val sourceRoot = File("src/main/kotlin")

    private val forbidden = listOf(
        "ANDROID_ID",
        "getImei",
        "getDeviceId",
        "getSerial",
        "Build.SERIAL",
        "getMacAddress",
        "AdvertisingIdClient",
        "getSubscriberId",
        "getSimSerialNumber",
        "TelephonyManager"
    ).map { name -> name to Regex("""(?<!\w)${Regex.escape(name)}(?!\w)""") }

    private fun sourceFiles(): List<File> =
        sourceRoot.walkTopDown().filter { it.isFile && it.extension == "kt" }.toList()

    @Test
    fun sourceDirectory_isNotEmpty() {
        assertTrue("no Kotlin sources found under ${sourceRoot.absolutePath}", sourceFiles().isNotEmpty())
    }

    @Test
    fun noSource_readsAHardwareOrAdvertisingIdentifier() {
        sourceFiles().forEach { file ->
            file.readLines().forEachIndexed { index, line ->
                forbidden.forEach { (name, pattern) ->
                    assertFalse("${file.path}:${index + 1} must not use $name: $line", pattern.containsMatchIn(line))
                }
            }
        }
    }

    @Test
    fun forbiddenPatterns_matchQualifiedAndImportedUses() {
        val samples = listOf(
            "Settings.Secure.getString(resolver, Settings.Secure.ANDROID_ID)",
            "telephony.getImei()",
            "telephony.getDeviceId()",
            "Build.getSerial()",
            "val serial = android.os.Build.SERIAL",
            "wifiInfo.getMacAddress()",
            "import com.google.android.gms.ads.identifier.AdvertisingIdClient",
            "telephony.getSubscriberId()",
            "telephony.getSimSerialNumber()",
            "import android.telephony.TelephonyManager"
        )

        samples.zip(forbidden).forEach { (sample, entry) ->
            assertTrue("${entry.first} must match: $sample", entry.second.containsMatchIn(sample))
        }
    }
}
