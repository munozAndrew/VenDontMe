package com.example.vendontme.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Bill(
    val id: String? = null,
    @SerialName("group_id")
    val groupId: String,
    @SerialName("created_by")
    val createdBy: String,
    @SerialName("merchant_name")
    val merchantName: String? = null,
    val subtotal: Double? = null,
    val tax: Double? = null,
    val tip: Double? = null,
    val total: Double,
    @SerialName("image_url")
    val imageUrl: String? = null,
    val date: String? = null,
    val status: String = "pending"  // pending, completed, etc.
)