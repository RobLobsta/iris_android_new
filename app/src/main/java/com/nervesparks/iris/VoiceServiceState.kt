package com.nervesparks.iris

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object VoiceServiceState {
    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening

    fun setListening(isListening: Boolean) {
        _isListening.value = isListening
    }
}
