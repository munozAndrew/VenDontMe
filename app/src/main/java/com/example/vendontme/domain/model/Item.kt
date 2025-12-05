package com.example.vendontme.domain.model

import com.example.vendontme.data.model.Profile

data class Item(
    val id: String,
    val name: String,
    val price: Double,
    val quantity: Int = 1,
    val assignedTo: Profile? = null
)
