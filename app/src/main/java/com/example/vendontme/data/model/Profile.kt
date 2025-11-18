package com.example.vendontme.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Profile(
    val id: String,
    val username: String? = null,
    val displayName: String? = null,
    val avatarUrl: String? = null,
    val phoneNumber: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)
