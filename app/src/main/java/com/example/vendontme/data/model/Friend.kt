package com.example.vendontme.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Friend(
    val id: String,

    @SerialName("user_id")
    val userId: String,

    @SerialName("friend_id")
    val friendId: String,

    val status: String, // "pending", "accepted", "rejected", "blocked"

    @SerialName("created_at")
    val createdAt: String? = null,

    val friendProfile: Profile? = null
)

fun Friend.getDisplayName(): String {
    return friendProfile?.displayName ?: friendProfile?.username ?: "Unknown User"
}

fun Friend.getUsername(): String {
    return friendProfile?.username ?: "unknown"
}

fun Friend.getAvatarUrl(): String? {
    return friendProfile?.avatarUrl
}