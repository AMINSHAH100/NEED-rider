package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "quests")
data class QuestEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val titleBn: String,
    val description: String,
    val descriptionBn: String,
    val targetCount: Int,
    val currentCount: Int,
    val rewardAmount: Double,
    val isCompleted: Boolean = false,
    val isClaimed: Boolean = false,
    val badgeIcon: String = "STAR",
    val expiresAtText: String = "Today 11:59 PM"
)
