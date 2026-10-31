package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.example.ui.theme.OfflineRed
import com.example.ui.theme.RiderPrimary

@Composable
fun RejectOrderReasonDialog(
    orderNumber: String,
    onConfirmReject: (String) -> Unit,
    onDismiss: () -> Unit,
    isBn: Boolean
) {
    val predefinedReasons = if (isBn) {
        listOf(
            "ড্রপঅফ লোকেশন অনেক দূরে (Distance too far)",
            "ডেলিভারি ফি / পেমেন্ট কম (Low delivery payout)",
            "যানবাহনের সমস্যা / জ্বালানি স্বল্পতা (Vehicle breakdown / Fuel issue)",
            "তীব্র যানজট অথবা বৈরী আবহাওয়া (Heavy traffic / Bad weather)",
            "শিফট শেষ বা খাবার বিরতি (Taking a break / Duty ending)",
            "ব্যক্তিগত জরুরি কাজ (Personal emergency)"
        )
    } else {
        listOf(
            "Drop-off distance too far",
            "Low delivery fee / payout",
            "Vehicle mechanical or fuel issue",
            "Heavy traffic or adverse weather",
            "Shift ending or meal break",
            "Personal emergency"
        )
    }

    var selectedReason by remember { mutableStateOf(predefinedReasons[0]) }
    var customNote by remember { mutableStateOf("") }
    var isOtherSelected by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = OfflineRed.copy(alpha = 0.15f),
                        modifier = Modifier.size(38.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = OfflineRed,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (isBn) "অর্ডার বাতিলের কারণ নির্বাচন করুন" else "Select Rejection Reason",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Order $orderNumber",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                Spacer(modifier = Modifier.height(10.dp))

                // Reasons radio options
                predefinedReasons.forEach { reason ->
                    val isChecked = selectedReason == reason && !isOtherSelected
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable {
                                selectedReason = reason
                                isOtherSelected = false
                            }
                            .padding(vertical = 7.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isChecked) Icons.Default.RadioButtonChecked else Icons.Default.RadioButtonUnchecked,
                            contentDescription = null,
                            tint = if (isChecked) OfflineRed else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = reason,
                            fontSize = 13.sp,
                            fontWeight = if (isChecked) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (isChecked) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Other option
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { isOtherSelected = true }
                        .padding(vertical = 7.dp, horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isOtherSelected) Icons.Default.RadioButtonChecked else Icons.Default.RadioButtonUnchecked,
                        contentDescription = null,
                        tint = if (isOtherSelected) OfflineRed else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (isBn) "অন্যান্য কারণ (Other reason)" else "Other reason",
                        fontSize = 13.sp,
                        fontWeight = if (isOtherSelected) FontWeight.SemiBold else FontWeight.Normal,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                if (isOtherSelected) {
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = customNote,
                        onValueChange = { customNote = it },
                        placeholder = { Text(if (isBn) "কারণটি সংক্ষেপে লিখুন..." else "Enter reason...", fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 2,
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(text = if (isBn) "ফিরে যান" else "Cancel")
                    }

                    Button(
                        onClick = {
                            val finalReason = if (isOtherSelected && customNote.isNotBlank()) customNote.trim() else selectedReason
                            onConfirmReject(finalReason)
                        },
                        modifier = Modifier
                            .weight(1.3f)
                            .testTag("confirm_reject_order_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = OfflineRed)
                    ) {
                        Text(
                            text = if (isBn) "প্রত্যাখ্যান নিশ্চিত" else "Confirm Decline",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}
