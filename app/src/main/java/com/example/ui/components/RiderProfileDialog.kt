package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.RiderProfile
import com.example.ui.theme.*
import java.util.Locale

@Composable
fun RiderProfileDialog(
    profile: RiderProfile,
    onDismiss: () -> Unit,
    onToggleDuty: () -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .testTag("rider_profile_dialog"),
            shape = RoundedCornerShape(24.dp),
            color = DarkCard
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Top Header Row
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
                                imageVector = Icons.Default.Badge,
                                contentDescription = null,
                                tint = OrangePrimary,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "রাইডার প্রোফাইল",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("btn_close_profile")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "বন্ধ করুন", tint = Color.LightGray)
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Profile Header Card (Avatar, Name, ID, Verification)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Avatar with initials
                        Box(contentAlignment = Alignment.BottomEnd) {
                            Box(
                                modifier = Modifier
                                    .size(76.dp)
                                    .background(OrangePrimary, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                val initials = profile.fullName
                                    .split(" ")
                                    .filter { it.isNotBlank() }
                                    .take(2)
                                    .mapNotNull { it.firstOrNull()?.uppercaseChar() }
                                    .joinToString("")
                                    .ifBlank { "RD" }

                                Text(
                                    text = initials,
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                            }

                            // Verified Checkmark Badge
                            Surface(
                                shape = CircleShape,
                                color = SuccessGreen,
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Verified",
                                    tint = Color.White,
                                    modifier = Modifier.padding(4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Full Name
                        Text(
                            text = profile.fullName.ifBlank { "Food Rider" },
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Rider ID with Copy button
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = DarkCard,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    val clip = ClipData.newPlainText("Rider ID", profile.riderId)
                                    clipboard?.setPrimaryClip(clip)
                                    Toast.makeText(context, "রাইডার আইডি কপি করা হয়েছে!", Toast.LENGTH_SHORT).show()
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Rider ID: ${profile.riderId.take(12)}...",
                                    fontSize = 12.sp,
                                    color = OrangeSecondary,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy",
                                    tint = OrangeSecondary,
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Approved & Active Status Pill
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = SuccessGreen.copy(alpha = 0.15f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(modifier = Modifier.size(7.dp).background(SuccessGreen, CircleShape))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "ভেরিফাইড ও সক্রিয় রাইডার",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SuccessGreen
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Stats Summary (Earnings, Deliveries, Cash)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ProfileStatItem(
                        title = "আজকের আয়",
                        value = "€${String.format(Locale.US, "%.2f", profile.todayEarnings)}",
                        color = OrangePrimary,
                        modifier = Modifier.weight(1f)
                    )
                    ProfileStatItem(
                        title = "ডেলিভারি",
                        value = "${profile.todayDeliveries} টি",
                        color = SuccessGreen,
                        modifier = Modifier.weight(1f)
                    )
                    ProfileStatItem(
                        title = "ক্যাশ ব্যালেন্স",
                        value = "€${String.format(Locale.US, "%.2f", profile.cashInHand)}",
                        color = BlueAccent,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Personal & Contact Info Card
                Text(
                    text = "ব্যক্তিগত ও যোগাযোগ তথ্য",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = OrangeSecondary
                )
                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        // Phone Number
                        ProfileInfoRow(
                            icon = Icons.Default.Phone,
                            iconTint = SuccessGreen,
                            title = "মোবাইল নম্বর",
                            value = profile.phoneNumber.ifBlank { "+39 000 0000000" },
                            onActionClick = {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${profile.phoneNumber}"))
                                try { context.startActivity(intent) } catch (_: Exception) {}
                            },
                            actionText = "কল"
                        )

                        Divider(modifier = Modifier.padding(vertical = 10.dp), color = Color.DarkGray.copy(alpha = 0.4f))

                        // Email
                        ProfileInfoRow(
                            icon = Icons.Default.Email,
                            iconTint = BlueAccent,
                            title = "ইমেইল ঠিকানা",
                            value = profile.email.ifBlank { "rider@need.it" },
                            onActionClick = {
                                val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:${profile.email}"))
                                try { context.startActivity(intent) } catch (_: Exception) {}
                            },
                            actionText = "মেইল"
                        )

                        Divider(modifier = Modifier.padding(vertical = 10.dp), color = Color.DarkGray.copy(alpha = 0.4f))

                        // Emergency Contact
                        ProfileInfoRow(
                            icon = Icons.Default.ContactEmergency,
                            iconTint = ErrorRed,
                            title = "জরুরি যোগাযোগ",
                            value = profile.emergencyContact.ifBlank { "জরুরি সাপোর্ট: 112" },
                            onActionClick = {
                                val num = profile.emergencyContact.ifBlank { "112" }
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$num"))
                                try { context.startActivity(intent) } catch (_: Exception) {}
                            },
                            actionText = "কল"
                        )

                        Divider(modifier = Modifier.padding(vertical = 10.dp), color = Color.DarkGray.copy(alpha = 0.4f))

                        // Working City / Hub
                        ProfileInfoRow(
                            icon = Icons.Default.LocationOn,
                            iconTint = OrangePrimary,
                            title = "কাজের এরিয়া / জোন",
                            value = profile.city.ifBlank { "Collesalvetti / Livorno (Italy)" },
                            onActionClick = null,
                            actionText = null
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Vehicle & License Information Card
                Text(
                    text = "যানবাহন ও লাইসেন্স বিবরণ",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = OrangeSecondary
                )
                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        // Vehicle Type
                        ProfileInfoRow(
                            icon = Icons.Default.TwoWheeler,
                            iconTint = OrangePrimary,
                            title = "যানবাহনের ধরন",
                            value = profile.vehicleType,
                            onActionClick = null,
                            actionText = null
                        )

                        Divider(modifier = Modifier.padding(vertical = 10.dp), color = Color.DarkGray.copy(alpha = 0.4f))

                        // Vehicle Plate
                        ProfileInfoRow(
                            icon = Icons.Default.Pin,
                            iconTint = SuccessGreen,
                            title = "লাইসেন্স প্লেট নম্বর",
                            value = profile.vehicleNumber.ifBlank { "LI 123 AA" },
                            onActionClick = null,
                            actionText = null
                        )

                        if (profile.licenseNumber.isNotBlank()) {
                            Divider(modifier = Modifier.padding(vertical = 10.dp), color = Color.DarkGray.copy(alpha = 0.4f))
                            ProfileInfoRow(
                                icon = Icons.Default.DriveEta,
                                iconTint = BlueAccent,
                                title = "ড্রাইভিং লাইসেন্স নম্বর",
                                value = profile.licenseNumber,
                                onActionClick = null,
                                actionText = null
                            )
                        }

                        if (profile.nidNumber.isNotBlank()) {
                            Divider(modifier = Modifier.padding(vertical = 10.dp), color = Color.DarkGray.copy(alpha = 0.4f))
                            ProfileInfoRow(
                                icon = Icons.Default.Fingerprint,
                                iconTint = TextSecondary,
                                title = "এনআইডি / Codice Fiscale",
                                value = profile.nidNumber,
                                onActionClick = null,
                                actionText = null
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Duty Toggle Row
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .background(if (profile.isOnline) SuccessGreen.copy(alpha = 0.2f) else Color.DarkGray, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PowerSettingsNew,
                                    contentDescription = null,
                                    tint = if (profile.isOnline) SuccessGreen else Color.LightGray,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (profile.isOnline) "ডিউটি স্ট্যাটাস: অনলাইন" else "ডিউটি স্ট্যাটাস: অফলাইন",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = if (profile.isOnline) "নতুন অর্ডার আসার জন্য সক্রিয়" else "অর্ডার আসা বন্ধ আছে",
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                            }
                        }

                        Switch(
                            checked = profile.isOnline,
                            onCheckedChange = { onToggleDuty() },
                            colors = SwitchDefaults.colors(checkedThumbColor = OrangePrimary)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Action Buttons: Help & Logout
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:112"))
                            try { context.startActivity(intent) } catch (_: Exception) {}
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorRed)
                    ) {
                        Icon(Icons.Default.Sos, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("জরুরি SOS", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            onDismiss()
                            onLogout()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_profile_logout"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4A1515), contentColor = ErrorRed)
                    ) {
                        Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("লগআউট", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileInfoRow(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    value: String,
    onActionClick: (() -> Unit)?,
    actionText: String?
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(iconTint.copy(alpha = 0.15f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(16.dp))
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontSize = 11.sp, color = TextSecondary)
            Text(
                text = value,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
        if (onActionClick != null && actionText != null) {
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = iconTint.copy(alpha = 0.2f),
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .clickable { onActionClick() }
            ) {
                Text(
                    text = actionText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = iconTint,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun ProfileStatItem(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = DarkSurface
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold,
                color = color
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = title,
                fontSize = 10.sp,
                color = TextSecondary
            )
        }
    }
}
