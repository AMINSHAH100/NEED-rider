package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val orderNumber: String,
    val restaurantName: String,
    val restaurantAddress: String,
    val restaurantPhone: String,
    val restaurantLat: Double = 23.7937,
    val restaurantLng: Double = 90.4066,
    val customerName: String,
    val customerAddress: String,
    val customerPhone: String,
    val customerLat: Double = 23.7509,
    val customerLng: Double = 90.3807,
    val customerNote: String = "",
    val itemsSummary: String,
    val itemsJson: String = "",
    val totalBill: Double,
    val deliveryFee: Double,
    val tip: Double = 0.0,
    val surgeBonus: Double = 0.0,
    val paymentMethod: String, // "COD" or "PREPAID"
    val status: String, // "NEW_PING", "ACCEPTED", "ARRIVED_AT_REST", "PICKED_UP", "ARRIVED_AT_CUST", "DELIVERED", "REJECTED", "CANCELLED"
    val distanceKm: Double,
    val estimatedMinutes: Int,
    val customerOtp: String = "4892",
    val pickupOtp: String = "1024",
    val cashReceived: Double = 0.0,
    val changeToReturn: Double = 0.0,
    val isItemsChecked: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
