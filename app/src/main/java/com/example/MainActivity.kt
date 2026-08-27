package com.example

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.DeliveryDining
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Navigation
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.CashCalculatorDialog
import com.example.ui.components.ChatBottomSheet
import com.example.ui.components.DepositCodDialog
import com.example.ui.components.EmergencySosDialog
import com.example.ui.components.IssueReportDialog
import com.example.ui.components.OrderIncomingDialog
import com.example.ui.components.WithdrawDialog
import com.example.ui.screens.ActiveOrderScreen
import com.example.ui.screens.EarningsScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.QuestsScreen
import com.example.ui.theme.FoodRiderTheme
import com.example.ui.theme.OfflineRed
import com.example.ui.theme.OnlineGreen
import com.example.ui.theme.RiderPrimary
import com.example.viewmodel.RiderViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: RiderViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FoodRiderTheme {
                RiderMainApp(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RiderMainApp(viewModel: RiderViewModel) {
    val context = LocalContext.current
    val profile by viewModel.profile.collectAsStateWithLifecycle()
    val activeOrders by viewModel.activeOrders.collectAsStateWithLifecycle()
    val allEarnings by viewModel.allEarnings.collectAsStateWithLifecycle()
    val allTransactions by viewModel.allTransactions.collectAsStateWithLifecycle()
    val allQuests by viewModel.allQuests.collectAsStateWithLifecycle()
    val pendingPingOrder by viewModel.pendingPingOrder.collectAsStateWithLifecycle()

    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val routeProgress by viewModel.routeProgress.collectAsStateWithLifecycle()

    // Dialog states
    val showIncomingDialog by viewModel.showIncomingDialog.collectAsStateWithLifecycle()
    val currentIncomingOrder by viewModel.currentIncomingOrder.collectAsStateWithLifecycle()
    val showCashCalculator by viewModel.showCashCalculator.collectAsStateWithLifecycle()
    val showIssueReport by viewModel.showIssueReport.collectAsStateWithLifecycle()
    val showSosDialog by viewModel.showSosDialog.collectAsStateWithLifecycle()
    val showWithdrawDialog by viewModel.showWithdrawDialog.collectAsStateWithLifecycle()
    val showDepositDialog by viewModel.showDepositDialog.collectAsStateWithLifecycle()
    val showChatSheet by viewModel.showChatSheet.collectAsStateWithLifecycle()
    val chatRecipient by viewModel.chatRecipient.collectAsStateWithLifecycle()
    val snackbarMsg by viewModel.snackbarMessage.collectAsStateWithLifecycle()

    val lang = profile?.language ?: "bn"
    val isBn = lang == "bn"
    val isIt = lang == "it"
    val isOnline = profile?.isOnline ?: true
    val activeOrder = activeOrders.firstOrNull()

    // Show incoming dialog if pending order found
    LaunchedEffect(pendingPingOrder) {
        if (pendingPingOrder != null && !showIncomingDialog) {
            // Can trigger dialog automatically
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(snackbarMsg) {
        snackbarMsg?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSnackbar()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = RiderPrimary,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.TwoWheeler,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.padding(4.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isBn) "ফুড রাইডার" else if (isIt) "Food Rider Italia" else "Food Rider Pro",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = Color.White
                        )
                    }
                },
                navigationIcon = {
                    // Duty Status Indicator Pill
                    Surface(
                        modifier = Modifier
                            .padding(start = 12.dp)
                            .clickable { viewModel.toggleOnlineStatus() },
                        shape = RoundedCornerShape(16.dp),
                        color = if (isOnline) OnlineGreen.copy(alpha = 0.2f) else OfflineRed.copy(alpha = 0.2f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(if (isOnline) OnlineGreen else OfflineRed, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = if (isOnline) {
                                    if (isBn) "ডিউটি অন" else if (isIt) "ATTIVO" else "ON"
                                } else {
                                    if (isBn) "ডিউটি অফ" else if (isIt) "OFF" else "OFF"
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isOnline) OnlineGreen else OfflineRed
                            )
                        }
                    }
                },
                actions = {
                    // Language Switcher (Bangla / English / Italian)
                    IconButton(onClick = { viewModel.toggleLanguage() }) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF1E293B)
                        ) {
                            Text(
                                text = when (lang) {
                                    "bn" -> "বাং 🇧🇩"
                                    "it" -> "IT 🇮🇹"
                                    else -> "EN 🇬🇧"
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = RiderPrimary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }

                    // Emergency SOS 1-Tap Trigger
                    IconButton(
                        onClick = { viewModel.openSosDialog() },
                        modifier = Modifier.testTag("top_bar_sos_button")
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = OfflineRed.copy(alpha = 0.25f),
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Emergency SOS",
                                tint = OfflineRed,
                                modifier = Modifier.padding(6.dp)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFF0F172A)
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                // Tab 0: Home / Dashboard
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { viewModel.setTab(0) },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == 0) Icons.Filled.Home else Icons.Outlined.Home,
                            contentDescription = "Home"
                        )
                    },
                    label = {
                        Text(
                            text = if (isBn) "হোম" else if (isIt) "Home" else "Home",
                            fontSize = 11.sp
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = RiderPrimary,
                        selectedTextColor = RiderPrimary,
                        indicatorColor = RiderPrimary.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.testTag("nav_home")
                )

                // Tab 1: Active Delivery (with badge count if active)
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { viewModel.setTab(1) },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (activeOrders.isNotEmpty()) {
                                    Badge(containerColor = RiderPrimary) {
                                        Text(text = "${activeOrders.size}")
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = if (selectedTab == 1) Icons.Filled.Navigation else Icons.Outlined.Navigation,
                                contentDescription = "Active Order"
                            )
                        }
                    },
                    label = {
                        Text(
                            text = if (isBn) "ডেলিভারি" else if (isIt) "Ordine" else "Order",
                            fontSize = 11.sp
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = RiderPrimary,
                        selectedTextColor = RiderPrimary,
                        indicatorColor = RiderPrimary.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.testTag("nav_active_order")
                )

                // Tab 2: Earnings & Wallet
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { viewModel.setTab(2) },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == 2) Icons.Filled.AccountBalanceWallet else Icons.Outlined.AccountBalanceWallet,
                            contentDescription = "Earnings"
                        )
                    },
                    label = {
                        Text(
                            text = if (isBn) "উপার্জন" else if (isIt) "Guadagni" else "Earnings",
                            fontSize = 11.sp
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = RiderPrimary,
                        selectedTextColor = RiderPrimary,
                        indicatorColor = RiderPrimary.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.testTag("nav_earnings")
                )

                // Tab 3: Daily Quests
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { viewModel.setTab(3) },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == 3) Icons.Filled.EmojiEvents else Icons.Outlined.EmojiEvents,
                            contentDescription = "Quests"
                        )
                    },
                    label = {
                        Text(
                            text = if (isBn) "কোয়েস্ট" else if (isIt) "Sfide" else "Quests",
                            fontSize = 11.sp
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = RiderPrimary,
                        selectedTextColor = RiderPrimary,
                        indicatorColor = RiderPrimary.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.testTag("nav_quests")
                )

                // Tab 4: Profile
                NavigationBarItem(
                    selected = selectedTab == 4,
                    onClick = { viewModel.setTab(4) },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == 4) Icons.Filled.Person else Icons.Outlined.Person,
                            contentDescription = "Profile"
                        )
                    },
                    label = {
                        Text(
                            text = if (isBn) "প্রোফাইল" else if (isIt) "Profilo" else "Profile",
                            fontSize = 11.sp
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = RiderPrimary,
                        selectedTextColor = RiderPrimary,
                        indicatorColor = RiderPrimary.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.testTag("nav_profile")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> HomeScreen(
                    profile = profile,
                    activeOrders = activeOrders,
                    allEarnings = allEarnings,
                    onToggleDuty = { viewModel.toggleOnlineStatus() },
                    onSimulateOrder = { viewModel.simulateNewIncomingOrder() },
                    onNavigateToActiveOrder = {
                        viewModel.setTab(1)
                    },
                    onOpenEarnings = { viewModel.setTab(2) },
                    onOpenQuests = { viewModel.setTab(3) },
                    isBn = isBn
                )

                1 -> ActiveOrderScreen(
                    order = activeOrder,
                    routeProgress = routeProgress,
                    onSimulateStep = { viewModel.stepRouteProgress() },
                    onResetSimulation = { viewModel.resetRouteProgress() },
                    onUpdateStatus = { ord, newSt -> viewModel.updateOrderStep(ord, newSt) },
                    onOpenCashCalculator = { viewModel.openCashCalculator() },
                    onOpenIssueReport = { viewModel.openIssueReport() },
                    onOpenSos = { viewModel.openSosDialog() },
                    onOpenChat = { recip -> viewModel.openChat(recip) },
                    onCompleteDelivery = { ord, rec, chg, tip ->
                        viewModel.completeDelivery(ord, rec, chg, tip)
                    },
                    isBn = isBn
                )

                2 -> EarningsScreen(
                    profile = profile,
                    earnings = allEarnings,
                    transactions = allTransactions,
                    onOpenWithdraw = { viewModel.openWithdrawDialog() },
                    onOpenDeposit = { viewModel.openDepositDialog() },
                    isBn = isBn
                )

                3 -> QuestsScreen(
                    quests = allQuests,
                    onClaimReward = { qst -> viewModel.claimQuest(qst) },
                    isBn = isBn
                )

                4 -> ProfileScreen(
                    profile = profile,
                    onSetLanguage = { langCode -> viewModel.setLanguage(langCode) },
                    onToggleAutoAccept = { viewModel.toggleAutoAccept() },
                    onSetZone = { zName -> viewModel.setZone(zName) },
                    onOpenSos = { viewModel.openSosDialog() },
                    onRequestVehicleChange = { newType, newPlate, reason ->
                        viewModel.requestVehicleChange(newType, newPlate, reason)
                    },
                    lang = lang
                )
            }
        }
    }

    // Modal Dialogs & Sheets

    // 1. Incoming Order Ping Dialog
    if (showIncomingDialog && currentIncomingOrder != null) {
        OrderIncomingDialog(
            order = currentIncomingOrder!!,
            onAccept = { viewModel.acceptOrder(currentIncomingOrder!!) },
            onReject = { viewModel.rejectOrder(currentIncomingOrder!!) },
            isBn = isBn
        )
    }

    // 2. Cash Calculator Dialog
    if (showCashCalculator && activeOrder != null) {
        CashCalculatorDialog(
            billAmount = activeOrder.totalBill,
            initialTip = activeOrder.tip,
            onConfirm = { rec, chg, tip ->
                viewModel.completeDelivery(activeOrder, rec, chg, tip)
            },
            onDismiss = { viewModel.closeCashCalculator() },
            isBn = isBn
        )
    }

    // 3. Issue Report Dialog
    if (showIssueReport && activeOrder != null) {
        IssueReportDialog(
            orderNumber = activeOrder.orderNumber,
            onSubmit = { title, note -> viewModel.submitIssue(title, note) },
            onDismiss = { viewModel.closeIssueReport() },
            isBn = isBn
        )
    }

    // 4. Emergency SOS Dialog
    if (showSosDialog) {
        EmergencySosDialog(
            onDismiss = { viewModel.closeSosDialog() },
            onCallEmergency = { num ->
                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$num"))
                try {
                    context.startActivity(intent)
                } catch (_: Exception) { }
            },
            isBn = isBn
        )
    }

    // 5. Withdraw Earnings Dialog
    if (showWithdrawDialog) {
        WithdrawDialog(
            availableBalance = profile?.walletBalance ?: 0.0,
            onConfirm = { amt, method, acc -> viewModel.withdrawFunds(amt, method, acc) },
            onDismiss = { viewModel.closeWithdrawDialog() },
            isBn = isBn
        )
    }

    // 6. Deposit COD Floating Cash Dialog
    if (showDepositDialog) {
        DepositCodDialog(
            currentFloatingCash = profile?.currentFloatingCash ?: 0.0,
            limit = profile?.floatingCashLimit ?: 3500.0,
            onConfirm = { amt, method -> viewModel.depositFloatingCash(amt, method) },
            onDismiss = { viewModel.closeDepositDialog() },
            isBn = isBn
        )
    }

    // 7. Live In-App Chat Bottom Sheet
    if (showChatSheet) {
        ChatBottomSheet(
            recipientName = chatRecipient,
            onDismiss = { viewModel.closeChat() },
            isBn = isBn
        )
    }
}
