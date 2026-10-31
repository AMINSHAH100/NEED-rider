package com.example.ui.screens

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material.icons.filled.LocalAtm
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.PriceCheck
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.EarningEntity
import com.example.data.local.entity.RiderProfileEntity
import com.example.data.local.entity.TransactionEntity
import com.example.ui.theme.CashGold
import com.example.ui.theme.OfflineRed
import com.example.ui.theme.OnlineGreen
import com.example.ui.theme.RiderPrimary

@Composable
fun EarningsScreen(
    profile: RiderProfileEntity?,
    earnings: List<EarningEntity>,
    transactions: List<TransactionEntity>,
    onOpenWithdraw: () -> Unit,
    onOpenDeposit: () -> Unit,
    isBn: Boolean
) {
    val totalEarned = earnings.sumOf { it.totalEarned }
    val totalBase = earnings.sumOf { it.baseFee }
    val totalDistance = earnings.sumOf { it.distanceFee }
    val totalSurge = earnings.sumOf { it.surgeFee }
    // Strict requirement: Tips field must still appear in Rider App UI, but its value must always be hardcoded/displayed as 0.00 (or €0.00)
    val totalTips = 0.0
    // Admin Dashboard Visibility: Total accumulated company_tip / admin_fund collected from online orders
    val totalCompanyFund = earnings.sumOf { it.companyTip.coerceAtLeast(it.adminFund) }
    val totalOnlineTipOrders = earnings.count { it.companyTip > 0.0 || it.adminFund > 0.0 }

    val walletBalance = profile?.walletBalance ?: 1450.0
    val currentFloatingCash = profile?.currentFloatingCash ?: 840.0
    val floatingLimit = profile?.floatingCashLimit ?: 3500.0
    val cashRatio = (currentFloatingCash / floatingLimit).toFloat().coerceIn(0f, 1f)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Top Main Balance Hero Card (Instant Cashout)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF0F172A)
                )
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isBn) "উত্তোলনযোগ্য ওয়ালেট ব্যালেন্স" else "Withdrawable Wallet Balance",
                            fontSize = 13.sp,
                            color = Color(0xFF94A3B8)
                        )
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = OnlineGreen.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = if (isBn) "সরাসরি ক্যাশআউট" else "Instant Payout",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = OnlineGreen,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "৳${String.format("%.0f", walletBalance)}",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = onOpenWithdraw,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("cashout_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = OnlineGreen)
                    ) {
                        Icon(imageVector = Icons.Default.AccountBalanceWallet, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isBn) "বিকাশ / নগদ এ টাকা উত্তোলন করুন" else "Withdraw to bKash / Nagad",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }

        // Floating Cash (COD) Ledger Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
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
                                    imageVector = Icons.Default.LocalAtm,
                                    contentDescription = null,
                                    tint = Color(0xFFD97706),
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (isBn) "ফ্লোটিং ক্যাশ (কাস্টমার থেকে সংগৃহীত)" else "Floating Cash (In Hand COD)",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (isBn) "কোম্পানির প্রাপ্য ক্যাশ টাকা" else "Company remittance liability",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Text(
                            text = "৳${String.format("%.0f", currentFloatingCash)}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFD97706)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Progress bar for cash limit
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (isBn) "জমার লিমিট: ৳${floatingLimit.toInt()}" else "Limit: ৳${floatingLimit.toInt()}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${(cashRatio * 100).toInt()}% ব্যবহৃত",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (cashRatio > 0.8f) OfflineRed else Color(0xFFD97706)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    LinearProgressIndicator(
                        progress = { cashRatio },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = if (cashRatio > 0.8f) OfflineRed else Color(0xFFD97706),
                        trackColor = Color(0xFFE2E8F0)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedButton(
                        onClick = onOpenDeposit,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("deposit_floating_cash_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFD97706))
                    ) {
                        Icon(imageVector = Icons.Default.Paid, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isBn) "কোম্পানিতে ক্যাশ জমা প্রদান করুন" else "Remit / Settle Floating Cash",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        // Earnings Breakdown
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = if (isBn) "উপার্জনের বিস্তারিত বিবরণ" else "Earnings Breakdown",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Rows
                    BreakdownRow(
                        title = if (isBn) "বেস ডেলিভারি ফি" else "Base Delivery Fee",
                        amount = totalBase,
                        icon = Icons.Default.DeliveryDining,
                        iconColor = RiderPrimary
                    )
                    Divider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                    BreakdownRow(
                        title = if (isBn) "দূরত্ব সারচার্জ (Distance Pay)" else "Distance Surcharge",
                        amount = totalDistance,
                        icon = Icons.Default.PriceCheck,
                        iconColor = Color(0xFF0284C7)
                    )
                    Divider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                    BreakdownRow(
                        title = if (isBn) "সার্জ ও পিক আওয়ার বোনাস" else "Peak Hour Surge",
                        amount = totalSurge,
                        icon = Icons.Default.MonetizationOn,
                        iconColor = Color(0xFFF59E0B)
                    )
                    Divider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                    BreakdownRow(
                        title = if (isBn) "কাস্টমার বকশিশ (Tips)" else "Customer Tips",
                        amount = 0.00, // Strictly forced to 0.00 in Rider UI
                        icon = Icons.Default.VolunteerActivism,
                        iconColor = Color(0xFFEC4899)
                    )
                }
            }
        }

        // Admin Dashboard: Company Development Fund (company_tip / admin_fund) Record
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("admin_company_fund_report_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF38BDF8).copy(alpha = 0.6f))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFF0284C7).copy(alpha = 0.25f),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccountBalance,
                                    contentDescription = null,
                                    tint = Color(0xFF38BDF8),
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (isBn) "এডমিন ড্যাশবোর্ড: কোম্পানি ফান্ড রিপোর্ট" else "Admin Dashboard: Company Fund Report",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 14.sp,
                                    color = Color.White
                                )
                                Text(
                                    text = if (isBn) "অনলাইন টিপস ➔ কোম্পানি উন্নয়ন তহবিল" else "Online Tips ➔ Company Development Fund",
                                    fontSize = 11.sp,
                                    color = Color(0xFF38BDF8)
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF0284C7).copy(alpha = 0.3f)
                        ) {
                            Text(
                                text = if (isBn) "এডমিন রেকর্ড" else "Admin View",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF38BDF8),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (isBn) "মোট সংগৃহীত কোম্পানি ফান্ড (company_tip)" else "Total Collected Fund (company_tip)",
                                fontSize = 11.sp,
                                color = Color(0xFF94A3B8)
                            )
                            Text(
                                text = "৳${String.format("%.2f", totalCompanyFund)}",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF38BDF8)
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = if (isBn) "অনলাইন টিপস অর্ডার" else "Online Tip Orders",
                                fontSize = 11.sp,
                                color = Color(0xFF94A3B8)
                            )
                            Text(
                                text = "$totalOnlineTipOrders টি সম্পন্ন",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF1E293B).copy(alpha = 0.8f)
                    ) {
                        Text(
                            text = if (isBn) {
                                "ℹ️ চেকআউটে অনলাইন পেমেন্টের সকল টিপস স্বয়ংক্রিয়ভাবে কোম্পানি উন্নয়ন ফান্ডে (company_tip / admin_fund) জমা হয় এবং রাইডার ইন্টারফেসে 'Tips: ৳0.00' হিসেবে সংরক্ষিত থাকে।"
                            } else {
                                "ℹ️ Customer checkout online tips are allocated 100% to company development fund (company_tip / admin_fund) and forced as 0.00 in rider interface."
                            },
                            fontSize = 11.sp,
                            color = Color(0xFFCBD5E1),
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            }
        }

        // Section Title: Transactions History
        item {
            Text(
                text = if (isBn) "লেনদেন ও পেমেন্ট হিস্ট্রি" else "Transaction & Payout History",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        items(transactions) { tx ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = if (tx.type == "CASHOUT") Color(0xFFDCFCE7) else Color(0xFFFEF3C7),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = if (tx.type == "CASHOUT") Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                                contentDescription = null,
                                tint = if (tx.type == "CASHOUT") OnlineGreen else Color(0xFFD97706),
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (isBn) tx.titleBn else tx.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${tx.method} • ${tx.referenceNumber}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "${if (tx.type == "CASHOUT") "-" else "+"}৳${String.format("%.0f", tx.amount)}",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (tx.type == "CASHOUT") OnlineGreen else Color(0xFFD97706)
                        )
                        Text(
                            text = tx.status,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = OnlineGreen
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BreakdownRow(
    title: String,
    amount: Double,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Text(
            text = "৳${String.format("%.0f", amount)}",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
