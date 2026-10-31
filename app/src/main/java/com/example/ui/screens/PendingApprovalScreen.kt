package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.RiderProfileEntity
import com.example.ui.theme.OfflineRed
import com.example.ui.theme.OnlineGreen
import com.example.ui.theme.RiderPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PendingApprovalScreen(
    profile: RiderProfileEntity?,
    onRefreshStatus: () -> Unit,
    onReApply: () -> Unit
) {
    val isRejected = profile?.approvalStatus == "REJECTED"

    Scaffold(
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
                            text = if (isRejected) "আবেদন প্রত্যাখ্যাত" else "এডমিন অনুমোদন স্ট্যাটাস",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFF0F172A)
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Status Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isRejected) OfflineRed.copy(alpha = 0.10f) else Color(0xFF0F172A)
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.5.dp,
                        if (isRejected) OfflineRed else Color(0xFFF59E0B)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = if (isRejected) OfflineRed.copy(alpha = 0.2f) else Color(0xFFF59E0B).copy(alpha = 0.2f),
                            modifier = Modifier.size(68.dp)
                        ) {
                            Icon(
                                imageVector = if (isRejected) Icons.Default.Warning else Icons.Default.HourglassTop,
                                contentDescription = null,
                                tint = if (isRejected) OfflineRed else Color(0xFFF59E0B),
                                modifier = Modifier.padding(16.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = if (isRejected) "আবেদন অনুমোদিত হয়নি" else "আবেদন পর্যালোচনায় রয়েছে",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 19.sp,
                            color = if (isRejected) OfflineRed else Color.White
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Status Pill Badge
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isRejected) OfflineRed else Color(0xFFF59E0B)
                        ) {
                            Text(
                                text = if (isRejected) "REJECTED (প্রত্যাখ্যাত)" else "⏳ PENDING ADMIN APPROVAL (এডমিন অনুমোদনের অপেক্ষায়)",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isRejected) Color.White else Color(0xFF451A03)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = if (isRejected) {
                                "কারণ: ${profile?.rejectionReason?.ifBlank { "আপনার প্রদত্ত তথ্য যাচাই করা সম্ভব হয়নি।" }}"
                            } else {
                                "আপনার সকল ব্যক্তিগত ও যানবাহনের তথ্য প্রধান এডমিন প্যানেলে জমা হয়েছে। এডমিন প্যানেল থেকে অনুমোদন (Approve) করার পর আপনি ডিউটি চালু করে রাইডার হিসেবে কাজ শুরু করতে পারবেন।"
                            },
                            fontSize = 12.sp,
                            color = if (isRejected) MaterialTheme.colorScheme.onSurface else Color(0xFFCBD5E1),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // Submitted Application Summary Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = RiderPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "আবেদনের বিবরণ (Submitted Details)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                        Spacer(modifier = Modifier.height(12.dp))

                        DetailRow("রাইডার কোড:", profile?.riderCode?.ifBlank { "#RD-PENDING" } ?: "#RD-PENDING")
                        DetailRow("আবেদনকারীর নাম:", profile?.name?.ifBlank { "N/A" } ?: "N/A")
                        DetailRow("মোবাইল নম্বর:", profile?.phone?.ifBlank { "N/A" } ?: "N/A")
                        DetailRow("এনআইডি নম্বর:", profile?.nidNumber?.ifBlank { "N/A" } ?: "N/A")
                        DetailRow("যানবাহন:", "${profile?.vehicleType ?: "MOTORCYCLE"} (${profile?.vehiclePlate?.ifBlank { "N/A" }})")
                        DetailRow("কাজের জোন:", profile?.currentZone ?: "Gulshan Hub")
                        DetailRow("জরুরি যোগাযোগ:", profile?.emergencyContact?.ifBlank { "N/A" } ?: "N/A")
                    }
                }
            }

            // Real-time Status Sync Button
            item {
                OutlinedButton(
                    onClick = onRefreshStatus,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("refresh_approval_status_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "স্ট্যাটাস চেক ও রিফ্রেশ করুন",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }


            if (isRejected) {
                item {
                    Button(
                        onClick = onReApply,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("re_apply_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = RiderPrimary)
                    ) {
                        Text(
                            text = "তথ্য সংশোধন করে পুনরায় আবেদন করুন",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
