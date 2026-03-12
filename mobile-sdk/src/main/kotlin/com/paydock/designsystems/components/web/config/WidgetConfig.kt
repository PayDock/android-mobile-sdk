package com.paydock.designsystems.components.web.config

import android.util.Log
import com.paydock.MobileSDK
import com.paydock.core.ClientSDKConstants
import com.paydock.core.MobileSDKConstants
import com.paydock.core.domain.mapper.mapToClientSDKEnv
import com.paydock.core.domain.mapper.mapToClientSDKLibrary
import com.paydock.core.network.extensions.convertToJsonString
import com.paydock.feature.src.domain.model.integration.meta.ClickToPayMeta
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
        val meta: ClickToPayMeta?,
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
     * Configuration for Cart widget.
     *
     * @property title Title for the Cart widget. Default is "Cart".
     * @property jsLibraryUrl URL of the JavaScript library for Cart widget.
     *                        Default is [ClientSDKConstants.CLIENT_SDK_JS_LIBRARY].
     * @property environment Environment for the Cart widget. Default is the environment
     *                        mapped from [MobileSDK.getInstance().environment].
     * @property events List of events supported by the Cart widget.
     *                  Default is an empty list as Cart is not a web widget.
     * @property amount The cart amount value.
     */
    data class CartConfig(
        override val title: String = "Cart",
        override val jsLibraryUrl: String = MobileSDK.getInstance().environment.mapToClientSDKLibrary(),
        override val environment: String = MobileSDK.getInstance().environment.mapToClientSDKEnv(),
        override val events: List<String> = emptyList(),
        val amount: String,
    ) : WidgetConfig() {
        /**
         * Create the widget initialization script for Cart.
         * Since Cart is not a web widget, this returns an empty string.
         *
         * @return Empty string as Cart doesn't require widget initialization.
         */
        override fun createWidget(): String {
            return ""
        }
    }

    /**
     * `ThreeDSConfigBase` is a sealed class that serves as the base configuration for 3D Secure authentication widgets.
     * It provides a common structure for different types of 3DS configurations, such as MPGS and Standalone.
     *
     * This class inherits from `WidgetConfig`, indicating that it represents the configuration for a widget.
     *
     * Each concrete subclass (e.g., `MPGS3dsConfig`, `Standalone3DSConfig`) defines specific properties
     * and behaviors for its respective 3DS implementation.
     *
     * @see MPGS3dsConfig
     * @see Standalone3DSConfig
     * @see WidgetConfig
     */
    sealed class ThreeDSConfigBase : WidgetConfig() {

        /**
         * Configuration class for the MPGS 3DS (3D Secure) authentication flow.
         *
         * This class encapsulates the necessary parameters to configure and initialize the MPGS 3DS authentication widget
         * within a mobile application. It extends `ThreeDSConfigBase` and provides specific configuration for
         * the MPGS 3DS experience.
         *
         * @property title The title displayed for the 3DS authentication process. Defaults to "3d secure authentication".
         * @property jsLibraryUrl The URL of the JavaScript library required for the client-side 3DS integration.
         *                       It's dynamically retrieved from the `MobileSDK` environment.
         * @property environment The environment in which the 3DS process is running (e.g., "sandbox", "production").
         *                      It's dynamically retrieved from the `MobileSDK` environment.
         * @property events A list of event names that the client can subscribe to for the 3DS process. These events indicate
         *                  the different stages and outcomes of the authentication flow.
         *                  - "chargeAuthSuccess": Indicates a successful 3DS authentication.
         *                  - "chargeAuthReject": Indicates a failed or rejected 3DS authentication.
         *                  - "additionalDataCollectSuccess": Indicates successful collection of additional data.
         *                  - "additionalDataCollectReject": Indicates a failure in collecting additional data.
         *                  - "chargeAuth": Indicates the start of the 3DS authentication process.
         * @property token The unique token required to initialize the 3DS widget. This token is typically provided
         *                 by the backend server and is essential for the authentication process.
         *
         * @constructor Creates an instance of [MPGS3dsConfig].
         */
        data class MPGS3dsConfig(
            override val title: String = "3D Secure Authentication",
            override val jsLibraryUrl: String = MobileSDK.getInstance().environment.mapToClientSDKLibrary(),
            override val environment: String = MobileSDK.getInstance().environment.mapToClientSDKEnv(),
            override val events: List<String> = listOf(
                "chargeAuthSuccess",
                "chargeAuthReject",
                "additionalDataCollectSuccess",
                "additionalDataCollectReject",
                "chargeAuth"
            ),
            val token: String,
        ) : ThreeDSConfigBase() {
            /**
             * Creates the widget initialization script for MPGS 3D Secure.
             *
             * This script initializes the 3D Secure widget using the provided token.
             *
             * @return The widget initialization script.
             */
            override fun createWidget(): String {
                return """
                new ${ClientSDKConstants.WIDGET_ID}.Canvas3ds("#${ClientSDKConstants.WIDGET_CONTAINER_ID}", "$token")
                """.trimIndent()
            }
        }

        /**
         * Configuration class for standalone 3D Secure authentication.
         *
         * This class defines the necessary parameters for initializing and configuring a standalone 3D Secure flow.
         * It extends [ThreeDSConfigBase] and provides specific configurations for standalone 3DS, including
         * the title, JavaScript library URL, environment, supported events, and a unique token.
         *
         * @property title The title to be displayed during the 3D Secure process. Defaults to "3d secure authentication".
         * @property jsLibraryUrl The URL of the JavaScript library required for the 3D Secure flow.
         *                        It is dynamically retrieved from the Mobile SDK's environment.
         * @property environment The environment (e.g., "sandbox", "production") in which the 3D Secure process is running.
         *                       It is dynamically retrieved from the Mobile SDK's environment.
         * @property events The list of events that the 3D Secure component will emit.
         *                  This includes events like "chargeAuthSuccess", "chargeAuthReject", "chargeAuthChallenge",
         *                  "chargeAuthDecoupled", "chargeAuthInfo", and "error".
         * @property token A unique token required for the standalone 3D Secure process.
         *                 This token is essential for authenticating the transaction.
         */
        data class Standalone3DSConfig(
            override val title: String = "3D Secure Authentication",
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
            val token: String,
        ) : ThreeDSConfigBase() {
            /**
             * Creates the widget initialization script for standalone 3D Secure.
             *
             * This script initializes the 3D Secure widget using the provided token.
             *
             * @return The widget initialization script.
             */
            override fun createWidget(): String {
                return """
                new ${ClientSDKConstants.WIDGET_ID}.Canvas3ds("#${ClientSDKConstants.WIDGET_CONTAINER_ID}", "$token")
                """.trimIndent()
            }
        }
    }
}
