package com.paydock.sample.feature.customer.data.api.dto

import com.paydock.sample.core.data.api.dto.Resource

data class CustomerResponse(
    val error: Any,
    val resource: Resource<CustomerDTO>,
    val status: Int,
)
