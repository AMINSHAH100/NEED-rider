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
    version = 1,
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
            // Initial Profile
            dao.insertOrUpdateProfile(
                RiderProfileEntity(
                    id = 1,
                    name = "তানভীর আহমেদ (Tanvir Ahmed)",
                    phone = "+880 1712-345678",
                    riderCode = "#RD-9942",
                    vehicleType = "MOTORCYCLE",
                    vehiclePlate = "Dhaka Metro-HA 55-8912",
                    rating = 4.92,
                    totalDeliveries = 384,
                    acceptanceRate = 98,
                    completionRate = 99,
                    onTimeRate = 96,
                    walletBalance = 1450.0,
                    floatingCashLimit = 3500.0,
                    currentFloatingCash = 840.0,
                    isOnline = true,
                    currentZone = "Gulshan & Banani Hub",
                    language = "bn",
                    autoAccept = false
                )
            )

            // Active order ready to be delivered or navigated
            val sampleOrder1 = OrderEntity(
                orderNumber = "#FD-7829",
                restaurantName = "Sultan's Dine - সুলতান'স ডাইন",
                restaurantAddress = "House 43, Road 11, Block D, Banani",
                restaurantPhone = "+880 1911-223344",
                restaurantLat = 23.7937,
                restaurantLng = 90.4066,
                customerName = "Sadia Ahmed (সাদিয়া আহমেদ)",
                customerAddress = "Flat 4B, House 18, Road 7, Gulshan-1",
                customerPhone = "+880 1819-998877",
                customerLat = 23.7785,
                customerLng = 90.4162,
                customerNote = "গেট লক থাকলে সিকিউরিটি গার্ডকে কল দিন। লিফট দিয়ে ৪ তলায় আসুন।",
                itemsSummary = "Kacchi Biryani (Full) x2, Borhani (500ml) x1, Firni x2",
                itemsJson = """[{"name":"Kacchi Biryani (Full)","quantity":2,"price":480.0,"instructions":"Extra salad please","isChecked":true},{"name":"Borhani (500ml)","quantity":1,"price":120.0,"instructions":"","isChecked":true},{"name":"Firni","quantity":2,"price":80.0,"instructions":"","isChecked":true}]""",
                totalBill = 1240.0,
                deliveryFee = 95.0,
                tip = 30.0,
                surgeBonus = 25.0,
                paymentMethod = "COD",
                status = "ACCEPTED",
                distanceKm = 3.2,
                estimatedMinutes = 18,
                customerOtp = "6824",
                pickupOtp = "7829",
                cashReceived = 0.0,
                changeToReturn = 0.0,
                isItemsChecked = false
            )

            val sampleOrder2 = OrderEntity(
                orderNumber = "#FD-7815",
                restaurantName = "Kacchi Bhai - কাচ্চি ভাই",
                restaurantAddress = "Navana Tower, Gulshan-2, Dhaka",
                restaurantPhone = "+880 1711-334455",
                customerName = "Rafiqul Islam (রফিকুল ইসলাম)",
                customerAddress = "House 12, Road 103, Gulshan-2",
                customerPhone = "+880 1612-445566",
                customerNote = "খাবার গরম থাকলে ভালো হয়।",
                itemsSummary = "Basmati Kacchi Combo x1, Jali Kabab x2",
                totalBill = 680.0,
                deliveryFee = 85.0,
                tip = 20.0,
                surgeBonus = 15.0,
                paymentMethod = "PREPAID",
                status = "DELIVERED",
                distanceKm = 2.1,
                estimatedMinutes = 15,
                customerOtp = "3190",
                pickupOtp = "7815",
                timestamp = System.currentTimeMillis() - 45 * 60 * 1000
            )

            val sampleOrder3 = OrderEntity(
                orderNumber = "#FD-7802",
                restaurantName = "Pizza Hut - পিজ্জা হাট",
                restaurantAddress = "Kemal Ataturk Avenue, Banani",
                restaurantPhone = "+880 1812-778899",
                customerName = "Tanvir Hasan (তানভীর হাসান)",
                customerAddress = "Road 27, Block K, Banani",
                customerPhone = "+880 1515-667788",
                customerNote = "ডোরবেল বাজাবেন না, কল করবেন।",
                itemsSummary = "12\" Beef Pepperoni Lovers x1, Garlic Bread x1",
                totalBill = 1150.0,
                deliveryFee = 110.0,
                tip = 50.0,
                surgeBonus = 0.0,
                paymentMethod = "COD",
                status = "DELIVERED",
                distanceKm = 2.8,
                estimatedMinutes = 16,
                customerOtp = "9941",
                pickupOtp = "7802",
                timestamp = System.currentTimeMillis() - 110 * 60 * 1000
            )

            dao.insertOrders(listOf(sampleOrder1, sampleOrder2, sampleOrder3))

            // Initial Earnings
            val earning1 = EarningEntity(
                orderId = 2,
                orderNumber = "#FD-7815",
                baseFee = 60.0,
                distanceFee = 25.0,
                surgeFee = 15.0,
                tip = 20.0,
                questBonus = 0.0,
                totalEarned = 120.0,
                paymentMethod = "PREPAID",
                timestamp = System.currentTimeMillis() - 45 * 60 * 1000
            )

            val earning2 = EarningEntity(
                orderId = 3,
                orderNumber = "#FD-7802",
                baseFee = 70.0,
                distanceFee = 40.0,
                surgeFee = 0.0,
                tip = 50.0,
                questBonus = 0.0,
                totalEarned = 160.0,
                paymentMethod = "COD",
                timestamp = System.currentTimeMillis() - 110 * 60 * 1000
            )

            dao.insertEarnings(listOf(earning1, earning2))

            // Initial Transactions
            val trans1 = TransactionEntity(
                title = "Cashout to bKash",
                titleBn = "বিকাশে ক্যাশআউট",
                type = "CASHOUT",
                amount = 1200.0,
                method = "bKash (01712-***678)",
                referenceNumber = "TRX-99382109",
                status = "COMPLETED",
                timestamp = System.currentTimeMillis() - 24 * 3600 * 1000
            )

            val trans2 = TransactionEntity(
                title = "Floating Cash Remittance",
                titleBn = "ফ্লোটিং ক্যাশ জমা প্রদান",
                type = "COD_DEPOSIT",
                amount = 2500.0,
                method = "Nagad Platform Gateway",
                referenceNumber = "DEP-4820194",
                status = "COMPLETED",
                timestamp = System.currentTimeMillis() - 48 * 3600 * 1000
            )

            dao.insertTransactions(listOf(trans1, trans2))

            // Initial Quests
            val questList = listOf(
                QuestEntity(
                    title = "Lunch Rush Champion",
                    titleBn = "লাঞ্চ রাশ চ্যাম্পিয়ন",
                    description = "Complete 5 deliveries between 12:00 PM and 3:30 PM",
                    descriptionBn = "দুপুর ১২টা থেকে ৩:৩০ এর মধ্যে ৫টি ডেলিভারি সম্পন্ন করুন",
                    targetCount = 5,
                    currentCount = 3,
                    rewardAmount = 180.0,
                    isCompleted = false,
                    isClaimed = false,
                    badgeIcon = "ROCKET",
                    expiresAtText = "Today 3:30 PM"
                ),
                QuestEntity(
                    title = "Rain Surge Hero",
                    titleBn = "বৃষ্টির দিনের বিশেষ বোনাস",
                    description = "Deliver 3 orders in Monsoon surge hours",
                    descriptionBn = "সার্জ সময়ে ৩টি অর্ডার সফলভাবে ডেলিভারি করুন",
                    targetCount = 3,
                    currentCount = 3,
                    rewardAmount = 120.0,
                    isCompleted = true,
                    isClaimed = false,
                    badgeIcon = "LIGHTNING",
                    expiresAtText = "Today 11:59 PM"
                ),
                QuestEntity(
                    title = "Weekend Superstar",
                    titleBn = "উইকেন্ড সুপারস্টার",
                    description = "Deliver 20 orders this weekend to unlock Gold bonus",
                    descriptionBn = "এই সপ্তাহে ২০টি অর্ডার সম্পন্ন করে গোল্ড বোনাস আনলক করুন",
                    targetCount = 20,
                    currentCount = 14,
                    rewardAmount = 500.0,
                    isCompleted = false,
                    isClaimed = false,
                    badgeIcon = "STAR",
                    expiresAtText = "Sunday 11:59 PM"
                )
            )

            dao.insertQuests(questList)
        }
    }
}
