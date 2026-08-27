package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.QuestEntity
import com.example.ui.theme.OnlineGreen
import com.example.ui.theme.RiderPrimary
import com.example.ui.theme.RiderTertiary

@Composable
fun QuestsScreen(
    quests: List<QuestEntity>,
    onClaimReward: (QuestEntity) -> Unit,
    isBn: Boolean
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Hero Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A))
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFF59E0B),
                        modifier = Modifier.size(52.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = if (isBn) "ডেইলি কোয়েস্ট ও টার্গেট বোনাস" else "Daily Quests & Target Rewards",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isBn) "অর্ডার টার্গেট পূরণ করে জিতে নিন অতিরিক্ত ক্যাশ বোনাস!" else "Hit daily delivery milestones to unlock extra wallet bonuses!",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        // Quests List
        items(quests) { quest ->
            val progress = (quest.currentCount.toFloat() / quest.targetCount.toFloat()).coerceIn(0f, 1f)
            val isReadyToClaim = quest.isCompleted && !quest.isClaimed

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = if (isReadyToClaim) androidx.compose.foundation.BorderStroke(1.5.dp, OnlineGreen) else null
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = when (quest.badgeIcon) {
                                    "ROCKET" -> Color(0xFFEFF6FF)
                                    "LIGHTNING" -> Color(0xFFFEF3C7)
                                    else -> Color(0xFFFDF2F8)
                                },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = when (quest.badgeIcon) {
                                        "ROCKET" -> Icons.Default.RocketLaunch
                                        "LIGHTNING" -> Icons.Default.ElectricBolt
                                        else -> Icons.Default.Star
                                    },
                                    contentDescription = null,
                                    tint = when (quest.badgeIcon) {
                                        "ROCKET" -> Color(0xFF2563EB)
                                        "LIGHTNING" -> Color(0xFFD97706)
                                        else -> Color(0xFFBE185D)
                                    },
                                    modifier = Modifier.padding(8.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Column {
                                Text(
                                    text = if (isBn) quest.titleBn else quest.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = quest.expiresAtText,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Reward Badge
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (quest.isClaimed) MaterialTheme.colorScheme.surfaceVariant else Color(0xFFD1FAE5)
                        ) {
                            Text(
                                text = "+৳${quest.rewardAmount.toInt()}",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (quest.isClaimed) MaterialTheme.colorScheme.onSurfaceVariant else Color(0xFF047857)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = if (isBn) quest.descriptionBn else quest.description,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Progress bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${quest.currentCount}/${quest.targetCount} ${if (isBn) "অর্ডার" else "Orders"}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${(progress * 100).toInt()}%",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = if (quest.isCompleted) OnlineGreen else RiderPrimary,
                        trackColor = Color(0xFFE2E8F0)
                    )

                    if (isReadyToClaim) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = { onClaimReward(quest) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("claim_quest_${quest.id}"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = OnlineGreen)
                        ) {
                            Icon(imageVector = Icons.Default.Check, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isBn) "৳${quest.rewardAmount.toInt()} বোনাস ওয়ালেটে জমা নিন" else "Claim ৳${quest.rewardAmount.toInt()} Bonus",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else if (quest.isClaimed) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (isBn) "✓ বোনাস সংগৃহীত হয়েছে" else "✓ Bonus Claimed",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = OnlineGreen,
                            modifier = Modifier.align(Alignment.End)
                        )
                    }
                }
            }
        }
    }
}
