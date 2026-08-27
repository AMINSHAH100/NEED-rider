package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "rider_profile")
data class RiderProfileEntity(
    @PrimaryKey
    val id: Int = 1,
    val name: String = "তানভীর আহমেদ (Tanvir Ahmed)",
    val phone: String = "+880 1712-345678",
    val riderCode: String = "#RD-9942",
    val vehicleType: String = "MOTORCYCLE", // "MOTORCYCLE", "BICYCLE", "SCOOTER"
    val vehiclePlate: String = "Dhaka Metro-HA 55-8912",
    val rating: Double = 4.92,
    val totalDeliveries: Int = 384,
    val acceptanceRate: Int = 98,
    val completionRate: Int = 99,
    val onTimeRate: Int = 96,
    val walletBalance: Double = 1450.0,
    val floatingCashLimit: Double = 3500.0,
    val currentFloatingCash: Double = 840.0,
    val isOnline: Boolean = true,
    val currentZone: String = "Gulshan & Banani Hub",
    val language: String = "bn", // "bn" or "en"
    val autoAccept: Boolean = false,
    val dutyStartTime: Long = System.currentTimeMillis() - (2 * 3600 * 1000 + 15 * 60 * 1000)
)
