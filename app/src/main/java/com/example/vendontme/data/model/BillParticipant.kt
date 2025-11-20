package com.example.vendontme.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BillParticipant(
    val id: String? = null,
    @SerialName("bill_id")
    val billId: String,
    @SerialName("user_id")
    val userId: String,
    val amount: Double,
    @SerialName("is_paid")
    val isPaid: Boolean = false
)