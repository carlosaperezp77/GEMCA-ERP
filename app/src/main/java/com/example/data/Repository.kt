package com.example.data

import kotlinx.coroutines.flow.Flow

class FuelRepository(private val db: AppDatabase) {

    suspend fun getUserByEmail(email: String): User? = db.userDao.getUserByEmail(email)

    suspend fun insertUser(user: User) = db.userDao.insertUser(user)

    val allInventoryItems: Flow<List<InventoryItem>> = db.inventoryItemDao.getAllItems()

    suspend fun insertInventoryItem(item: InventoryItem) = db.inventoryItemDao.insertItem(item)

    suspend fun deleteInventoryItem(sku: String) = db.inventoryItemDao.deleteItem(sku)

    val allWorkOrders: Flow<List<WorkOrder>> = db.workOrderDao.getAllOrders()

    suspend fun insertWorkOrder(order: WorkOrder) = db.workOrderDao.insertOrder(order)

    suspend fun getWorkOrderById(otId: String): WorkOrder? = db.workOrderDao.getOrderById(otId)

    suspend fun deleteWorkOrder(otId: String) = db.workOrderDao.deleteOrder(otId)

    val allDispatchLogs: Flow<List<DispatchLog>> = db.dispatchLogDao.getAllDispatchLogs()

    suspend fun insertDispatchLog(log: DispatchLog) = db.dispatchLogDao.insertLog(log)

    val allNotifications: Flow<List<SystemNotification>> = db.systemNotificationDao.getAllNotifications()

    suspend fun insertNotification(notification: SystemNotification) = db.systemNotificationDao.insertNotification(notification)

    suspend fun markNotificationAsRead(id: Long) = db.systemNotificationDao.markAsRead(id)

    val allFuelReceipts: Flow<List<FuelReceipt>> = db.fuelReceiptDao.getAllReceipts()

    suspend fun insertFuelReceipt(receipt: FuelReceipt) = db.fuelReceiptDao.insertReceipt(receipt)
}
