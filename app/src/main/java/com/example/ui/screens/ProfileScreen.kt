package com.example.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ElectricBike
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.PedalBike
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.SupportAgent
import com.example.ui.components.LanguagePickerDialog
import com.example.ui.components.VehicleChangeRequestDialog
import com.example.data.local.entity.RiderProfileEntity
import com.example.util.AppStrings
import com.example.ui.theme.CashGold
import com.example.ui.theme.OfflineRed
import com.example.ui.theme.OnlineGreen
import com.example.ui.theme.RiderPrimary

@Composable
fun ProfileScreen(
    profile: RiderProfileEntity?,
    onSetLanguage: (String) -> Unit,
    onToggleAutoAccept: () -> Unit,
    onSetZone: (String) -> Unit,
    onOpenSos: () -> Unit,
    onRequestVehicleChange: (newType: String, newPlate: String, reason: String) -> Unit,
    lang: String = if (java.util.Locale.getDefault().language.startsWith("it")) "it" else "en"
) {
    var showZoneSelector by remember { mutableStateOf(false) }
    var showLangSelector by remember { mutableStateOf(false) }
    var showVehicleChangeDialog by remember { mutableStateOf(false) }

    val isBn = lang == "bn"
    val isIt = lang == "it"

    val zones = listOf("Gulshan-1 & 2", "Banani", "Dhanmondi", "Uttara", "Mirpur-10", "Bashundhara R/A")
    val languages = listOf(
        Triple("bn", "বাংলা", "Bengali (বাং)"),
        Triple("en", "English", "English (EN)"),
        Triple("it", "Italiano", "Italian (IT)")
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Rider ID & Avatar Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A))
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(RiderPrimary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "TR",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = profile?.name ?: "তানভীর আহমেদ",
                                color = Color.White,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = "Verified",
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Text(
                            text = "Rider ID: ${profile?.riderCode ?: "#RD-9942"}",
                            color = Color(0xFF94A3B8),
                            fontSize = 12.sp
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = CashGold,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${profile?.rating ?: 4.92} Rating",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = " • ${profile?.totalDeliveries ?: 384} Trips",
                                color = Color(0xFF94A3B8),
                                fontSize = 12.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Availability status badge
                        val currentAvail = profile?.availabilityStatus ?: if (profile?.isOnline == true) "Available" else "Offline"
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = when (currentAvail) {
                                "Available" -> OnlineGreen.copy(alpha = 0.2f)
                                "Busy" -> Color(0xFFF59E0B).copy(alpha = 0.2f)
                                else -> OfflineRed.copy(alpha = 0.2f)
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .background(
                                            when (currentAvail) {
                                                "Available" -> OnlineGreen
                                                "Busy" -> Color(0xFFF59E0B)
                                                else -> OfflineRed
                                            },
                                            CircleShape
                                        )
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = when (currentAvail) {
                                        "Available" -> if (isBn) "উপলব্ধ (Available)" else "Available"
                                        "Busy" -> if (isBn) "অর্ডার ডেলিভারিতে ব্যস্ত" else "On Active Order"
                                        else -> if (isBn) "অফলাইন (Offline)" else "Offline"
                                    },
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = when (currentAvail) {
                                        "Available" -> OnlineGreen
                                        "Busy" -> Color(0xFFF59E0B)
                                        else -> OfflineRed
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Contact Information Card (Phone, Email, Emergency)
        item {
            val context = androidx.compose.ui.platform.LocalContext.current
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("rider_contact_info_card"),
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
                            text = if (isBn) "যোগাযোগের বিবরণ (Contact Info)" else "Contact Information",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = if (isBn) "যাচাইকৃত" else "Verified",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = OnlineGreen,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Phone Number Row
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${profile?.phone ?: "+8801712345678"}"))
                                try { context.startActivity(intent) } catch (_: Exception) {}
                            },
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = OnlineGreen.copy(alpha = 0.15f),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Phone,
                                    contentDescription = null,
                                    tint = OnlineGreen,
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isBn) "মোবাইল নম্বর (Phone)" else "Phone Number",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = profile?.phone ?: "+880 1712-345678",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Text(
                                text = if (isBn) "কল করুন" else "Call",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = OnlineGreen
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Email Address Row
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                val emailIntent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:${profile?.email ?: "tanvir.rider@foodie.com"}"))
                                try { context.startActivity(emailIntent) } catch (_: Exception) {}
                            },
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = RiderPrimary.copy(alpha = 0.15f),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SupportAgent,
                                    contentDescription = null,
                                    tint = RiderPrimary,
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isBn) "ইমেইল ঠিকানা (Email)" else "Email Address",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = profile?.email ?: "tanvir.rider@foodie.com",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Text(
                                text = if (isBn) "ইমেইল পাঠান" else "Mail",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = RiderPrimary
                            )
                        }
                    }
                }
            }
        }

        // Performance KPI Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                KpiBadge(
                    title = if (isBn) "গ্রহণ হার" else "Acceptance",
                    value = "${profile?.acceptanceRate ?: 98}%",
                    modifier = Modifier.weight(1f),
                    color = OnlineGreen
                )
                KpiBadge(
                    title = if (isBn) "সম্পন্ন হার" else "Completion",
                    value = "${profile?.completionRate ?: 99}%",
                    modifier = Modifier.weight(1f),
                    color = RiderPrimary
                )
                KpiBadge(
                    title = if (isBn) "সময়মতো" else "On-Time",
                    value = "${profile?.onTimeRate ?: 96}%",
                    modifier = Modifier.weight(1f),
                    color = Color(0xFF0284C7)
                )
            }
        }

        // Registered & Locked Vehicle Card (Policy Protected)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("registered_vehicle_card"),
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
                            Text(
                                text = if (isBn) "নিবন্ধিত ডেলিভারি যানবাহন" else if (isIt) "Veicolo di Consegna Registrato" else "Registered Delivery Vehicle",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }

                        // Verified & Locked Badge
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF10B981).copy(alpha = 0.15f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Locked",
                                    tint = Color(0xFF10B981),
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isBn) "ভেরিফাইড ও লকড" else if (isIt) "Verificato (Bloccato)" else "Verified & Locked",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF10B981)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Vehicle Details Row
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(RiderPrimary.copy(alpha = 0.15f), RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = when (profile?.vehicleType) {
                                        "BICYCLE" -> Icons.Default.PedalBike
                                        "SCOOTER" -> Icons.Default.ElectricBike
                                        else -> Icons.Default.TwoWheeler
                                    },
                                    contentDescription = null,
                                    tint = RiderPrimary,
                                    modifier = Modifier.size(26.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = when (profile?.vehicleType) {
                                        "BICYCLE" -> if (isBn) "বাইসাইকেল (Bicycle)" else if (isIt) "Bicicletta" else "Bicycle"
                                        "SCOOTER" -> if (isBn) "ইলেকট্রিক স্কুটার (E-Scooter)" else if (isIt) "Monopattino Elettrico" else "E-Scooter"
                                        else -> if (isBn) "মোটরবাইক (Motorcycle)" else if (isIt) "Moto / Scooter" else "Motorcycle"
                                    },
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${profile?.vehiclePlate ?: "Dhaka Metro-HA 55-8912"} • অনবোর্ডিং ভেরিফাইড",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Policy Explanation
                    Text(
                        text = if (isBn) {
                            "ℹ️ রেজিস্ট্রেশন বা অনবোর্ডিংয়ের সময় এই যানবাহনটি নির্ধারণ করা হয়েছে। নিয়ম অনুযায়ী এটি সরাসরি পরিবর্তন করা যায় না। যানবাহন পরিবর্তন করতে প্রধান এডমিন বা হেল্পলাইনে মেসেজ পাঠান।"
                        } else if (isIt) {
                            "ℹ️ Questo veicolo è stato registrato durante l'onboarding iniziale. Come da regolamento non è modificabile direttamente dal profilo. Per cambiare mezzo, invia una richiesta all'Admin."
                        } else {
                            "ℹ️ This vehicle was selected and approved during rider registration. It cannot be directly modified. To update or switch vehicles, send a change request to Main Admin."
                        },
                        fontSize = 11.sp,
                        lineHeight = 15.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Request Change to Admin Button
                    OutlinedButton(
                        onClick = { showVehicleChangeDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("request_vehicle_change_button"),
                        shape = RoundedCornerShape(10.dp),
                        colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                            contentColor = RiderPrimary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.SupportAgent,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isBn) "এডমিন / হেল্পলাইনে পরিবর্তনের মেসেজ পাঠান" else if (isIt) "Richiedi Modifica all'Admin / Supporto" else "Message Admin to Request Vehicle Change",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // App Preferences & Settings
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (isBn) "অ্যাপ সেটিংস ও সুবিধা" else "Preferences & Controls",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Language Selector (English & Italian Default + All Other Languages)
                    val currentLangItem = AppStrings.supportedLanguages.find { it.code.equals(lang, ignoreCase = true) }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { showLangSelector = true }
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Language,
                                contentDescription = null,
                                tint = RiderPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (isBn) "অ্যাপের ভাষা (Language)" else if (isIt) "Lingua dell'Applicazione" else "App Language",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = if (isBn) "ডিফল্ট: ইংরেজি ও ইতালিয়ান • পরিবর্তন করতে ট্যাপ করুন" 
                                           else if (isIt) "Predefinito: Italiano & Inglese • Tocca per cambiare"
                                           else "Default: English & Italian • Tap to change",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = RiderPrimary.copy(alpha = 0.12f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, RiderPrimary.copy(alpha = 0.4f))
                        ) {
                            Text(
                                text = "${currentLangItem?.flag ?: "🌐"} ${currentLangItem?.nativeName ?: lang.uppercase()}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                color = RiderPrimary
                            )
                        }
                    }

                    if (showLangSelector) {
                        LanguagePickerDialog(
                            currentLang = lang,
                            onSelectLanguage = { newLang ->
                                onSetLanguage(newLang)
                                showLangSelector = false
                            },
                            onDismiss = { showLangSelector = false }
                        )
                    }

                    Divider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                    // Auto-Accept Orders Toggle
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Sync,
                                contentDescription = null,
                                tint = OnlineGreen,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (isBn) "অটো-অ্যাকসেপ্ট অর্ডার" else "Auto-Accept Orders",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = if (isBn) "পিক আওয়ারে অটোমেটিক অর্ডার গ্রহণ করুন" else "Automatically accept nearby orders",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Switch(
                            checked = profile?.autoAccept ?: false,
                            onCheckedChange = { onToggleAutoAccept() },
                            colors = SwitchDefaults.colors(checkedThumbColor = OnlineGreen, checkedTrackColor = OnlineGreen.copy(alpha = 0.4f))
                        )
                    }

                    Divider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                    // Operating Zone Switcher
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showZoneSelector = !showZoneSelector }
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocationCity,
                                contentDescription = null,
                                tint = Color(0xFFF59E0B),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (isBn) "ডিউটি জোন পরিবর্তন" else "Change Duty Hub Zone",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = profile?.currentZone ?: "Gulshan Hub",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (showZoneSelector) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(10.dp))
                                .padding(8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            zones.forEach { zName ->
                                val isCur = profile?.currentZone == zName
                                Text(
                                    text = zName,
                                    fontSize = 12.sp,
                                    fontWeight = if (isCur) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isCur) RiderPrimary else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            onSetZone(zName)
                                            showZoneSelector = false
                                        }
                                        .padding(vertical = 4.dp, horizontal = 8.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Emergency SOS & Help Support Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (isBn) "জরুরি সাহায্য ও সাপোর্ট" else "Support & Emergency",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = onOpenSos,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("profile_sos_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = OfflineRed)
                    ) {
                        Icon(imageVector = Icons.Default.Warning, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isBn) "জরুরি এসওএস (SOS) চালু করুন" else "Activate Emergency SOS",
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = { /* Call helpdesk */ },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.HelpOutline, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isBn) "২৪/৭ রাইডার হেল্পডেস্ক" else "24/7 Rider Helpdesk",
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }

    if (showVehicleChangeDialog) {
        VehicleChangeRequestDialog(
            currentVehicleType = profile?.vehicleType ?: "MOTORCYCLE",
            currentPlate = profile?.vehiclePlate ?: "Dhaka Metro-HA 55-8912",
            onDismiss = { showVehicleChangeDialog = false },
            onSubmitRequest = { newType, newPlate, reason ->
                onRequestVehicleChange(newType, newPlate, reason)
                showVehicleChangeDialog = false
            },
            lang = lang
        )
    }
}

@Composable
private fun KpiBadge(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                color = color
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = title,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
