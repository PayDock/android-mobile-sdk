package com.paydock.designsystems.components.web.config

import android.util.Log
import com.paydock.MobileSDK
import com.paydock.core.ClientSDKConstants
import com.paydock.core.MobileSDKConstants
import com.paydock.core.domain.mapper.mapToClientSDKEnv
import com.paydock.core.domain.mapper.mapToClientSDKLibrary
import com.paydock.core.domain.model.meta.ClickToPayMeta
import com.paydock.core.network.extensions.convertToJsonString
import kotlinx.serialization.SerializationException

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
     * Configuration for Click to Pay widget.
     *
     * @property title Title for the Click to Pay widget. Default is "Click to Pay".
     * @property jsLibraryUrl URL of the JavaScript library for Click to Pay widget.
     *                        Default is [ClientSDKConstants.CLIENT_SDK_JS_LIBRARY].
     * @property environment Environment for the Click to Pay widget. Default is the environment
     *                        mapped from [MobileSDK.getInstance().environment].
     * @property events List of events supported by the Click to Pay widget.
     *                  Default includes "iframeLoaded", "checkoutReady", "checkoutCompleted", "checkoutError".
     * @property accessToken Access Token for authentication.
     * @property serviceId Service ID for Click to Pay.
     * @property meta Meta data for configuring Click to Pay.
     */
    data class ClickToPayConfig(
        override val title: String = "Click to Pay",
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
        val accessToken: String,
        val serviceId: String,
        val meta: ClickToPayMeta?
    ) : WidgetConfig() {

        /**
         * Get the JSON string representation of the meta data.
         *
         * @return JSON string representation of the meta data.
         */
        private fun getMetaJson(): String = try {
            meta?.convertToJsonString() ?: "{}"
        } catch (exception: SerializationException) {
            Log.w(MobileSDKConstants.MOBILE_SDK_TAG, exception.message, exception)
            "{}"
        }

        /**
         * Create the widget initialization script for Click to Pay.
         *
         * @return Widget initialization script.
         */
        override fun createWidget(): String {
            return """
                new ${ClientSDKConstants.WIDGET_ID}.ClickToPay(
                    "#${ClientSDKConstants.WIDGET_CONTAINER_ID}",
                    "$serviceId", // service_id
                    "$accessToken", // paydock_public_key_or_access_token
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
                new ${ClientSDKConstants.WIDGET_ID}.Canvas3ds("#${ClientSDKConstants.WIDGET_CONTAINER_ID}", "$token")
            """.trimIndent()
        }
    }
}
