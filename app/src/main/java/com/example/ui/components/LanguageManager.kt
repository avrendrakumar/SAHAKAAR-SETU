package com.example.ui.components

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

object LanguageManager {
    var isHindi by mutableStateOf(false)

    fun toggleLanguage() {
        isHindi = !isHindi
    }

    fun t(en: String, hi: String): String {
        return if (isHindi) hi else en
    }
}
