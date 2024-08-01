package com.paydock.sample.core.presentation.utils

import com.paydock.sample.BuildConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

class AccessTokenProvider @Inject constructor() {
    private val _accessToken = MutableStateFlow(BuildConfig.ACCESS_TOKEN)
    val accessToken: StateFlow<String> = _accessToken.asStateFlow()

    fun updateAccessToken(newAccessToken: String) {
        _accessToken.value = newAccessToken
    }
}