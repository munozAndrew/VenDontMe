package com.example.vendontme.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ItemClaim(
    val id: String? = null,
    @SerialName("bill_item_id")
    val billItemId: String,
    @SerialName("user_id")
    val userId: String,
    val quantity: Int
)