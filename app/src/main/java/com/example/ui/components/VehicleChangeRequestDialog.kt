package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ElectricBike
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PedalBike
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.RiderPrimary

@Composable
fun VehicleChangeRequestDialog(
    currentVehicleType: String,
    currentPlate: String,
    onDismiss: () -> Unit,
    onSubmitRequest: (newType: String, newPlate: String, reason: String) -> Unit,
    lang: String = "bn"
) {
    val isBn = lang == "bn"
    val isIt = lang == "it"

    var selectedVehicle by remember {
        mutableStateOf(
            if (currentVehicleType == "MOTORCYCLE") "BICYCLE" else "MOTORCYCLE"
        )
    }
    var newPlateNumber by remember { mutableStateOf("") }
    var changeReason by remember { mutableStateOf("") }
    var showConfirmation by remember { mutableStateOf(false) }

    val vehicleOptions = listOf(
        Triple("MOTORCYCLE", if (isBn) "মোটরবাইক" else if (isIt) "Moto / Scooter" else "Motorcycle", Icons.Default.TwoWheeler),
        Triple("BICYCLE", if (isBn) "বাইসাইকেল" else if (isIt) "Bicicletta" else "Bicycle", Icons.Default.PedalBike),
        Triple("SCOOTER", if (isBn) "ই-স্কুটার" else if (isIt) "Monopattino" else "E-Scooter", Icons.Default.ElectricBike)
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
                .testTag("vehicle_change_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(RiderPrimary.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.VerifiedUser,
                                contentDescription = null,
                                tint = RiderPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (isBn) "যানবাহন পরিবর্তন আবেদন" else if (isIt) "Richiesta Modifica Veicolo" else "Vehicle Change Request",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isBn) "এডমিন / হেল্পলাইন ভেরিফিকেশন" else if (isIt) "Verifica Admin / Supporto" else "Admin Desk Approval",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Security Policy Note
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFFEF3C7).copy(alpha = 0.25f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = Color(0xFFF59E0B),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isBn) {
                                "রেজিস্ট্রেশনের সময় সিলেক্ট করা যানবাহন সিস্টেম দ্বারা লক থাকে। নিরাপত্তার স্বার্থে সরাসরি পরিবর্তন করা যায় না। নতুন যানবাহনের তথ্য দিলে প্রধান এডমিন যাচাই করে অনুমোদন করবেন।"
                            } else if (isIt) {
                                "Il veicolo scelto in fase di registrazione è bloccato dal sistema per motivi di sicurezza e assicurazione. L'amministrazione esaminerà e approverà la richiesta."
                            } else {
                                "The vehicle selected during registration is locked by system policy. Submit your new vehicle details below for Main Admin review and activation."
                            },
                            fontSize = 11.sp,
                            lineHeight = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Target New Vehicle Selection
                Text(
                    text = if (isBn) "নতুন কাঙ্ক্ষিত যানবাহন সিলেক্ট করুন:" else if (isIt) "Seleziona nuovo veicolo richiesto:" else "Select New Requested Vehicle:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    vehicleOptions.forEach { (vType, label, icon) ->
                        val isSelected = selectedVehicle == vType
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { selectedVehicle = vType },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) RiderPrimary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
                            border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, RiderPrimary) else null
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = if (isSelected) RiderPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = label,
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) RiderPrimary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Number Plate (if motorcycle/scooter)
                if (selectedVehicle != "BICYCLE") {
                    OutlinedTextField(
                        value = newPlateNumber,
                        onValueChange = { newPlateNumber = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("vehicle_plate_input"),
                        label = {
                            Text(
                                if (isBn) "গাড়ির নম্বর প্লেট / রেজিস্ট্রেশন" else if (isIt) "Targa / Numero Telaio" else "Vehicle Plate / Reg No",
                                fontSize = 11.sp
                            )
                        },
                        placeholder = {
                            Text(
                                if (isBn) "উদাঃ ঢাকা মেট্রো-হ ৫৫-XXXX" else "e.g. Dhaka Metro-HA 55-XXXX",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // Reason for changing
                OutlinedTextField(
                    value = changeReason,
                    onValueChange = { changeReason = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("vehicle_change_reason_input"),
                    label = {
                        Text(
                            if (isBn) "পরিবর্তনের কারণ (এডমিনের জন্য)" else if (isIt) "Motivo del cambio (per Admin)" else "Reason for change (for Admin)",
                            fontSize = 11.sp
                        )
                    },
                    placeholder = {
                        Text(
                            if (isBn) "উদাঃ নতুন বাইক কিনেছি / আগের বাইক সার্ভিসিংয়ে" else if (isIt) "es. Nuovo mezzo acquistato" else "e.g. Bought a new bike / upgrading",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    shape = RoundedCornerShape(10.dp),
                    minLines = 2,
                    maxLines = 3
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Text(if (isBn) "বাতিল" else if (isIt) "Annulla" else "Cancel", fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            onSubmitRequest(
                                selectedVehicle,
                                if (newPlateNumber.isBlank()) "Dhaka Metro-HA 55-9900" else newPlateNumber,
                                if (changeReason.isBlank()) "Standard Vehicle Update Request" else changeReason
                            )
                            onDismiss()
                        },
                        modifier = Modifier
                            .weight(1.3f)
                            .testTag("submit_vehicle_request_btn"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = RiderPrimary)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isBn) "এডমিনকে পাঠান" else if (isIt) "Invia all'Admin" else "Send to Admin",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}
