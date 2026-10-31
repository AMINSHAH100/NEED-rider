package com.example.ui

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RiderViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "RiderViewModel"
    }

    // Screen State: Default is AUTH (Login / Register option screen)
    private val _screenState = MutableStateFlow(ScreenState.AUTH)
    val screenState: StateFlow<ScreenState> = _screenState.asStateFlow()

    // Auth Mode: LOGIN or REGISTER
    private val _authMode = MutableStateFlow(AuthMode.LOGIN)
    val authMode: StateFlow<AuthMode> = _authMode.asStateFlow()

    // Form inputs for Login (Email + Password)
    private val _loginIdentifier = MutableStateFlow("")
    val loginIdentifier: StateFlow<String> = _loginIdentifier.asStateFlow()

    private val _loginPassword = MutableStateFlow("")
    val loginPassword: StateFlow<String> = _loginPassword.asStateFlow()

    // Form inputs for Registration
    private val _registrationForm = MutableStateFlow(RiderRegistrationData())
    val registrationForm: StateFlow<RiderRegistrationData> = _registrationForm.asStateFlow()

    // Pending registration data held while completing verification
    private val _pendingRegistrationData = MutableStateFlow(RiderRegistrationData())
    val pendingRegistrationData: StateFlow<RiderRegistrationData> = _pendingRegistrationData.asStateFlow()

    // Email Verification States
    private val _isEmailVerified = MutableStateFlow(false)
    val isEmailVerified: StateFlow<Boolean> = _isEmailVerified.asStateFlow()

    private val _emailVerificationNotice = MutableStateFlow<String?>(
        "Verification email sent, check your inbox and spam folder."
    )
    val emailVerificationNotice: StateFlow<String?> = _emailVerificationNotice.asStateFlow()

    private val _isResendingEmail = MutableStateFlow(false)
    val isResendingEmail: StateFlow<Boolean> = _isResendingEmail.asStateFlow()

    private val _emailCooldownSeconds = MutableStateFlow(0)
    val emailCooldownSeconds: StateFlow<Int> = _emailCooldownSeconds.asStateFlow()

    private val _verificationError = MutableStateFlow<String?>(null)
    val verificationError: StateFlow<String?> = _verificationError.asStateFlow()

    private val _verificationSuccessMessage = MutableStateFlow<String?>(null)
    val verificationSuccessMessage: StateFlow<String?> = _verificationSuccessMessage.asStateFlow()

    private var emailCooldownJob: Job? = null

    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting.asStateFlow()

    private val _isCheckingStatus = MutableStateFlow(false)
    val isCheckingStatus: StateFlow<Boolean> = _isCheckingStatus.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _resetPasswordSuccess = MutableStateFlow<String?>(null)
    val resetPasswordSuccess: StateFlow<String?> = _resetPasswordSuccess.asStateFlow()

    private val _riderProfile = MutableStateFlow(RiderProfile())
    val riderProfile: StateFlow<RiderProfile> = _riderProfile.asStateFlow()

    // Real-time Incoming Order offer before acceptance
    private val _incomingOrder = MutableStateFlow<DeliveryOrder?>(null)
    val incomingOrder: StateFlow<DeliveryOrder?> = _incomingOrder.asStateFlow()

    // Currently accepted and active order being delivered
    private val _activeOrder = MutableStateFlow<DeliveryOrder?>(null)
    val activeOrder: StateFlow<DeliveryOrder?> = _activeOrder.asStateFlow()

    val locationStreamingManager = LocationStreamingManager(application)

    init {
        checkCurrentUserSession()
    }

    /**
     * Check if a Firebase Authentication user session exists on app start.
     */
    fun checkCurrentUserSession() {
        val currentUser = FirebaseRiderService.auth.currentUser
        if (currentUser != null && !currentUser.isAnonymous) {
            val uid = currentUser.uid
            val email = currentUser.email.orEmpty()
            Log.i(TAG, "Existing authenticated Firebase user found: $uid ($email). Verifying approval in riders collection...")

            FirebaseRiderService.checkApprovalStatus(uid) { isApproved, status ->
                _riderProfile.value = _riderProfile.value.copy(
                    riderId = uid,
                    authUid = uid,
                    email = email,
                    status = status,
                    isApproved = isApproved
                )
                if (isApproved) {
                    Log.i(TAG, "Rider is approved. Opening app dashboard.")
                    _screenState.value = ScreenState.DASHBOARD
                    locationStreamingManager.startLocationUpdates()
                } else if (status != "not_registered") {
                    Log.i(TAG, "Rider is registered and pending (status=$status). Opening pending approval screen.")
                    _screenState.value = ScreenState.PENDING_APPROVAL
                    startApprovalListener(uid)
                } else {
                    // Not registered yet or verification pending
                    _screenState.value = ScreenState.AUTH
                }
            }
        } else {
            _screenState.value = ScreenState.AUTH
        }
    }

    fun setAuthMode(mode: AuthMode) {
        _authMode.value = mode
        _errorMessage.value = null
        _resetPasswordSuccess.value = null
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun clearResetSuccess() {
        _resetPasswordSuccess.value = null
    }

    fun clearVerificationMessages() {
        _verificationError.value = null
        _verificationSuccessMessage.value = null
    }

    fun updateLoginFields(identifier: String? = null, password: String? = null) {
        if (identifier != null) _loginIdentifier.value = identifier
        if (password != null) _loginPassword.value = password
        _errorMessage.value = null
        _resetPasswordSuccess.value = null
    }

    fun updateFormField(
        fullName: String? = null,
        phoneNumber: String? = null,
        email: String? = null,
        password: String? = null,
        nidNumber: String? = null,
        vehicleType: String? = null,
        vehicleNumber: String? = null,
        licenseNumber: String? = null,
        emergencyContact: String? = null,
        city: String? = null
    ) {
        val current = _registrationForm.value
        _registrationForm.value = current.copy(
            fullName = fullName ?: current.fullName,
            phoneNumber = phoneNumber ?: current.phoneNumber,
            email = email ?: current.email,
            password = password ?: current.password,
            nidNumber = nidNumber ?: current.nidNumber,
            vehicleType = vehicleType ?: current.vehicleType,
            vehicleNumber = vehicleNumber ?: current.vehicleNumber,
            licenseNumber = licenseNumber ?: current.licenseNumber,
            emergencyContact = emergencyContact ?: current.emergencyContact,
            city = city ?: current.city
        )
        _errorMessage.value = null
    }

    /**
     * Start 60-second cooldown timer for Resend Email button.
     */
    private fun startEmailCooldown(seconds: Int = 60) {
        emailCooldownJob?.cancel()
        emailCooldownJob = viewModelScope.launch {
            for (i in seconds downTo 1) {
                _emailCooldownSeconds.value = i
                delay(1000L)
            }
            _emailCooldownSeconds.value = 0
        }
    }

    /**
     * 1. Registration: Create account with createUserWithEmailAndPassword.
     * Calls sendEmailVerification() on the new user and shows notice.
     * Transitions to VERIFICATION screen with a 60-second cooldown on Resend button.
     * Request is NOT sent to admin panel until email is verified.
     */
    fun submitRegistration() {
        val form = _registrationForm.value

        // Strict Validation
        if (form.fullName.trim().length < 3) {
            _errorMessage.value = "দয়া করে আপনার পূর্ণ নাম লিখুন (কমপক্ষে ৩ অক্ষর)"
            return
        }
        if (form.phoneNumber.trim().length < 8) {
            _errorMessage.value = "দয়া করে সঠিক মোবাইল নম্বর লিখুন"
            return
        }
        if (form.email.trim().isBlank() || !form.email.contains("@")) {
            _errorMessage.value = "দয়া করে সঠিক ইমেইল ঠিকানা দিন (Firebase Auth-এর জন্য আবশ্যক)"
            return
        }
        if (form.password.trim().length < 6) {
            _errorMessage.value = "নিরাপদ লগইনের জন্য কমপক্ষে ৬ অক্ষরের পাসওয়ার্ড দিন"
            return
        }
        if (form.vehicleNumber.trim().isBlank()) {
            _errorMessage.value = "যানবাহনের প্লেট নম্বর লিখুন (যেমন: LI 123 AA)"
            return
        }

        _isSubmitting.value = true
        _errorMessage.value = null

        FirebaseRiderService.createAuthUserAndSendEmail(
            rider = form,
            onSuccess = { uid ->
                _isSubmitting.value = false
                Log.i(TAG, "Rider user created in Firebase Auth! UID=$uid. Moving to VERIFICATION.")

                // Keep form in pending state without password
                _pendingRegistrationData.value = form.copy(password = "")
                _registrationForm.value = RiderRegistrationData()
                _loginIdentifier.value = form.email
                _loginPassword.value = ""

                // Reset verification states
                _isEmailVerified.value = false
                _verificationError.value = null
                _emailVerificationNotice.value = "Verification email sent, check your inbox and spam folder."
                _verificationSuccessMessage.value = "অ্যাকাউন্ট সফলভাবে তৈরি হয়েছে! এবার ইমেইল ভেরিফিকেশন সম্পন্ন করুন।"

                // Start 60-second cooldown on Resend email button
                startEmailCooldown(60)

                // Transition to VERIFICATION screen
                _screenState.value = ScreenState.VERIFICATION
            },
            onFailure = { e ->
                _isSubmitting.value = false
                val msg = when {
                    e.message?.contains("email-already-in-use", ignoreCase = true) == true ->
                        "এই ইমেইলটি ইতিমধ্যে ব্যবহৃত হয়েছে। লগইন করুন অথবা অন্য ইমেইল দিন।"
                    e.message?.contains("weak-password", ignoreCase = true) == true ->
                        "পাসওয়ার্ডটি অন্তত ৬ অক্ষরের হতে হবে।"
                    else -> e.localizedMessage ?: "রেজিস্ট্রেশন তৈরি করা যায়নি"
                }
                _errorMessage.value = msg
                Log.e(TAG, "Registration failed: $msg", e)
            }
        )
    }

    /**
     * 1. Resend verification email with a 60-second cooldown.
     */
    fun resendVerificationEmail() {
        if (_emailCooldownSeconds.value > 0) return

        _isResendingEmail.value = true
        _verificationError.value = null
        _verificationSuccessMessage.value = null

        FirebaseRiderService.resendEmailVerification(
            onSuccess = {
                _isResendingEmail.value = false
                _emailVerificationNotice.value = "Verification email sent, check your inbox and spam folder."
                _verificationSuccessMessage.value = "Verification email sent, check your inbox and spam folder."
                startEmailCooldown(60)
            },
            onFailure = { err ->
                _isResendingEmail.value = false
                _verificationError.value = err
            }
        )
    }

    /**
     * 2. "I have verified" button: calls user.reload() and checks emailVerified.
     * Shows a clear error if it is not verified yet.
     * 3 & 4. After email is verified, saves emailVerified: true in rider_requests document (never stores password),
     * and sends rider's request to admin panel.
     */
    fun onIHaveVerifiedClicked() {
        _isCheckingStatus.value = true
        _verificationError.value = null
        _verificationSuccessMessage.value = null

        FirebaseRiderService.checkEmailVerified { isVerified ->
            _isCheckingStatus.value = false
            _isEmailVerified.value = isVerified

            if (isVerified) {
                _verificationSuccessMessage.value = "ইমেইল সফলভাবে ভেরিফাইড হয়েছে! আবেদন অ্যাডমিন প্যানেলে পাঠানো হচ্ছে..."
                val form = _pendingRegistrationData.value
                Log.i(TAG, "Email is verified! Submitting request to Admin Panel...")

                _isSubmitting.value = true
                FirebaseRiderService.saveRiderRequestAfterEmailVerified(
                    rider = form,
                    onSuccess = { uid ->
                        _isSubmitting.value = false
                        _riderProfile.value = RiderProfile(
                            riderId = uid,
                            authUid = uid,
                            fullName = form.fullName,
                            phoneNumber = form.phoneNumber,
                            email = form.email,
                            vehicleType = form.vehicleType,
                            vehicleNumber = form.vehicleNumber,
                            licenseNumber = form.licenseNumber,
                            nidNumber = form.nidNumber,
                            emergencyContact = form.emergencyContact,
                            city = form.city,
                            status = "pending",
                            isApproved = false,
                            emailVerified = true
                        )

                        // Transition to PENDING_APPROVAL screen
                        _screenState.value = ScreenState.PENDING_APPROVAL
                        startApprovalListener(uid)
                    },
                    onFailure = { e ->
                        _isSubmitting.value = false
                        _verificationError.value = "এডমিন প্যানেলে আবেদন জমা দিতে সমস্যা হয়েছে: ${e.localizedMessage}"
                    }
                )
            } else {
                _verificationError.value = "ইমেইল এখনও ভেরিফাই হয়নি। আপনার ইনবক্স অথবা স্প্যাম ফোল্ডারে পাঠানো লিংকে ক্লিক করুন এবং তারপর 'I have verified' বাটনে চাপ দিন।"
            }
        }
    }

    fun cancelVerification() {
        _screenState.value = ScreenState.AUTH
        _verificationError.value = null
        _verificationSuccessMessage.value = null
    }

    /**
     * 2. Login: signInWithEmailAndPassword.
     * After login, read riders/{auth uid}. Only if isApproved is true, open the app.
     * Otherwise show the pending approval screen.
     */
    fun login() {
        val email = _loginIdentifier.value.trim()
        val pwd = _loginPassword.value.trim()

        if (email.isBlank() || !email.contains("@")) {
            _errorMessage.value = "দয়া করে সঠিক ইমেইল ঠিকানা লিখুন"
            return
        }
        if (pwd.isBlank()) {
            _errorMessage.value = "দয়া করে আপনার পাসওয়ার্ড লিখুন"
            return
        }

        _isSubmitting.value = true
        _errorMessage.value = null
        _resetPasswordSuccess.value = null

        FirebaseRiderService.loginRiderWithAuth(
            emailInput = email,
            passwordInput = pwd,
            onSuccess = { profile, isApproved ->
                _isSubmitting.value = false
                _riderProfile.value = profile
                _loginPassword.value = "" // Clear password from memory

                if (isApproved) {
                    Log.i(TAG, "Rider is APPROVED in riders/${profile.authUid}. Entering DASHBOARD.")
                    _screenState.value = ScreenState.DASHBOARD
                    locationStreamingManager.startLocationUpdates()
                } else {
                    Log.i(TAG, "Rider is PENDING in riders/${profile.authUid}. Entering PENDING_APPROVAL.")
                    _screenState.value = ScreenState.PENDING_APPROVAL
                    startApprovalListener(profile.riderId)
                }
            },
            onFailure = { errorMsg ->
                _isSubmitting.value = false
                _errorMessage.value = errorMsg
            }
        )
    }

    /**
     * 3. Forgot Password: Send reset email via sendPasswordResetEmail.
     */
    fun sendPasswordReset(emailInput: String) {
        val email = emailInput.trim().ifBlank { _loginIdentifier.value.trim() }
        if (email.isBlank() || !email.contains("@")) {
            _errorMessage.value = "পাসওয়ার্ড রিসেটের জন্য একটি সঠিক ইমেইল ঠিকানা লিখুন"
            return
        }

        _isSubmitting.value = true
        _errorMessage.value = null
        _resetPasswordSuccess.value = null

        FirebaseRiderService.sendPasswordReset(
            email = email,
            onSuccess = {
                _isSubmitting.value = false
                _resetPasswordSuccess.value = "পাসওয়ার্ড রিসেট লিংক $email ঠিকানায় পাঠানো হয়েছে। আপনার ইমেইল ইনবক্স চেক করুন।"
            },
            onFailure = { errorMsg ->
                _isSubmitting.value = false
                _errorMessage.value = errorMsg
            }
        )
    }

    /**
     * Listen in real-time to Admin Approval from needadmin.jotpot.it.
     * The rider app only observes changes; it never sets isApproved or approvalStatus.
     */
    fun startApprovalListener(riderId: String) {
        FirebaseRiderService.listenToApprovalStatus(riderId) { isApproved, status ->
            if (isApproved) {
                Log.i(TAG, "Rider approved by admin! Transitioning to DASHBOARD.")
                _riderProfile.value = _riderProfile.value.copy(
                    status = "approved",
                    isApproved = true
                )
                _screenState.value = ScreenState.DASHBOARD
                locationStreamingManager.startLocationUpdates()
            } else {
                _riderProfile.value = _riderProfile.value.copy(status = status, isApproved = false)
            }
        }
    }

    fun refreshApprovalStatus() {
        val riderId = _riderProfile.value.riderId.ifBlank { FirebaseRiderService.auth.currentUser?.uid.orEmpty() }
        if (riderId.isBlank()) return

        _isCheckingStatus.value = true
        FirebaseRiderService.checkApprovalStatus(riderId) { isApproved, status ->
            _isCheckingStatus.value = false
            if (isApproved) {
                _riderProfile.value = _riderProfile.value.copy(
                    status = "approved",
                    isApproved = true
                )
                _screenState.value = ScreenState.DASHBOARD
                locationStreamingManager.startLocationUpdates()
            } else {
                _riderProfile.value = _riderProfile.value.copy(status = status, isApproved = false)
            }
        }
    }

    fun toggleDutyStatus() {
        val current = _riderProfile.value
        if (!current.isApproved) return

        val newOnline = !current.isOnline
        _riderProfile.value = current.copy(isOnline = newOnline)
        if (newOnline) {
            // Automatically detect live location & broadcast to Firestore
            locationStreamingManager.startLocationUpdates()
            // Start listening for dispatched orders from needadmin.jotpot.it
            FirebaseRiderService.listenToDispatchedOrders { order ->
                if (_activeOrder.value == null) {
                    _incomingOrder.value = order
                }
            }
        } else {
            locationStreamingManager.stopLocationUpdates()
            _incomingOrder.value = null
        }
    }

    /**
     * Accept incoming order after previewing restaurant, client, distance, earnings & payment
     */
    fun acceptOrder(order: DeliveryOrder) {
        FirebaseRiderService.acceptOrder(
            orderId = order.id,
            onSuccess = {
                _activeOrder.value = order.copy(status = "RIDER_ASSIGNED")
                _incomingOrder.value = null
            },
            onFailure = {
                _activeOrder.value = order.copy(status = "RIDER_ASSIGNED")
                _incomingOrder.value = null
            }
        )
    }

    /**
     * Decline / Pass incoming order
     */
    fun rejectOrder(order: DeliveryOrder, reason: String = "Declined by rider") {
        FirebaseRiderService.rejectOrder(order.id, reason) {
            _incomingOrder.value = null
        }
    }

    /**
     * Complete active order delivery and credit rider earnings in Euros (€)
     */
    fun completeActiveOrder() {
        val active = _activeOrder.value ?: return
        val currentProfile = _riderProfile.value
        val earned = active.riderEarningsEuro
        val codCollected = if (active.paymentType == "CASH_ON_DELIVERY") active.totalBillEuro else 0.0

        FirebaseRiderService.updateOrderStatus(active.id, "DELIVERED") {
            _riderProfile.value = currentProfile.copy(
                todayEarnings = currentProfile.todayEarnings + earned,
                todayDeliveries = currentProfile.todayDeliveries + 1,
                cashInHand = currentProfile.cashInHand + codCollected
            )
            _activeOrder.value = null
        }
    }

    /**
     * Test order simulation to verify parameters
     */
    fun simulateTestOrder(isBankPaid: Boolean = true) {
        val testOrder = if (isBankPaid) {
            DeliveryOrder(
                id = "ORD-TEST-${System.currentTimeMillis() % 1000}",
                orderNumber = "#IT-7842",
                restaurantName = "Pizzeria Trattoria Da Mario",
                restaurantAddress = "Via Roma 42, 57014 Collesalvetti LI",
                restaurantPhone = "+39 0586 940112",
                restaurantLat = 43.5950,
                restaurantLng = 10.4725,
                customerName = "Marco Rossi",
                customerPhone = "+39 347 1234567",
                customerAddress = "Via Garibaldi 18, Collesalvetti LI (Scala B, Int. 4)",
                customerNote = "দয়া করে বেল বাজান, ৩য় তলায় লিফট আছে",
                customerLat = 43.5840,
                customerLng = 10.4860,
                distanceKm = 4.2,
                estimatedMinutes = 18,
                riderEarningsEuro = 6.80,
                deliveryFeeEuro = 5.50,
                tipEuro = 1.30,
                isBankPaidToRestaurant = true,
                paymentType = "BANK_PAID",
                totalBillEuro = 0.0,
                itemsSummary = "2x Pizza Diavola, 1x Tiramisù, 1x Acqua Minerale",
                status = "READY_FOR_PICKUP"
            )
        } else {
            DeliveryOrder(
                id = "ORD-TEST-${System.currentTimeMillis() % 1000}",
                orderNumber = "#IT-8914",
                restaurantName = "Ristorante Il Mandarino",
                restaurantAddress = "Via Livornese 88, 57014 Collesalvetti LI",
                restaurantPhone = "+39 0586 962341",
                restaurantLat = 43.5990,
                restaurantLng = 10.4680,
                customerName = "Giuseppe Bianchi",
                customerPhone = "+39 340 9876543",
                customerAddress = "Piazza della Repubblica 9, Collesalvetti LI",
                customerNote = "গেট দিয়ে ঢুকে ডান পাশের ডোরবেল চাপুন",
                customerLat = 43.5820,
                customerLng = 10.4910,
                distanceKm = 5.5,
                estimatedMinutes = 22,
                riderEarningsEuro = 8.20,
                deliveryFeeEuro = 6.50,
                tipEuro = 1.70,
                isBankPaidToRestaurant = false,
                paymentType = "CASH_ON_DELIVERY",
                totalBillEuro = 32.50,
                itemsSummary = "1x Spaghetti ai Frutti di Mare, 1x Fritto Misto, 2x Birra Moretti",
                status = "READY_FOR_PICKUP"
            )
        }
        _incomingOrder.value = testOrder
    }

    fun logout() {
        FirebaseRiderService.signOut()
        locationStreamingManager.stopLocationUpdates()
        _incomingOrder.value = null
        _activeOrder.value = null
        _riderProfile.value = RiderProfile()
        _loginIdentifier.value = ""
        _loginPassword.value = ""
        _registrationForm.value = RiderRegistrationData()
        _errorMessage.value = null
        _resetPasswordSuccess.value = null
        _screenState.value = ScreenState.AUTH
        _authMode.value = AuthMode.LOGIN
    }

    override fun onCleared() {
        super.onCleared()
        FirebaseRiderService.stopListening()
        locationStreamingManager.stopLocationUpdates()
    }
}
