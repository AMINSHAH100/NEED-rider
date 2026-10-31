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
    val tip: Double = 0.0, // Forced to 0.00 in Rider UI and earnings
    val companyTip: Double = 0.0, // Hidden Company Tip
    val adminFund: Double = 0.0, // Admin Development Fund
    val questBonus: Double = 0.0,
    val totalEarned: Double, // Excludes tip (baseFee + distanceFee + surgeFee + questBonus)
    val paymentMethod: String,
    val timestamp: Long = System.currentTimeMillis()
)
