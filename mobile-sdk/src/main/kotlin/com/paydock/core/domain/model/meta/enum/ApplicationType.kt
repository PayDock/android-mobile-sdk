package com.paydock.core.domain.model.meta.enum

import kotlinx.serialization.Serializable

/**
 * Represents the type of application.
 */
@Serializable
enum class ApplicationType {
    /**
     * Application is a web browser.
     */
    WEB_BROWSER,

    /**
     * Application is a mobile app.
     */
    MOBILE_APP
}
