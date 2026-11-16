package com.paydock.core.domain.error.exceptions

/**
 * Represents errors in the configuration of the Mobile SDK itself, raised while the SDK is built
 * (`MobileSDK.Builder.build`), before it is initialised and before any widget can be opened.
 *
 * @param displayableMessage A message describing the configuration error.
 */
sealed class MobileSdkException(displayableMessage: String) : SdkException(displayableMessage) {

    /**
     * The SDK configuration is invalid, e.g. a `clientSdkVersion` pin that is not a plain release (`vX.Y.Z`) of the
     * Client SDK line this Mobile SDK supports. The message names the supported line.
     *
     * @param displayableMessage A message describing the invalid configuration.
     */
    class InvalidConfiguration(displayableMessage: String) : MobileSdkException(displayableMessage)
}
