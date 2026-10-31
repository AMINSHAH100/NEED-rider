package com.example.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import need.rider.R
import com.example.data.AuthMode
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    viewModel: RiderViewModel
) {
    val authMode by viewModel.authMode.collectAsState()
    val isSubmitting by viewModel.isSubmitting.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val resetPasswordSuccess by viewModel.resetPasswordSuccess.collectAsState()

    val loginIdentifier by viewModel.loginIdentifier.collectAsState()
    val loginPassword by viewModel.loginPassword.collectAsState()
    val regForm by viewModel.registrationForm.collectAsState()

    var passwordVisible by remember { mutableStateOf(false) }
    var showForgotPasswordDialog by remember { mutableStateOf(false) }
    var forgotPasswordEmail by remember { mutableStateOf("") }

    val vehicleOptions = listOf(
        Triple("Motorcycle", "মোটরবাইক", Icons.Default.TwoWheeler),
        Triple("Bicycle", "বাইসাইকেল", Icons.Default.DirectionsBike),
        Triple("Scooter", "ই-স্কুটার", Icons.Default.ElectricScooter),
        Triple("Car", "কার / প্রাইভেট কার", Icons.Default.DirectionsCar)
    )
    var vehicleExpanded by remember { mutableStateOf(false) }

    val cityOptions = listOf(
        "Collesalvetti / Livorno (Italy)",
        "Toscana / Pisa Hub (Italy)",
        "Firenze Centro Hub (Italy)",
        "Lucca / Viareggio (Italy)",
        "Rosignano Marittimo (Italy)"
    )
    var cityExpanded by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = DarkBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // App Brand Header with Authentic Rider Icon
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .border(2.dp, OrangePrimary, RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_rider_logo),
                    contentDescription = "Food Rider Logo",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Food Rider Italy",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Text(
                text = "ডেলিভারি রাইডার পোর্টাল (লাইভ সিস্টেম)",
                fontSize = 13.sp,
                color = OrangeSecondary
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Tab Switcher: Login / Register
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = DarkCard)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (authMode == AuthMode.LOGIN) OrangePrimary else Color.Transparent)
                            .clickable { viewModel.setAuthMode(AuthMode.LOGIN) }
                            .testTag("tab_login"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "লগইন (Login)",
                            fontWeight = if (authMode == AuthMode.LOGIN) FontWeight.Bold else FontWeight.Medium,
                            color = if (authMode == AuthMode.LOGIN) Color.White else TextSecondary,
                            fontSize = 14.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (authMode == AuthMode.REGISTER) OrangePrimary else Color.Transparent)
                            .clickable { viewModel.setAuthMode(AuthMode.REGISTER) }
                            .testTag("tab_register"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "নতুন রেজিস্ট্রেশন",
                            fontWeight = if (authMode == AuthMode.REGISTER) FontWeight.Bold else FontWeight.Medium,
                            color = if (authMode == AuthMode.REGISTER) Color.White else TextSecondary,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Error Banner
            if (errorMessage != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = ErrorRed.copy(alpha = 0.15f))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Error, contentDescription = null, tint = ErrorRed, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = errorMessage ?: "",
                            color = Color(0xFFFF8A80),
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            // Success Banner for Password Reset
            if (resetPasswordSuccess != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SuccessGreen.copy(alpha = 0.15f))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = resetPasswordSuccess ?: "",
                            color = Color(0xFFA7F3D0),
                            fontSize = 13.sp,
                            lineHeight = 18.sp,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { viewModel.clearResetSuccess() }) {
                            Icon(Icons.Default.Close, contentDescription = "বন্ধ করুন", tint = Color.LightGray, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            // MODE 1: LOGIN FORM
            if (authMode == AuthMode.LOGIN) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkCard)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Text(
                            text = "রাইডার লগইন (Email Login)",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "আপনার রেজিস্টার্ড ইমেইল ও পাসওয়ার্ড দিয়ে লগইন করুন",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        // Email Field
                        OutlinedTextField(
                            value = loginIdentifier,
                            onValueChange = { viewModel.updateLoginFields(identifier = it) },
                            label = { Text("ইমেইল ঠিকানা (Email)") },
                            placeholder = { Text("rider@example.com") },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = OrangePrimary) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("login_identifier_input"),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = OrangePrimary,
                                unfocusedBorderColor = Color.DarkGray,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Password Field
                        OutlinedTextField(
                            value = loginPassword,
                            onValueChange = { viewModel.updateLoginFields(password = it) },
                            label = { Text("পাসওয়ার্ড (Password)") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = OrangePrimary) },
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = null,
                                        tint = Color.Gray
                                    )
                                }
                            },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("login_password_input"),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = OrangePrimary,
                                unfocusedBorderColor = Color.DarkGray,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // 3. Forgot Password Link
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Text(
                                text = "পাসওয়ার্ড ভুলে গেছেন? (Forgot Password?)",
                                color = OrangeSecondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier
                                    .clickable {
                                        forgotPasswordEmail = loginIdentifier.ifBlank { "" }
                                        showForgotPasswordDialog = true
                                    }
                                    .padding(vertical = 4.dp)
                                    .testTag("btn_forgot_password")
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { viewModel.login() },
                            enabled = !isSubmitting,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("btn_login_submit"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
                        ) {
                            if (isSubmitting) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Text("যাচাই করা হচ্ছে...", color = Color.White)
                            } else {
                                Icon(Icons.Default.Login, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("লগইন করুন", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("নতুন রাইডার হিসেবে যোগ দিতে চান? ", color = TextSecondary, fontSize = 13.sp)
                    Text(
                        text = "রেজিস্ট্রেশন করুন",
                        color = OrangeSecondary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        modifier = Modifier
                            .clickable { viewModel.setAuthMode(AuthMode.REGISTER) }
                            .padding(4.dp)
                    )
                }

            } else {
                // MODE 2: REGISTRATION FORM (Real details, strictly no demo)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkCard)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Text(
                            text = "রাইডার রেজিস্ট্রেশন আবেদন",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "আপনার আসল তথ্য দিয়ে ফর্ম পূরণ করুন (এডমিন প্যানেলে জমা হবে)",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        // Full Name
                        OutlinedTextField(
                            value = regForm.fullName,
                            onValueChange = { viewModel.updateFormField(fullName = it) },
                            label = { Text("পূর্ণ নাম (Full Name)*") },
                            placeholder = { Text("আপনার আসল নাম লিখুন") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = OrangePrimary) },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("reg_name_input"),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = OrangePrimary,
                                unfocusedBorderColor = Color.DarkGray,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Mobile Number
                        OutlinedTextField(
                            value = regForm.phoneNumber,
                            onValueChange = { viewModel.updateFormField(phoneNumber = it) },
                            label = { Text("মোবাইল নম্বর (Phone Number)*") },
                            placeholder = { Text("+39 351 ...") },
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = OrangePrimary) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("reg_phone_input"),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = OrangePrimary,
                                unfocusedBorderColor = Color.DarkGray,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Email
                        OutlinedTextField(
                            value = regForm.email,
                            onValueChange = { viewModel.updateFormField(email = it) },
                            label = { Text("ইমেইল ঠিকানা (Email)*") },
                            placeholder = { Text("rider@example.com") },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = OrangePrimary) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("reg_email_input"),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = OrangePrimary,
                                unfocusedBorderColor = Color.DarkGray,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Password (Firebase Auth requires min 6 chars, never stored in Firestore)
                        OutlinedTextField(
                            value = regForm.password,
                            onValueChange = { viewModel.updateFormField(password = it) },
                            label = { Text("লগইন পাসওয়ার্ড (কমপক্ষে ৬ অক্ষর)*") },
                            placeholder = { Text("কমপক্ষে ৬ অক্ষর বা সংখ্যা") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = OrangePrimary) },
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("reg_password_input"),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = OrangePrimary,
                                unfocusedBorderColor = Color.DarkGray,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Vehicle Type Selection
                        ExposedDropdownMenuBox(
                            expanded = vehicleExpanded,
                            onExpandedChange = { vehicleExpanded = it }
                        ) {
                            OutlinedTextField(
                                value = regForm.vehicleType,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("যানবাহনের ধরন (Vehicle Type)") },
                                leadingIcon = { Icon(Icons.Default.TwoWheeler, contentDescription = null, tint = OrangePrimary) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = vehicleExpanded) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor()
                                    .testTag("reg_vehicle_dropdown"),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = OrangePrimary,
                                    unfocusedBorderColor = Color.DarkGray,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                )
                            )
                            ExposedDropdownMenu(
                                expanded = vehicleExpanded,
                                onDismissRequest = { vehicleExpanded = false },
                                modifier = Modifier.background(DarkSurface)
                            ) {
                                vehicleOptions.forEach { (typeKey, labelBn, icon) ->
                                    DropdownMenuItem(
                                        text = {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(icon, contentDescription = null, tint = OrangePrimary, modifier = Modifier.size(18.dp))
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text("$labelBn ($typeKey)", color = Color.White)
                                            }
                                        },
                                        onClick = {
                                            viewModel.updateFormField(vehicleType = typeKey)
                                            vehicleExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Vehicle Plate Number
                        OutlinedTextField(
                            value = regForm.vehicleNumber,
                            onValueChange = { viewModel.updateFormField(vehicleNumber = it) },
                            label = { Text("গাড়ির প্লেট নম্বর (Plate Number)*") },
                            placeholder = { Text("যেমন: LI 482 AA") },
                            leadingIcon = { Icon(Icons.Default.Pin, contentDescription = null, tint = OrangePrimary) },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("reg_plate_input"),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = OrangePrimary,
                                unfocusedBorderColor = Color.DarkGray,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Driving License Number
                        OutlinedTextField(
                            value = regForm.licenseNumber,
                            onValueChange = { viewModel.updateFormField(licenseNumber = it) },
                            label = { Text("ড্রাইভিং লাইসেন্স নম্বর (License No)") },
                            placeholder = { Text("Patente / License No") },
                            leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null, tint = OrangePrimary) },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("reg_license_input"),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = OrangePrimary,
                                unfocusedBorderColor = Color.DarkGray,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // City / Zone Selection (Strictly Italy Zones)
                        ExposedDropdownMenuBox(
                            expanded = cityExpanded,
                            onExpandedChange = { cityExpanded = it }
                        ) {
                            OutlinedTextField(
                                value = regForm.city,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("কাজের এরিয়া ও জোন (Work Area)") },
                                leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = OrangePrimary) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = cityExpanded) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor()
                                    .testTag("reg_city_dropdown"),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = OrangePrimary,
                                    unfocusedBorderColor = Color.DarkGray,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                )
                            )
                            ExposedDropdownMenu(
                                expanded = cityExpanded,
                                onDismissRequest = { cityExpanded = false },
                                modifier = Modifier.background(DarkSurface)
                            ) {
                                cityOptions.forEach { city ->
                                    DropdownMenuItem(
                                        text = { Text(city, color = Color.White) },
                                        onClick = {
                                            viewModel.updateFormField(city = city)
                                            cityExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Emergency Contact
                        OutlinedTextField(
                            value = regForm.emergencyContact,
                            onValueChange = { viewModel.updateFormField(emergencyContact = it) },
                            label = { Text("জরুরি যোগাযোগ নম্বর (Emergency Contact)") },
                            placeholder = { Text("জরুরি যোগাযোগের ফোন নম্বর") },
                            leadingIcon = { Icon(Icons.Default.ContactPhone, contentDescription = null, tint = OrangePrimary) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("reg_emergency_input"),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = OrangePrimary,
                                unfocusedBorderColor = Color.DarkGray,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Submit Button
                        Button(
                            onClick = { viewModel.submitRegistration() },
                            enabled = !isSubmitting,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("btn_registration_submit"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
                        ) {
                            if (isSubmitting) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Text("অ্যাকাউন্ট তৈরি হচ্ছে...", color = Color.White)
                            } else {
                                Icon(Icons.Default.ArrowForward, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("রেজিস্ট্রেশন ও ভেরিফিকেশন শুরু করুন", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("ইতিমধ্যে অ্যাকাউন্ট আছে? ", color = TextSecondary, fontSize = 13.sp)
                    Text(
                        text = "লগইন করুন",
                        color = OrangeSecondary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        modifier = Modifier
                            .clickable { viewModel.setAuthMode(AuthMode.LOGIN) }
                            .padding(4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // 3. Forgot Password Dialog
    if (showForgotPasswordDialog) {
        AlertDialog(
            onDismissRequest = { showForgotPasswordDialog = false },
            title = {
                Text(
                    text = "পাসওয়ার্ড রিসেট (Reset Password)",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column {
                    Text(
                        text = "আপনার রেজিস্টার্ড ইমেইল ঠিকানা দিন। একটি পাসওয়ার্ড রিসেট করার লিংক পাঠানো হবে।",
                        color = TextSecondary,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    OutlinedTextField(
                        value = forgotPasswordEmail,
                        onValueChange = { forgotPasswordEmail = it },
                        label = { Text("ইমেইল ঠিকানা (Email)") },
                        placeholder = { Text("rider@example.com") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = OrangePrimary) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("forgot_password_email_input"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = OrangePrimary,
                            unfocusedBorderColor = Color.DarkGray,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.sendPasswordReset(forgotPasswordEmail)
                        showForgotPasswordDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("btn_confirm_send_reset")
                ) {
                    Text("রিসেট লিংক পাঠান", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showForgotPasswordDialog = false },
                    modifier = Modifier.testTag("btn_cancel_forgot_password")
                ) {
                    Text("বাতিল", color = Color.LightGray)
                }
            },
            containerColor = DarkSurface,
            shape = RoundedCornerShape(16.dp)
        )
    }
}
