package com.example.ui.components

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.OnlineGreen
import com.example.ui.theme.RiderPrimary

data class ChatMessage(
    val sender: String, // "RIDER", "RESTAURANT", or "CUSTOMER"
    val text: String,
    val time: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatBottomSheet(
    restaurantName: String = "Sultan's Dine",
    restaurantPhone: String = "+880 1911-223344",
    customerName: String = "Sadia Ahmed",
    customerPhone: String = "+880 1819-998877",
    initialRecipient: String = "Customer",
    onSendMessage: (recipientRole: String, text: String) -> Unit = { _, _ -> },
    onDismiss: () -> Unit,
    isBn: Boolean
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedChannel by remember {
        mutableStateOf(if (initialRecipient.contains("Rest", ignoreCase = true)) "RESTAURANT" else "CUSTOMER")
    }
    var inputText by remember { mutableStateOf("") }

    // Separate conversation threads for Restaurant and Customer apps
    val restaurantMessages = remember {
        mutableStateListOf(
            ChatMessage(
                sender = "RESTAURANT",
                text = if (isBn) "আসসালামু আলাইকুম, কাচ্চি ও বোরহানি প্যাক করা হচ্ছে। ২ মিনিটে রেডি হয়ে যাবে।" else "Hello, your order is being packed. Ready in 2 minutes.",
                time = "10:12 AM"
            ),
            ChatMessage(
                sender = "RIDER",
                text = if (isBn) "আমি রেস্তোরাঁর গেটে আছি। পিকআপ পিনটি ভেরিফাই করার জন্য রেডি রাখুন।" else "I am outside the restaurant. Ready with pickup PIN.",
                time = "10:14 AM"
            )
        )
    }

    val customerMessages = remember {
        mutableStateListOf(
            ChatMessage(
                sender = "CUSTOMER",
                text = if (isBn) "ভাইয়া, খাবার কি সংগ্রহ করা হয়েছে?" else "Brother, have you picked up the food?",
                time = "10:15 AM"
            ),
            ChatMessage(
                sender = "RIDER",
                text = if (isBn) "হ্যাঁ, খাবার নিয়ে রওনা হয়েছি। লাইভ জিপিএস লোকেশন শেয়ার হচ্ছে, ১০ মিনিটে পৌঁছে যাব।" else "Yes, food collected and heading your way. Live GPS is active, arriving in 10 mins.",
                time = "10:16 AM"
            )
        )
    }

    val restaurantTemplates = if (isBn) listOf(
        "আমি রেস্তোরাঁর সামনে পৌঁছেছি।",
        "অর্ডার প্যাক করা কি প্রস্তুত?",
        "পিকআপ পিন কোডটি ভেরিফাই করুন।",
        "খাবার রেডি হলে স্টাফকে বলুন।"
    ) else listOf(
        "I have arrived at the restaurant.",
        "Is the order packed and ready?",
        "Please verify the pickup PIN.",
        "Please notify staff food is ready."
    )

    val customerTemplates = if (isBn) listOf(
        "আমি আপনার বাসার গেটে পৌঁছেছি।",
        "দয়া করে ক্যাশ টাকা রেডি রাখুন।",
        "৪ ডিজিট ডেলিভারি পিনটি প্রস্তুত রাখুন।",
        "আমি রওনা হয়েছি, ৫ মিনিটে আসছি।"
    ) else listOf(
        "I have arrived outside your gate.",
        "Please keep COD cash ready.",
        "Please have your 4-digit PIN ready.",
        "On the way, arriving in 5 mins."
    )

    val isRestaurant = selectedChannel == "RESTAURANT"
    val currentRecipientName = if (isRestaurant) restaurantName else customerName
    val currentPhone = if (isRestaurant) restaurantPhone else customerPhone
    val currentMessages = if (isRestaurant) restaurantMessages else customerMessages
    val currentTemplates = if (isRestaurant) restaurantTemplates else customerTemplates

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 28.dp)
        ) {
            // Header with Direct Call & Close
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (isBn) "লাইভ দ্বি-পাক্ষিক কমিনিউকেশন" else "Live Communication Hub",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 17.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (isBn) "রেস্তোরাঁ ও কাস্টমার অ্যাপের সাথে সিঙ্কড" else "Connected with Restaurant & Customer Apps",
                        fontSize = 11.sp,
                        color = OnlineGreen,
                        fontWeight = FontWeight.Medium
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Call button
                    IconButton(
                        onClick = {
                            try {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$currentPhone"))
                                context.startActivity(intent)
                            } catch (_: Exception) {
                                Toast.makeText(context, "Dial: $currentPhone", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier
                            .background(OnlineGreen.copy(alpha = 0.15f), CircleShape)
                            .size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = "Call $currentRecipientName",
                            tint = OnlineGreen,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Channel Switcher Tabs: [ 🍽️ রেস্তোরাঁ অ্যাপ ] | [ 👤 কাস্টমার অ্যাপ ]
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
            ) {
                Row(
                    modifier = Modifier.padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Restaurant Tab
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { selectedChannel = "RESTAURANT" }
                            .testTag("chat_restaurant_tab"),
                        shape = RoundedCornerShape(10.dp),
                        color = if (isRestaurant) Color(0xFFF59E0B) else Color.Transparent
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Storefront,
                                contentDescription = null,
                                tint = if (isRestaurant) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isBn) "রেস্তোরাঁ অ্যাপ" else "Restaurant App",
                                fontSize = 12.sp,
                                fontWeight = if (isRestaurant) FontWeight.Bold else FontWeight.Medium,
                                color = if (isRestaurant) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Customer Tab
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { selectedChannel = "CUSTOMER" }
                            .testTag("chat_customer_tab"),
                        shape = RoundedCornerShape(10.dp),
                        color = if (!isRestaurant) RiderPrimary else Color.Transparent
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = if (!isRestaurant) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isBn) "কাস্টমার অ্যাপ" else "Customer App",
                                fontSize = 12.sp,
                                fontWeight = if (!isRestaurant) FontWeight.Bold else FontWeight.Medium,
                                color = if (!isRestaurant) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Connected Recipient Info Pill
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = (if (isRestaurant) Color(0xFFF59E0B) else RiderPrimary).copy(alpha = 0.10f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "গন্তব্য: $currentRecipientName",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isRestaurant) Color(0xFFD97706) else RiderPrimary,
                        maxLines = 1
                    )
                    Text(
                        text = "📞 $currentPhone",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Chat Messages List
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(190.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(currentMessages) { msg ->
                    val isMe = msg.sender == "RIDER"
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
                    ) {
                        Surface(
                            shape = RoundedCornerShape(
                                topStart = 14.dp,
                                topEnd = 14.dp,
                                bottomStart = if (isMe) 14.dp else 2.dp,
                                bottomEnd = if (isMe) 2.dp else 14.dp
                            ),
                            color = when {
                                isMe -> RiderPrimary
                                isRestaurant -> Color(0xFFFEF3C7)
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            }
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                                Text(
                                    text = msg.text,
                                    color = when {
                                        isMe -> Color.White
                                        isRestaurant -> Color(0xFF92400E)
                                        else -> MaterialTheme.colorScheme.onSurface
                                    },
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = msg.time,
                                    color = when {
                                        isMe -> Color.White.copy(alpha = 0.7f)
                                        isRestaurant -> Color(0xFFB45309)
                                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                                    },
                                    fontSize = 9.sp,
                                    modifier = Modifier.align(Alignment.End)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Quick Canned Replies
            Text(
                text = if (isBn) "দ্রুত মেসেজ পাঠান:" else "Quick replies:",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                currentTemplates.take(2).forEach { quickText ->
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                currentMessages.add(ChatMessage("RIDER", quickText, "Just now"))
                                onSendMessage(selectedChannel, quickText)
                            },
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = quickText,
                            fontSize = 10.sp,
                            maxLines = 1,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Text Input & Send
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = {
                        Text(
                            if (isRestaurant) {
                                if (isBn) "রেস্তোরাঁকে মেসেজ লিখুন..." else "Message restaurant..."
                            } else {
                                if (isBn) "কাস্টমারকে মেসেজ লিখুন..." else "Message customer..."
                            },
                            fontSize = 12.sp
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("chat_input_field"),
                    shape = RoundedCornerShape(20.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = {
                        if (inputText.isNotBlank()) {
                            val textToSend = inputText.trim()
                            currentMessages.add(ChatMessage("RIDER", textToSend, "Just now"))
                            onSendMessage(selectedChannel, textToSend)
                            inputText = ""
                        }
                    },
                    modifier = Modifier
                        .background(if (isRestaurant) Color(0xFFF59E0B) else RiderPrimary, CircleShape)
                        .size(44.dp)
                        .testTag("chat_send_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
