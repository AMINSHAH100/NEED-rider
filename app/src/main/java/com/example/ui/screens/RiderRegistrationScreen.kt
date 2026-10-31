package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.ContactPhone
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.HowToReg
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.OnlineGreen
import com.example.ui.theme.RiderPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RiderRegistrationScreen(
    onSubmitRegistration: (
        name: String,
        phone: String,
        email: String,
        nidNumber: String,
        vehicleType: String,
        vehiclePlate: String,
        zone: String,
        emergencyContact: String,
        licenseNumber: String,
        onComplete: (Boolean, String?) -> Unit
    ) -> Unit
) {
    val context = LocalContext.current

    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var nidNumber by remember { mutableStateOf("") }
    var vehicleType by remember { mutableStateOf("MOTORCYCLE") } // MOTORCYCLE, BICYCLE, SCOOTER
    var vehiclePlate by remember { mutableStateOf("") }
    var licenseNumber by remember { mutableStateOf("") }
    var zone by remember { mutableStateOf("Collesalvetti / Livorno (Italy)") }
    var emergencyContact by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }
    var submissionError by remember { mutableStateOf<String?>(null) }

    val vehicleOptions = listOf(
        Triple("MOTORCYCLE", "মোটরবাইক", "🏍️"),
        Triple("BICYCLE", "বাইসাইকেল", "🚲"),
        Triple("SCOOTER", "ই-স্কুটার", "🛵")
    )

    val zoneOptions = listOf(
        "Collesalvetti / Livorno (Italy)",
        "Toscana / Pisa Hub (Italy)",
        "Firenze Centro Hub (Italy)",
        "Lucca / Viareggio (Italy)",
        "Rosignano Marittimo (Italy)"
    )

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
                            text = "রাইডার রেজিস্ট্রেশন আবেদন",
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Hero Banner
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = RiderPrimary.copy(alpha = 0.2f),
                            modifier = Modifier.size(54.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.HowToReg,
                                contentDescription = null,
                                tint = RiderPrimary,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "রাইডার পার্টনার আবেদন ফরম",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "অ্যাপ থেকে আপনার জাতীয় পরিচয়পত্র ও প্রয়োজনীয় তথ্য দিয়ে এডমিন প্যানেলে আবেদন পাঠান। এডমিন অনুমোদন দেওয়ার পর আপনি কাজ শুরু করতে পারবেন।",
                            fontSize = 12.sp,
                            color = Color(0xFF94A3B8),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // Section 1: Personal Details
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "ব্যক্তিগত তথ্য (Personal Info)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = RiderPrimary
                        )

                        // Full Name
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("পূর্ণ নাম (Full Name)*") },
                            placeholder = { Text("যেমন: তানভীর আহমেদ") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("reg_name_input"),
                            singleLine = true,
                            leadingIcon = {
                                Icon(Icons.Default.Person, contentDescription = null, tint = RiderPrimary)
                            },
                            shape = RoundedCornerShape(12.dp)
                        )

                        // Mobile Phone Number
                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = { Text("মোবাইল নম্বর (Phone Number)*") },
                            placeholder = { Text("+880 1712-345678") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("reg_phone_input"),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true,
                            leadingIcon = {
                                Icon(Icons.Default.Phone, contentDescription = null, tint = RiderPrimary)
                            },
                            shape = RoundedCornerShape(12.dp)
                        )

                        // Email
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("ইমেইল ঠিকানা (Email)") },
                            placeholder = { Text("tanvir.rider@gmail.com") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            singleLine = true,
                            leadingIcon = {
                                Icon(Icons.Default.Email, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            },
                            shape = RoundedCornerShape(12.dp)
                        )

                        // National ID (NID)
                        OutlinedTextField(
                            value = nidNumber,
                            onValueChange = { nidNumber = it },
                            label = { Text("জাতীয় পরিচয়পত্র নম্বর (NID Number)*") },
                            placeholder = { Text("1994269123456789") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("reg_nid_input"),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            leadingIcon = {
                                Icon(Icons.Default.CreditCard, contentDescription = null, tint = RiderPrimary)
                            },
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }

            // Section 2: Vehicle & Driving License Info
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "যানবাহন ও লাইসেন্স (Vehicle & License)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = RiderPrimary
                        )

                        // Vehicle Type Selector Chips
                        Text(
                            text = "যানবাহনের ধরন নির্বাচন করুন:",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            vehicleOptions.forEach { (typeCode, typeLabel, iconEmoji) ->
                                val isSelected = vehicleType == typeCode
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { vehicleType = typeCode },
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) RiderPrimary else MaterialTheme.colorScheme.surfaceVariant,
                                    border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                                ) {
                                    Column(
                                        modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(text = iconEmoji, fontSize = 20.sp)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = typeLabel,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }

                        // Vehicle Plate
                        OutlinedTextField(
                            value = vehiclePlate,
                            onValueChange = { vehiclePlate = it },
                            label = { Text("গাড়ির নম্বর প্লেট (Plate No)*") },
                            placeholder = { Text("ঢাকা মেট্রো-হ ৫৫-৮৯১২") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            leadingIcon = {
                                Icon(Icons.Default.TwoWheeler, contentDescription = null, tint = RiderPrimary)
                            },
                            shape = RoundedCornerShape(12.dp)
                        )

                        // Driving License
                        OutlinedTextField(
                            value = licenseNumber,
                            onValueChange = { licenseNumber = it },
                            label = { Text("ড্রাইভিং লাইসেন্স নম্বর (License No)") },
                            placeholder = { Text("DK-8921004") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            leadingIcon = {
                                Icon(Icons.Default.Badge, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            },
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }

            // Section 3: Zone & Emergency Contact
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "কাজের এরিয়া ও জরুরি যোগাযোগ",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = RiderPrimary
                        )

                        // Delivery Zone Choice
                        Text(text = "পছন্দের কাজের জোন:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            zoneOptions.forEach { zName ->
                                val isSelected = zone == zName
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable { zone = zName },
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSelected) RiderPrimary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant,
                                    border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, RiderPrimary) else null
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.LocationOn,
                                            contentDescription = null,
                                            tint = if (isSelected) RiderPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = zName,
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) RiderPrimary else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }

                        // Emergency Contact
                        OutlinedTextField(
                            value = emergencyContact,
                            onValueChange = { emergencyContact = it },
                            label = { Text("জরুরি যোগাযোগ নম্বর (Emergency Contact)*") },
                            placeholder = { Text("+880 1819-990011") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true,
                            leadingIcon = {
                                Icon(Icons.Default.ContactPhone, contentDescription = null, tint = RiderPrimary)
                            },
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }

            // Submit Button & Error Display
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (submissionError != null) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFEF4444).copy(alpha = 0.15f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF4444))
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = Color(0xFFEF4444),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = submissionError ?: "",
                                    fontSize = 12.sp,
                                    color = Color(0xFFEF4444)
                                )
                            }
                        }
                    }

                    Button(
                        onClick = {
                            if (isSubmitting) return@Button
                            if (name.isBlank()) {
                                Toast.makeText(context, "দয়া করে আপনার পূর্ণ নাম লিখুন", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (phone.isBlank()) {
                                Toast.makeText(context, "দয়া করে মোবাইল নম্বর লিখুন", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            if (nidNumber.isBlank()) {
                                Toast.makeText(context, "দয়া করে জাতীয় পরিচয়পত্র (NID) নম্বর লিখুন", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            isSubmitting = true
                            submissionError = null

                            val performSubmit = {
                                onSubmitRegistration(
                                    name,
                                    phone,
                                    email.ifBlank { "${name.lowercase().replace(" ", "")}@rider.com" },
                                    nidNumber,
                                    vehicleType,
                                    vehiclePlate.ifBlank { "LI ${(100..999).random()} AA" },
                                    zone,
                                    emergencyContact.ifBlank { phone },
                                    licenseNumber.ifBlank { "LIC-${(100000..999999).random()}" },
                                    { success, errorMsg ->
                                        isSubmitting = false
                                        if (!success) {
                                            submissionError = errorMsg ?: "Firestore-এ আবেদন সংরক্ষণ করা যায়নি।"
                                            Toast.makeText(context, submissionError, Toast.LENGTH_LONG).show()
                                        }
                                    }
                                )
                            }

                            val auth = try {
                                com.google.firebase.auth.FirebaseAuth.getInstance()
                            } catch (e: Throwable) {
                                null
                            }

                            if (auth != null && auth.currentUser == null) {
                                auth.signInAnonymously()
                                    .addOnSuccessListener {
                                        performSubmit()
                                    }
                                    .addOnFailureListener { e ->
                                        // Still attempt submission in case Firestore rule allows unauthenticated writes
                                        performSubmit()
                                    }
                            } else {
                                performSubmit()
                            }
                        },
                        enabled = !isSubmitting,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .testTag("submit_registration_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = RiderPrimary,
                            disabledContainerColor = RiderPrimary.copy(alpha = 0.6f)
                        )
                    ) {
                        if (isSubmitting) {
                            androidx.compose.material3.CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = Color.White,
                                strokeWidth = 2.5.dp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Firestore-এ জমা হচ্ছে...",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color.White
                            )
                        } else {
                            Icon(imageVector = Icons.Default.HowToReg, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "এডমিন প্যানেলে অনুমোদনের আবেদন পাঠান",
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
