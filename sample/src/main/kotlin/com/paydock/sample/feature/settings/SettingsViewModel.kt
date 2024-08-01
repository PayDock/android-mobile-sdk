package com.paydock.sample.feature.settings

import androidx.lifecycle.ViewModel
import com.paydock.sample.core.presentation.utils.AccessTokenProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(private val accessTokenProvider: AccessTokenProvider) :
    ViewModel() {
    val accessToken: StateFlow<String> = accessTokenProvider.accessToken

    fun updateAccessToken(newAccessToken: String) {
        accessTokenProvider.updateAccessToken(newAccessToken)
    }
}
