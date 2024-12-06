package com.paydock.core.domain.mapper

import com.afterpay.android.AfterpayEnvironment
import com.paydock.core.ClientSDKConstants
import com.paydock.core.MobileSDKConstants
import com.paydock.core.domain.model.Environment

/**
 * Maps the current environment to the corresponding Base url (API endpoint).
 *
 * @receiver Environment The environment for which the SDK base url string is required.
 * @return The Base url.
 */
internal fun Environment.mapToBaseUrl(): String = when (this) {
    Environment.PRODUCTION -> MobileSDKConstants.BaseUrls.PRODUCTION_BASE_URL
    Environment.SANDBOX -> MobileSDKConstants.BaseUrls.SANDBOX_BASE_URL
    Environment.STAGING -> MobileSDKConstants.BaseUrls.STAGING_BASE_URL
}

/**
 * Maps the current environment to the corresponding Client SDK library url.
 *
 * @receiver Environment The environment for which the Client-SDK library url string is required.
 * @return The Client SDK library url.
 */
internal fun Environment.mapToClientSDKLibrary(): String = when (this) {
    Environment.PRODUCTION -> ClientSDKConstants.Library.PROD
    Environment.SANDBOX,
    Environment.STAGING -> ClientSDKConstants.Library.STAGING_SANDBOX
}

/**
 * Maps the current environment to the corresponding Client SDK environment string.
 *
 * @receiver Environment The environment for which the Client-SDK environment name string is required.
 * @return The Client SDK environment string.
 */
internal fun Environment.mapToClientSDKEnv(): String = when (this) {
    Environment.PRODUCTION -> ClientSDKConstants.Env.PROD
    Environment.SANDBOX -> ClientSDKConstants.Env.SANDBOX
    Environment.STAGING -> ClientSDKConstants.Env.STAGING
}

/**
 * Maps the [Environment] of the application to the corresponding PayPal environment.
 *
 * This function converts the internal [Environment] type to the equivalent
 * PayPal SDK environment ([com.paypal.android.corepayments.Environment]).
 *
 * The mapping is as follows:
 * - [Environment.PRODUCTION] maps to [com.paypal.android.corepayments.Environment.LIVE]
 * - [Environment.SANDBOX] and [Environment.STAGING] map to [com.paypal.android.corepayments.Environment.SANDBOX]
 *
 * @return The corresponding [com.paypal.android.corepayments.Environment].
 */
internal fun Environment.mapToPayPalEnv(): com.paypal.android.corepayments.Environment = when (this) {
    Environment.PRODUCTION -> com.paypal.android.corepayments.Environment.LIVE
    Environment.SANDBOX, Environment.STAGING -> com.paypal.android.corepayments.Environment.SANDBOX
}

/**
 * Maps the application's custom `Environment` enum to the corresponding `AfterpayEnvironment`.
 *
 * This function is used to convert the application's internal representation of environments
 * (e.g., `Environment.SANDBOX`, `Environment.STAGING`, `Environment.PRODUCTION`) to the
 * `AfterpayEnvironment` used by the Afterpay SDK.
 *
 * The mapping ensures that:
 * - Both `Environment.SANDBOX` and `Environment.STAGING` are treated as `AfterpayEnvironment.SANDBOX`.
 * - `Environment.PRODUCTION` is directly mapped to `AfterpayEnvironment.PRODUCTION`.
 *
 * @receiver The application's internal `Environment` enum instance.
 * @return The corresponding `AfterpayEnvironment` used by the Afterpay SDK.
 */
internal fun Environment.mapToAfterpayEnv(): AfterpayEnvironment = when (this) {
    Environment.SANDBOX, Environment.STAGING -> AfterpayEnvironment.SANDBOX
    Environment.PRODUCTION -> AfterpayEnvironment.PRODUCTION
}
