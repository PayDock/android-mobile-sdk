package com.paydock.sample.designsystems.theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

enum class AppTheme {
    LIGHT, DARK, SYSTEM
}

class ThemeViewModel(/* private val dataStore: DataStore<Preferences> */) :
    ViewModel() { // Inject DataStore if using

    // In-memory state for this example. Replace with DataStore for persistence.
    var currentAppTheme by mutableStateOf(AppTheme.SYSTEM)
        private set

    // Example of loading from DataStore (simplified)
    // val appThemeFlow = dataStore.data.map { preferences ->
    //     AppTheme.valueOf(preferences[THEME_KEY] ?: AppTheme.SYSTEM.name)
    // }
    // init {
    //     viewModelScope.launch {
    //         appThemeFlow.collect { currentAppTheme = it }
    //     }
    //  }

    fun onThemeChange(newTheme: AppTheme) {
        currentAppTheme = newTheme
        // viewModelScope.launch { // If using DataStore
        //     dataStore.edit { settings ->
        //         settings[THEME_KEY] = newTheme.name
        //     }
        // }
    }

    companion object {
        // val THEME_KEY = stringPreferencesKey("app_theme") // For DataStore
    }
}