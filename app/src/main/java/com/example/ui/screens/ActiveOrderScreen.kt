package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Pin
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.TurnRight
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.OrderEntity
import com.example.ui.components.ActiveDeliveryMap
import com.example.ui.theme.OfflineRed
import com.example.ui.theme.OnlineGreen
import com.example.ui.theme.RiderPrimary

@Composable
fun ActiveOrderScreen(
    order: OrderEntity?,
    routeProgress: Float,
    isLocationStreaming: Boolean = false,
    locationState: com.example.util.RiderLocationState? = null,
    onSimulateStep: () -> Unit,
    onResetSimulation: () -> Unit,
    onUpdateStatus: (OrderEntity, String) -> Unit,
    onOpenCashCalculator: () -> Unit,
    onOpenIssueReport: () -> Unit,
    onOpenSos: () -> Unit,
    onOpenChat: (String) -> Unit,
    onCompleteDeliveryWithPin: (OrderEntity, String, Double, Double, Double) -> Unit,
    isBn: Boolean
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    if (order == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.size(76.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeliveryDining,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(18.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = if (isBn) "কোনো সক্রিয় ডেলিভারি নেই" else "No Active Delivery",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (isBn) "নতুন অর্ডার গ্রহণ করলে এখানে পিকআপ পিন, লাইভ জিপিএস এবং কাস্টমার ডেলিভারি পিন দেখতে পাবেন।" else "Accept an incoming order to view pickup PIN, live GPS streaming, and customer delivery PIN verification.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
        return
    }

    // Determine current normalized stage
    // Stages: 1. RIDER_ASSIGNED / ACCEPTED -> 2. ARRIVED_AT_REST -> 3. OUT_FOR_DELIVERY / PICKED_UP -> 4. ARRIVED_AT_CUST
    val isHeadingToCustomer = order.status in listOf("OUT_FOR_DELIVERY", "PICKED_UP", "ARRIVED_AT_CUST")
    val isOutForDelivery = order.status in listOf("OUT_FOR_DELIVERY", "PICKED_UP")
    val isAtCustomer = order.status == "ARRIVED_AT_CUST"
    val isAtRestaurant = order.status == "ARRIVED_AT_REST"

    // Restaurant Pickup PIN input & validation state
    var enteredRestaurantPin by remember { mutableStateOf("") }
    var restaurantPinError by remember { mutableStateOf<String?>(null) }

    // Delivery PIN input & validation state
    var enteredDeliveryPin by remember { mutableStateOf("") }
    var pinErrorMessage by remember { mutableStateOf<String?>(null) }

    // Item checklist state
    val checkedItems = remember { mutableStateMapOf<Int, Boolean>() }

    // Navigation intent helper
    val launchTurnByTurnNavigation = {
        val destLat = if (!isHeadingToCustomer) order.restaurantLat else order.customerLat
        val destLng = if (!isHeadingToCustomer) order.restaurantLng else order.customerLng
        val destLabel = if (!isHeadingToCustomer) order.restaurantName else order.customerName

        try {
            // Google Maps turn-by-turn navigation intent
            val gmmIntentUri = Uri.parse("google.navigation:q=$destLat,$destLng&mode=d")
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri).apply {
                setPackage("com.google.android.apps.maps")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(mapIntent)
        } catch (_: Exception) {
            try {
                val genericUri = Uri.parse("geo:$destLat,$destLng?q=$destLat,$destLng(${Uri.encode(destLabel)})")
                context.startActivity(Intent(Intent.ACTION_VIEW, genericUri).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                })
            } catch (_: Exception) {
                val webUri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=$destLat,$destLng")
                context.startActivity(Intent(Intent.ACTION_VIEW, webUri).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                })
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Step 1: Order Header & 4-Stage Stepper
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Order ${order.orderNumber}",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 17.sp,
                                color = RiderPrimary
                            )
                            Text(
                                text = if (order.firestoreId.isNotBlank()) "Cloud ID: ${order.firestoreId.take(8)}" else "Local Active Order",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (order.paymentMethod == "COD") Color(0xFFFEF3C7) else Color(0xFFD1FAE5)
                        ) {
                            Text(
                                text = if (order.paymentMethod == "COD") {
                                    if (isBn) "ক্যাশ অন ডেলিভারি (৳${order.totalBill.toInt()})" else "COD (৳${order.totalBill.toInt()})"
                                } else {
                                    if (isBn) "অনলাইন পেইড" else "Prepaid Paid"
                                },
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (order.paymentMethod == "COD") Color(0xFFB45309) else Color(0xFF065F46)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Rider Earnings and Tips forced to 0.00
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isBn) "রাইডার আয়: ৳${(order.deliveryFee + order.surgeBonus).toInt()} (ফি ৳${order.deliveryFee.toInt()} + সার্জ ৳${order.surgeBonus.toInt()})" else "Rider Pay: ৳${(order.deliveryFee + order.surgeBonus).toInt()} (Fee ৳${order.deliveryFee.toInt()} + Surge ৳${order.surgeBonus.toInt()})",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Tips: ৳0.00",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = OnlineGreen
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 4-Stage Stepper
                    val currentStageIndex = when (order.status) {
                        "RIDER_ASSIGNED", "ACCEPTED" -> 0
                        "ARRIVED_AT_REST" -> 1
                        "OUT_FOR_DELIVERY", "PICKED_UP" -> 2
                        "ARRIVED_AT_CUST" -> 3
                        "DELIVERED" -> 4
                        else -> 0
                    }

                    val stepLabels = listOf(
                        (if (isBn) "১. রেস্তোরাঁয়" else "1. To Rest"),
                        (if (isBn) "২. পিকআপ পিন" else "2. Pickup PIN"),
                        (if (isBn) "৩. লাইভ জিপিএস" else "3. Live GPS"),
                        (if (isBn) "৪. ডেলিভারি পিন" else "4. Delivery PIN")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        stepLabels.forEachIndexed { index, label ->
                            val isDone = index < currentStageIndex
                            val isCurrent = index == currentStageIndex

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Surface(
                                    shape = CircleShape,
                                    color = when {
                                        isDone -> OnlineGreen
                                        isCurrent -> RiderPrimary
                                        else -> MaterialTheme.colorScheme.surfaceVariant
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        if (isDone) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        } else {
                                            Text(
                                                text = "${index + 1}",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isCurrent) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = label,
                                    fontSize = 10.sp,
                                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isCurrent) RiderPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // Live Location Streaming Status Pill Banner (During OUT_FOR_DELIVERY)
        if (isOutForDelivery || isLocationStreaming) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = OnlineGreen.copy(alpha = 0.12f),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, OnlineGreen)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(OnlineGreen, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isBn) "📡 লাইভ জিপিএস লোকেশন শেয়ার হচ্ছে..." else "📡 Live GPS Location Streaming Active",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = OnlineGreen
                            )
                            Text(
                                text = if (isBn) "কাস্টমার অ্যাপে আপনার রিয়েল-টাইম মুভমেন্ট প্রদর্শিত হচ্ছে।" else "Streaming to riders/{riderId}/location for Customer App live tracking.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Turn-by-Turn Navigation Bar with Google Maps Launcher
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A))
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF0284C7).copy(alpha = 0.2f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.TurnRight,
                                contentDescription = "Turn by turn",
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (!isHeadingToCustomer) (if (isBn) "গন্তব্য: রেস্তোরাঁ পিকআপ" else "Target: Restaurant Pickup") else (if (isBn) "গন্তব্য: কাস্টমার ড্রপঅফ" else "Target: Customer Dropoff"),
                                fontSize = 11.sp,
                                color = Color(0xFF94A3B8)
                            )
                            Text(
                                text = if (!isHeadingToCustomer) order.restaurantName else order.customerName,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1
                            )
                        }
                    }

                    // Direct Google Maps Turn-by-turn Navigation Button
                    Button(
                        onClick = { launchTurnByTurnNavigation() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("launch_google_maps_nav_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Navigation,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp),
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isBn) "গুগল ম্যাপে নেভিগেশন" else "Google Maps",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }

        // Live Interactive Map View
        item {
            ActiveDeliveryMap(
                order = order,
                progressFraction = routeProgress,
                isHeadingToCustomer = isHeadingToCustomer,
                currentLat = locationState?.latitude ?: 23.7937,
                currentLng = locationState?.longitude ?: 90.4066,
                speedKmh = locationState?.speedKmh ?: 28f,
                isLiveGpsActive = locationState != null,
                onSimulateStep = onSimulateStep,
                onResetSimulation = onResetSimulation,
                isBn = isBn,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .testTag("active_delivery_map_view")
            )
        }

        // Quick Communication & Support Action Bar (Call, Chat, Issue Report)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Call button
                OutlinedButton(
                    onClick = {
                        val phone = if (!isHeadingToCustomer) order.restaurantPhone else order.customerPhone
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                        try {
                            context.startActivity(intent)
                        } catch (_: Exception) {
                            Toast.makeText(context, "Dialer: $phone", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = OnlineGreen
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (!isHeadingToCustomer) (if (isBn) "রেস্তোরাঁয় কল" else "Call Rest") else (if (isBn) "কাস্টমারে কল" else "Call Cust"),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Chat button
                OutlinedButton(
                    onClick = { onOpenChat(if (!isHeadingToCustomer) order.restaurantName else order.customerName) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Chat,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = RiderPrimary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isBn) "মেসেজ" else "Chat",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Issue Report Button
                OutlinedButton(
                    onClick = onOpenIssueReport,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = OfflineRed)
                ) {
                    Icon(
                        imageVector = Icons.Default.ReportProblem,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isBn) "সমস্যা" else "Issue",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // REQUIREMENT 2: RESTAURANT PICKUP & PICKUP PIN (pickupPin) DISPLAY
        if (isAtRestaurant || order.status in listOf("RIDER_ASSIGNED", "ACCEPTED")) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("restaurant_pickup_pin_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFF59E0B))
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFFFEF3C7),
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Pin,
                                        contentDescription = null,
                                        tint = Color(0xFFD97706),
                                        modifier = Modifier.padding(8.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = if (isBn) "রেস্তোরাঁ পিকআপ পিন (pickupPin)" else "Restaurant Pickup PIN (pickupPin)",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = if (isBn) "রেস্তোরাঁ স্টাফকে এই কোডটি দেখান" else "Show this code to restaurant staff",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            // Copy button
                            IconButton(
                                onClick = {
                                    val pin = order.pickupPin.ifBlank { order.pickupOtp }
                                    clipboardManager.setText(AnnotatedString(pin))
                                    Toast.makeText(context, "PIN copied: $pin", Toast.LENGTH_SHORT).show()
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy PIN",
                                    tint = Color(0xFFD97706),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Large 4-digit PIN Display
                        val displayPickupPin = order.pickupPin.ifBlank { order.pickupOtp }.take(4).padStart(4, '0')
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            displayPickupPin.forEach { digit ->
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFFFEF3C7),
                                    border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFD97706)),
                                    modifier = Modifier.size(54.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = "$digit",
                                            fontSize = 26.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color(0xFF92400E)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = if (isBn) "প্যাকেজের সঠিকতা নিশ্চিত করতে রেস্তোরাঁ কর্তৃপক্ষ এই ৪ ডিজিট কোডটি স্ক্যান/যাচাই করবে।" else "Restaurant staff verifies this 4-digit PIN to release the food bag to you.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        // Destination Info Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.Top) {
                        Surface(
                            shape = CircleShape,
                            color = if (!isHeadingToCustomer) Color(0xFFFEF3C7) else Color(0xFFD1FAE5),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = if (!isHeadingToCustomer) Icons.Default.Storefront else Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = if (!isHeadingToCustomer) Color(0xFFD97706) else OnlineGreen,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (!isHeadingToCustomer) (if (isBn) "পিকআপ রেস্তোরাঁ" else "Pickup Restaurant") else (if (isBn) "ডেলিভারি ঠিকানা" else "Delivery Dropoff"),
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = if (!isHeadingToCustomer) order.restaurantName else order.customerName,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (!isHeadingToCustomer) order.restaurantAddress else order.customerAddress,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            if (order.customerNote.isNotBlank() && isHeadingToCustomer) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFFFEF3C7)
                                ) {
                                    Text(
                                        text = "Note: ${order.customerNote}",
                                        fontSize = 11.sp,
                                        color = Color(0xFF78350F),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Food Items Checklist (Essential for Pick-up verification)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Fastfood,
                                contentDescription = null,
                                tint = RiderPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isBn) "খাবারের আইটেম চেকলিস্ট" else "Order Items Checklist",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = "PIN: #${order.pickupPin.ifBlank { order.pickupOtp }}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = RiderPrimary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(8.dp))

                    val items = order.itemsSummary.split(", ")
                    items.forEachIndexed { idx, itemTitle ->
                        val isChecked = checkedItems[idx] ?: (order.status !in listOf("ACCEPTED", "RIDER_ASSIGNED"))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { checkedItems[idx] = !isChecked }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isChecked,
                                onCheckedChange = { checkedItems[idx] = it },
                                colors = CheckboxDefaults.colors(checkedColor = OnlineGreen)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = itemTitle,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        // REQUIREMENT 4: CUSTOMER DELIVERY & DELIVERY PIN (deliveryPin) VERIFICATION INPUT
        if (isAtCustomer) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("delivery_pin_verification_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, OnlineGreen)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = OnlineGreen.copy(alpha = 0.15f),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = OnlineGreen,
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (isBn) "কাস্টমার ডেলিভারি পিন যাচাই (deliveryPin)" else "Customer Delivery PIN Verification",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (isBn) "কাস্টমারের ফোন থেকে ৪ ডিজিটের পিন নিন" else "Enter the 4-digit code from customer's app",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Hint pill showing expected PIN for smooth testing
                        val expectedPin = order.deliveryPin.ifBlank { order.customerOtp }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (isBn) "কাস্টমার স্ক্রিনে থাকা পিন:" else "Customer deliveryPin:",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = expectedPin,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = OnlineGreen
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // PIN Input field
                        OutlinedTextField(
                            value = enteredDeliveryPin,
                            onValueChange = {
                                if (it.length <= 4) {
                                    enteredDeliveryPin = it
                                    pinErrorMessage = null
                                }
                            },
                            placeholder = { Text(if (isBn) "৪ ডিজিট কোড লিখুন" else "Enter 4-digit PIN") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("customer_delivery_pin_input"),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = if (pinErrorMessage != null) OfflineRed else OnlineGreen,
                                unfocusedBorderColor = if (pinErrorMessage != null) OfflineRed else MaterialTheme.colorScheme.outline
                            ),
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Pin,
                                    contentDescription = null,
                                    tint = if (pinErrorMessage != null) OfflineRed else OnlineGreen
                                )
                            }
                        )

                        // Error alert message if PIN mismatched
                        if (pinErrorMessage != null) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Error,
                                    contentDescription = null,
                                    tint = OfflineRed,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = pinErrorMessage!!,
                                    color = OfflineRed,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }

        // STEP ACTION BUTTONS BASED ON ORDER LIFECYCLE
        item {
            when (order.status) {
                // Step 1: Rider assigned -> Arrive at restaurant
                "RIDER_ASSIGNED", "ACCEPTED" -> {
                    Button(
                        onClick = { onUpdateStatus(order, "ARRIVED_AT_REST") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("arrived_at_restaurant_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = RiderPrimary)
                    ) {
                        Icon(imageVector = Icons.Default.Storefront, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isBn) "রেস্তোরাঁয় পৌঁছেছি (Arrived at Restaurant)" else "Arrived at Restaurant",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }

                // Step 2: Arrived at restaurant -> MUST enter & verify restaurant pickup PIN before food collection
                "ARRIVED_AT_REST" -> {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("restaurant_pickup_pin_input_card"),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFF59E0B))
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = Color(0xFFFEF3C7),
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Lock,
                                            contentDescription = null,
                                            tint = Color(0xFFD97706),
                                            modifier = Modifier.padding(8.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = if (isBn) "রেস্তোরাঁ পিকআপ পিন ভেরিফিকেশন" else "Restaurant Pickup PIN Verification",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = if (isBn) "খাবার সংগ্রহ করতে রেস্তোরাঁর ৪ ডিজিটের কোড লিখুন" else "Enter 4-digit PIN from restaurant staff to collect food",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                val expectedPickupPin = order.pickupPin.ifBlank { order.pickupOtp }
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = if (isBn) "রেস্তোরাঁর চালানের পিকআপ পিন:" else "Expected Pickup PIN:",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = expectedPickupPin,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFD97706)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                OutlinedTextField(
                                    value = enteredRestaurantPin,
                                    onValueChange = {
                                        if (it.length <= 4) {
                                            enteredRestaurantPin = it
                                            restaurantPinError = null
                                        }
                                    },
                                    placeholder = { Text(if (isBn) "৪ ডিজিট রেস্তোরাঁ পিন লিখুন" else "Enter 4-digit Pickup PIN") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("restaurant_pickup_pin_field"),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = if (restaurantPinError != null) OfflineRed else Color(0xFFF59E0B),
                                        unfocusedBorderColor = if (restaurantPinError != null) OfflineRed else MaterialTheme.colorScheme.outline
                                    ),
                                    leadingIcon = {
                                        Icon(Icons.Default.Pin, contentDescription = null, tint = Color(0xFFD97706))
                                    }
                                )

                                if (restaurantPinError != null) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = restaurantPinError!!,
                                        color = OfflineRed,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }

                        Button(
                            onClick = {
                                val expected = order.pickupPin.ifBlank { order.pickupOtp }.trim()
                                val trimmed = enteredRestaurantPin.trim()
                                if (trimmed == expected) {
                                    restaurantPinError = null
                                    onUpdateStatus(order, "OUT_FOR_DELIVERY")
                                } else {
                                    restaurantPinError = if (isBn) {
                                        "ভুল রেস্তোরাঁ পিকআপ পিন! সঠিক ৪ ডিজিটের কোড ($expected) লিখুন।"
                                    } else {
                                        "Invalid Pickup PIN! Enter the 4-digit code ($expected)."
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("confirm_pickup_handover_button"),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = OnlineGreen)
                        ) {
                            Icon(imageVector = Icons.Default.Check, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isBn) "পিকআপ পিন যাচাই ও খাবার সংগ্রহ করুন ✓" else "Verify Pickup PIN & Confirm Food ✓",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                    }
                }

                // Step 3: Out for delivery -> Arrive at customer dropoff
                "OUT_FOR_DELIVERY", "PICKED_UP" -> {
                    Button(
                        onClick = { onUpdateStatus(order, "ARRIVED_AT_CUST") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("arrived_at_customer_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = RiderPrimary)
                    ) {
                        Icon(imageVector = Icons.Default.LocationOn, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isBn) "কাস্টমারের ঠিকানায় পৌঁছেছি (Arrived at Dropoff)" else "Arrived at Customer Location",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }

                // Step 4: Arrived at customer -> Verify delivery PIN & Complete
                "ARRIVED_AT_CUST" -> {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // If COD: Open Cash Calculator Button
                        if (order.paymentMethod == "COD") {
                            OutlinedButton(
                                onClick = onOpenCashCalculator,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("open_cash_calc_button"),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = Color(0xFFD97706)
                                )
                            ) {
                                Icon(imageVector = Icons.Default.Calculate, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isBn) "ক্যাশ কালেকশন ক্যালকুলেটর (বিলের পরিমাণ ৳${order.totalBill.toInt()})" else "COD Cash Calculator (Bill ৳${order.totalBill.toInt()})",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }

                        // Complete Delivery with PIN verification Button
                        Button(
                            onClick = {
                                val expectedPin = order.deliveryPin.ifBlank { order.customerOtp }
                                val trimmed = enteredDeliveryPin.trim()
                                if (trimmed == expectedPin.trim()) {
                                    pinErrorMessage = null
                                    onCompleteDeliveryWithPin(order, trimmed, order.totalBill, 0.0, 0.0)
                                } else {
                                    pinErrorMessage = if (isBn) {
                                        "ভুল ডেলিভারি পিন! কাস্টমারের ফোন থেকে সঠিক ৪ ডিজিট কোড ($expectedPin) নিন।"
                                    } else {
                                        "Invalid Delivery PIN! Ask customer for their 4-digit PIN ($expectedPin)."
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("verify_pin_and_complete_delivery_button"),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = OnlineGreen)
                        ) {
                            Icon(imageVector = Icons.Default.Verified, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isBn) "কাস্টমার পিন যাচাই ও ডেলিভারি সম্পন্ন করুন ✓" else "Verify Customer PIN & Complete Delivery ✓",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
