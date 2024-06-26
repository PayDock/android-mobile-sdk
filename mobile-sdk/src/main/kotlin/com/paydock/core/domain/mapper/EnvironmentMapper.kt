package com.paydock.core.domain.mapper

import com.paydock.core.ClientSDKConstants
import com.paydock.core.MobileSDKConstants
import com.paydock.core.domain.model.Environment

/**
 * Maps the current environment to the corresponding Base url (API endpoint).
 *
 * @receiver Environment The environment for which the SDK base url string is required.
 * @return The Base url.
 */
fun Environment.mapToBaseUrl(): String = when (this) {
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
fun Environment.mapToClientSDKLibrary(): String = when (this) {
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
fun Environment.mapToClientSDKEnv(): String = when (this) {
    Environment.PRODUCTION -> ClientSDKConstants.Env.PROD
    Environment.SANDBOX -> ClientSDKConstants.Env.SANDBOX
    Environment.STAGING -> ClientSDKConstants.Env.STAGING
}
