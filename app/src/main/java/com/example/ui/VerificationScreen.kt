package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerificationScreen(
    viewModel: RiderViewModel
) {
    val regData by viewModel.pendingRegistrationData.collectAsState()
    val isEmailVerified by viewModel.isEmailVerified.collectAsState()
    val emailNotice by viewModel.emailVerificationNotice.collectAsState()
    val isResendingEmail by viewModel.isResendingEmail.collectAsState()
    val emailCooldown by viewModel.emailCooldownSeconds.collectAsState()

    val verificationError by viewModel.verificationError.collectAsState()
    val verificationSuccess by viewModel.verificationSuccessMessage.collectAsState()
    val isChecking by viewModel.isCheckingStatus.collectAsState()
    val isSubmitting by viewModel.isSubmitting.collectAsState()

    BackHandler {
        viewModel.cancelVerification()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "ইমেইল ভেরিফিকেশন (Email Verification)",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.cancelVerification() },
                        modifier = Modifier.testTag("btn_back_verification")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "ফিরে যান",
                            tint = Color.White
                        )
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
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Verified Badge Icon
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .clip(CircleShape)
                    .background(OrangePrimary.copy(alpha = 0.15f))
                    .border(2.dp, OrangePrimary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.MarkEmailRead,
                    contentDescription = null,
                    tint = OrangePrimary,
                    modifier = Modifier.size(46.dp)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "ইমেইল ঠিকানা যাচাইকরণ",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Notice text required: "Verification email sent, check your inbox and spam folder."
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = DarkCard,
                border = androidx.compose.foundation.BorderStroke(1.dp, OrangePrimary.copy(alpha = 0.4f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = OrangeSecondary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = emailNotice ?: "Verification email sent, check your inbox and spam folder.",
                        color = Color(0xFFFFD54F),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Error Message Banner (Shows a clear error if email is not verified yet)
            if (verificationError != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 14.dp)
                        .testTag("verification_error_banner"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = ErrorRed.copy(alpha = 0.15f))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Error, contentDescription = null, tint = ErrorRed, modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = verificationError ?: "",
                            color = Color(0xFFFF8A80),
                            fontSize = 13.sp,
                            lineHeight = 18.sp,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Success Message Banner
            if (verificationSuccess != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 14.dp)
                        .testTag("verification_success_banner"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SuccessGreen.copy(alpha = 0.15f))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = verificationSuccess ?: "",
                            color = Color(0xFFA7F3D0),
                            fontSize = 13.sp,
                            lineHeight = 18.sp,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Rider Registered Email Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkCard)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "রেজিস্টার্ড ইমেইল",
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                        if (isEmailVerified) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = SuccessGreen.copy(alpha = 0.2f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("VERIFIED", color = SuccessGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        } else {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = PendingYellow.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = "UNVERIFIED",
                                    color = PendingYellow,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = regData.email.ifBlank { "আপনার ইমেইল" },
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = Color.DarkGray)
                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "আমরা আপনার ইমেইল ঠিকানায় একটি অ্যাক্টিভেশন লিংক পাঠিয়েছি। ইনবক্স অথবা স্প্যাম ফোল্ডার চেক করে লিংকে ক্লিক করুন, এরপর নিচের বাটনে চাপ দিন।",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )
                }
            }

            // 2. "I have verified" button
            Button(
                onClick = { viewModel.onIHaveVerifiedClicked() },
                enabled = !isChecking && !isSubmitting,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("btn_i_have_verified"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
            ) {
                if (isChecking || isSubmitting) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("যাচাই করা হচ্ছে...", color = Color.White, fontSize = 15.sp)
                } else {
                    Icon(Icons.Default.DoneAll, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("I have verified (আমি ভেরিফাই করেছি)", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 1. "Resend email" button with 60-second cooldown
            OutlinedButton(
                onClick = { viewModel.resendVerificationEmail() },
                enabled = emailCooldown == 0 && !isResendingEmail,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("btn_resend_email"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = androidx.compose.ui.graphics.SolidColor(if (emailCooldown == 0) OrangePrimary else Color.DarkGray)
                )
            ) {
                if (isResendingEmail) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("ইমেইল পাঠানো হচ্ছে...", fontSize = 14.sp)
                } else if (emailCooldown > 0) {
                    Icon(Icons.Default.Timer, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Resend email (${emailCooldown}s)", color = Color.Gray, fontSize = 14.sp)
                } else {
                    Icon(Icons.Default.Email, contentDescription = null, tint = OrangeSecondary, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Resend email (পুনরায় ইমেইল পাঠান)", color = OrangeSecondary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "ইমেইল ভেরিফাই হওয়ার পর আপনার আবেদনটি স্বয়ংক্রিয়ভাবে অ্যাডমিন প্যানেলে (needadmin.jotpot.it) জমা হবে।",
                fontSize = 12.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}
