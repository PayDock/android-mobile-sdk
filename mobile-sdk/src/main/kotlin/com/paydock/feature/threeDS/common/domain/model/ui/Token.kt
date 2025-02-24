package com.paydock.feature.threeDS.common.domain.model.ui

import com.paydock.feature.threeDS.common.domain.model.ui.enums.TokenFormat
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents a single token of content, potentially formatted, and optionally associated with a 3DS charge ID.
 *
 * This class encapsulates a piece of content, its formatting, and an optional identifier for a 3D Secure charge.
 * It's designed to be used within a larger system that processes and manages formatted content,
 * potentially within a payment or transaction flow where 3DS authentication is involved.
 *
 * @property content The raw content of the token. This can be HTML, plain text, or any other string-based data.
 * @property format The format of the token's content. This dictates how the `content` should be interpreted and rendered.
 *                  See [TokenFormat] for the available format options.
 * @property charge3dsId An optional identifier for a 3D Secure charge associated with this token.
 *                       This field is typically present when the token is part of a payment flow that requires 3DS authentication.
 *                       If no 3DS charge is associated with this token, this field will be `null`.
 */
@Serializable
internal data class Token(
    val content: String,
    val format: TokenFormat,
    @SerialName("charge_3ds_id") val charge3dsId: String? = null
)