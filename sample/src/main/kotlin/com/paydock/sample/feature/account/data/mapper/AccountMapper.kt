package com.paydock.sample.feature.account.data.mapper

import com.paydock.sample.feature.account.data.api.dto.CustomerResponse
import com.paydock.sample.feature.account.domain.model.Customer

fun CustomerResponse.toDomain(): Customer = Customer(
    email = this.resource.resourceData.email,
    firstName = this.resource.resourceData.firstName,
    lastName = this.resource.resourceData.lastName,
    phone = this.resource.resourceData.phone
)