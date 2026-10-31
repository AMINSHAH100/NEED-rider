package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "rider_profile")
data class RiderProfileEntity(
    @PrimaryKey
    val id: Int = 1,
    val name: String = "",
    val phone: String = "",
    val email: String = "",
    val riderCode: String = "",
    val vehicleType: String = "MOTORCYCLE", // "MOTORCYCLE", "BICYCLE", "SCOOTER"
    val vehiclePlate: String = "",
    val rating: Double = 5.0,
    val totalDeliveries: Int = 0,
    val acceptanceRate: Int = 100,
    val completionRate: Int = 100,
    val onTimeRate: Int = 100,
    val walletBalance: Double = 0.0,
    val floatingCashLimit: Double = 3500.0,
    val currentFloatingCash: Double = 0.0,
    val isOnline: Boolean = false,
    val availabilityStatus: String = "Offline", // "Available", "Busy", "Offline", "Pending Approval"
    val currentZone: String = "Gulshan & Banani Hub",
    val language: String = if (java.util.Locale.getDefault().language.startsWith("it")) "it" else "en",
    val autoAccept: Boolean = false,
    val profileImageUrl: String = "",
    val emergencyContact: String = "",
    val dutyStartTime: Long = 0L,
    // Rider Registration & Admin Approval fields
    val nidNumber: String = "",
    val licenseNumber: String = "",
    val approvalStatus: String = "NOT_REGISTERED", // "NOT_REGISTERED", "PENDING_APPROVAL", "APPROVED", "REJECTED"
    val rejectionReason: String = "",
    val registeredAt: Long = 0L
)
