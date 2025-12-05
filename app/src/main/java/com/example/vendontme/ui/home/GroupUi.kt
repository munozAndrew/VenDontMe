package com.example.vendontme.ui.home

data class GroupUi(
    val id: String,
    val name: String,
    val description: String? = null,
    val avatarUrl: String? = null,
    val membersCount: Int = 0
)