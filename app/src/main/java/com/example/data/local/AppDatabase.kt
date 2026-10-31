package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.RiderDao
import com.example.data.local.entity.EarningEntity
import com.example.data.local.entity.OrderEntity
import com.example.data.local.entity.QuestEntity
import com.example.data.local.entity.RiderProfileEntity
import com.example.data.local.entity.TransactionEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        OrderEntity::class,
        EarningEntity::class,
        TransactionEntity::class,
        QuestEntity::class,
        RiderProfileEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun riderDao(): RiderDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "rider_database"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database.riderDao())
                    }
                }
            }
        }

        suspend fun populateInitialData(dao: RiderDao) {
            // Initial Empty Profile with NOT_REGISTERED status
            // Rider must register and get approved by Admin before working
            dao.insertOrUpdateProfile(
                RiderProfileEntity(
                    id = 1,
                    name = "",
                    phone = "",
                    riderCode = "",
                    vehicleType = "MOTORCYCLE",
                    vehiclePlate = "",
                    rating = 5.0,
                    totalDeliveries = 0,
                    acceptanceRate = 100,
                    completionRate = 100,
                    onTimeRate = 100,
                    walletBalance = 0.0,
                    floatingCashLimit = 3500.0,
                    currentFloatingCash = 0.0,
                    isOnline = false,
                    availabilityStatus = "Offline",
                    currentZone = "Collesalvetti / Livorno",
                    language = if (java.util.Locale.getDefault().language.startsWith("it")) "it" else "en",
                    autoAccept = false,
                    approvalStatus = "NOT_REGISTERED"
                )
            )

            // NO DEMO ORDERS OR DEMO EARNINGS ARE INSERTED.
            // App strictly operates with real registered rider and live incoming orders from Firestore.

            // Initial Quest definitions
            val questList = listOf(
                QuestEntity(
                    title = "Lunch Rush Champion",
                    titleBn = "লাঞ্চ রাশ চ্যাম্পিয়ন",
                    description = "Complete 5 deliveries between 12:00 PM and 3:30 PM",
                    descriptionBn = "দুপুর ১২টা থেকে ৩:৩০ এর মধ্যে ৫টি ডেলিভারি সম্পন্ন করুন",
                    targetCount = 5,
                    currentCount = 0,
                    rewardAmount = 180.0,
                    isCompleted = false,
                    isClaimed = false,
                    badgeIcon = "ROCKET",
                    expiresAtText = "Today 3:30 PM"
                ),
                QuestEntity(
                    title = "Rain Surge Hero",
                    titleBn = "সার্জ বোনাস হিরো",
                    description = "Deliver 3 orders in peak surge hours",
                    descriptionBn = "সার্জ সময়ে ৩টি অর্ডার সফলভাবে ডেলিভারি করুন",
                    targetCount = 3,
                    currentCount = 0,
                    rewardAmount = 120.0,
                    isCompleted = false,
                    isClaimed = false,
                    badgeIcon = "LIGHTNING",
                    expiresAtText = "Today 11:59 PM"
                )
            )

            dao.insertQuests(questList)
        }
    }
}
