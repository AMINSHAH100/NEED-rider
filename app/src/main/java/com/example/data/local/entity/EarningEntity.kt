package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "earnings")
data class EarningEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val orderId: Long,
    val orderNumber: String,
    val baseFee: Double,
    val distanceFee: Double,
    val surgeFee: Double,
    val tip: Double,
    val questBonus: Double,
    val totalEarned: Double,
    val paymentMethod: String,
    val timestamp: Long = System.currentTimeMillis()
)
