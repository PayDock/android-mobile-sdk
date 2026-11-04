package com.paydock.core.telemetry

import com.paydock.BuildConfig
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.util.Properties

/**
 * MD-5 acceptance (R_08): the SDK knows its own version at runtime, and it equals the published `versionName`.
 *
 * The working directory of a module's unit tests is the module directory, so `../gradle.properties` is the root
 * file the publishing build reads.
 */
internal class SdkVersionTest {

    @Test
    fun sdkVersion_equalsThePublishedVersionName() {
        val file = File("../gradle.properties")
        assertTrue("gradle.properties not found at ${file.absolutePath}", file.isFile)
        val versionName = Properties().apply { file.inputStream().use { load(it) } }.getProperty("versionName")

        assertTrue("versionName must be set", !versionName.isNullOrBlank())
        assertEquals(versionName, BuildConfig.SDK_VERSION)
    }
}
