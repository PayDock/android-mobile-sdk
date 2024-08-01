package com.paydock.core.domain.model.meta

import com.paydock.core.domain.model.meta.base.BaseDPAData
import com.paydock.core.domain.model.meta.enum.ApplicationType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents data related to ClickToPay's Digital Payment Application (DPA).
 *
 * @property dpaAddress Address associated with the DPA.
 * @property dpaEmailAddress Email address for DPA communication.
 * @property dpaPhoneNumber Phone number structure for DPA communication.
 * @property dpaLogoUri URI for the DPA logo.
 * @property dpaSupportedEmailAddress Supported email address for DPA support.
 * @property dpaSupportedPhoneNumber Supported phone number for DPA support.
 * @property dpaUri URI for DPA support.
 * @property dpaSupportUri URI for DPA support.
 * @property applicationType Application type, either 'WEB_BROWSER' or 'MOBILE_APP'.
 */
@Serializable
data class ClickToPayDPAData(
    @SerialName("dpa_address") val dpaAddress: String? = null,
    @SerialName("dpa_email_address") val dpaEmailAddress: String? = null,
    @SerialName("dpa_phone_number") val dpaPhoneNumber: PhoneNumber? = null,
    @SerialName("dpa_logo_uri") val dpaLogoUri: String? = null,
    @SerialName("dpa_supported_email_address") val dpaSupportedEmailAddress: String? = null,
    @SerialName("dpa_supported_phone_number") val dpaSupportedPhoneNumber: PhoneNumber? = null,
    @SerialName("dpa_uri") override val dpaUri: String? = null,
    @SerialName("dpa_support_uri") val dpaSupportUri: String? = null,
    @SerialName("application_type") val applicationType: ApplicationType? = null
) : BaseDPAData(dpaUri = dpaUri)
