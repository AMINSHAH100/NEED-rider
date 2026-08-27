package com.example.data.firebase

import android.util.Log
import com.example.data.local.entity.OrderEntity
import com.example.data.local.entity.RiderProfileEntity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class FirebaseRiderService {
    private val tag = "FirebaseRiderService"
    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    fun updateRiderPresence(profile: RiderProfileEntity) {
        try {
            val riderData = hashMapOf(
                "riderCode" to profile.riderCode,
                "name" to profile.name,
                "phone" to profile.phone,
                "isOnline" to profile.isOnline,
                "vehicleType" to profile.vehicleType,
                "vehiclePlate" to profile.vehiclePlate,
                "zone" to profile.currentZone,
                "rating" to profile.rating,
                "walletBalance" to profile.walletBalance,
                "currentFloatingCash" to profile.currentFloatingCash,
                "lastActive" to System.currentTimeMillis()
            )
            firestore.collection("riders").document(profile.riderCode)
                .set(riderData)
                .addOnSuccessListener {
                    Log.d(tag, "Rider presence synced to Firebase")
                }
                .addOnFailureListener { e ->
                    Log.w(tag, "Failed to sync rider presence", e)
                }
        } catch (e: Exception) {
            Log.e(tag, "Firebase Firestore error: ${e.message}")
        }
    }

    fun submitVehicleChangeRequest(
        riderId: String,
        ticketId: String,
        newType: String,
        newPlate: String,
        reason: String
    ) {
        try {
            val requestData = hashMapOf(
                "ticketId" to ticketId,
                "riderCode" to riderId,
                "requestedVehicleType" to newType,
                "requestedPlate" to newPlate,
                "reason" to reason,
                "status" to "PENDING_REVIEW",
                "timestamp" to System.currentTimeMillis()
            )
            firestore.collection("vehicle_change_requests").document(ticketId)
                .set(requestData)
                .addOnSuccessListener {
                    Log.d(tag, "Vehicle change request synced to Firebase (#$ticketId)")
                }
        } catch (e: Exception) {
            Log.e(tag, "Failed to send vehicle change ticket: ${e.message}")
        }
    }

    fun listenToRealtimeOrders(
        zone: String,
        onNewOrderReceived: (OrderEntity) -> Unit
    ): ListenerRegistration? {
        return try {
            firestore.collection("active_orders")
                .whereEqualTo("zone", zone)
                .whereEqualTo("status", "SEARCHING_RIDER")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w(tag, "Order listener failed", error)
                        return@addSnapshotListener
                    }
                    if (snapshot != null && !snapshot.isEmpty) {
                        for (doc in snapshot.documents) {
                            val orderNumber = doc.getString("orderNumber") ?: "ORD-${doc.id}"
                            val restaurantName = doc.getString("restaurantName") ?: "Popular Kitchen"
                            val customerName = doc.getString("customerName") ?: "Customer"
                            val fee = doc.getDouble("deliveryFee") ?: 85.0
                            val totalBill = doc.getDouble("totalBill") ?: 450.0
                            
                            val incomingOrder = OrderEntity(
                                orderNumber = orderNumber,
                                customerName = customerName,
                                customerPhone = doc.getString("customerPhone") ?: "+8801700000000",
                                customerAddress = doc.getString("customerAddress") ?: "Road 11, Banani, Dhaka",
                                restaurantName = restaurantName,
                                restaurantAddress = doc.getString("restaurantAddress") ?: "Block D, Gulshan 1, Dhaka",
                                restaurantPhone = doc.getString("restaurantPhone") ?: "+8801800000000",
                                itemsSummary = doc.getString("itemsSummary") ?: "1x Chicken Biryani, 1x Coke 500ml",
                                totalBill = totalBill,
                                deliveryFee = fee,
                                paymentMethod = doc.getString("paymentMethod") ?: "COD",
                                status = "NEW_PING",
                                distanceKm = doc.getDouble("distanceKm") ?: 2.4,
                                estimatedMinutes = (doc.getLong("estimatedMinutes") ?: 18).toInt(),
                                pickupOtp = doc.getString("pickupOtp") ?: "5821",
                                customerOtp = doc.getString("customerOtp") ?: "9412"
                            )
                            onNewOrderReceived(incomingOrder)
                        }
                    }
                }
        } catch (e: Exception) {
            Log.e(tag, "Error setting up realtime order listener: ${e.message}")
            null
        }
    }
}
