package com.example.vendontme.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BillItem(
    val id: String? = null,
    @SerialName("bill_id")
    val billId: String,
    val name: String,
    val price: Double,
    val quantity: Int
)