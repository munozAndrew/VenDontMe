package com.example.vendontme.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Profile(
    val id: String,

    @SerialName("username")
    val username: String? = null,

    @SerialName("display_name")
    val displayName: String? = null,

    @SerialName("avatar_url")
    val avatarUrl: String? = null,

    @SerialName("phone_number")
    val phoneNumber: String? = null,

    @SerialName("created_at")
    val createdAt: String? = null,

    @SerialName("updated_at")
    val updatedAt: String? = null
)
