package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.EarningEntity
import com.example.data.local.entity.OrderEntity
import com.example.data.local.entity.QuestEntity
import com.example.data.local.entity.RiderProfileEntity
import com.example.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RiderDao {

    // Orders
    @Query("SELECT * FROM orders ORDER BY id DESC")
    fun getAllOrders(): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE status IN ('ACCEPTED', 'ARRIVED_AT_REST', 'PICKED_UP', 'ARRIVED_AT_CUST') ORDER BY id DESC")
    fun getActiveOrders(): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE status = 'NEW_PING' LIMIT 1")
    fun getPendingPingOrder(): Flow<OrderEntity?>

    @Query("SELECT * FROM orders WHERE id = :id")
    fun getOrderById(id: Long): Flow<OrderEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: OrderEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrders(orders: List<OrderEntity>)

    @Update
    suspend fun updateOrder(order: OrderEntity)

    @Query("UPDATE orders SET status = :status WHERE id = :id")
    suspend fun updateOrderStatus(id: Long, status: String)

    @Query("DELETE FROM orders WHERE id = :id")
    suspend fun deleteOrder(id: Long)

    // Earnings
    @Query("SELECT * FROM earnings ORDER BY timestamp DESC")
    fun getAllEarnings(): Flow<List<EarningEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEarning(earning: EarningEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEarnings(earnings: List<EarningEntity>)

    // Transactions
    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactions(transactions: List<TransactionEntity>)

    // Quests
    @Query("SELECT * FROM quests ORDER BY id ASC")
    fun getAllQuests(): Flow<List<QuestEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuests(quests: List<QuestEntity>)

    @Update
    suspend fun updateQuest(quest: QuestEntity)

    // Profile
    @Query("SELECT * FROM rider_profile WHERE id = 1")
    fun getRiderProfile(): Flow<RiderProfileEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: RiderProfileEntity)

    @Query("UPDATE rider_profile SET isOnline = :isOnline WHERE id = 1")
    suspend fun setOnlineStatus(isOnline: Boolean)

    @Query("UPDATE rider_profile SET walletBalance = :balance WHERE id = 1")
    suspend fun updateWalletBalance(balance: Double)

    @Query("UPDATE rider_profile SET currentFloatingCash = :floatingCash WHERE id = 1")
    suspend fun updateFloatingCash(floatingCash: Double)

    @Query("UPDATE rider_profile SET language = :lang WHERE id = 1")
    suspend fun updateLanguage(lang: String)

    @Query("UPDATE rider_profile SET currentZone = :zone WHERE id = 1")
    suspend fun updateZone(zone: String)

    @Query("UPDATE rider_profile SET autoAccept = :autoAccept WHERE id = 1")
    suspend fun updateAutoAccept(autoAccept: Boolean)

    @Query("UPDATE rider_profile SET vehicleType = :vehicleType WHERE id = 1")
    suspend fun updateVehicleType(vehicleType: String)
}
