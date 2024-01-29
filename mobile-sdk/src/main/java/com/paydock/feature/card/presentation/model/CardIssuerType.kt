/*
 * Created by Paydock on 1/26/24, 6:24 PM
 * Copyright (c) 2024 Paydock Ltd.
 *
 * Last modified 1/26/24, 2:24 PM
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.paydock.feature.card.presentation.model

import androidx.annotation.DrawableRes
import com.paydock.R

/**
 * Enum class representing different card issuers, each associated with a corresponding image resource.
 * The image resource ID can be used to display the logo or icon of the card issuer.
 *
 * @property imageResId The resource ID of the image representing the card issuer.
 */
enum class CardIssuerType(@DrawableRes val imageResId: Int) {
    VISA(R.drawable.ic_visa),
    MASTERCARD(R.drawable.ic_mastercard),
    AMERICAN_EXPRESS(R.drawable.ic_amex),
    DINERS_CLUB(R.drawable.ic_diners_club),
    JCB(R.drawable.ic_jcb),
    MAESTRO(R.drawable.ic_maestro),
    DISCOVER(R.drawable.ic_discover),
    UNION_PAY(R.drawable.ic_union_pay),
    INTER_PAY(R.drawable.ic_interpay),
    INSTA_PAYMENT(R.drawable.ic_insta_payment),
    UATP(R.drawable.ic_uatp),
    OTHER(R.drawable.ic_credit_card)
}