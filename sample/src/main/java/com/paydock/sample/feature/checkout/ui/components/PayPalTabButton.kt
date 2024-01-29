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

package com.paydock.sample.feature.checkout.ui.components

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.paydock.sample.R
import com.paydock.sample.designsystems.theme.PayPalYellow
import com.paydock.sample.designsystems.theme.SampleTheme

@Composable
fun PayPalTabButton(isSelected: Boolean, onClick: () -> Unit) {
    TabButton(
        isSelected = isSelected,
        selectedBorderColor = PayPalYellow,
        selectedBackgroundColor = PayPalYellow,
        onClick = onClick
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_paypal),
            contentDescription = null
        )
    }
}

@Composable
@Preview
private fun PayPalTabButtonDefault() {
    SampleTheme {
        PayPalTabButton(
            isSelected = false
        ) {}
    }
}

@Composable
@Preview
private fun PayPalTabButtonSelected() {
    SampleTheme {
        PayPalTabButton(
            isSelected = true
        ) {}
    }
}