package com.example.vendontme.domain.model

import com.example.vendontme.data.model.Profile

data class Split(
    val id: String,
    val receiptId: String,
    val user: Profile,
    val amount: Double,
    val isPaid: Boolean
)
