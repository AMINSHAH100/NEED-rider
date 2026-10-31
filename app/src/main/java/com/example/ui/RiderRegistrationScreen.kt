package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import com.example.data.RiderRegistrationData
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RiderRegistrationScreen(
    viewModel: RiderViewModel
) {
    val form by viewModel.registrationForm.collectAsState()
    val isSubmitting by viewModel.isSubmitting.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    val vehicleOptions = listOf("Motorcycle", "Bicycle", "Scooter", "Car")
    var vehicleDropdownExpanded by remember { mutableStateOf(false) }

    val cityOptions = listOf("Collesalvetti", "Livorno", "Pisa", "Firenze")
    var cityDropdownExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "রাইডার রেজিস্ট্রেশন",
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
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                colors = CardDefaults.cardColors(containerColor = DarkCard),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .background(OrangePrimary, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.TwoWheeler,
                            contentDescription = "Rider Logo",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "Food Rider Italy",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "লাইভ অ্যাডমিন প্যানেল অ্যাপ্রুভাল সিস্টেম",
                            fontSize = 13.sp,
                            color = OrangeSecondary
                        )
                    }
                }
            }

            // Error Banner
            if (errorMessage != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF3E1A1A)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = "Error",
                            tint = ErrorRed
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = errorMessage ?: "",
                            color = Color.White,
                            fontSize = 13.sp,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { viewModel.clearError() }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Color.LightGray
                            )
                        }
                    }
                }
            }

            // Form Fields
            OutlinedTextField(
                value = form.fullName,
                onValueChange = { viewModel.updateFormField(fullName = it) },
                label = { Text("পূর্ণ নাম (Full Name)") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = OrangePrimary) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .testTag("input_full_name"),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = OrangePrimary,
                    unfocusedBorderColor = Color.Gray,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedLabelColor = OrangePrimary
                )
            )

            OutlinedTextField(
                value = form.phoneNumber,
                onValueChange = { viewModel.updateFormField(phoneNumber = it) },
                label = { Text("ফোন নম্বর (Phone Number)") },
                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = OrangePrimary) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .testTag("input_phone_number"),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = OrangePrimary,
                    unfocusedBorderColor = Color.Gray,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedLabelColor = OrangePrimary
                )
            )

            OutlinedTextField(
                value = form.email,
                onValueChange = { viewModel.updateFormField(email = it) },
                label = { Text("ইমেইল (Email)") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = OrangePrimary) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .testTag("input_email"),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = OrangePrimary,
                    unfocusedBorderColor = Color.Gray,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedLabelColor = OrangePrimary
                )
            )

            // Vehicle Type Dropdown
            ExposedDropdownMenuBox(
                expanded = vehicleDropdownExpanded,
                onExpandedChange = { vehicleDropdownExpanded = !vehicleDropdownExpanded },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                OutlinedTextField(
                    value = form.vehicleType,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("যানবাহনের ধরন (Vehicle Type)") },
                    leadingIcon = { Icon(Icons.Default.TwoWheeler, contentDescription = null, tint = OrangePrimary) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = vehicleDropdownExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                        .testTag("dropdown_vehicle_type"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = OrangePrimary,
                        unfocusedBorderColor = Color.Gray,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedLabelColor = OrangePrimary
                    )
                )
                ExposedDropdownMenu(
                    expanded = vehicleDropdownExpanded,
                    onDismissRequest = { vehicleDropdownExpanded = false }
                ) {
                    vehicleOptions.forEach { opt ->
                        DropdownMenuItem(
                            text = { Text(opt) },
                            onClick = {
                                viewModel.updateFormField(vehicleType = opt)
                                vehicleDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = form.vehicleNumber,
                onValueChange = { viewModel.updateFormField(vehicleNumber = it) },
                label = { Text("গাড়ি/লাইসেন্স প্লেট নম্বর (Plate Number)") },
                leadingIcon = { Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = OrangePrimary) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .testTag("input_plate_number"),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = OrangePrimary,
                    unfocusedBorderColor = Color.Gray,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedLabelColor = OrangePrimary
                )
            )

            OutlinedTextField(
                value = form.licenseNumber,
                onValueChange = { viewModel.updateFormField(licenseNumber = it) },
                label = { Text("ড্রাইভিং লাইসেন্স নম্বর (License Number)") },
                leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null, tint = OrangePrimary) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .testTag("input_license_number"),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = OrangePrimary,
                    unfocusedBorderColor = Color.Gray,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedLabelColor = OrangePrimary
                )
            )

            // City Selection
            ExposedDropdownMenuBox(
                expanded = cityDropdownExpanded,
                onExpandedChange = { cityDropdownExpanded = !cityDropdownExpanded },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            ) {
                OutlinedTextField(
                    value = form.city,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("শহর (City - Italy)") },
                    leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = OrangePrimary) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = cityDropdownExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                        .testTag("dropdown_city"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = OrangePrimary,
                        unfocusedBorderColor = Color.Gray,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedLabelColor = OrangePrimary
                    )
                )
                ExposedDropdownMenu(
                    expanded = cityDropdownExpanded,
                    onDismissRequest = { cityDropdownExpanded = false }
                ) {
                    cityOptions.forEach { opt ->
                        DropdownMenuItem(
                            text = { Text(opt) },
                            onClick = {
                                viewModel.updateFormField(city = opt)
                                cityDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            // Submit Button
            Button(
                onClick = { viewModel.submitRegistration() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("submit_registration_button"),
                enabled = !isSubmitting,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = OrangePrimary,
                    disabledContainerColor = OrangePrimary.copy(alpha = 0.5f)
                )
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        "জমা হচ্ছে...",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "রেজিস্ট্রেশন জমা দিন",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}
