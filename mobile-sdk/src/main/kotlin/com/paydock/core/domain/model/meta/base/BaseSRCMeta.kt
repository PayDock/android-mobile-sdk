package com.paydock.core.domain.model.meta.base

import com.paydock.core.domain.model.meta.Styles
import kotlinx.serialization.Serializable

/**
 * Represents the base metadata for the Secure Remote Commerce (SRC) integration.
 *
 * @property customizations Customization options for the SRC widget.
 * @property dpaData Object where the Direct Payment Application (DPA) creation data is stored.
 * @property dpaTransactionOptions Object that stores options for creating a transaction with DPA.
 */
@Serializable
abstract class BaseSRCMeta(
    val customizations: Styles? = null,
    open val dpaData: BaseDPAData? = null,
    open val dpaTransactionOptions: BaseDPAOptions? = null
)
