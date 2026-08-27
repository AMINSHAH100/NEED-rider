package com.example.ui.components

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.material.icons.filled.PhoneMissed
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.SoupKitchen
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.OfflineRed
import com.example.ui.theme.RiderPrimary

data class IssueOption(
    val id: Int,
    val icon: ImageVector,
    val titleBn: String,
    val titleEn: String,
    val descBn: String,
    val descEn: String
)

@Composable
fun IssueReportDialog(
    orderNumber: String,
    onSubmit: (issueTitle: String, note: String) -> Unit,
    onDismiss: () -> Unit,
    isBn: Boolean
) {
    val issues = listOf(
        IssueOption(
            1, Icons.Default.HourglassBottom,
            "রেস্তোরাঁয় খাবার প্রস্তুতিতে অনেক দেরি হচ্ছে", "Restaurant food prep long delay",
            "১৫ মিনিটের বেশি অপেক্ষা করতে হচ্ছে", "Waiting over 15 minutes"
        ),
        IssueOption(
            2, Icons.Default.PhoneMissed,
            "কাস্টমার ফোন ধরছে না বা বন্ধ", "Customer phone unreachable",
            "ঠিকানায় এসে একাধিকবার কল দেওয়া হয়েছে", "Called multiple times at location"
        ),
        IssueOption(
            3, Icons.Default.LocationOff,
            "ভুল ঠিকানা / পিন পয়েন্ট ভুল", "Wrong delivery address / map pin",
            "কাস্টমার অন্য কোনো লোকেশনে যেতে বলছে", "Customer asking to go to different area"
        ),
        IssueOption(
            4, Icons.Default.DirectionsBike,
            "বাইক নষ্ট / দুর্ঘটনা / টায়ার পাংচার", "Bike breakdown / Flat tire",
            "জরুরিভাবে অন্য রাইডারকে অর্ডার রি-অ্যাসাইন করুন", "Emergency re-assignment needed"
        ),
        IssueOption(
            5, Icons.Default.SoupKitchen,
            "খাবার প্যাকেজিং নষ্ট বা পড়ে গেছে", "Spilled food or damaged package",
            "রেস্তোরাঁর দুর্বল প্যাকেজিং বা ঝাঁকুনিতে ক্ষতি", "Package leaked or damaged"
        )
    )

    var selectedIssueId by remember { mutableIntStateOf(1) }
    var extraNote by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
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
                        Surface(
                            shape = CircleShape,
                            color = OfflineRed.copy(alpha = 0.15f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ReportProblem,
                                contentDescription = null,
                                tint = OfflineRed,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = if (isBn) "সমস্যা রিপোর্ট করুন" else "Report Delivery Issue",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "Order $orderNumber",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Issue Options List
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    issues.forEach { issue ->
                        val isSelected = selectedIssueId == issue.id
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { selectedIssueId = issue.id },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) OfflineRed.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, OfflineRed) else null
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = issue.icon,
                                    contentDescription = null,
                                    tint = if (isSelected) OfflineRed else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (isBn) issue.titleBn else issue.titleEn,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = if (isBn) issue.descBn else issue.descEn,
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { selectedIssueId = issue.id },
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = extraNote,
                    onValueChange = { extraNote = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            text = if (isBn) "অতিরিক্ত কোনো বিবরণ থাকলে লিখুন..." else "Add extra note for support team...",
                            fontSize = 12.sp
                        )
                    },
                    shape = RoundedCornerShape(12.dp),
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = {
                        val selected = issues.firstOrNull { it.id == selectedIssueId }
                        val title = if (isBn) selected?.titleBn ?: "Issue" else selected?.titleEn ?: "Issue"
                        onSubmit(title, extraNote)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("submit_issue_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = OfflineRed)
                ) {
                    Text(
                        text = if (isBn) "সাপোর্ট সেন্টারে পাঠান" else "Submit to Rider Support",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}
