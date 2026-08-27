package com.example.data.model

data class FoodItem(
    val name: String,
    val quantity: Int,
    val price: Double,
    val instructions: String = "",
    var isChecked: Boolean = false
)
