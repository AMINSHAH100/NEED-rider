package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.DeliveryOrder
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import java.util.Locale

@Composable
fun OrderIncomingDialog(
    order: DeliveryOrder,
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    var secondsLeft by remember { mutableIntStateOf(30) }

    LaunchedEffect(order.id) {
        secondsLeft = 30
        while (secondsLeft > 0) {
            delay(1000)
            secondsLeft--
        }
        if (secondsLeft == 0) {
            onReject()
        }
    }

    val progress by animateFloatAsState(
        targetValue = secondsLeft / 30f,
        animationSpec = tween(durationMillis = 1000, easing = LinearEasing),
        label = "timerProgress"
    )

    Dialog(
        onDismissRequest = onReject,
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .testTag("incoming_order_dialog"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = DarkCard)
        ) {
            Column(
                modifier = Modifier
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header with countdown timer
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = OrangePrimary.copy(alpha = 0.2f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeliveryDining,
                                contentDescription = null,
                                tint = OrangePrimary,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "নতুন অর্ডার অফার",
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp,
                                color = Color.White
                            )
                            Text(
                                text = order.orderNumber.ifBlank { "#ORD-NEW" },
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    // Countdown Ring
                    Box(contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.size(42.dp),
                            color = if (secondsLeft > 10) SuccessGreen else ErrorRed,
                            trackColor = Color(0xFF2C3240),
                            strokeWidth = 3.5.dp
                        )
                        Text(
                            text = "$secondsLeft",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (secondsLeft > 10) SuccessGreen else ErrorRed
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 1. Rider Earnings in Euros (€) - এই অর্ডার এর জন্য রাইডার কে কত ইউরো দেওয়া হবে
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = OrangePrimary.copy(alpha = 0.15f))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "রাইডার উপার্জন (Euros)",
                                fontSize = 12.sp,
                                color = OrangeSecondary,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "€${String.format(Locale.US, "%.2f", order.riderEarningsEuro)}",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = OrangePrimary
                            )
                            Text(
                                text = "ডেলিভারি ফি: €${String.format(Locale.US, "%.2f", order.deliveryFeeEuro)}",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }

                        // Distance Badge - রেষ্টুরেন্ট থেকে কাষ্টমার এর ডিসটেন্স কত কিলোমিটার
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = DarkSurface
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalAlignment = Alignment.End
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Straighten,
                                        contentDescription = null,
                                        tint = BlueAccent,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${order.distanceKm} km",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                                Text(
                                    text = "~${order.estimatedMinutes} মিনিট সময়",
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 2. Payment & Settlement Status - এই অর্ডার কি রেষ্টুরেন্ট কে ব্যংকে পেমেন্ট করেছে কি না, নাকি ক্যাশ অন ডেলিভারি
                val isBankPaid = order.paymentType == "BANK_PAID" || order.isBankPaidToRestaurant
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isBankPaid) SuccessGreen.copy(alpha = 0.12f) else Color(0xFF4A3410)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isBankPaid) Icons.Default.AccountBalance else Icons.Default.Payments,
                            contentDescription = null,
                            tint = if (isBankPaid) SuccessGreen else OrangePrimary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isBankPaid)
                                    "✅ রেস্তোরাঁর পেমেন্ট ব্যাংকে পেইড (Online)"
                                else
                                    "💵 ক্যাশ অন ডেলিভারি (Cash On Delivery)",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isBankPaid) SuccessGreen else Color(0xFFFFB74D)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (isBankPaid)
                                    "রেস্তোরাঁর টাকা ব্যাংকে পরিশোধিত। কাস্টমার থেকে কোনো ক্যাশ নিতে হবে না (€0.00)।"
                                else
                                    "কাস্টমার থেকে ক্যাশ সংগ্রহ করতে হবে: €${String.format(Locale.US, "%.2f", order.totalBillEuro)}",
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.85f),
                                lineHeight = 15.sp
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))

                // Real Interactive Delivery Route Map (Restaurant to Customer Navigation Preview)
                Text(
                    text = "ম্যাপ ও রুট প্রিভিউ (রেস্তোরাঁ ➔ কাস্টমার)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = OrangeSecondary,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(6.dp))

                DeliveryRouteMapView(
                    order = order,
                    isNavigationToCustomer = false,
                    showExternalNavButton = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 3. Pickup (রেষ্টুরেন্ট এর ঠিকানা কই) & Delivery (ক্লাইন্ট এর ঠিকানা কই)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        // Restaurant Pickup Address
                        Row(verticalAlignment = Alignment.Top) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .background(OrangePrimary.copy(alpha = 0.2f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Storefront,
                                    contentDescription = "রেস্টুরেন্ট",
                                    tint = OrangePrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "পিকআপ: ${order.restaurantName}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = order.restaurantAddress,
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                                if (order.restaurantPhone.isNotBlank()) {
                                    Text(
                                        text = "ফোন: ${order.restaurantPhone}",
                                        fontSize = 10.sp,
                                        color = BlueAccent
                                    )
                                }
                            }
                        }

                        Divider(
                            modifier = Modifier.padding(vertical = 10.dp),
                            color = Color.DarkGray.copy(alpha = 0.5f)
                        )

                        // Customer Delivery Address
                        Row(verticalAlignment = Alignment.Top) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .background(SuccessGreen.copy(alpha = 0.2f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = "কাস্টমার ঠিকানা",
                                    tint = SuccessGreen,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "ডেলিভারি ক্লায়েন্ট: ${order.customerName}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = order.customerAddress,
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                                if (order.customerNote.isNotBlank()) {
                                    Text(
                                        text = "নোট: ${order.customerNote}",
                                        fontSize = 10.sp,
                                        color = Color(0xFFFFD54F)
                                    )
                                }
                            }
                        }
                    }
                }

                if (order.itemsSummary.isNotBlank()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Fastfood,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = order.itemsSummary,
                            fontSize = 11.sp,
                            color = TextSecondary,
                            maxLines = 1
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Action Buttons: Accept / Reject
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onReject,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("btn_decline_order"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorRed)
                    ) {
                        Text(
                            text = "পাস করুন",
                            color = ErrorRed,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    Button(
                        onClick = onAccept,
                        modifier = Modifier
                            .weight(1.6f)
                            .height(48.dp)
                            .testTag("btn_accept_order"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "অর্ডার গ্রহণ করুন",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}
