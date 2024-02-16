/*
 * Created by Paydock on 1/26/24, 6:24 PM
 * Copyright (c) 2024 Paydock Ltd.
 *
 * Last modified 1/26/24, 5:58 PM
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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.paydock.sample.designsystems.theme.Theme
import com.paydock.sample.designsystems.theme.typography.FontFamily
import com.paydock.sample.feature.widgets.ui.models.WidgetType

@Composable
fun PaymentMethodContainer(
    modifier: Modifier = Modifier,
    paymentMethods: List<WidgetType>,
    selectedTab: WidgetType,
    onTabSelected: (WidgetType) -> Unit
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            modifier = Modifier.fillMaxWidth(),
            style = TextStyle(
                fontSize = 18.sp,
                lineHeight = 22.sp,
                fontFamily = FontFamily,
                fontWeight = FontWeight(500)
            ),
            text = "Payment Method",
            color = Theme.colors.onSurface
        )
        HorizontalTabButtonCarousel(
            modifier = Modifier.fillMaxWidth(),
            paymentMethods = paymentMethods,
            selectedTab = selectedTab,
            onTabSelected = onTabSelected
        )
    }
}