package com.paydock.designsystems.components.web.config

import android.util.Log
import com.paydock.MobileSDK
import com.paydock.core.ClientSDKConstants
import com.paydock.core.MobileSDKConstants
import com.paydock.core.data.injection.modules.provideJson
import com.paydock.core.domain.mapper.mapToClientSDKEnv
import com.paydock.core.domain.mapper.mapToClientSDKLibrary
import com.paydock.core.domain.model.meta.MastercardSRCMeta
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString

/**
 * Sealed class representing configurations for different widgets.
 */
internal sealed class WidgetConfig {
    /**
     * Title of the widget.
     */
    abstract val title: String

    /**
     * URL of the JavaScript library required by the widget.
     */
    abstract val jsLibraryUrl: String

    /**
     * Environment for the widget (e.g., 'sandbox', 'production').
     */
    abstract val environment: String

    /**
     * List of events the widget can trigger.
     */
    abstract val events: List<String>

    /**
     * Abstract method to create the widget initialization script.
     *
     * @return Widget initialization script.
     */
    abstract fun createWidget(): String

    /**
     * Configuration for Mastercard SRC widget.
     *
     * @property title Title for the Mastercard SRC widget. Default is "Mastercard SRC".
     * @property jsLibraryUrl URL of the JavaScript library for Mastercard SRC widget.
     *                        Default is [ClientSDKConstants.CLIENT_SDK_JS_LIBRARY].
     * @property environment Environment for the Mastercard SRC widget. Default is the environment
     *                        mapped from [MobileSDK.getInstance().environment].
     * @property events List of events supported by the Mastercard SRC widget.
     *                  Default includes "iframeLoaded", "checkoutReady", "checkoutCompleted", "checkoutError".
     * @property publicKey Public key for authentication.
     * @property serviceId Service ID for Mastercard SRC.
     * @property meta Meta data for configuring Mastercard SRC.
     */
    data class MastercardSRCConfig(
        override val title: String = "Mastercard SRC",
        override val jsLibraryUrl: String = MobileSDK.getInstance().environment.mapToClientSDKLibrary(),
        override val environment: String = MobileSDK.getInstance().environment.mapToClientSDKEnv(),
        override val events: List<String> = listOf(
            "iframeLoaded",
            "checkoutReady",
            "checkoutCompleted",
            "checkoutPopupOpen",
            "checkoutPopupClose",
            "checkoutError"
        ),
        val publicKey: String = MobileSDK.getInstance().publicKey,
        val serviceId: String,
        val meta: MastercardSRCMeta?
    ) : WidgetConfig() {

        /**
         * Get the JSON string representation of the meta data.
         *
         * @return JSON string representation of the meta data.
         */
        private fun getMetaJson(): String = try {
            meta?.let { provideJson().encodeToString(it) } ?: "{}"
        } catch (exception: SerializationException) {
            Log.w(MobileSDKConstants.MOBILE_SDK_TAG, exception.message, exception)
            "{}"
        }

        /**
         * Create the widget initialization script for Mastercard SRC.
         *
         * @return Widget initialization script.
         */
        override fun createWidget(): String {
            return """
                new paydock.ClickToPay(
                    "#${ClientSDKConstants.WIDGET_CONTAINER_ID}",
                    "$serviceId", // service_id
                    "$publicKey", // paydock_public_key_or_access_token
                    ${getMetaJson()}
                );
            """.trimIndent()
        }

    }

    /**
     * Configuration for 3DS widget.
     *
     * @property title Title for the 3DS widget. Default is "3DS".
     * @property jsLibraryUrl URL of the JavaScript library for 3DS widget.
     *                        Default is [ClientSDKConstants.CLIENT_SDK_JS_LIBRARY].
     * @property environment Environment for the 3DS widget. Default is the environment
     *                        mapped from [MobileSDK.getInstance().environment].
     * @property events List of events supported by the 3DS widget.
     *                  Default includes "chargeAuthSuccess", "chargeAuthReject", "chargeAuthChallenge",
     *                  "chargeAuthDecoupled", "chargeAuthInfo", "error".
     * @property token Token for 3DS widget initialization.
     */
    data class ThreeDSConfig(
        override val title: String = "3DS",
        override val jsLibraryUrl: String = MobileSDK.getInstance().environment.mapToClientSDKLibrary(),
        override val environment: String = MobileSDK.getInstance().environment.mapToClientSDKEnv(),
        override val events: List<String> = listOf(
            "chargeAuthSuccess",
            "chargeAuthReject",
            "chargeAuthChallenge",
            "chargeAuthDecoupled",
            "chargeAuthInfo",
            "error"
        ),
        val token: String
    ) : WidgetConfig() {

        /**
         * Create the widget initialization script for 3DS.
         *
         * @return Widget initialization script.
         */
        override fun createWidget(): String {
            return """
                new paydock.Canvas3ds("#${ClientSDKConstants.WIDGET_CONTAINER_ID}", "$token")
            """.trimIndent()
        }
    }
}
