package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val titleBn: String,
    val type: String, // "CASHOUT", "COD_DEPOSIT", "TRIP_EARNING", "QUEST_REWARD"
    val amount: Double,
    val method: String, // "bKash", "Nagad", "Bank Account", "Cash Pocket"
    val referenceNumber: String,
    val status: String, // "COMPLETED", "PROCESSING"
    val timestamp: Long = System.currentTimeMillis()
)
