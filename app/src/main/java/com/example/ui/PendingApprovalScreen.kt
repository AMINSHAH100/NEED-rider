package com.example.ui

import androidx.compose.animation.core.*
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PendingApprovalScreen(
    viewModel: RiderViewModel
) {
    val profile by viewModel.riderProfile.collectAsState()
    val isChecking by viewModel.isCheckingStatus.collectAsState()

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "অ্যাপ্রুভাল অপেক্ষা",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkSurface
                )
            )
        },
        containerColor = DarkBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            Box(
                modifier = Modifier
                    .size(110.dp)
                    .scale(pulseScale)
                    .background(PendingYellow.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(PendingYellow, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.HourglassTop,
                        contentDescription = "Pending Status",
                        tint = DarkBackground,
                        modifier = Modifier.size(42.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "রেজিস্ট্রেশন পর্যালোচনায় আছে",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "আপনার রাইডার রিকোয়েস্টটি লাইভ অ্যাডমিন প্যানেলে (needadmin.jotpot.it) পাঠানো হয়েছে। অ্যাডমিন কর্তৃক ভেরিফিকেশন ও অ্যাপ্রুভালের পর অ্যাপ স্বয়ংক্রিয়ভাবে অ্যাক্টিভ হবে।",
                fontSize = 14.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp,
                modifier = Modifier.padding(horizontal = 12.dp)
            )

            Spacer(modifier = Modifier.height(28.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkCard),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "লাইভ রিকোয়েস্ট বিবরণ",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = OrangeSecondary
                        )
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = PendingYellow.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "PENDING",
                                color = PendingYellow,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = Color.DarkGray)
                    Spacer(modifier = Modifier.height(14.dp))

                    InfoRow(label = "রাইডার আইডি", value = profile.riderId.take(12).ifBlank { profile.authUid.take(12) })
                    InfoRow(label = "নাম", value = profile.fullName.ifBlank { "Rider" })
                    InfoRow(label = "ফোন", value = profile.phoneNumber)
                    InfoRow(label = "ইমেইল ভেরিফিকেশন", value = "ভেরিফাইড ✓")
                    InfoRow(label = "যানবাহন", value = "${profile.vehicleType} (${profile.vehicleNumber})")
                    InfoRow(label = "লোকেশন", value = profile.city.ifBlank { "Collesalvetti, Italy" })
                    InfoRow(label = "অ্যাডমিন প্যানেল", value = "needadmin.jotpot.it")
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            Button(
                onClick = { viewModel.refreshApprovalStatus() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("refresh_status_button"),
                enabled = !isChecking,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DarkSurface,
                    contentColor = OrangePrimary
                )
            ) {
                if (isChecking) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = OrangePrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("স্ট্যাটাস চেক করা হচ্ছে...")
                } else {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "স্ট্যাটাস রিফ্রেশ করুন",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            TextButton(
                onClick = { viewModel.logout() },
                modifier = Modifier.testTag("btn_back_to_login")
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "লগইন স্ক্রিনে ফিরে যান",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            color = TextSecondary
        )
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White
        )
    }
}
