package com.nervesparks.iris

data class MainUiState(
    val assistantResponse: String = "",
    val isRagEnabled: Boolean = false,
    val isRagReady: Boolean = false,
)
