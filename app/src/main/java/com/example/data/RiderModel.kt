package com.example.data

data class RiderRegistrationData(
    val fullName: String = "",
    val phoneNumber: String = "",
    val email: String = "",
    val password: String = "",
    val nidNumber: String = "",
    val vehicleType: String = "Motorcycle",
    val vehicleNumber: String = "",
    val licenseNumber: String = "",
    val emergencyContact: String = "",
    val city: String = "Collesalvetti / Livorno (Italy)"
)

data class RiderProfile(
    val riderId: String = "",
    val authUid: String = "",
    val fullName: String = "",
    val phoneNumber: String = "",
    val email: String = "",
    val vehicleType: String = "Motorcycle",
    val vehicleNumber: String = "",
    val licenseNumber: String = "",
    val nidNumber: String = "",
    val emergencyContact: String = "",
    val city: String = "Collesalvetti / Livorno (Italy)",
    val status: String = "pending",
    val isApproved: Boolean = false,
    val isOnline: Boolean = false,
    val latitude: Double = 43.5933,
    val longitude: Double = 10.4735,
    val todayEarnings: Double = 0.0,
    val todayDeliveries: Int = 0,
    val cashInHand: Double = 0.0,
    val emailVerified: Boolean = false,
    val phoneVerified: Boolean = false
)

data class DeliveryOrder(
    val id: String = "",
    val orderNumber: String = "",
    // Restaurant Details
    val restaurantName: String = "",
    val restaurantAddress: String = "",
    val restaurantPhone: String = "",
    val restaurantLat: Double = 43.5933,
    val restaurantLng: Double = 10.4735,
    // Customer Details
    val customerName: String = "",
    val customerPhone: String = "",
    val customerAddress: String = "",
    val customerNote: String = "",
    val customerLat: Double = 43.5850,
    val customerLng: Double = 10.4850,
    // Distance in KM
    val distanceKm: Double = 0.0,
    val estimatedMinutes: Int = 15,
    // Rider Earnings in Euros (€)
    val riderEarningsEuro: Double = 0.0,
    val deliveryFeeEuro: Double = 0.0,
    val tipEuro: Double = 0.0,
    // Payment Settlement: Bank Paid vs Cash On Delivery
    val isBankPaidToRestaurant: Boolean = true,
    val paymentType: String = "BANK_PAID", // "BANK_PAID" or "CASH_ON_DELIVERY"
    val totalBillEuro: Double = 0.0, // Amount to collect from customer if COD
    val itemsSummary: String = "",
    val status: String = "READY_FOR_PICKUP"
)

enum class ScreenState {
    AUTH,             // Shows Login and Registration options
    VERIFICATION,     // Email & Phone OTP verification before sending to admin
    PENDING_APPROVAL, // Shows pending status until Admin on needadmin.jotpot.it approves
    DASHBOARD         // Full app access when approved
}

enum class AuthMode {
    LOGIN,
    REGISTER
}
