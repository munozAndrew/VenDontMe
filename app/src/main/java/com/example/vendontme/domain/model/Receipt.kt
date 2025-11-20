package com.example.vendontme.domain.model

data class Receipt(
    val id: String,
    val groupId: String,
    val merchantName: String,
    val items: List<Item>,
    val subtotal: Double,
    val tax: Double,
    val tip: Double,
    val total: Double,
    val imageUrl: String? = null,
    val date: String,
    val status: String    // "pending" or "settled"
)
