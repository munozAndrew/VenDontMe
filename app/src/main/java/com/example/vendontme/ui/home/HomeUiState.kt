package com.example.vendontme.ui.home

data class HomeUiState(
    val isLoading: Boolean = false,
    val groups: List<GroupUi> = emptyList()
)
