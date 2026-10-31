package com.example.data.repository

import com.example.data.local.dao.RiderDao
import com.example.data.local.entity.EarningEntity
import com.example.data.local.entity.OrderEntity
import com.example.data.local.entity.QuestEntity
import com.example.data.local.entity.RiderProfileEntity
import com.example.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

class RiderRepository(private val dao: RiderDao) {

    val allOrders: Flow<List<OrderEntity>> = dao.getAllOrders()
    val activeOrders: Flow<List<OrderEntity>> = dao.getActiveOrders()
    val pendingPingOrder: Flow<OrderEntity?> = dao.getPendingPingOrder()
    val allEarnings: Flow<List<EarningEntity>> = dao.getAllEarnings()
    val allTransactions: Flow<List<TransactionEntity>> = dao.getAllTransactions()
    val allQuests: Flow<List<QuestEntity>> = dao.getAllQuests()
    val riderProfile: Flow<RiderProfileEntity?> = dao.getRiderProfile()

    fun getOrderById(id: Long): Flow<OrderEntity?> = dao.getOrderById(id)

    suspend fun insertOrder(order: OrderEntity): Long = dao.insertOrder(order)
    suspend fun updateOrder(order: OrderEntity) = dao.updateOrder(order)
    suspend fun updateOrderStatus(id: Long, status: String) = dao.updateOrderStatus(id, status)
    suspend fun deleteOrder(id: Long) = dao.deleteOrder(id)

    suspend fun addEarning(earning: EarningEntity): Long = dao.insertEarning(earning)
    suspend fun addTransaction(transaction: TransactionEntity): Long = dao.insertTransaction(transaction)

    suspend fun updateQuest(quest: QuestEntity) = dao.updateQuest(quest)

    suspend fun updateProfile(profile: RiderProfileEntity) = dao.insertOrUpdateProfile(profile)
    suspend fun setOnlineStatus(isOnline: Boolean) = dao.setOnlineStatus(isOnline)
    suspend fun updateWalletBalance(balance: Double) = dao.updateWalletBalance(balance)
    suspend fun updateFloatingCash(floatingCash: Double) = dao.updateFloatingCash(floatingCash)
    suspend fun updateLanguage(lang: String) = dao.updateLanguage(lang)
    suspend fun updateZone(zone: String) = dao.updateZone(zone)
    suspend fun updateAutoAccept(autoAccept: Boolean) = dao.updateAutoAccept(autoAccept)
    suspend fun updateVehicleType(vehicleType: String) = dao.updateVehicleType(vehicleType)
    suspend fun updateAvailabilityStatus(status: String) = dao.updateAvailabilityStatus(status)
    suspend fun updateApprovalStatus(status: String) = dao.updateApprovalStatus(status)
    suspend fun clearAllOrders() = dao.clearAllOrders()
}
