package com.example.inv_5.ui.entities

import java.util.Date

data class Purchase(
    val id: String,
    val date: Date,
    val vendor: String,
    val totalAmount: Double
)