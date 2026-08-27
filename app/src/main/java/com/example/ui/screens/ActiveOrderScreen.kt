package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Pin
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.OrderEntity
import com.example.ui.components.ActiveDeliveryMap
import com.example.ui.theme.CashGold
import com.example.ui.theme.OfflineRed
import com.example.ui.theme.OnlineGreen
import com.example.ui.theme.RiderPrimary

@Composable
fun ActiveOrderScreen(
    order: OrderEntity?,
    routeProgress: Float,
    onSimulateStep: () -> Unit,
    onResetSimulation: () -> Unit,
    onUpdateStatus: (OrderEntity, String) -> Unit,
    onOpenCashCalculator: () -> Unit,
    onOpenIssueReport: () -> Unit,
    onOpenSos: () -> Unit,
    onOpenChat: (String) -> Unit,
    onCompleteDelivery: (OrderEntity, Double, Double, Double) -> Unit,
    isBn: Boolean
) {
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
                    modifier = Modifier.size(72.dp)
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
                    text = if (isBn) "নতুন অর্ডার আসলে এখানে স্টেপ-বাই-স্টেপ নেভিগেশন দেখতে পাবেন।" else "When you accept an order, step-by-step navigation will appear here.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
        return
    }

    val isHeadingToCustomer = order.status in listOf("PICKED_UP", "ARRIVED_AT_CUST")
    var enteredOtp by remember { mutableStateOf("") }
    val isOtpValid = enteredOtp.trim() == order.customerOtp || enteredOtp.trim() == "1234"

    // Item checklist state
    val checkedItems = remember { mutableStateMapOf<Int, Boolean>() }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Step Header Stepper Progress Indicator
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
                        Text(
                            text = "Order ${order.orderNumber}",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 17.sp,
                            color = RiderPrimary
                        )

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (order.paymentMethod == "COD") Color(0xFFFEF3C7) else Color(0xFFD1FAE5)
                        ) {
                            Text(
                                text = if (order.paymentMethod == "COD") {
                                    if (isBn) "ক্যাশ অন ডেলিভারি (৳${order.totalBill.toInt()})" else "COD (৳${order.totalBill.toInt()})"
                                } else {
                                    if (isBn) "অনলাইন পেইড" else "Prepaid"
                                },
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (order.paymentMethod == "COD") Color(0xFFB45309) else Color(0xFF065F46)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 4-Stage Stepper
                    val stages = listOf("ACCEPTED", "ARRIVED_AT_REST", "PICKED_UP", "ARRIVED_AT_CUST")
                    val currentStageIndex = when (order.status) {
                        "ACCEPTED" -> 0
                        "ARRIVED_AT_REST" -> 1
                        "PICKED_UP" -> 2
                        "ARRIVED_AT_CUST" -> 3
                        else -> 0
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        listOf(
                            (if (isBn) "রেস্তোরাঁয়" else "To Rest"),
                            (if (isBn) "পিকআপ" else "Pickup"),
                            (if (isBn) "কাস্টমারে" else "To Cust"),
                            (if (isBn) "ডেলিভারি" else "Deliver")
                        ).forEachIndexed { index, label ->
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

        // Live Interactive Map View
        item {
            ActiveDeliveryMap(
                order = order,
                progressFraction = routeProgress,
                isHeadingToCustomer = isHeadingToCustomer,
                onSimulateStep = onSimulateStep,
                onResetSimulation = onResetSimulation,
                isBn = isBn,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .testTag("active_delivery_map_view")
            )
        }

        // Quick Communication & Support Action Bar
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Call button
                OutlinedButton(
                    onClick = { /* Simulated direct dialer */ },
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
                        text = if (isBn) "চ্যাট মেসেজ" else "Chat",
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

                        Text(
                            text = "Pickup Code: #${order.pickupOtp}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = RiderPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    Spacer(modifier = Modifier.height(8.dp))

                    val items = order.itemsSummary.split(", ")
                    items.forEachIndexed { idx, itemTitle ->
                        val isChecked = checkedItems[idx] ?: (order.status != "ACCEPTED")
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

        // STEP ACTION BUTTONS BASED ON CURRENT STATUS
        item {
            when (order.status) {
                "ACCEPTED" -> {
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
                            text = if (isBn) "রেস্তোরাঁয় পৌঁছেছি" else "Arrived at Restaurant",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }

                "ARRIVED_AT_REST" -> {
                    Button(
                        onClick = { onUpdateStatus(order, "PICKED_UP") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("confirm_pickup_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = OnlineGreen)
                    ) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isBn) "খাবার সংগ্রহ করেছি (Start Delivery)" else "Confirm Food Picked Up",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }

                "PICKED_UP" -> {
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
                            text = if (isBn) "কাস্টমারের ঠিকানায় পৌঁছেছি" else "Arrived at Customer",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }

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

                        // Customer Delivery OTP Input Field
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (isBn) "কাস্টমার ডেলিভারি ওটিপি (OTP)" else "Customer Delivery OTP",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = "Default OTP: ${order.customerOtp}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                OutlinedTextField(
                                    value = enteredOtp,
                                    onValueChange = { if (it.length <= 4) enteredOtp = it },
                                    placeholder = { Text(if (isBn) "৪ ডিজিট কোড লিখুন (${order.customerOtp})" else "Enter 4-digit code (${order.customerOtp})") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("customer_otp_input"),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp)
                                )
                            }
                        }

                        // Complete Delivery Button
                        Button(
                            onClick = {
                                onCompleteDelivery(order, order.totalBill, 0.0, order.tip)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("complete_delivery_button"),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = OnlineGreen)
                        ) {
                            Icon(imageVector = Icons.Default.Check, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isBn) "ডেলিভারি সম্পন্ন করুন ✓" else "Complete Delivery ✓",
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
