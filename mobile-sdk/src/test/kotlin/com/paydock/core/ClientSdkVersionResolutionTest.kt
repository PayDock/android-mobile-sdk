package com.paydock.core

import android.content.Context
import com.paydock.MobileSDK
import com.paydock.core.domain.error.exceptions.MobileSdkException
import com.paydock.core.domain.mapper.mapToClientSDKLibrary
import com.paydock.core.domain.model.Environment
import com.paydock.designsystems.components.web.config.WidgetConfig
import io.mockk.every
import io.mockk.mockk
import org.junit.After
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse

/**
 * Which Client SDK address a widget loads: the environment's alias of the supported line without a pin, the pinned
 * release in every environment with one, and no SDK at all for a pin that is not a release of the supported line.
 */
internal class ClientSdkVersionResolutionTest : BaseUnitTest() {

    @After
    fun resetSdk() {
        MobileSDK.reset()
    }

    @Test
    fun `the supported line and its aliases`() {
        assertEquals("v1.150", ClientSDKConstants.SUPPORTED_LINE)
        assertEquals("v1.150.stable", ClientSDKConstants.STABLE_VERSION)
        assertEquals("v1.150.candidate", ClientSDKConstants.CANDIDATE_VERSION)
    }

    @Test
    fun `production without a pin loads the stable alias`() {
        assertEquals(STABLE_URL, Environment.PRODUCTION.mapToClientSDKLibrary(null))
    }

    @Test
    fun `sandbox without a pin loads the stable alias, like production`() {
        assertEquals(STABLE_URL, Environment.SANDBOX.mapToClientSDKLibrary(null))
    }

    @Test
    fun `staging without a pin loads the candidate alias`() {
        assertEquals(CANDIDATE_URL, Environment.STAGING.mapToClientSDKLibrary(null))
    }

    @Test
    fun `a pin wins in production, sandbox and staging`() {
        listOf(Environment.PRODUCTION, Environment.SANDBOX, Environment.STAGING).forEach { environment ->
            assertEquals(PINNED_URL, environment.mapToClientSDKLibrary(VALID_PIN), "environment $environment")
        }
    }

    @Test
    fun `every widget configuration loads the address resolved from the sdk environment and pin`() {
        Environment.entries.forEach { environment ->
            listOf(null, VALID_PIN).forEach { pin ->
                MobileSDK.registerInstance(
                    mockk {
                        every { this@mockk.environment } returns environment
                        every { clientSdkVersion } returns pin
                    }
                )
                val expected = environment.mapToClientSDKLibrary(pin)
                listOf(
                    WidgetConfig.ClickToPayConfig(accessToken = "token", serviceId = "service", meta = null),
                    WidgetConfig.CartConfig(amount = "1.00"),
                    WidgetConfig.ThreeDSConfigBase.MPGS3dsConfig(token = "token"),
                    WidgetConfig.ThreeDSConfigBase.Standalone3DSConfig(token = "token"),
                ).forEach { config ->
                    assertEquals(expected, config.jsLibraryUrl, "${config.title} in $environment with pin $pin")
                }
            }
        }
    }

    @Test
    fun `a pin from another line is refused at build with an error naming the supported line`() {
        val error = assertFailsWith<MobileSdkException.InvalidConfiguration> {
            MobileSDK.Builder()
                .environment(Environment.PRODUCTION)
                .clientSdkVersion("v1.151.0")
                .build(mockk<Context>())
        }

        assertEquals("clientSdkVersion v1.151.0 is outside supported line v1.150", error.message)
        assertFalse(MobileSDK.isInitialised())
    }

    @Test
    fun `a beta pin is refused at build with an error naming the supported line`() {
        val error = assertFailsWith<MobileSdkException.InvalidConfiguration> {
            MobileSDK.Builder()
                .environment(Environment.SANDBOX)
                .clientSdkVersion("v1.150.1-beta")
                .build(mockk<Context>())
        }

        assertEquals(
            "clientSdkVersion v1.150.1-beta is not a release (vX.Y.Z) of supported line v1.150",
            error.message
        )
        assertFalse(MobileSDK.isInitialised())
    }

    @Test
    fun `a malformed pin is refused at build`() {
        listOf("v1.150", "v1.150.stable", "v1.150.candidate", "1.150.1", "v1.150.1.2", " v1.150.1", "", "latest")
            .forEach { pin ->
                val error = assertFailsWith<MobileSdkException.InvalidConfiguration>("pin '$pin'") {
                    MobileSDK.Builder().clientSdkVersion(pin).build(mockk<Context>())
                }
                assertEquals(
                    "clientSdkVersion $pin is not a release (vX.Y.Z) of supported line v1.150",
                    error.message
                )
            }
        assertFalse(MobileSDK.isInitialised())
    }

    @Test
    fun `a null pin keeps the environment's alias`() {
        MobileSDK.registerInstance(
            mockk {
                every { environment } returns Environment.PRODUCTION
                every { clientSdkVersion } returns null
            }
        )

        assertEquals(STABLE_URL, WidgetConfig.ThreeDSConfigBase.MPGS3dsConfig(token = "token").jsLibraryUrl)
    }

    private companion object {
        const val VALID_PIN = "v1.150.1"
        const val STABLE_URL = "https://widget.paydock.com/sdk/v1.150.stable/widget.umd.min.js"
        const val CANDIDATE_URL = "https://widget.paydock.com/sdk/v1.150.candidate/widget.umd.min.js"
        const val PINNED_URL = "https://widget.paydock.com/sdk/v1.150.1/widget.umd.min.js"
    }
}
