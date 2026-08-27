package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.entity.EarningEntity
import com.example.data.local.entity.OrderEntity
import com.example.data.local.entity.QuestEntity
import com.example.data.local.entity.RiderProfileEntity
import com.example.data.local.entity.TransactionEntity
import com.example.data.repository.RiderRepository
import com.example.data.firebase.FirebaseRiderService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RiderViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: RiderRepository
    private val firebaseService = FirebaseRiderService()
    val profile: StateFlow<RiderProfileEntity?>
    val allOrders: StateFlow<List<OrderEntity>>
    val activeOrders: StateFlow<List<OrderEntity>>
    val pendingPingOrder: StateFlow<OrderEntity?>
    val allEarnings: StateFlow<List<EarningEntity>>
    val allTransactions: StateFlow<List<TransactionEntity>>
    val allQuests: StateFlow<List<QuestEntity>>

    // UI state variables
    private val _routeProgress = MutableStateFlow(0.15f)
    val routeProgress: StateFlow<Float> = _routeProgress.asStateFlow()

    private val _selectedTab = MutableStateFlow(0) // 0: Home, 1: Active Order, 2: Earnings, 3: Quests, 4: Profile
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    private val _showIncomingDialog = MutableStateFlow(false)
    val showIncomingDialog: StateFlow<Boolean> = _showIncomingDialog.asStateFlow()

    private val _currentIncomingOrder = MutableStateFlow<OrderEntity?>(null)
    val currentIncomingOrder: StateFlow<OrderEntity?> = _currentIncomingOrder.asStateFlow()

    private val _showCashCalculator = MutableStateFlow(false)
    val showCashCalculator: StateFlow<Boolean> = _showCashCalculator.asStateFlow()

    private val _showIssueReport = MutableStateFlow(false)
    val showIssueReport: StateFlow<Boolean> = _showIssueReport.asStateFlow()

    private val _showSosDialog = MutableStateFlow(false)
    val showSosDialog: StateFlow<Boolean> = _showSosDialog.asStateFlow()

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

        // Auto-populate initial database data if needed
        viewModelScope.launch(Dispatchers.IO) {
            AppDatabase.populateInitialData(db.riderDao())
        }
    }

    fun setTab(index: Int) {
        _selectedTab.value = index
    }

    fun toggleOnlineStatus() {
        viewModelScope.launch(Dispatchers.IO) {
            val current = profile.value?.isOnline ?: true
            repository.setOnlineStatus(!current)
            profile.value?.copy(isOnline = !current)?.let { updated ->
                firebaseService.updateRiderPresence(updated)
            }
            _snackbarMessage.value = if (!current) "You are now ONLINE! Ready for orders." else "You went OFFLINE."
        }
    }

    fun toggleLanguage() {
        viewModelScope.launch(Dispatchers.IO) {
            val current = profile.value?.language ?: "bn"
            val newLang = when (current) {
                "bn" -> "en"
                "en" -> "it"
                else -> "bn"
            }
            repository.updateLanguage(newLang)
            _snackbarMessage.value = when (newLang) {
                "bn" -> "ভাষা পরিবর্তন: বাংলা"
                "it" -> "Lingua cambiata: Italiano"
                else -> "Language changed: English"
            }
        }
    }

    fun setLanguage(lang: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateLanguage(lang)
            _snackbarMessage.value = when (lang) {
                "bn" -> "ভাষা পরিবর্তন: বাংলা"
                "it" -> "Lingua impostata: Italiano"
                else -> "Language set: English"
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
            val currentRiderId = profile.value?.riderCode ?: "RD-8842"
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

    // Order Actions
    fun acceptOrder(order: OrderEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateOrderStatus(order.id, "ACCEPTED")
            _showIncomingDialog.value = false
            _currentIncomingOrder.value = null
            _routeProgress.value = 0.1f
            _selectedTab.value = 1 // Switch to active order screen
            _snackbarMessage.value = "অর্ডার গ্রহণ করা হয়েছে! রেস্তোরাঁর দিকে রওনা দিন।"
        }
    }

    fun rejectOrder(order: OrderEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateOrderStatus(order.id, "REJECTED")
            _showIncomingDialog.value = false
            _currentIncomingOrder.value = null
            _snackbarMessage.value = "অর্ডার পাস করা হয়েছে।"
        }
    }

    fun updateOrderStep(order: OrderEntity, newStatus: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateOrderStatus(order.id, newStatus)
            when (newStatus) {
                "ARRIVED_AT_REST" -> {
                    _routeProgress.value = 1.0f
                    _snackbarMessage.value = "রেস্তোরাঁয় পৌঁছানো নিশ্চিত হয়েছে!"
                }
                "PICKED_UP" -> {
                    _routeProgress.value = 0.1f
                    _snackbarMessage.value = "খাবার পিকআপ সম্পন্ন! কাস্টমারের ঠিকানায় যান।"
                }
                "ARRIVED_AT_CUST" -> {
                    _routeProgress.value = 1.0f
                    _snackbarMessage.value = "কাস্টমারের ঠিকানায় পৌঁছেছেন!"
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
        viewModelScope.launch(Dispatchers.IO) {
            val totalEarned = order.deliveryFee + order.surgeBonus + tip
            // Update order status
            repository.updateOrderStatus(order.id, "DELIVERED")

            // Record earning
            val earning = EarningEntity(
                orderId = order.id,
                orderNumber = order.orderNumber,
                baseFee = order.deliveryFee * 0.7,
                distanceFee = order.deliveryFee * 0.3,
                surgeFee = order.surgeBonus,
                tip = tip,
                questBonus = 0.0,
                totalEarned = totalEarned,
                paymentMethod = order.paymentMethod,
                timestamp = System.currentTimeMillis()
            )
            repository.addEarning(earning)

            // Update rider wallet balance
            val currentWallet = profile.value?.walletBalance ?: 0.0
            repository.updateWalletBalance(currentWallet + totalEarned)

            // Update floating cash if COD
            if (order.paymentMethod == "COD") {
                val currentFloating = profile.value?.currentFloatingCash ?: 0.0
                repository.updateFloatingCash(currentFloating + order.totalBill)
            }

            // Update quest progress
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
            _snackbarMessage.value = "অভিনন্দন! অর্ডার সফলভাবে ডেলিভারি হয়েছে (আয়: ৳${String.format("%.0f", totalEarned)})"
        }
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

        val newOrder = OrderEntity(
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
            tip = 20.0,
            surgeBonus = 15.0,
            paymentMethod = if ((0..1).random() == 0) "COD" else "PREPAID",
            status = "NEW_PING",
            distanceKm = randomDistance,
            estimatedMinutes = (randomDistance * 5 + 8).toInt()
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
}
