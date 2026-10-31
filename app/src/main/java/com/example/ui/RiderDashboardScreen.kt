package com.example.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DeliveryOrder
import com.example.ui.components.DeliveryRouteMapView
import com.example.ui.components.OrderIncomingDialog
import com.example.ui.components.RiderProfileDialog
import com.example.ui.theme.*
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RiderDashboardScreen(
    viewModel: RiderViewModel
) {
    val context = LocalContext.current
    val profile by viewModel.riderProfile.collectAsState()
    val location by viewModel.locationStreamingManager.currentLocation.collectAsState()
    val locationAddress by viewModel.locationStreamingManager.locationAddress.collectAsState()

    val incomingOrder by viewModel.incomingOrder.collectAsState()
    val activeOrder by viewModel.activeOrder.collectAsState()

    var showCodDialog by remember { mutableStateOf(false) }
    var showProfileDialog by remember { mutableStateOf(false) }

    // Display Pre-Accept Order Dialog when an order arrives
    incomingOrder?.let { order ->
        OrderIncomingDialog(
            order = order,
            onAccept = { viewModel.acceptOrder(order) },
            onReject = { viewModel.rejectOrder(order) }
        )
    }

    // Display Full Rider Profile Dialog
    if (showProfileDialog) {
        RiderProfileDialog(
            profile = profile,
            onDismiss = { showProfileDialog = false },
            onToggleDuty = { viewModel.toggleDutyStatus() },
            onLogout = { viewModel.logout() }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { showProfileDialog = true }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(if (profile.isOnline) SuccessGreen else Color.Gray, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = if (profile.isOnline) "Food Rider (অনলাইন)" else "Food Rider (অফলাইন)",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = profile.fullName.ifBlank { "আমার প্রোফাইল" },
                                fontSize = 11.sp,
                                color = OrangeSecondary
                            )
                        }
                    }
                },
                actions = {
                    // Profile Button in Top App Bar
                    FilledTonalButton(
                        onClick = { showProfileDialog = true },
                        modifier = Modifier
                            .padding(end = 6.dp)
                            .testTag("btn_top_profile"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = DarkCard,
                            contentColor = OrangePrimary
                        ),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "প্রোফাইল",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("প্রোফাইল", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    // Online/Offline Switch
                    Switch(
                        checked = profile.isOnline,
                        onCheckedChange = { viewModel.toggleDutyStatus() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = OrangePrimary,
                            checkedTrackColor = OrangePrimary.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier
                            .padding(end = 4.dp)
                            .testTag("toggle_online_status")
                    )

                    // Logout Icon
                    IconButton(
                        onClick = { viewModel.logout() },
                        modifier = Modifier.testTag("btn_dashboard_logout")
                    ) {
                        Icon(Icons.Default.Logout, contentDescription = "লগআউট", tint = Color.LightGray)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface)
            )
        },
        containerColor = DarkBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Live GPS Status Banner: Automatically detected on Online
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkCard),
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .background(if (profile.isOnline) SuccessGreen.copy(alpha = 0.2f) else Color.DarkGray, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.GpsFixed,
                            contentDescription = "GPS",
                            tint = if (profile.isOnline) SuccessGreen else Color.Gray,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (profile.isOnline) "স্বয়ংক্রিয় লাইভ অবস্থান সক্রিয়" else "GPS ট্র্যাকিং স্ট্যান্ডবাই",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = if (profile.isOnline) SuccessGreen.copy(alpha = 0.2f) else Color.DarkGray
                            ) {
                                Text(
                                    text = if (profile.isOnline) "LIVE" else "OFFLINE",
                                    color = if (profile.isOnline) SuccessGreen else Color.LightGray,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = locationAddress,
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                        Text(
                            text = "Lat: ${"%.4f".format(location.first)}, Lng: ${"%.4f".format(location.second)}",
                            fontSize = 11.sp,
                            color = OrangeSecondary
                        )
                    }
                }
            }

            // Quick Stats Grid
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    title = "আজকের আয়",
                    value = "€${String.format(Locale.US, "%.2f", profile.todayEarnings)}",
                    icon = Icons.Default.Euro,
                    iconTint = OrangePrimary,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "ডেলিভারি",
                    value = "${profile.todayDeliveries} টি",
                    icon = Icons.Default.CheckCircle,
                    iconTint = SuccessGreen,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "ক্যাশ ইন হ্যান্ড",
                    value = "€${String.format(Locale.US, "%.2f", profile.cashInHand)}",
                    icon = Icons.Default.AccountBalanceWallet,
                    iconTint = BlueAccent,
                    modifier = Modifier.weight(1f)
                )
            }

            // Active Order Card OR Waiting State
            if (activeOrder != null) {
                ActiveOrderCard(
                    order = activeOrder!!,
                    onCallRestaurant = {
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${activeOrder!!.restaurantPhone}"))
                        context.startActivity(intent)
                    },
                    onCallCustomer = {
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${activeOrder!!.customerPhone}"))
                        context.startActivity(intent)
                    },
                    onComplete = { viewModel.completeActiveOrder() }
                )
            } else {
                // Idle / Waiting for Order Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkCard),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .background(
                                    if (profile.isOnline) SuccessGreen.copy(alpha = 0.15f) else Color.DarkGray,
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (profile.isOnline) Icons.Default.TwoWheeler else Icons.Default.PowerSettingsNew,
                                contentDescription = null,
                                tint = if (profile.isOnline) SuccessGreen else Color.LightGray,
                                modifier = Modifier.size(30.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = if (profile.isOnline) "নতুন অর্ডারের অপেক্ষায়..." else "আপনি বর্তমানে অফলাইনে আছেন",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = if (profile.isOnline)
                                "লাইভ জিপিএস চালু রয়েছে। নতুন অর্ডার আসলে রিয়েল ম্যাপ, রেস্তোরাঁ ও কাস্টমারের পূর্ণ ঠিকানা, দূরত্ব এবং এই অর্ডারে মোট কত ইউরো পাবেন তা প্রিভিউতে দেখা যাবে।"
                            else
                                "কাজের জন্য উপরের ডান পাশের সুইচটি অন করে অনলাইন (Online) করুন।",
                            fontSize = 12.sp,
                            color = TextSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            lineHeight = 18.sp
                        )

                        // Test Simulation Buttons for user convenience
                        if (profile.isOnline) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Divider(color = Color.DarkGray.copy(alpha = 0.5f))
                            Spacer(modifier = Modifier.height(14.dp))

                            Text(
                                text = "ম্যাপ ও অর্ডার প্রিভিউ টেস্ট করুন:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = OrangeSecondary
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { viewModel.simulateTestOrder(isBankPaid = true) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("btn_test_bank_paid_order"),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = DarkSurface, contentColor = SuccessGreen)
                                ) {
                                    Icon(Icons.Default.AccountBalance, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("ব্যাংক পেইড টেস্ট", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = { viewModel.simulateTestOrder(isBankPaid = false) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("btn_test_cod_order"),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = DarkSurface, contentColor = OrangePrimary)
                                ) {
                                    Icon(Icons.Default.Payments, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("COD টেস্ট", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // Quick Actions: COD Calculator & SOS & Profile Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { showProfileDialog = true },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("btn_dashboard_open_profile"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DarkCard, contentColor = Color.White)
                ) {
                    Icon(Icons.Default.AccountCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("প্রোফাইল", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { showCodDialog = true },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("btn_cod_calculator"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DarkCard, contentColor = OrangePrimary)
                ) {
                    Icon(Icons.Default.Calculate, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("COD হিসাব", fontSize = 12.sp)
                }

                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:112"))
                        context.startActivity(intent)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("btn_emergency_sos"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4A1515), contentColor = ErrorRed)
                ) {
                    Icon(Icons.Default.Sos, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("SOS (112)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    if (showCodDialog) {
        AlertDialog(
            onDismissRequest = { showCodDialog = false },
            title = { Text("ক্যাশ অন ডেলিভারি (COD) হিসাব", color = Color.White) },
            text = {
                Column {
                    Text("অর্ডারের মূল্য: €24.50", color = TextSecondary)
                    Text("গ্রাহকের প্রদেয় ক্যাশ: €30.00", color = TextSecondary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("কাস্টমারকে ফেরতযোগ্য বাকি: €5.50", color = OrangePrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            },
            confirmButton = {
                TextButton(onClick = { showCodDialog = false }) {
                    Text("ঠিক আছে", color = OrangePrimary)
                }
            },
            containerColor = DarkSurface
        )
    }
}

@Composable
private fun ActiveOrderCard(
    order: DeliveryOrder,
    onCallRestaurant: () -> Unit,
    onCallCustomer: () -> Unit,
    onComplete: () -> Unit
) {
    val isBankPaid = order.paymentType == "BANK_PAID" || order.isBankPaidToRestaurant
    var isNavigatingToCustomer by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
            .testTag("active_order_card"),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = OrangePrimary.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = "চলমান ডেলিভারি",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = OrangePrimary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Text(
                    text = order.orderNumber,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Big Euro Payout Banner - এই অর্ডার সম্পন্ন করলে কত ইউরো পাবে
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = OrangePrimary.copy(alpha = 0.15f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "অর্ডার সম্পন্ন করলে আয়:",
                            fontSize = 11.sp,
                            color = OrangeSecondary
                        )
                        Text(
                            text = "€${String.format(Locale.US, "%.2f", order.riderEarningsEuro)}",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = OrangePrimary
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "মোট দূরত্ব:",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                        Text(
                            text = "${order.distanceKm} কি.মি.",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Real Map Layer connecting Restaurant to Customer with Navigation Action
            Text(
                text = "লাইভ রুট ম্যাপ ও নেভিগেশন:",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(6.dp))

            DeliveryRouteMapView(
                order = order,
                isNavigationToCustomer = isNavigatingToCustomer,
                showExternalNavButton = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Target selector: Navigate to Restaurant OR Navigate to Customer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilledTonalButton(
                    onClick = { isNavigatingToCustomer = false },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = if (!isNavigatingToCustomer) OrangePrimary else DarkSurface,
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(vertical = 4.dp, horizontal = 8.dp)
                ) {
                    Icon(Icons.Default.Storefront, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("১. রেস্তোরাঁ পিকআপ", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                FilledTonalButton(
                    onClick = { isNavigatingToCustomer = true },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = if (isNavigatingToCustomer) SuccessGreen else DarkSurface,
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(vertical = 4.dp, horizontal = 8.dp)
                ) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("২. কাস্টমার ড্রপঅফ", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Payment Settlement Banner
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (isBankPaid) SuccessGreen.copy(alpha = 0.15f) else Color(0xFF4A3410),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isBankPaid) Icons.Default.CheckCircle else Icons.Default.Payments,
                        contentDescription = null,
                        tint = if (isBankPaid) SuccessGreen else Color(0xFFFFB74D),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isBankPaid)
                            "✅ রেস্তোরাঁ পেইড: ব্যাংকে পরিশোধিত (ক্যাশ সংগ্রহ নেই)"
                        else
                            "💵 ক্যাশ অন ডেলিভারি: গ্রাহক থেকে €${String.format(Locale.US, "%.2f", order.totalBillEuro)} সংগ্রহ করুন",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isBankPaid) SuccessGreen else Color(0xFFFFB74D)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = Color.DarkGray.copy(alpha = 0.4f))
            Spacer(modifier = Modifier.height(10.dp))

            // 1. Restaurant Details
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(OrangePrimary.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Storefront, contentDescription = null, tint = OrangePrimary, modifier = Modifier.size(18.dp))
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(order.restaurantName, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                    Text(order.restaurantAddress, fontSize = 11.sp, color = TextSecondary)
                }
                IconButton(onClick = onCallRestaurant) {
                    Icon(Icons.Default.Call, contentDescription = "কল", tint = OrangePrimary)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 2. Customer Delivery Details
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(SuccessGreen.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(18.dp))
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("গ্রাহক: ${order.customerName}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                    Text(order.customerAddress, fontSize = 11.sp, color = TextSecondary)
                }
                IconButton(onClick = onCallCustomer) {
                    Icon(Icons.Default.Call, contentDescription = "কল", tint = SuccessGreen)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Complete Delivery Button
            Button(
                onClick = onComplete,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("btn_complete_delivery"),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen)
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(6.dp))
                Text("ডেলিভারি সম্পন্ন (Complete Delivery)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = value, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = title, fontSize = 11.sp, color = TextSecondary)
        }
    }
}
