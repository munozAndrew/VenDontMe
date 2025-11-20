package com.example.vendontme.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GroupMember(
    val id: String? = null,

    @SerialName("group_id")
    val groupId: String,

    @SerialName("user_id")
    val userId: String,

    val role: String? = "member",

    @SerialName("joined_at")
    val joinedAt: String? = null
)