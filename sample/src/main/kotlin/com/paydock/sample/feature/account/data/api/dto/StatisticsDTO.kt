package com.paydock.sample.feature.account.data.api.dto

import com.google.gson.annotations.SerializedName

data class StatisticsDTO(
    @SerializedName("successful_transactions") val successfulTransactions: Int,
    @SerializedName("total_collected_amount") val totalCollectedAmount: Int,
)