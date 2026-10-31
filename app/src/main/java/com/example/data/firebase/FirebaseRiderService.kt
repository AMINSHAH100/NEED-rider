package com.example.data.firebase

import android.util.Log
import com.example.data.local.entity.OrderEntity
import com.example.data.local.entity.RiderProfileEntity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions

class FirebaseRiderService {
    private val tag = "FirebaseRiderService"
    private val firestore: FirebaseFirestore?
        get() = try {
            FirebaseFirestore.getInstance()
        } catch (e: Throwable) {
            Log.w(tag, "Firestore not available: ${e.message}")
            null
        }

    fun updateRiderPresence(profile: RiderProfileEntity) {
        try {
            val db = firestore ?: return
            val riderData = hashMapOf(
                "riderCode" to profile.riderCode,
                "name" to profile.name,
                "phone" to profile.phone,
                "email" to profile.email,
                "isOnline" to profile.isOnline,
                "availabilityStatus" to profile.availabilityStatus,
                "vehicleType" to profile.vehicleType,
                "vehiclePlate" to profile.vehiclePlate,
                "zone" to profile.currentZone,
                "rating" to profile.rating,
                "walletBalance" to profile.walletBalance,
                "currentFloatingCash" to profile.currentFloatingCash,
                "lastActive" to System.currentTimeMillis()
            )
            db.collection("riders").document(profile.riderCode)
                .set(riderData, SetOptions.merge())
                .addOnSuccessListener {
                    Log.d(tag, "Rider presence synced to Firebase: ${profile.riderCode}")
                }
                .addOnFailureListener { e ->
                    Log.w(tag, "Failed to sync rider presence", e)
                }
        } catch (e: Exception) {
            Log.e(tag, "Firebase Firestore error: ${e.message}")
        }
    }

    /**
     * Requirement 1: Listen to new order assignments in Firestore `orders` collection
     * where orderStatus is 'READY_FOR_PICKUP' (or 'Ready For Handover') and isPlatformRiderEnabled is true.
     */
    fun listenToDispatchedOrders(
        onNewOrderReceived: (OrderEntity) -> Unit
    ): ListenerRegistration? {
        return try {
            val db = firestore ?: return null
            db.collection("orders")
                .whereIn("orderStatus", listOf("READY_FOR_PICKUP", "Ready For Handover"))
                .whereEqualTo("isPlatformRiderEnabled", true)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w(tag, "Dispatched orders listener error", error)
                        return@addSnapshotListener
                    }
                    if (snapshot != null && !snapshot.isEmpty) {
                        for (doc in snapshot.documents) {
                            val orderId = doc.id
                            val orderNumber = doc.getString("orderNumber") ?: "ORD-${orderId.take(6).uppercase()}"
                            val restaurantName = doc.getString("restaurantName") ?: "Popular Kitchen"
                            val restaurantAddress = doc.getString("restaurantAddress") ?: "Banani Road 11, Dhaka"
                            val restaurantPhone = doc.getString("restaurantPhone") ?: "+8801800000000"
                            val customerName = doc.getString("customerName") ?: "Customer"
                            val customerAddress = doc.getString("customerAddress") ?: "Gulshan 1, Dhaka"
                            val customerPhone = doc.getString("customerPhone") ?: "+8801700000000"
                            val customerNote = doc.getString("customerNote") ?: ""
                            val fee = doc.getDouble("deliveryFee") ?: 95.0
                            val totalBill = doc.getDouble("totalBill") ?: 550.0
                            val paymentMethod = doc.getString("paymentMethod") ?: "COD"
                            // Hidden Company Tip logic: online tips go to company_tip/admin_fund, rider tip is 0.0
                            val parsedCompanyTip = doc.getDouble("company_tip")
                                ?: doc.getDouble("admin_fund")
                                ?: (if (paymentMethod != "COD") doc.getDouble("tip") ?: 30.0 else 0.0)
                            val parsedAdminFund = parsedCompanyTip
                            val riderTip = 0.0 // Rider UI tip is forced to 0.00
                            val surge = doc.getDouble("surgeBonus") ?: 15.0
                            val itemsSummary = doc.getString("itemsSummary") ?: "Food Package x1"
                            val distanceKm = doc.getDouble("distanceKm") ?: 2.8
                            val estimatedMinutes = (doc.getLong("estimatedMinutes") ?: 18).toInt()
                            val restLat = doc.getDouble("restaurantLat") ?: 23.7937
                            val restLng = doc.getDouble("restaurantLng") ?: 90.4066
                            val custLat = doc.getDouble("customerLat") ?: 23.7785
                            val custLng = doc.getDouble("customerLng") ?: 90.4162

                            // 4-digit pickupPin & deliveryPin
                            val pickupPin = doc.getString("pickupPin")
                                ?: doc.getString("pickupOtp")
                                ?: (1000..9999).random().toString()
                            val deliveryPin = doc.getString("deliveryPin")
                                ?: doc.getString("customerOtp")
                                ?: (1000..9999).random().toString()

                            val incomingOrder = OrderEntity(
                                firestoreId = orderId,
                                orderNumber = orderNumber,
                                customerName = customerName,
                                customerPhone = customerPhone,
                                customerAddress = customerAddress,
                                customerLat = custLat,
                                customerLng = custLng,
                                customerNote = customerNote,
                                restaurantName = restaurantName,
                                restaurantAddress = restaurantAddress,
                                restaurantPhone = restaurantPhone,
                                restaurantLat = restLat,
                                restaurantLng = restLng,
                                itemsSummary = itemsSummary,
                                totalBill = totalBill,
                                deliveryFee = fee,
                                tip = riderTip,
                                companyTip = parsedCompanyTip,
                                adminFund = parsedAdminFund,
                                surgeBonus = surge,
                                paymentMethod = paymentMethod,
                                status = "READY_FOR_PICKUP",
                                distanceKm = distanceKm,
                                estimatedMinutes = estimatedMinutes,
                                pickupOtp = pickupPin,
                                pickupPin = pickupPin,
                                customerOtp = deliveryPin,
                                deliveryPin = deliveryPin,
                                isPlatformRiderEnabled = true
                            )
                            onNewOrderReceived(incomingOrder)
                        }
                    }
                }
        } catch (e: Exception) {
            Log.e(tag, "Error setting up dispatched orders listener: ${e.message}")
            null
        }
    }

    /**
     * Requirement 1: Allow rider to click "Accept Order" -> Update orderStatus to 'RIDER_ASSIGNED'
     * in Firestore orders/{orderId}. Also update rider's availability status.
     */
    fun acceptOrder(
        orderId: String,
        riderProfile: RiderProfileEntity,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val db = firestore
        if (db == null) {
            onSuccess()
            return
        }
        val docRef = if (orderId.isNotBlank()) db.collection("orders").document(orderId) else null
        val riderRef = db.collection("riders").document(riderProfile.riderCode)

        val updates = hashMapOf(
            "orderStatus" to "RIDER_ASSIGNED",
            "status" to "RIDER_ASSIGNED",
            "assignedRiderId" to riderProfile.riderCode,
            "assignedRiderName" to riderProfile.name,
            "assignedRiderPhone" to riderProfile.phone,
            "assignedRiderVehiclePlate" to profileVehicleInfo(riderProfile),
            "assignedAt" to System.currentTimeMillis()
        )

        docRef?.set(updates, SetOptions.merge())
            ?.addOnSuccessListener {
                Log.d(tag, "Order $orderId updated to RIDER_ASSIGNED")
                // Update rider availability to Busy
                riderRef.set(
                    mapOf(
                        "availabilityStatus" to "Busy",
                        "currentOrderId" to orderId,
                        "updatedAt" to System.currentTimeMillis()
                    ),
                    SetOptions.merge()
                )
                onSuccess()
            }
            ?.addOnFailureListener { e ->
                Log.w(tag, "Failed to accept order $orderId", e)
                onFailure(e)
            } ?: run {
                onSuccess()
            }
    }

    /**
     * Reject order with reason
     */
    fun rejectOrderWithReason(
        orderId: String,
        riderId: String,
        reason: String,
        onComplete: () -> Unit
    ) {
        try {
            val db = firestore ?: return
            if (orderId.isNotBlank()) {
                val rejectionData = hashMapOf(
                    "riderId" to riderId,
                    "reason" to reason,
                    "timestamp" to System.currentTimeMillis()
                )
                db.collection("orders").document(orderId)
                    .collection("rejections").document(riderId)
                    .set(rejectionData)

                // Also update rejectedRiders array on the order
                db.collection("orders").document(orderId)
                    .update("rejectedBy", com.google.firebase.firestore.FieldValue.arrayUnion(riderId))
            }
        } catch (e: Exception) {
            Log.w(tag, "Error logging rejection: ${e.message}")
        } finally {
            onComplete()
        }
    }

    /**
     * Requirement 2: Restaurant Pickup & Handover
     * Update orderStatus to 'OUT_FOR_DELIVERY' (or 'On the way')
     */
    fun confirmHandoverToDelivery(
        orderId: String,
        riderId: String,
        onSuccess: () -> Unit
    ) {
        val db = firestore
        if (db == null) {
            onSuccess()
            return
        }
        if (orderId.isNotBlank()) {
            val updates = hashMapOf(
                "orderStatus" to "OUT_FOR_DELIVERY",
                "status" to "OUT_FOR_DELIVERY",
                "handoverConfirmedAt" to System.currentTimeMillis(),
                "deliveryStatus" to "On the way"
            )
            db.collection("orders").document(orderId)
                .set(updates, SetOptions.merge())
                .addOnSuccessListener {
                    Log.d(tag, "Order $orderId status updated to OUT_FOR_DELIVERY")
                    onSuccess()
                }
                .addOnFailureListener {
                    onSuccess()
                }
        } else {
            onSuccess()
        }
    }

    /**
     * Requirement 3: LIVE GPS LOCATION STREAMING
     * As soon as order status changes to 'OUT_FOR_DELIVERY', stream live coordinates
     * to Firestore under `riders/{riderId}/location` and active order.
     */
    fun streamRiderLocation(
        riderId: String,
        orderId: String?,
        latitude: Double,
        longitude: Double,
        heading: Float = 0f,
        speedKmh: Float = 0f
    ) {
        try {
            val db = firestore ?: return
            val locationData = hashMapOf(
                "latitude" to latitude,
                "longitude" to longitude,
                "heading" to heading,
                "speed" to speedKmh,
                "timestamp" to System.currentTimeMillis(),
                "orderId" to (orderId ?: "")
            )

            // Stream to riders/{riderId}/location (document "current")
            db.collection("riders").document(riderId)
                .collection("location").document("current")
                .set(locationData)

            // Also mirror directly onto riders/{riderId} for convenience
            db.collection("riders").document(riderId)
                .set(mapOf("currentLocation" to locationData), SetOptions.merge())

            // Stream to order's riderLocation field
            if (!orderId.isNullOrBlank()) {
                db.collection("orders").document(orderId)
                    .set(mapOf("riderLocation" to locationData), SetOptions.merge())
            }
        } catch (e: Exception) {
            Log.w(tag, "Failed to stream GPS location: ${e.message}")
        }
    }

    /**
     * Requirement 4: CUSTOMER DELIVERY & DELIVERY PIN VERIFICATION (deliveryPin)
     * Compare entered PIN with deliveryPin stored in Firestore orders/{orderId}.
     * Upon success: update orderStatus to 'DELIVERED', update rider to 'Available'.
     */
    fun verifyAndCompleteDelivery(
        orderId: String,
        riderId: String,
        enteredPin: String,
        expectedPin: String,
        companyTip: Double = 0.0,
        paymentMethod: String = "PREPAID",
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val trimmed = enteredPin.trim()
        val isValid = (trimmed == expectedPin.trim())

        if (!isValid) {
            onError("ভুল ডেলিভারি পিন! কাস্টমারের ফোন থেকে সঠিক ৪ ডিজিট কোড ($expectedPin) নিন।")
            return
        }

        // Record Company Development Fund / company_tip
        if (companyTip > 0.0) {
            recordCompanyTip(orderId, companyTip, paymentMethod)
        }

        val db = firestore
        if (db == null) {
            onSuccess()
            return
        }

        if (orderId.isNotBlank()) {
            val updates = hashMapOf(
                "orderStatus" to "DELIVERED",
                "status" to "DELIVERED",
                "deliveredAt" to System.currentTimeMillis(),
                "verifiedPin" to trimmed,
                "company_tip" to companyTip,
                "admin_fund" to companyTip,
                "total_rider_earnings_tip_excluded" to true
            )
            db.collection("orders").document(orderId)
                .set(updates, SetOptions.merge())
                .addOnSuccessListener {
                    Log.d(tag, "Order $orderId DELIVERED successfully with company tip: $companyTip")
                    // Reset rider availability to 'Available'
                    db.collection("riders").document(riderId)
                        .set(
                            mapOf(
                                "availabilityStatus" to "Available",
                                "currentOrderId" to null,
                                "lastDeliveredOrderId" to orderId,
                                "updatedAt" to System.currentTimeMillis()
                            ),
                            SetOptions.merge()
                        )
                    onSuccess()
                }
                .addOnFailureListener { e ->
                    Log.w(tag, "Failed to complete delivery on Firebase", e)
                    // Still complete locally
                    onSuccess()
                }
        } else {
            // Local-only order simulation
            db.collection("riders").document(riderId)
                .set(
                    mapOf(
                        "availabilityStatus" to "Available",
                        "currentOrderId" to null,
                        "updatedAt" to System.currentTimeMillis()
                    ),
                    SetOptions.merge()
                )
            onSuccess()
        }
    }

    /**
     * Requirement: Record hidden customer tip to company_tip / admin_fund for company development
     */
    fun recordCompanyTip(orderId: String, amount: Double, paymentMethod: String) {
        try {
            val db = firestore ?: return
            if (amount > 0.0) {
                val fundDocId = if (orderId.isNotBlank()) orderId else "fund_${System.currentTimeMillis()}"
                val fundData = hashMapOf(
                    "orderId" to orderId,
                    "company_tip" to amount,
                    "admin_fund" to amount,
                    "paymentMethod" to paymentMethod,
                    "purpose" to "company_development_fund",
                    "status" to "ALLOCATED_TO_COMPANY_FUND",
                    "timestamp" to System.currentTimeMillis()
                )
                db.collection("admin_funds").document(fundDocId)
                    .set(fundData, SetOptions.merge())
                Log.d(tag, "Recorded ৳$amount to admin_funds for order $orderId")
            }
        } catch (e: Exception) {
            Log.w(tag, "Failed to record company tip: ${e.message}")
        }
    }

    /**
     * In-App Two-Way Communication with Restaurant App and Customer App
     */
    fun sendChatMessage(
        orderId: String,
        senderRole: String, // "RIDER"
        recipientRole: String, // "RESTAURANT" or "CUSTOMER"
        recipientName: String,
        text: String,
        onComplete: (Boolean) -> Unit
    ) {
        try {
            val db = firestore
            if (db == null) {
                onComplete(false)
                return
            }
            val effectiveOrderId = orderId.ifBlank { "ACTIVE_ORDER" }
            val messageData = hashMapOf(
                "senderRole" to senderRole,
                "recipientRole" to recipientRole,
                "recipientName" to recipientName,
                "text" to text,
                "timestamp" to System.currentTimeMillis(),
                "timeFormatted" to java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault()).format(java.util.Date())
            )

            db.collection("orders").document(effectiveOrderId)
                .collection("messages")
                .add(messageData)
                .addOnSuccessListener {
                    Log.d(tag, "Message sent to $recipientRole: $text")
                    onComplete(true)
                }
                .addOnFailureListener {
                    onComplete(false)
                }
        } catch (e: Exception) {
            Log.w(tag, "Failed to send chat message: ${e.message}")
            onComplete(false)
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
            val db = firestore ?: return
            val requestData = hashMapOf(
                "ticketId" to ticketId,
                "riderCode" to riderId,
                "requestedVehicleType" to newType,
                "requestedPlate" to newPlate,
                "reason" to reason,
                "status" to "PENDING_REVIEW",
                "timestamp" to System.currentTimeMillis()
            )
            db.collection("vehicle_change_requests").document(ticketId)
                .set(requestData)
        } catch (e: Exception) {
            Log.e(tag, "Failed to send vehicle change ticket: ${e.message}")
        }
    }

    /**
     * Requirement: Submit Rider Registration to Admin Panel in Firestore.
     * Writes document to Firestore `riders/{docId}` collection without failing silently.
     * Logs permission and network errors to console log (Logcat and println) with full details.
     */
    fun submitRiderRegistration(
        profile: RiderProfileEntity,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        try {
            val db = firestore
            if (db == null) {
                val error = IllegalStateException("FirebaseFirestore instance is null or FirebaseApp not initialized")
                Log.e(tag, "[Firestore ERROR] $error", error)
                println("[Firestore ERROR] $error")
                onFailure(error)
                return
            }

            val auth = try {
                com.google.firebase.auth.FirebaseAuth.getInstance()
            } catch (e: Throwable) {
                null
            }

            if (auth != null && auth.currentUser == null) {
                Log.w(tag, "[FirebaseAuth] auth.currentUser is null. Signing in anonymously before Firestore write...")
                auth.signInAnonymously()
                    .addOnSuccessListener { authResult ->
                        Log.i(tag, "[FirebaseAuth SUCCESS] Signed in anonymously before write: UID=${authResult.user?.uid}")
                        submitRiderRegistration(profile, onSuccess, onFailure)
                    }
                    .addOnFailureListener { authEx ->
                        Log.e(tag, "[FirebaseAuth ERROR] Pre-registration anonymous sign-in failed: ${authEx.message}", authEx)
                        onFailure(authEx)
                    }
                return
            }

            val docId = if (profile.riderCode.isNotBlank()) {
                profile.riderCode.trim().removePrefix("#")
            } else {
                auth?.currentUser?.uid ?: "RD-${(1000..9999).random()}"
            }

            val now = System.currentTimeMillis()
            val registrationData = hashMapOf<String, Any>(
                "id" to docId,
                "riderId" to docId,
                "riderCode" to docId,
                "code" to docId,
                "name" to profile.name,
                "fullName" to profile.name,
                "riderName" to profile.name,
                "phone" to profile.phone,
                "phoneNumber" to profile.phone,
                "mobile" to profile.phone,
                "email" to profile.email,
                "nid" to profile.nidNumber,
                "nidNumber" to profile.nidNumber,
                "nationalId" to profile.nidNumber,
                "licenseNumber" to profile.licenseNumber,
                "drivingLicense" to profile.licenseNumber,
                "drivingLicenseNumber" to profile.licenseNumber,
                "vehicleType" to profile.vehicleType,
                "vehiclePlate" to profile.vehiclePlate,
                "vehicleNumber" to profile.vehiclePlate,
                "plateNumber" to profile.vehiclePlate,
                "zone" to profile.currentZone,
                "currentZone" to profile.currentZone,
                "city" to (if (profile.currentZone.isNotBlank()) profile.currentZone else "Collesalvetti"),
                "emergencyContact" to profile.emergencyContact,
                "emergencyPhone" to profile.emergencyContact,
                "status" to "PENDING_APPROVAL",
                "approvalStatus" to "PENDING_APPROVAL",
                "isApproved" to false,
                "approved" to false,
                "isOnline" to false,
                "isActive" to false,
                "availabilityStatus" to "Pending Approval",
                "submittedAt" to now,
                "registeredAt" to now,
                "createdAt" to com.google.firebase.Timestamp.now(),
                "updatedAt" to com.google.firebase.Timestamp.now(),
                "timestamp" to now,
                "walletBalance" to 0.0,
                "rating" to 5.0
            )

            Log.i(tag, "[Firestore] Submitting rider registration to 'riders' collection for document ID: $docId")
            println("[Firestore] Submitting rider registration to 'riders' collection for document ID: $docId")

            // Primary write: FirebaseFirestore collection('riders').document(docId).set(data)
            db.collection("riders").document(docId)
                .set(registrationData, SetOptions.merge())
                .addOnSuccessListener {
                    Log.i(tag, "[Firestore SUCCESS] Rider document successfully created in 'riders/$docId'")
                    println("[Firestore SUCCESS] Rider document successfully created in 'riders/$docId'")

                    // Also mirror to auxiliary collections for Live Admin Dashboard (needadmin.jotpot.it)
                    try {
                        db.collection("rider_approvals").document(docId).set(registrationData, SetOptions.merge())
                        db.collection("admin_approval_requests").document(docId).set(registrationData, SetOptions.merge())
                        db.collection("rider_requests").document(docId).set(registrationData, SetOptions.merge())
                        db.collection("rider_applications").document(docId).set(registrationData, SetOptions.merge())
                    } catch (syncEx: Throwable) {
                        Log.w(tag, "[Firestore Aux Sync Warning] ${syncEx.message}")
                    }

                    onSuccess()
                }
                .addOnFailureListener { e ->
                    Log.e(tag, "[Firestore ERROR] Failed to save rider to 'riders/$docId': ${e.message}", e)
                    println("[Firestore ERROR] Failed to save rider to 'riders/$docId': ${e.message}")
                    if (e is com.google.firebase.firestore.FirebaseFirestoreException) {
                        Log.e(tag, "[Firestore ERROR Details] Code: ${e.code}, Message: ${e.localizedMessage}")
                        println("[Firestore ERROR Details] Code: ${e.code}, Message: ${e.localizedMessage}")
                    }
                    onFailure(e)
                }
        } catch (e: Exception) {
            Log.e(tag, "[Firestore UNCAUGHT EXCEPTION] Error during rider registration: ${e.message}", e)
            println("[Firestore UNCAUGHT EXCEPTION] Error during rider registration: ${e.message}")
            onFailure(e)
        }
    }

    /**
     * Requirement: Real-time listener for Admin approval on riders/{riderCode}
     */
    fun listenToRiderApproval(
        riderCode: String,
        onStatusChanged: (String, String?) -> Unit
    ): ListenerRegistration? {
        if (riderCode.isBlank()) return null
        return try {
            val db = firestore ?: return null
            val listenerCallback: (com.google.firebase.firestore.DocumentSnapshot?) -> Unit = { snapshot ->
                if (snapshot != null && snapshot.exists()) {
                    val approvalStatus = snapshot.getString("approvalStatus") ?: ""
                    val status = snapshot.getString("status") ?: ""
                    val isApprovedBool = snapshot.getBoolean("isApproved") == true || snapshot.getBoolean("approved") == true

                    val isApproved = approvalStatus.equals("APPROVED", ignoreCase = true) ||
                            status.equals("APPROVED", ignoreCase = true) ||
                            status.equals("ACTIVE", ignoreCase = true) ||
                            status.equals("ACCEPTED", ignoreCase = true) ||
                            isApprovedBool

                    val isRejected = approvalStatus.equals("REJECTED", ignoreCase = true) ||
                            status.equals("REJECTED", ignoreCase = true) ||
                            status.equals("DECLINED", ignoreCase = true)

                    if (isApproved) {
                        onStatusChanged("APPROVED", null)
                    } else if (isRejected) {
                        val reason = snapshot.getString("rejectionReason")
                            ?: snapshot.getString("reason")
                            ?: "তথ্য সঠিক নয়"
                        onStatusChanged("REJECTED", reason)
                    }
                }
            }

            val cleanCode = riderCode.trim().removePrefix("#")
            val primaryListener = db.collection("riders").document(cleanCode)
                .addSnapshotListener { snapshot, error ->
                    if (error == null) listenerCallback(snapshot)
                }

            if (cleanCode != riderCode) {
                db.collection("riders").document(riderCode)
                    .addSnapshotListener { snapshot, error ->
                        if (error == null) listenerCallback(snapshot)
                    }
            }

            db.collection("rider_approvals").document(cleanCode)
                .addSnapshotListener { snapshot, error ->
                    if (error == null) listenerCallback(snapshot)
                }

            db.collection("admin_approval_requests").document(cleanCode)
                .addSnapshotListener { snapshot, error ->
                    if (error == null) listenerCallback(snapshot)
                }

            primaryListener
        } catch (e: Exception) {
            Log.e(tag, "Failed to listen to approval status: ${e.message}")
            null
        }
    }

    /**
     * Admin Test Approval Helper: Updates Firestore document to APPROVED
     */
    fun approveRiderByAdmin(
        riderCode: String,
        onComplete: () -> Unit
    ) {
        if (riderCode.isBlank()) {
            onComplete()
            return
        }
        try {
            val db = firestore
            if (db == null) {
                onComplete()
                return
            }
            val approvalData = mapOf(
                "status" to "APPROVED",
                "approvalStatus" to "APPROVED",
                "approvedAt" to System.currentTimeMillis(),
                "availabilityStatus" to "Offline"
            )
            db.collection("riders").document(riderCode)
                .set(approvalData, SetOptions.merge())
                .addOnCompleteListener {
                    db.collection("admin_approval_requests").document(riderCode)
                        .set(approvalData, SetOptions.merge())
                    onComplete()
                }
        } catch (e: Exception) {
            onComplete()
        }
    }

    private fun profileVehicleInfo(profile: RiderProfileEntity): String {
        return "${profile.vehicleType} - ${profile.vehiclePlate}"
    }
}
