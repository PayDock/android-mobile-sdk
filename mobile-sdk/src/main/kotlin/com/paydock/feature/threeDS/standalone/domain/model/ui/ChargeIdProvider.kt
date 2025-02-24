package com.paydock.feature.threeDS.standalone.domain.model.ui

/**
 * Interface for providing a charge ID that may be used for 3DS authentication.
 *
 * This interface is designed to be implemented by classes that hold or can generate
 * a unique identifier for a specific charge, which might be required for 3D Secure
 * authentication processes. The ID may or may not be present, depending on the
 * charge's lifecycle and whether 3DS is needed.
 */
internal interface ChargeIdProvider {
    val charge3dsId: String?
}