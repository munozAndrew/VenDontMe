package com.example.vendontme.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class Friend(
    val id: String? = null,

    @SerialName("user_id")
    val userId: String,

    @SerialName("friend_id")
    val friendId: String,

    val status: String = "pending",  // "pending", "accepted", "blocked"

    @SerialName("created_at")
    val createdAt: String? = null
)