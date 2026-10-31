package com.example.data

import android.util.Log
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions

object FirebaseRiderService {
    private const val TAG = "FirebaseRiderService"
    const val COLLECTION_RIDER_REQUESTS = "rider_requests"
    const val COLLECTION_RIDERS = "riders"

    val auth: FirebaseAuth
        get() = FirebaseAuth.getInstance()

    private val firestore: FirebaseFirestore
        get() = FirebaseFirestore.getInstance()

    private var requestsListener: ListenerRegistration? = null
    private var ridersListener: ListenerRegistration? = null
    private var ordersListener: ListenerRegistration? = null

    /**
     * 1. Register with email and password:
     * Creates user via createUserWithEmailAndPassword, then calls sendEmailVerification().
     * Does NOT save to rider_requests yet (only after email is verified).
     */
    fun createAuthUserAndSendEmail(
        rider: RiderRegistrationData,
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val email = rider.email.trim()
        val password = rider.password.trim()

        Log.i(TAG, "[FirebaseAuth] Creating Auth user for email: $email")

        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                val user = authResult.user
                if (user == null) {
                    onFailure(IllegalStateException("রেজিস্ট্রেশন হয়েছে কিন্তু ব্যবহারকারী আইডি পাওয়া যায়নি"))
                    return@addOnSuccessListener
                }
                val uid = user.uid
                Log.i(TAG, "[FirebaseAuth SUCCESS] User created with UID: $uid. Sending verification email...")

                // Call sendEmailVerification() on the new Firebase Auth user
                user.sendEmailVerification()
                    .addOnSuccessListener {
                        Log.i(TAG, "[FirebaseAuth] Verification email sent to: $email")
                        onSuccess(uid)
                    }
                    .addOnFailureListener { e ->
                        Log.w(TAG, "[FirebaseAuth] sendEmailVerification warning: ${e.message}")
                        // Still proceed to verification screen even if initial email sending throws a warning
                        onSuccess(uid)
                    }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "[FirebaseAuth ERROR] createUserWithEmailAndPassword failed: ${e.message}", e)
                onFailure(e)
            }
    }

    /**
     * Resend verification email to current user.
     */
    fun resendEmailVerification(
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val user = auth.currentUser
        if (user == null) {
            onFailure("লগইন সেশন পাওয়া যায়নি। অনুগ্রহ করে আবার লগইন করুন।")
            return
        }

        user.sendEmailVerification()
            .addOnSuccessListener {
                Log.i(TAG, "[FirebaseAuth] Verification email resent to: ${user.email}")
                onSuccess()
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "[FirebaseAuth] resendEmailVerification failed: ${e.message}", e)
                val friendly = when {
                    e is FirebaseTooManyRequestsException || e.message?.contains("too-many-requests", true) == true ->
                        "অনেক বেশি অনুরোধ পাঠানো হয়েছে। অনুগ্রহ করে কিছুক্ষণ পর আবার চেষ্টা করুন।"
                    else -> e.localizedMessage ?: "ইমেইল পাঠানো সম্ভব হয়নি"
                }
                onFailure(friendly)
            }
    }

    /**
     * Check if email has been verified by reloading current user.
     */
    fun checkEmailVerified(
        onResult: (Boolean) -> Unit
    ) {
        val user = auth.currentUser
        if (user == null) {
            onResult(false)
            return
        }

        user.reload()
            .addOnSuccessListener {
                val isVerified = auth.currentUser?.isEmailVerified == true
                Log.i(TAG, "[FirebaseAuth] Reloaded user. isEmailVerified=$isVerified")
                onResult(isVerified)
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "reload user error: ${e.message}")
                onResult(auth.currentUser?.isEmailVerified == true)
            }
    }

    /**
     * 3. Save to rider_requests only AFTER email is verified:
     * Saves emailVerified: true.
     * NEVER stores the password.
     */
    fun saveRiderRequestAfterEmailVerified(
        rider: RiderRegistrationData,
        onSuccess: (String) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val user = auth.currentUser
        val uid = user?.uid ?: rider.phoneNumber.takeLast(6)
        val email = user?.email?.ifBlank { rider.email.trim() } ?: rider.email.trim()

        Log.i(TAG, "[Firestore] Saving verified rider request to $COLLECTION_RIDER_REQUESTS/$uid (no password)")

        val requestData = hashMapOf<String, Any>(
            "id" to uid,
            "riderId" to uid,
            "riderCode" to uid,
            "authUid" to uid,
            "name" to rider.fullName,
            "fullName" to rider.fullName,
            "riderName" to rider.fullName,
            "phone" to rider.phoneNumber,
            "phoneNumber" to rider.phoneNumber,
            "mobile" to rider.phoneNumber,
            "email" to email,
            "nidNumber" to rider.nidNumber,
            "nationalId" to rider.nidNumber,
            "vehicleType" to rider.vehicleType,
            "vehicleNumber" to rider.vehicleNumber,
            "plateNumber" to rider.vehicleNumber,
            "licenseNumber" to rider.licenseNumber,
            "emergencyContact" to rider.emergencyContact,
            "location" to rider.city,
            "city" to rider.city,
            "zone" to rider.city,
            "status" to "pending",
            "approvalStatus" to "pending",
            "isApproved" to false,
            "approved" to false,
            "emailVerified" to true,
            "isOnline" to false,
            "active" to false,
            "role" to "rider",
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp(),
            "submittedAt" to System.currentTimeMillis(),
            "latitude" to 43.5933,
            "longitude" to 10.4735
        )

        firestore.collection(COLLECTION_RIDER_REQUESTS).document(uid)
            .set(requestData, SetOptions.merge())
            .addOnSuccessListener {
                Log.i(TAG, "[Firestore SUCCESS] Saved to $COLLECTION_RIDER_REQUESTS/$uid with emailVerified: true")
                onSuccess(uid)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "[Firestore ERROR] Failed to save verified rider request: ${e.message}", e)
                onFailure(e)
            }
    }

    /**
     * 2. Login: signInWithEmailAndPassword.
     * After login, read riders/{auth uid}.
     * Only if isApproved is true, open the app. Otherwise show the pending approval screen.
     * The rider must never be able to set isApproved or approvalStatus from the app.
     */
    fun loginRiderWithAuth(
        emailInput: String,
        passwordInput: String,
        onSuccess: (profile: RiderProfile, isApproved: Boolean) -> Unit,
        onFailure: (String) -> Unit
    ) {
        val email = emailInput.trim()
        val password = passwordInput.trim()

        if (email.isBlank()) {
            onFailure("দয়া করে আপনার ইমেইল লিখুন")
            return
        }
        if (password.isBlank()) {
            onFailure("দয়া করে আপনার পাসওয়ার্ড দিন")
            return
        }

        Log.i(TAG, "[FirebaseAuth] Attempting signInWithEmailAndPassword for: $email")

        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                val user = authResult.user
                if (user == null) {
                    onFailure("লগইন ত্রুটি: ব্যবহারকারী পাওয়া যায়নি")
                    return@addOnSuccessListener
                }
                val uid = user.uid
                Log.i(TAG, "[FirebaseAuth SUCCESS] Logged in successfully! UID: $uid. Reading $COLLECTION_RIDERS/$uid...")

                // Check riders/{auth uid}
                firestore.collection(COLLECTION_RIDERS).document(uid).get()
                    .addOnSuccessListener { riderDoc ->
                        if (riderDoc != null && riderDoc.exists()) {
                            val isApproved = riderDoc.getBoolean("isApproved") == true ||
                                    riderDoc.getBoolean("approved") == true ||
                                    riderDoc.getString("status")?.equals("approved", ignoreCase = true) == true ||
                                    riderDoc.getString("approvalStatus")?.equals("approved", ignoreCase = true) == true

                            val profile = mapDocumentToProfile(riderDoc, uid, email)

                            Log.i(TAG, "Rider document found in $COLLECTION_RIDERS. isApproved=$isApproved")
                            onSuccess(profile.copy(isApproved = isApproved), isApproved)
                        } else {
                            // Document in riders/{uid} does not exist yet (pending approval in rider_requests)
                            Log.i(TAG, "Document in $COLLECTION_RIDERS not found. Reading $COLLECTION_RIDER_REQUESTS/$uid...")
                            firestore.collection(COLLECTION_RIDER_REQUESTS).document(uid).get()
                                .addOnSuccessListener { reqDoc ->
                                    if (reqDoc != null && reqDoc.exists()) {
                                        val isApproved = reqDoc.getBoolean("isApproved") == true ||
                                                reqDoc.getBoolean("approved") == true ||
                                                reqDoc.getString("status")?.equals("approved", ignoreCase = true) == true ||
                                                reqDoc.getString("approvalStatus")?.equals("approved", ignoreCase = true) == true

                                        val profile = mapDocumentToProfile(reqDoc, uid, email)
                                        Log.i(TAG, "Rider request found in $COLLECTION_RIDER_REQUESTS. isApproved=$isApproved")
                                        onSuccess(profile.copy(isApproved = isApproved), isApproved)
                                    } else {
                                        val basicProfile = RiderProfile(
                                            riderId = uid,
                                            authUid = uid,
                                            email = email,
                                            fullName = user.displayName ?: "Rider",
                                            status = "pending",
                                            isApproved = false
                                        )
                                        onSuccess(basicProfile, false)
                                    }
                                }
                                .addOnFailureListener {
                                    val basicProfile = RiderProfile(
                                        riderId = uid,
                                        authUid = uid,
                                        email = email,
                                        status = "pending",
                                        isApproved = false
                                    )
                                    onSuccess(basicProfile, false)
                                }
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "Error fetching rider profile: ${e.message}", e)
                        onFailure("প্রোফাইল লোড ত্রুটি: ${e.localizedMessage}")
                    }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "[FirebaseAuth ERROR] signInWithEmailAndPassword failed: ${e.message}", e)
                val friendlyMessage = when {
                    e.message?.contains("no user record", ignoreCase = true) == true ||
                    e.message?.contains("user-not-found", ignoreCase = true) == true ->
                        "এই ইমেইলে কোনো রাইডার অ্যাকাউন্ট পাওয়া যায়নি। দয়া করে নতুন রেজিস্ট্রেশন করুন।"
                    e.message?.contains("wrong-password", ignoreCase = true) == true ||
                    e.message?.contains("invalid-credential", ignoreCase = true) == true ->
                        "ভুল ইমেইল অথবা পাসওয়ার্ড! দয়া করে সঠিক তথ্য দিন।"
                    e.message?.contains("invalid-email", ignoreCase = true) == true ->
                        "সঠিক ফরম্যাটের ইমেইল ঠিকানা দিন।"
                    e.message?.contains("network", ignoreCase = true) == true ->
                        "ইন্টারনেট সংযোগ চেক করুন।"
                    else -> e.localizedMessage ?: "লগইন করা যায়নি"
                }
                onFailure(friendlyMessage)
            }
    }

    /**
     * 3. Forgot password: Send password reset email using FirebaseAuth.
     */
    fun sendPasswordReset(
        email: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val cleanEmail = email.trim()
        if (cleanEmail.isBlank()) {
            onFailure("পাসওয়ার্ড রিসেটের জন্য আপনার ইমেইল ঠিকানা লিখুন")
            return
        }

        Log.i(TAG, "[FirebaseAuth] Sending password reset email to: $cleanEmail")

        auth.sendPasswordResetEmail(cleanEmail)
            .addOnSuccessListener {
                Log.i(TAG, "[FirebaseAuth SUCCESS] Password reset email sent to $cleanEmail")
                onSuccess()
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "[FirebaseAuth ERROR] sendPasswordResetEmail failed: ${e.message}", e)
                val friendlyMessage = when {
                    e.message?.contains("user-not-found", ignoreCase = true) == true ->
                        "এই ইমেইলে কোনো অ্যাকাউন্ট পাওয়া যায়নি।"
                    e.message?.contains("invalid-email", ignoreCase = true) == true ->
                        "দয়া করে সঠিক ইমেইল ঠিকানা লিখুন।"
                    else -> e.localizedMessage ?: "পাসওয়ার্ড রিসেট ইমেইল পাঠানো সম্ভব হয়নি"
                }
                onFailure(friendlyMessage)
            }
    }

    /**
     * Map Firestore Document to RiderProfile WITHOUT reading or storing any password.
     */
    private fun mapDocumentToProfile(
        doc: com.google.firebase.firestore.DocumentSnapshot,
        uid: String,
        authEmail: String
    ): RiderProfile {
        val status = doc.getString("status") ?: doc.getString("approvalStatus") ?: "pending"
        val isApproved = doc.getBoolean("isApproved") == true ||
                doc.getBoolean("approved") == true ||
                status.equals("approved", ignoreCase = true)

        return RiderProfile(
            riderId = uid,
            authUid = uid,
            fullName = doc.getString("fullName") ?: doc.getString("name") ?: "Rider",
            phoneNumber = doc.getString("phone") ?: doc.getString("phoneNumber").orEmpty(),
            email = doc.getString("email")?.ifBlank { authEmail } ?: authEmail,
            vehicleType = doc.getString("vehicleType") ?: "Motorcycle",
            vehicleNumber = doc.getString("vehicleNumber") ?: doc.getString("plateNumber").orEmpty(),
            licenseNumber = doc.getString("licenseNumber").orEmpty(),
            nidNumber = doc.getString("nidNumber").orEmpty(),
            emergencyContact = doc.getString("emergencyContact").orEmpty(),
            city = doc.getString("location") ?: doc.getString("city") ?: "Collesalvetti / Livorno (Italy)",
            status = status,
            isApproved = isApproved,
            isOnline = doc.getBoolean("isOnline") ?: false,
            latitude = doc.getDouble("latitude") ?: 43.5933,
            longitude = doc.getDouble("longitude") ?: 10.4735,
            todayEarnings = doc.getDouble("todayEarnings") ?: 0.0,
            todayDeliveries = (doc.getLong("todayDeliveries") ?: 0L).toInt(),
            cashInHand = doc.getDouble("cashInHand") ?: 0.0,
            emailVerified = doc.getBoolean("emailVerified") ?: true
        )
    }

    /**
     * Real-time listener for Rider approval status.
     * Listens to riders/{auth uid} and rider_requests/{auth uid}.
     */
    fun listenToApprovalStatus(
        riderId: String,
        onStatusChange: (isApproved: Boolean, status: String) -> Unit
    ) {
        stopListening()

        val idToListen = riderId.ifBlank { auth.currentUser?.uid.orEmpty() }
        if (idToListen.isBlank()) {
            Log.w(TAG, "Cannot listen to approval status: riderId is blank")
            return
        }

        Log.i(TAG, "[Firestore] Starting real-time approval listeners for rider '$idToListen'...")

        val checkSnapshot: (com.google.firebase.firestore.DocumentSnapshot?, String) -> Unit = { snapshot, collectionName ->
            if (snapshot != null && snapshot.exists()) {
                val status = snapshot.getString("status") ?: snapshot.getString("approvalStatus") ?: "pending"
                val isApproved = snapshot.getBoolean("isApproved") == true ||
                        snapshot.getBoolean("approved") == true ||
                        status.equals("approved", ignoreCase = true)

                Log.i(TAG, "[Firestore $collectionName] Live Rider Status: status=$status, isApproved=$isApproved")
                onStatusChange(isApproved, status)
            }
        }

        // 1. Listen to riders collection
        ridersListener = firestore.collection(COLLECTION_RIDERS).document(idToListen)
            .addSnapshotListener { snapshot, error ->
                if (error == null) {
                    checkSnapshot(snapshot, COLLECTION_RIDERS)
                }
            }

        // 2. Listen to rider_requests collection
        requestsListener = firestore.collection(COLLECTION_RIDER_REQUESTS).document(idToListen)
            .addSnapshotListener { snapshot, error ->
                if (error == null) {
                    checkSnapshot(snapshot, COLLECTION_RIDER_REQUESTS)
                }
            }
    }

    /**
     * Manual check for approval status across both collections.
     */
    fun checkApprovalStatus(
        riderId: String,
        onResult: (Boolean, String) -> Unit
    ) {
        val uid = riderId.ifBlank { auth.currentUser?.uid.orEmpty() }
        if (uid.isBlank()) {
            onResult(false, "not_registered")
            return
        }

        // Check riders/{auth uid} first
        firestore.collection(COLLECTION_RIDERS).document(uid).get()
            .addOnSuccessListener { doc ->
                if (doc != null && doc.exists()) {
                    val status = doc.getString("status") ?: doc.getString("approvalStatus") ?: "pending"
                    val isApproved = doc.getBoolean("isApproved") == true ||
                            doc.getBoolean("approved") == true ||
                            status.equals("approved", ignoreCase = true)
                    onResult(isApproved, status)
                } else {
                    // Check rider_requests/{auth uid}
                    firestore.collection(COLLECTION_RIDER_REQUESTS).document(uid).get()
                        .addOnSuccessListener { reqDoc ->
                            if (reqDoc != null && reqDoc.exists()) {
                                val status = reqDoc.getString("status") ?: reqDoc.getString("approvalStatus") ?: "pending"
                                val isApproved = reqDoc.getBoolean("isApproved") == true ||
                                        reqDoc.getBoolean("approved") == true ||
                                        status.equals("approved", ignoreCase = true)
                                onResult(isApproved, status)
                            } else {
                                onResult(false, "pending")
                            }
                        }
                        .addOnFailureListener {
                            onResult(false, "pending")
                        }
                }
            }
            .addOnFailureListener {
                onResult(false, "pending")
            }
    }

    /**
     * Update rider's live GPS coordinates in Firestore.
     */
    fun updateLiveLocation(lat: Double, lng: Double) {
        val uid = auth.currentUser?.uid ?: return
        val locData = hashMapOf(
            "latitude" to lat,
            "longitude" to lng,
            "lastLocationUpdate" to FieldValue.serverTimestamp()
        )
        firestore.collection(COLLECTION_RIDERS).document(uid).set(locData, SetOptions.merge())
        firestore.collection(COLLECTION_RIDER_REQUESTS).document(uid).set(locData, SetOptions.merge())
    }

    /**
     * Listen to dispatched orders in Firestore `orders` collection in real-time.
     */
    fun listenToDispatchedOrders(
        onOrderReceived: (DeliveryOrder) -> Unit
    ) {
        ordersListener?.remove()

        Log.i(TAG, "[Firestore] Listening for dispatched orders in 'orders' collection...")

        ordersListener = firestore.collection("orders")
            .whereIn("orderStatus", listOf("READY_FOR_PICKUP", "Ready For Handover", "DISPATCHED", "PENDING_RIDER", "PENDING"))
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(TAG, "Error listening for orders: ${error.message}", error)
                    return@addSnapshotListener
                }

                if (snapshot != null && !snapshot.isEmpty) {
                    for (doc in snapshot.documents) {
                        val assignedRider = doc.getString("riderId") ?: ""
                        val currentUid = auth.currentUser?.uid ?: ""
                        if (assignedRider.isNotBlank() && assignedRider != currentUid) {
                            continue
                        }

                        val orderId = doc.id
                        val orderNumber = doc.getString("orderNumber") ?: "#ORD-${orderId.take(6).uppercase()}"

                        val restaurantName = doc.getString("restaurantName") ?: "Pizzeria Ristorante Collesalvetti"
                        val restaurantAddress = doc.getString("restaurantAddress") ?: "Via Roma 12, 57014 Collesalvetti LI"
                        val restaurantPhone = doc.getString("restaurantPhone") ?: "+39 0586 940001"

                        val customerName = doc.getString("customerName") ?: "Cliente"
                        val customerPhone = doc.getString("customerPhone") ?: "+39 340 0000000"
                        val customerAddress = doc.getString("customerAddress") ?: "Via Garibaldi 45, Collesalvetti LI"
                        val customerNote = doc.getString("customerNote") ?: ""

                        val distanceKm = doc.getDouble("distanceKm") ?: 3.8
                        val estimatedMins = (doc.getLong("estimatedMinutes") ?: 18).toInt()

                        val deliveryFee = doc.getDouble("deliveryFee") ?: doc.getDouble("riderFee") ?: 5.50
                        val surgeBonus = doc.getDouble("surgeBonus") ?: 0.0
                        val riderEarnings = deliveryFee + surgeBonus

                        val paymentMethodRaw = doc.getString("paymentMethod") ?: doc.getString("paymentType") ?: "ONLINE"
                        val isCod = paymentMethodRaw.contains("COD", ignoreCase = true) || paymentMethodRaw.contains("CASH", ignoreCase = true)

                        val isBankPaid = !isCod || doc.getBoolean("isBankPaid") == true || doc.getBoolean("isPaid") == true
                        val paymentType = if (isCod) "CASH_ON_DELIVERY" else "BANK_PAID"

                        val totalBill = doc.getDouble("totalBill") ?: doc.getDouble("totalAmount") ?: 24.50
                        val collectAmount = if (isCod) totalBill else 0.0

                        val itemsSummary = doc.getString("itemsSummary") ?: "2x Pizza Margherita, 1x Coca-Cola"

                        val restLat = doc.getDouble("restaurantLat") ?: doc.getDouble("pickupLat") ?: 43.5933
                        val restLng = doc.getDouble("restaurantLng") ?: doc.getDouble("pickupLng") ?: 10.4735
                        val custLat = doc.getDouble("customerLat") ?: doc.getDouble("dropoffLat") ?: 43.5850
                        val custLng = doc.getDouble("customerLng") ?: doc.getDouble("dropoffLng") ?: 10.4850

                        val order = DeliveryOrder(
                            id = orderId,
                            orderNumber = orderNumber,
                            restaurantName = restaurantName,
                            restaurantAddress = restaurantAddress,
                            restaurantPhone = restaurantPhone,
                            restaurantLat = restLat,
                            restaurantLng = restLng,
                            customerName = customerName,
                            customerPhone = customerPhone,
                            customerAddress = customerAddress,
                            customerNote = customerNote,
                            customerLat = custLat,
                            customerLng = custLng,
                            distanceKm = distanceKm,
                            estimatedMinutes = estimatedMins,
                            riderEarningsEuro = riderEarnings,
                            deliveryFeeEuro = deliveryFee,
                            tipEuro = 0.0,
                            isBankPaidToRestaurant = isBankPaid,
                            paymentType = paymentType,
                            totalBillEuro = collectAmount,
                            itemsSummary = itemsSummary,
                            status = doc.getString("orderStatus") ?: "READY_FOR_PICKUP"
                        )

                        onOrderReceived(order)
                        break
                    }
                }
            }
    }

    fun acceptOrder(
        orderId: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val uid = auth.currentUser?.uid ?: ""
        val updateData = hashMapOf<String, Any>(
            "orderStatus" to "RIDER_ASSIGNED",
            "status" to "RIDER_ASSIGNED",
            "riderId" to uid,
            "assignedAt" to FieldValue.serverTimestamp()
        )
        firestore.collection("orders").document(orderId)
            .set(updateData, SetOptions.merge())
            .addOnSuccessListener {
                Log.i(TAG, "Order $orderId accepted by rider $uid")
                onSuccess()
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to accept order $orderId: ${e.message}", e)
                onFailure(e)
            }
    }

    fun rejectOrder(
        orderId: String,
        reason: String = "Declined by rider",
        onSuccess: () -> Unit
    ) {
        val uid = auth.currentUser?.uid ?: ""
        val updateData = hashMapOf<String, Any>(
            "lastDeclinedByRider" to uid,
            "declinedAt" to FieldValue.serverTimestamp(),
            "declineReason" to reason
        )
        firestore.collection("orders").document(orderId)
            .set(updateData, SetOptions.merge())
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener {
                onSuccess()
            }
    }

    fun updateOrderStatus(
        orderId: String,
        newStatus: String,
        onSuccess: () -> Unit
    ) {
        val updateData = hashMapOf<String, Any>(
            "orderStatus" to newStatus,
            "status" to newStatus,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        firestore.collection("orders").document(orderId)
            .set(updateData, SetOptions.merge())
            .addOnSuccessListener {
                onSuccess()
            }
    }

    fun signOut() {
        stopListening()
        try {
            auth.signOut()
            Log.i(TAG, "[FirebaseAuth] User signed out successfully")
        } catch (e: Exception) {
            Log.e(TAG, "signOut error: ${e.message}", e)
        }
    }

    fun stopListening() {
        requestsListener?.remove()
        requestsListener = null
        ridersListener?.remove()
        ridersListener = null
        ordersListener?.remove()
        ordersListener = null
    }
}
