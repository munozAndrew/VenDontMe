package com.example.vendontme.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Receipt(
    val id: String,

    @SerialName("group_id")
    val groupId: String,

    @SerialName("image_url")
    val imageUrl: String,  // Supabase Storage URL

    @SerialName("total_amount")
    val totalAmount: Double,

    @SerialName("created_by")
    val createdBy: String,

    @SerialName("created_at")
    val createdAt: String? = null,

    val description: String? = null
)

@Serializable
data class ReceiptItem(
    val id: String,

    @SerialName("receipt_id")
    val receiptId: String,

    val name: String,
    val price: Double,
    val quantity: Int = 1
)
@Serializable
data class ReceiptItemClaim(
    val id: String,

    @SerialName("item_id")
    val itemId: String,

    @SerialName("user_id")
    val userId: String,

    @SerialName("amount")
    val amount: Double,

    @SerialName("created_at")
    val createdAt: String? = null
)