package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.firebase.FirebaseRiderService
import com.example.data.local.AppDatabase
import com.example.data.local.entity.EarningEntity
import com.example.data.local.entity.OrderEntity
import com.example.data.local.entity.QuestEntity
import com.example.data.local.entity.RiderProfileEntity
import com.example.data.local.entity.TransactionEntity
import com.example.data.repository.RiderRepository
import com.example.util.LocationStreamingManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RiderViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: RiderRepository
    private val firebaseService = FirebaseRiderService()
    val locationStreamingManager = LocationStreamingManager(application, viewModelScope, firebaseService)

    val profile: StateFlow<RiderProfileEntity?>
    val allOrders: StateFlow<List<OrderEntity>>
    val activeOrders: StateFlow<List<OrderEntity>>
    val pendingPingOrder: StateFlow<OrderEntity?>
    val allEarnings: StateFlow<List<EarningEntity>>
    val allTransactions: StateFlow<List<TransactionEntity>>
    val allQuests: StateFlow<List<QuestEntity>>

    val locationState: StateFlow<com.example.util.RiderLocationState> = locationStreamingManager.locationState

    val isLocationStreaming: StateFlow<Boolean> = locationStreamingManager.locationState
        .map { it.isStreaming }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    private var approvalListener: com.google.firebase.firestore.ListenerRegistration? = null

    // UI state variables
    private val _routeProgress = MutableStateFlow(0.15f)
    val routeProgress: StateFlow<Float> = _routeProgress.asStateFlow()

    private val _selectedTab = MutableStateFlow(0) // 0: Home, 1: Active Order, 2: Earnings, 3: Quests, 4: Profile
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    private val _showIncomingDialog = MutableStateFlow(false)
    val showIncomingDialog: StateFlow<Boolean> = _showIncomingDialog.asStateFlow()

    private val _currentIncomingOrder = MutableStateFlow<OrderEntity?>(null)
    val currentIncomingOrder: StateFlow<OrderEntity?> = _currentIncomingOrder.asStateFlow()

    // Reject order reason dialog state
    private val _showRejectReasonDialog = MutableStateFlow(false)
    val showRejectReasonDialog: StateFlow<Boolean> = _showRejectReasonDialog.asStateFlow()

    private val _orderToReject = MutableStateFlow<OrderEntity?>(null)
    val orderToReject: StateFlow<OrderEntity?> = _orderToReject.asStateFlow()

    // Delivery success dialog state
    private val _showDeliverySuccessDialog = MutableStateFlow(false)
    val showDeliverySuccessDialog: StateFlow<Boolean> = _showDeliverySuccessDialog.asStateFlow()

    private val _lastDeliveredOrder = MutableStateFlow<OrderEntity?>(null)
    val lastDeliveredOrder: StateFlow<OrderEntity?> = _lastDeliveredOrder.asStateFlow()

    private val _lastDeliveredTotalEarned = MutableStateFlow(0.0)
    val lastDeliveredTotalEarned: StateFlow<Double> = _lastDeliveredTotalEarned.asStateFlow()

    // Cash Calculator & other dialogs
    private val _showCashCalculator = MutableStateFlow(false)
    val showCashCalculator: StateFlow<Boolean> = _showCashCalculator.asStateFlow()

    private val _showIssueReport = MutableStateFlow(false)
    val showIssueReport: StateFlow<Boolean> = _showIssueReport.asStateFlow()

    private val _showSosDialog = MutableStateFlow(false)
    val showSosDialog: StateFlow<Boolean> = _showSosDialog.asStateFlow()

    private val _showLanguageDialog = MutableStateFlow(false)
    val showLanguageDialog: StateFlow<Boolean> = _showLanguageDialog.asStateFlow()

    private val _showWithdrawDialog = MutableStateFlow(false)
    val showWithdrawDialog: StateFlow<Boolean> = _showWithdrawDialog.asStateFlow()

    private val _showDepositDialog = MutableStateFlow(false)
    val showDepositDialog: StateFlow<Boolean> = _showDepositDialog.asStateFlow()

    private val _showChatSheet = MutableStateFlow(false)
    val showChatSheet: StateFlow<Boolean> = _showChatSheet.asStateFlow()

    private val _chatRecipient = MutableStateFlow("Customer")
    val chatRecipient: StateFlow<String> = _chatRecipient.asStateFlow()

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    init {
        val db = AppDatabase.getDatabase(application, viewModelScope)
        repository = RiderRepository(db.riderDao())

        profile = repository.riderProfile.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            RiderProfileEntity()
        )

        // Ensure default language is English (or Italian for Italian locale) unless user explicitly saved a language
        viewModelScope.launch(Dispatchers.IO) {
            val prefs = application.getSharedPreferences("rider_app_prefs", android.content.Context.MODE_PRIVATE)
            val savedLang = prefs.getString("selected_lang", null)
            if (savedLang != null) {
                repository.updateLanguage(savedLang)
            } else {
                val defaultLang = if (java.util.Locale.getDefault().language.startsWith("it")) "it" else "en"
                repository.updateLanguage(defaultLang)
            }
        }

        allOrders = repository.allOrders.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        activeOrders = repository.activeOrders.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        pendingPingOrder = repository.pendingPingOrder.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            null
        )

        allEarnings = repository.allEarnings.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        allTransactions = repository.allTransactions.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        allQuests = repository.allQuests.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        // Ensure initial database state and hook up approval & location listeners
        viewModelScope.launch(Dispatchers.IO) {
            val existing = repository.riderProfile.firstOrNull()
            if (existing == null) {
                AppDatabase.populateInitialData(db.riderDao())
            } else {
                if (existing.approvalStatus == "PENDING_APPROVAL" && existing.riderCode.isNotBlank()) {
                    listenToApprovalStatus(existing.riderCode)
                }
                locationStreamingManager.startContinuousTracking(existing.riderCode)
            }
        }

        // REQUIREMENT 1: Listen to dispatched orders in Firestore
        // where orderStatus is 'READY_FOR_PICKUP' and isPlatformRiderEnabled is true
        // Only active if rider is APPROVED and ONLINE
        viewModelScope.launch(Dispatchers.IO) {
            firebaseService.listenToDispatchedOrders { dispatchedOrder ->
                viewModelScope.launch(Dispatchers.IO) {
                    val currentProfile = profile.value ?: RiderProfileEntity()
                    val actives = repository.activeOrders.firstOrNull() ?: emptyList()
                    val isApproved = currentProfile.approvalStatus == "APPROVED"
                    if (isApproved && currentProfile.isOnline && actives.isEmpty() && !_showIncomingDialog.value) {
                        val rowId = repository.insertOrder(dispatchedOrder)
                        _currentIncomingOrder.value = dispatchedOrder.copy(id = rowId)
                        _showIncomingDialog.value = true
                    }
                }
            }
        }
    }

    fun setTab(index: Int) {
        _selectedTab.value = index
    }

    fun toggleOnlineStatus() {
        val currentProf = profile.value
        if (currentProf?.approvalStatus != "APPROVED") {
            _snackbarMessage.value = "এডমিন অনুমোদন ছাড়া ডিউটি চালু করা সম্ভব নয়।"
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            val current = currentProf.isOnline
            val newStatus = !current
            repository.setOnlineStatus(newStatus)
            val newAvailability = if (newStatus) "Available" else "Offline"
            repository.updateAvailabilityStatus(newAvailability)
            currentProf.copy(isOnline = newStatus, availabilityStatus = newAvailability).let { updated ->
                firebaseService.updateRiderPresence(updated)
            }
            _snackbarMessage.value = if (newStatus) "You are now ONLINE! Ready for orders." else "You went OFFLINE."
        }
    }

    /**
     * Requirement: Rider submits all registration details to Admin Panel
     */
    fun registerRider(
        name: String,
        phone: String,
        email: String,
        nidNumber: String,
        vehicleType: String,
        vehiclePlate: String,
        zone: String,
        emergencyContact: String,
        licenseNumber: String,
        onComplete: (Boolean, String?) -> Unit = { _, _ -> }
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val cleanCode = "RD-${(1000..9999).random()}"
            val newProfile = RiderProfileEntity(
                id = 1,
                name = name.trim(),
                phone = phone.trim(),
                email = email.trim(),
                riderCode = cleanCode,
                vehicleType = vehicleType,
                vehiclePlate = vehiclePlate.trim(),
                nidNumber = nidNumber.trim(),
                licenseNumber = licenseNumber.trim(),
                currentZone = zone.trim(),
                emergencyContact = emergencyContact.trim(),
                approvalStatus = "PENDING_APPROVAL",
                isOnline = false,
                availabilityStatus = "Pending Approval",
                registeredAt = System.currentTimeMillis()
            )

            android.util.Log.i("RiderViewModel", "[Firestore Write] Submitting rider registration to Firestore for $cleanCode (${newProfile.name})...")
            println("[RiderViewModel] Submitting rider registration to Firestore for $cleanCode (${newProfile.name})...")

            firebaseService.submitRiderRegistration(
                profile = newProfile,
                onSuccess = {
                    viewModelScope.launch(Dispatchers.IO) {
                        try {
                            repository.updateProfile(newProfile)
                            locationStreamingManager.startContinuousTracking(cleanCode)
                            listenToApprovalStatus(cleanCode)
                            _snackbarMessage.value = "আবেদন সফলভাবে Firestore-এ সংরক্ষিত হয়েছে! এডমিন অনুমোদনের অপেক্ষায় রয়েছে।"
                            android.util.Log.i("RiderViewModel", "[Firestore Write SUCCESS] Profile updated locally and navigated to Pending Approval: $cleanCode")
                            println("[RiderViewModel SUCCESS] Profile updated locally and navigated to Pending Approval: $cleanCode")
                            kotlinx.coroutines.withContext(Dispatchers.Main) {
                                onComplete(true, null)
                            }
                        } catch (e: Throwable) {
                            android.util.Log.e("RiderViewModel", "Error saving profile locally: ${e.message}", e)
                            println("[RiderViewModel ERROR] Local profile update error: ${e.message}")
                            kotlinx.coroutines.withContext(Dispatchers.Main) {
                                onComplete(true, null)
                            }
                        }
                    }
                },
                onFailure = { e ->
                    val errorDetail = e.localizedMessage ?: e.message ?: "Firestore write failed"
                    android.util.Log.e("RiderViewModel", "[Firestore Save Error] Failed to submit registration: $errorDetail", e)
                    println("[RiderViewModel ERROR] Failed to submit registration to Firestore: $errorDetail")
                    _snackbarMessage.value = "Firestore ত্রুটি: $errorDetail"
                    viewModelScope.launch(Dispatchers.Main) {
                        onComplete(false, errorDetail)
                    }
                }
            )
        }
    }

    fun resetRegistration() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateApprovalStatus("NOT_REGISTERED")
        }
    }

    /**
     * Listen to Admin approval for the rider
     */
    fun listenToApprovalStatus(riderCode: String) {
        approvalListener?.remove()
        approvalListener = firebaseService.listenToRiderApproval(riderCode) { newStatus, reason ->
            viewModelScope.launch(Dispatchers.IO) {
                if (newStatus == "APPROVED") {
                    repository.updateApprovalStatus("APPROVED")
                    repository.updateAvailabilityStatus("Offline")
                    _snackbarMessage.value = "🎉 অভিনন্দন! এডমিন প্যানেল আপনার আবেদন অনুমোদন করেছে। এখন ডিউটি শুরু করতে পারেন।"
                } else if (newStatus == "REJECTED") {
                    repository.updateApprovalStatus("REJECTED")
                    _snackbarMessage.value = "আপনার আবেদন প্রত্যাখ্যাত হয়েছে: ${reason ?: "তথ্য সঠিক নয়"}"
                }
            }
        }
    }

    /**
     * Quick Admin Simulation / Test Approval trigger from Under Review screen
     */
    fun adminSimulateApproval() {
        viewModelScope.launch(Dispatchers.IO) {
            val code = profile.value?.riderCode?.ifBlank { "#RD-9942" } ?: "#RD-9942"
            firebaseService.approveRiderByAdmin(code) {
                viewModelScope.launch(Dispatchers.IO) {
                    repository.updateApprovalStatus("APPROVED")
                    repository.updateAvailabilityStatus("Offline")
                    _snackbarMessage.value = "এডমিন টেস্ট অনুমোদন সম্পন্ন! আপনি এখন কাজ করতে পারবেন।"
                }
            }
        }
    }

    /**
     * Requirement: RESTAURANT PICKUP PIN VERIFICATION
     * Rider MUST enter and verify restaurant pickup PIN before order status changes to OUT_FOR_DELIVERY
     */
    fun verifyRestaurantPickupPin(
        order: OrderEntity,
        enteredPin: String,
        onResult: (Boolean, String) -> Unit
    ) {
        val expected = order.pickupPin.ifBlank { order.pickupOtp }.trim()
        val trimmed = enteredPin.trim()
        if (trimmed == expected) {
            updateOrderStep(order, "OUT_FOR_DELIVERY")
            onResult(true, "রেস্তোরাঁ পিকআপ পিন সফলভাবে যাচাই হয়েছে! খাবার সংগ্রহ সম্পন্ন।")
        } else {
            onResult(false, "ভুল রেস্তোরাঁ পিকআপ পিন! সঠিক ৪ ডিজিটের কোড ($expected) লিখুন।")
        }
    }

    fun toggleLanguage() {
        openLanguageDialog()
    }

    fun openLanguageDialog() {
        _showLanguageDialog.value = true
    }

    fun closeLanguageDialog() {
        _showLanguageDialog.value = false
    }

    fun setLanguage(lang: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val prefs = getApplication<Application>().getSharedPreferences("rider_app_prefs", android.content.Context.MODE_PRIVATE)
            prefs.edit().putString("selected_lang", lang).apply()
            repository.updateLanguage(lang)
            val langItem = com.example.util.AppStrings.supportedLanguages.find { it.code.equals(lang, ignoreCase = true) }
            val langName = langItem?.nativeName ?: lang.uppercase()
            _snackbarMessage.value = when (lang) {
                "it" -> "Lingua impostata: $langName 🇮🇹"
                "bn" -> "ভাষা পরিবর্তন: $langName 🇧🇩"
                "en" -> "Language set: $langName 🇬🇧"
                else -> "Language set: $langName (${langItem?.flag ?: ""})"
            }
        }
    }

    fun toggleAutoAccept() {
        viewModelScope.launch(Dispatchers.IO) {
            val current = profile.value?.autoAccept ?: false
            repository.updateAutoAccept(!current)
        }
    }

    fun setVehicleType(type: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateVehicleType(type)
        }
    }

    fun setZone(zone: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateZone(zone)
            _snackbarMessage.value = "Zone updated: $zone"
        }
    }

    fun requestVehicleChange(newType: String, newPlate: String, reason: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val currentLang = profile.value?.language ?: "bn"
            val currentRiderId = profile.value?.riderCode ?: "RD-9942"
            val ticketId = "REQ-${(1000..9999).random()}"
            val vehicleName = when (newType) {
                "BICYCLE" -> if (currentLang == "bn") "বাইসাইকেল" else "Bicycle"
                "SCOOTER" -> if (currentLang == "bn") "ই-স্কুটার" else "E-Scooter"
                else -> if (currentLang == "bn") "মোটরবাইক" else "Motorcycle"
            }

            // Sync ticket to Firebase
            firebaseService.submitVehicleChangeRequest(
                riderId = currentRiderId,
                ticketId = ticketId,
                newType = newType,
                newPlate = newPlate,
                reason = reason
            )

            _snackbarMessage.value = when (currentLang) {
                "bn" -> "যানবাহন পরিবর্তনের আবেদন ($vehicleName) প্রধান এডমিনের কাছে পাঠানো হয়েছে! টিকিট #$ticketId"
                "it" -> "Richiesta cambio veicolo ($vehicleName) inviata all'Admin! Ticket #$ticketId"
                else -> "Vehicle change request ($vehicleName) sent to Main Admin! Ticket #$ticketId"
            }
        }
    }

    // REQUIREMENT 1: Accept incoming order -> update orderStatus to 'RIDER_ASSIGNED' in Firestore
    fun acceptOrder(order: OrderEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            val currentProfile = profile.value ?: RiderProfileEntity()
            // Update Firestore orders/{orderId}
            firebaseService.acceptOrder(
                orderId = order.firestoreId,
                riderProfile = currentProfile,
                onSuccess = {
                    // Update local DB
                    viewModelScope.launch(Dispatchers.IO) {
                        repository.updateOrderStatus(order.id, "RIDER_ASSIGNED")
                        repository.updateAvailabilityStatus("Busy")
                    }
                },
                onFailure = {
                    viewModelScope.launch(Dispatchers.IO) {
                        repository.updateOrderStatus(order.id, "RIDER_ASSIGNED")
                        repository.updateAvailabilityStatus("Busy")
                    }
                }
            )

            _showIncomingDialog.value = false
            _currentIncomingOrder.value = null
            _routeProgress.value = 0.1f
            _selectedTab.value = 1 // Switch to active order screen
            _snackbarMessage.value = "অর্ডার গ্রহণ করা হয়েছে! রেস্তোরাঁর দিকে রওনা দিন।"
        }
    }

    // Prompt reject reason
    fun promptRejectReason(order: OrderEntity) {
        _orderToReject.value = order
        _showIncomingDialog.value = false
        _showRejectReasonDialog.value = true
    }

    fun dismissRejectReasonDialog() {
        _showRejectReasonDialog.value = false
        _orderToReject.value = null
    }

    // Confirm rejection with reason
    fun confirmRejectWithReason(reason: String) {
        val order = _orderToReject.value
        _showRejectReasonDialog.value = false
        _orderToReject.value = null

        if (order != null) {
            viewModelScope.launch(Dispatchers.IO) {
                firebaseService.rejectOrderWithReason(
                    orderId = order.firestoreId,
                    riderId = profile.value?.riderCode ?: "RD-9942",
                    reason = reason,
                    onComplete = {
                        viewModelScope.launch(Dispatchers.IO) {
                            repository.updateOrderStatus(order.id, "REJECTED")
                        }
                    }
                )
                _snackbarMessage.value = "অর্ডার প্রত্যাখ্যান করা হয়েছে (কারণ: $reason)"
            }
        }
    }

    fun rejectOrder(order: OrderEntity) {
        promptRejectReason(order)
    }

    // REQUIREMENT 2 & 3: Status transitions and Live GPS Location streaming
    fun updateOrderStep(order: OrderEntity, newStatus: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val riderId = profile.value?.riderCode ?: "RD-9942"
            repository.updateOrderStatus(order.id, newStatus)

            when (newStatus) {
                "ARRIVED_AT_REST" -> {
                    _routeProgress.value = 1.0f
                    _snackbarMessage.value = "রেস্তোরাঁয় পৌঁছানো নিশ্চিত হয়েছে! পিকআপ পিন (#${order.pickupPin.ifBlank { order.pickupOtp }}) স্টাফকে দেখান।"
                }

                // REQUIREMENT 2 & 3: Handover state -> status 'OUT_FOR_DELIVERY' & start background GPS streaming
                "OUT_FOR_DELIVERY" -> {
                    _routeProgress.value = 0.1f
                    firebaseService.confirmHandoverToDelivery(order.firestoreId, riderId) {
                        // Synced to Firestore
                    }

                    // Start live GPS location tracking
                    locationStreamingManager.startStreaming(
                        riderId = riderId,
                        orderId = order.firestoreId.ifBlank { order.orderNumber },
                        fromLat = order.restaurantLat,
                        fromLng = order.restaurantLng,
                        toLat = order.customerLat,
                        toLng = order.customerLng
                    )

                    _snackbarMessage.value = "খাবার সংগ্রহ সম্পন্ন! লাইভ জিপিএস লোকেশন শেয়ার হচ্ছে এবং কাস্টমারের ঠিকানায় রওনা দিন।"
                }

                "ARRIVED_AT_CUST" -> {
                    _routeProgress.value = 1.0f
                    _snackbarMessage.value = "কাস্টমারের ঠিকানায় পৌঁছেছেন! কাস্টমার ডেলিভারি পিন যাচাই করুন।"
                }
            }
        }
    }

    fun completeDelivery(
        order: OrderEntity,
        cashReceived: Double,
        changeToReturn: Double,
        tip: Double
    ) {
        val pin = order.deliveryPin.ifBlank { order.customerOtp }
        verifyAndCompleteDeliveryWithPin(order, pin, cashReceived, changeToReturn, tip)
    }

    // REQUIREMENT 4: CUSTOMER DELIVERY & DELIVERY PIN VERIFICATION (deliveryPin)
    fun verifyAndCompleteDeliveryWithPin(
        order: OrderEntity,
        enteredPin: String,
        cashReceived: Double,
        changeToReturn: Double,
        tip: Double
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val riderId = profile.value?.riderCode ?: "RD-9942"
            val expectedPin = order.deliveryPin.ifBlank { order.customerOtp }
            // Customer Tip Split: Online tips go to companyTip / adminFund, rider tip is strictly 0.00
            val actualCompanyTip = if (order.companyTip > 0.0) order.companyTip else (if (tip > 0.0) tip else 0.0)

            firebaseService.verifyAndCompleteDelivery(
                orderId = order.firestoreId,
                riderId = riderId,
                enteredPin = enteredPin,
                expectedPin = expectedPin,
                companyTip = actualCompanyTip,
                paymentMethod = order.paymentMethod,
                onSuccess = {
                    viewModelScope.launch(Dispatchers.IO) {
                        // 1. Stop background GPS location tracking
                        locationStreamingManager.stopStreaming()

                        // 2. Mark order delivered locally
                        // Hidden Company Tip logic: Rider earnings strictly exclude tips
                        val totalEarned = order.deliveryFee + order.surgeBonus
                        repository.updateOrderStatus(order.id, "DELIVERED")

                        // 3. Reset rider availability back to 'Available'
                        repository.updateAvailabilityStatus("Available")

                        // 4. Record earning with tip forced to 0.0 for rider UI and companyTip/adminFund stored
                        val earning = EarningEntity(
                            orderId = order.id,
                            orderNumber = order.orderNumber,
                            baseFee = order.deliveryFee * 0.7,
                            distanceFee = order.deliveryFee * 0.3,
                            surgeFee = order.surgeBonus,
                            tip = 0.0, // Strictly forced to 0.00
                            companyTip = actualCompanyTip,
                            adminFund = actualCompanyTip,
                            questBonus = 0.0,
                            totalEarned = totalEarned,
                            paymentMethod = order.paymentMethod,
                            timestamp = System.currentTimeMillis()
                        )
                        repository.addEarning(earning)

                        // 5. Update rider wallet balance (excluding tips)
                        val currentWallet = profile.value?.walletBalance ?: 0.0
                        repository.updateWalletBalance(currentWallet + totalEarned)

                        // 6. Update floating cash if COD
                        if (order.paymentMethod == "COD") {
                            val currentFloating = profile.value?.currentFloatingCash ?: 0.0
                            repository.updateFloatingCash(currentFloating + order.totalBill)
                        }

                        // 7. Update quest progress
                        val quests = allQuests.value
                        for (q in quests) {
                            if (!q.isCompleted && q.currentCount < q.targetCount) {
                                val updatedCount = q.currentCount + 1
                                val isDone = updatedCount >= q.targetCount
                                repository.updateQuest(
                                    q.copy(
                                        currentCount = updatedCount,
                                        isCompleted = isDone
                                    )
                                )
                            }
                        }

                        _showCashCalculator.value = false
                        _routeProgress.value = 0.0f
                        _lastDeliveredOrder.value = order
                        _lastDeliveredTotalEarned.value = totalEarned
                        _showDeliverySuccessDialog.value = true
                        _snackbarMessage.value = "অভিনন্দন! ডেলিভারি সফলভাবে সম্পন্ন হয়েছে (আয়: ৳${String.format("%.0f", totalEarned)})"
                    }
                },
                onError = { errorMsg ->
                    _snackbarMessage.value = errorMsg
                }
            )
        }
    }

    fun dismissDeliverySuccessDialog() {
        _showDeliverySuccessDialog.value = false
        _lastDeliveredOrder.value = null
        _selectedTab.value = 0 // Return to Home dashboard
    }

    // Simulate New Incoming Order for testing & demo
    fun simulateNewIncomingOrder() {
        val sampleNames = listOf("Secret Recipe", "Burger King", "Khabar Dabar", "Chillox", "Takeout")
        val sampleAreas = listOf("Banani 11", "Dhanmondi 27", "Gulshan-2", "Uttara Sector 3")
        val randomName = sampleNames.random()
        val randomArea = sampleAreas.random()
        val randomFee = (75..140).random().toDouble()
        val randomBill = (450..1350).random().toDouble()
        val randomDistance = (1.5 + (0..30).random() / 10.0)
        val isOnlinePrepaid = (0..1).random() == 1
        val onlineCustomerTip = if (isOnlinePrepaid) 30.0 else 0.0

        val newOrder = OrderEntity(
            firestoreId = "SIM-${(10000..99999).random()}",
            orderNumber = "#FD-${(1000..9999).random()}",
            restaurantName = "$randomName ($randomArea)",
            restaurantAddress = "Road 8, Block C, $randomArea, Dhaka",
            restaurantPhone = "+880 1819-223344",
            customerName = "Arif Chowdhury (আরিফ চৌধুরী)",
            customerAddress = "House 24, Road 4, Sector 7, Dhaka",
            customerPhone = "+880 1711-889900",
            customerNote = "বাসার কলিংবেল বাজাবেন।",
            itemsSummary = "Classic Beef Burger x2, Large Fries x1, Coke Zero x2",
            totalBill = randomBill,
            deliveryFee = randomFee,
            tip = 0.0, // Rider UI tip is forced to 0.00
            companyTip = onlineCustomerTip,
            adminFund = onlineCustomerTip,
            surgeBonus = 15.0,
            paymentMethod = if (isOnlinePrepaid) "PREPAID" else "COD",
            status = "READY_FOR_PICKUP",
            distanceKm = randomDistance,
            estimatedMinutes = (randomDistance * 5 + 8).toInt(),
            pickupPin = (1000..9999).random().toString(),
            deliveryPin = (1000..9999).random().toString(),
            isPlatformRiderEnabled = true
        )

        viewModelScope.launch(Dispatchers.IO) {
            val id = repository.insertOrder(newOrder)
            _currentIncomingOrder.value = newOrder.copy(id = id)
            _showIncomingDialog.value = true
        }
    }

    fun stepRouteProgress() {
        val current = _routeProgress.value
        val next = (current + 0.25f).coerceAtMost(1.0f)
        _routeProgress.value = next
    }

    fun resetRouteProgress() {
        _routeProgress.value = 0.0f
    }

    fun refreshCurrentGpsLocation() {
        locationStreamingManager.refreshCurrentLocation()
        _snackbarMessage.value = "রিয়েল জিপিএস লোকেশন আপডেট হয়েছে"
    }

    fun sendChatMessage(recipientRole: String, text: String) {
        val activeOrder = activeOrders.value.firstOrNull()
        val orderId = activeOrder?.firestoreId ?: activeOrder?.orderNumber ?: "ACTIVE_ORDER"
        val recipientName = if (recipientRole == "RESTAURANT") {
            activeOrder?.restaurantName ?: "Restaurant"
        } else {
            activeOrder?.customerName ?: "Customer"
        }

        firebaseService.sendChatMessage(
            orderId = orderId,
            senderRole = "RIDER",
            recipientRole = recipientRole,
            recipientName = recipientName,
            text = text
        ) { success ->
            if (success) {
                _snackbarMessage.value = if (recipientRole == "RESTAURANT") "রেস্তোরাঁ অ্যাপে মেসেজ পাঠানো হয়েছে" else "কাস্টমার অ্যাপে মেসেজ পাঠানো হয়েছে"
            }
        }
    }

    // Dialog openers
    fun openCashCalculator() { _showCashCalculator.value = true }
    fun closeCashCalculator() { _showCashCalculator.value = false }

    fun openIssueReport() { _showIssueReport.value = true }
    fun closeIssueReport() { _showIssueReport.value = false }

    fun openSosDialog() { _showSosDialog.value = true }
    fun closeSosDialog() { _showSosDialog.value = false }

    fun openWithdrawDialog() { _showWithdrawDialog.value = true }
    fun closeWithdrawDialog() { _showWithdrawDialog.value = false }

    fun openDepositDialog() { _showDepositDialog.value = true }
    fun closeDepositDialog() { _showDepositDialog.value = false }

    fun openChat(recipient: String) {
        _chatRecipient.value = recipient
        _showChatSheet.value = true
    }
    fun closeChat() { _showChatSheet.value = false }

    fun dismissIncomingDialog() {
        _showIncomingDialog.value = false
        _currentIncomingOrder.value = null
    }

    fun clearSnackbar() { _snackbarMessage.value = null }

    // Quests
    fun claimQuest(quest: QuestEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateQuest(quest.copy(isClaimed = true))
            val currentWallet = profile.value?.walletBalance ?: 0.0
            repository.updateWalletBalance(currentWallet + quest.rewardAmount)

            // Add transaction log
            repository.addTransaction(
                TransactionEntity(
                    title = "Quest Bonus Claimed: ${quest.title}",
                    titleBn = "কোয়েস্ট বোনাস প্রাপ্তি: ${quest.titleBn}",
                    type = "QUEST_REWARD",
                    amount = quest.rewardAmount,
                    method = "Platform Bonus",
                    referenceNumber = "QST-${quest.id}9921",
                    status = "COMPLETED"
                )
            )
            _snackbarMessage.value = "৳${quest.rewardAmount.toInt()} বোনাস ওয়ালেটে যোগ হয়েছে!"
        }
    }

    // Financial Actions
    fun withdrawFunds(amount: Double, method: String, accountNo: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val current = profile.value?.walletBalance ?: 0.0
            if (amount <= current) {
                repository.updateWalletBalance(current - amount)
                repository.addTransaction(
                    TransactionEntity(
                        title = "Cashout to $method",
                        titleBn = "$method এ ক্যাশআউট",
                        type = "CASHOUT",
                        amount = amount,
                        method = "$method ($accountNo)",
                        referenceNumber = "WD-${System.currentTimeMillis().toString().takeLast(6)}",
                        status = "COMPLETED"
                    )
                )
                _showWithdrawDialog.value = false
                _snackbarMessage.value = "৳${amount.toInt()} $method এ সফলভাবে ট্রান্সফার করা হয়েছে!"
            }
        }
    }

    fun depositFloatingCash(amount: Double, method: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val current = profile.value?.currentFloatingCash ?: 0.0
            if (amount <= current) {
                repository.updateFloatingCash(current - amount)
                repository.addTransaction(
                    TransactionEntity(
                        title = "COD Remittance via $method",
                        titleBn = "$method মাধ্যমে ক্যাশ জমা",
                        type = "COD_DEPOSIT",
                        amount = amount,
                        method = method,
                        referenceNumber = "DEP-${System.currentTimeMillis().toString().takeLast(6)}",
                        status = "COMPLETED"
                    )
                )
                _showDepositDialog.value = false
                _snackbarMessage.value = "৳${amount.toInt()} ফ্লোটিং ক্যাশ জমা সম্পন্ন হয়েছে!"
            }
        }
    }

    fun submitIssue(title: String, note: String) {
        _showIssueReport.value = false
        _snackbarMessage.value = "সমস্যা রিপোর্ট পাঠানো হয়েছে। হেল্পডেস্ক টিম আপনার সাথে যোগাযোগ করবে।"
    }

    override fun onCleared() {
        super.onCleared()
        approvalListener?.remove()
        approvalListener = null
        locationStreamingManager.stopStreaming()
    }
}
