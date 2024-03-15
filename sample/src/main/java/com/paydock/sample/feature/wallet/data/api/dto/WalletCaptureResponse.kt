/*
 * Created by Paydock on 10/16/23, 11:30 AM
 * Copyright (c) 2023 Lasting. All rights reserved.
 *
 * Last modified 10/16/23, 11:26 AM
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

package com.paydock.sample.feature.wallet.data.api.dto

import com.paydock.sample.core.data.api.dto.Resource

data class WalletCaptureResponse(
	val error: Any,
	val resource: Resource<WalletChargeData>,
	val status: Int
)