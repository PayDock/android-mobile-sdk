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

package com.paydock.sample.designsystems.theme.dimensions

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.paydock.sample.designsystems.theme.dimensions.Dimensions.Companion.Small

/**
 * Holder for the app dimensions that can be used to avoid hardcoded margins and paddings
 * Provides Default variant and [Small] that can be used for smaller devices
 */
class Dimensions(
    val medium1: Dp,
    val medium2: Dp,
    val medium3: Dp,
    val big1: Dp,
    val big2: Dp,
) {
    companion object {
        val Small = Dimensions(
            medium1 = 12.dp,
            medium2 = 9.dp,
            medium3 = 6.dp,
            big1 = 18.dp,
            big2 = 16.dp
        )

        val Default = Dimensions(
            medium1 = 16.dp,
            medium2 = 12.dp,
            medium3 = 8.dp,
            big1 = 24.dp,
            big2 = 32.dp
        )
    }
}
