package com.example.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): User?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)
}

@Dao
interface InventoryItemDao {
    @Query("SELECT * FROM inventory_items")
    fun getAllItems(): Flow<List<InventoryItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: InventoryItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<InventoryItem>)

    @Query("DELETE FROM inventory_items WHERE sku = :sku")
    suspend fun deleteItem(sku: String)
}

@Dao
interface WorkOrderDao {
    @Query("SELECT * FROM work_orders ORDER BY timestamp DESC")
    fun getAllOrders(): Flow<List<WorkOrder>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: WorkOrder)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(orders: List<WorkOrder>)

    @Query("SELECT * FROM work_orders WHERE otId = :otId LIMIT 1")
    suspend fun getOrderById(otId: String): WorkOrder?

    @Query("DELETE FROM work_orders WHERE otId = :otId")
    suspend fun deleteOrder(otId: String)
}

@Dao
interface DispatchLogDao {
    @Query("SELECT * FROM dispatch_logs ORDER BY timestamp DESC")
    fun getAllDispatchLogs(): Flow<List<DispatchLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: DispatchLog)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(logs: List<DispatchLog>)
}

@Dao
interface SystemNotificationDao {
    @Query("SELECT * FROM system_notifications ORDER BY timestamp DESC")
    fun getAllNotifications(): Flow<List<SystemNotification>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: SystemNotification)

    @Query("UPDATE system_notifications SET isRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: Long)
}

@Dao
interface FuelReceiptDao {
    @Query("SELECT * FROM fuel_receipts ORDER BY timestamp DESC")
    fun getAllReceipts(): Flow<List<FuelReceipt>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReceipt(receipt: FuelReceipt)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(receipts: List<FuelReceipt>)
}

@Database(
    entities = [
        User::class,
        InventoryItem::class,
        WorkOrder::class,
        DispatchLog::class,
        SystemNotification::class,
        FuelReceipt::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract val userDao: UserDao
    abstract val inventoryItemDao: InventoryItemDao
    abstract val workOrderDao: WorkOrderDao
    abstract val dispatchLogDao: DispatchLogDao
    abstract val systemNotificationDao: SystemNotificationDao
    abstract val fuelReceiptDao: FuelReceiptDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "fuelmanager_database"
                )
                .fallbackToDestructiveMigration()
                .addCallback(DatabaseCallback(context))
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback(val context: Context) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { appDb ->
                CoroutineScope(Dispatchers.IO).launch {
                    populateInitialData(appDb)
                }
            }
        }

        override fun onOpen(db: SupportSQLiteDatabase) {
            super.onOpen(db)
            INSTANCE?.let { appDb ->
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val countCursor = db.query("SELECT COUNT(*) FROM fuel_receipts")
                        var count = 0
                        if (countCursor.moveToFirst()) {
                            count = countCursor.getInt(0)
                        }
                        countCursor.close()
                        if (count == 0) {
                            val receipts = listOf(
                                FuelReceipt(
                                    fuelType = "Gasoil",
                                    totalReceived = 5000.0,
                                    usedAmount = 4850.0,
                                    wasteAmount = 150.0,
                                    operator = "Carlos Pérez"
                                ),
                                FuelReceipt(
                                    fuelType = "Aceite Usado",
                                    totalReceived = 2400.0,
                                    usedAmount = 2100.0,
                                    wasteAmount = 300.0,
                                    operator = "Pedro Gómez"
                                ),
                                FuelReceipt(
                                    fuelType = "Gasoil",
                                    totalReceived = 3500.0,
                                    usedAmount = 3400.0,
                                    wasteAmount = 100.0,
                                    operator = "Pedro Gómez"
                                )
                            )
                            appDb.fuelReceiptDao.insertAll(receipts)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }

        private suspend fun populateInitialData(db: AppDatabase) {
            // Initial Users
            val users = listOf(
                User(
                    email = "carlosaperezp@gmail.com",
                    name = "Carlos Pérez",
                    role = "ADMINISTRADOR",
                    token = "token_carlos_admin_987213",
                    imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuD7HusNPKwE728UyJ-X33JNw_wrTPlAUSdLskdEexgkpuPAW6L1Lm4e9s7QcF0MkWPlzypYZqZb1obsHkj8FYVlex0rByfCXMv60tQs--lKcjQgOnGhPMr2ln8gwdUlgz4X4G7vdd0cRhjcfJ43tIWKPOWlDZWzg788wApnGBcpTAkKMWpxwW4OOxHjujViy0yBYrMYngUjnv7TldNTqp5FnldZqEFrHohUMfMRf7yx6lPMiEBttgCSfDaYQnylHbpcl0emrWkzAEE3"
                ),
                User(
                    email = "tecnico@fuelmanager.com",
                    name = "Juan Pérez",
                    role = "TÉCNICO",
                    token = "token_juan_tecnico_334511",
                    imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuC_YZl9eDp9eiTgdInupe0y93O6ngospEvPVkmM-z3oTKgyg86tasMcn-gRj9KVSKvhPVHQOoGQDDqf9T7taCyySMKB6jYQuBZ-M_L0pJ74lA6jHGjO8Hpjnii4sNgYwrZjuBCxPkaVPS2ZHkGdh3vmV7JJ-ClmZToYZtuPJ9sc1rDOrN0N1qADWeVgAQzPsKReHFoyxinv4Vo50HXrC64_6968yWLS5Fjmpt9Ky7vVeChmWE84H0g5fL01yOanAaqJ4cFGlYYyVbMr"
                ),
                User(
                    email = "almacenista@fuelmanager.com",
                    name = "Pedro Gómez",
                    role = "ALMACENISTA",
                    token = "token_pedro_storage_112093",
                    imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuCC_sgz8qsZP1Ph55txYvU0O7iqZkmmfH4KEhrGkiunW_vNKH_pgNhYGOFD3zVEDn0PX_KLNSwK21POjbK4Yfb7bat6keaktkUL4wOEMam9rb85ZpBTkUYP-Ije73-V_FzSfIcwHxppmpFFJsWJvD8ZDeUx9-diSJMA_S16lXHXZuYW35RcB4qvIS3A7izqmPMtJVoaVFI1v4GklhBlGpYJgnB6edk_KX4cCL3GwjCjB6Nsy6MNRr1E0XLvSEdwyGzCSYYgSvjTwIyA"
                )
            )
            users.forEach { db.userDao.insertUser(it) }

            // Initial Inventory
            val inventory = listOf(
                InventoryItem(
                    sku = "VL-99283-X",
                    name = "Válvula Solenoide 3/4\"",
                    location = "Pasillo A / Estante 12",
                    stockActual = 14,
                    totalStockRequired = 18,
                    abcGrade = "A",
                    vedGrade = "V",
                    statusText = "Saludable",
                    dsiDays = "12 d",
                    iconName = "settings_input_component"
                ),
                InventoryItem(
                    sku = "FT-44021-M",
                    name = "Filtro de Aceite Industrial",
                    location = "Pasillo B / Estante 04",
                    stockActual = 3,
                    totalStockRequired = 12,
                    abcGrade = "B",
                    vedGrade = "E",
                    statusText = "Stock Bajo",
                    dsiDays = "45 d",
                    iconName = "oil_barrel"
                ),
                InventoryItem(
                    sku = "BJ-11200-S",
                    name = "Bujía de Ignición 22mm",
                    location = "Cajón 05 / Bloque 02",
                    stockActual = 142,
                    totalStockRequired = 150,
                    abcGrade = "C",
                    vedGrade = "D",
                    statusText = "Exceso",
                    dsiDays = "156 d",
                    iconName = "bolt"
                )
            )
            db.inventoryItemDao.insertAll(inventory)

            // Initial Work Orders
            val orders = listOf(
                WorkOrder(
                    otId = "OT-2024-892",
                    type = "Correctiva de Emergencia",
                    title = "Falla en Bomba Sumergible A-04",
                    location = "Tanque Estación Norte",
                    assignedTo = "J. Pérez",
                    scheduledTime = "Hace 15 min",
                    status = "PENDIENTE",
                    isEmergency = true,
                    readingHours = 12450.5,
                    mileage = 84210
                ),
                WorkOrder(
                    otId = "OT-2024-885",
                    type = "Preventiva Programada",
                    title = "Calibración de Dispensadores",
                    location = "Isla 02 - Disp 04",
                    assignedTo = "J. Pérez",
                    scheduledTime = "Mañana, 08:00",
                    status = "POR_INICIAR",
                    isEmergency = false
                ),
                WorkOrder(
                    otId = "OT-2024-870",
                    type = "Completada",
                    title = "Limpieza de Filtros de Aire",
                    location = "Generador Emergencia 01",
                    assignedTo = "J. Pérez",
                    scheduledTime = "Ayer, 16:45",
                    status = "COMPLETADA",
                    isEmergency = false
                )
            )
            db.workOrderDao.insertAll(orders)

            // Initial Dispatch Logs
            val dispatches = listOf(
                DispatchLog(
                    activeId = "GEN-022",
                    operator = "Pedro Gómez",
                    capacity = "300L",
                    imageUrl = "https://images.unsplash.com/photo-1518156677180-95a2893f3e9f?w=120",
                    observedVolume = 120.50,
                    correctedVolume = 120.50 * 0.9923,
                    timeStr = "14:22:10",
                    isOffline = false
                ),
                DispatchLog(
                    activeId = "VOL-118",
                    operator = "Juan Perez",
                    capacity = "450L",
                    imageUrl = "https://images.unsplash.com/photo-1601584115197-04ecc0da31d7?w=120",
                    observedVolume = 412.00,
                    correctedVolume = 412.00 * 0.9923,
                    timeStr = "13:45:02",
                    isOffline = true
                )
            )
            db.dispatchLogDao.insertAll(dispatches)

            // Initial Notifications
            val notifications = listOf(
                SystemNotification(
                    title = "ANOMALÍA DE TANQUE",
                    message = "Fuga detectada: anomalía en Tanque TK-001 (Diesel Premium).",
                ),
                SystemNotification(
                    title = "NUEVA ORDEN DE TRABAJO",
                    message = "Correctiva de Emergencia asignada: Falla en Bomba Sumergible A-04.",
                ),
                SystemNotification(
                    title = "BALANCE DESVIADO",
                    message = "Merma Crítica: Desviación de 1.5% entre sensor de entrada y ticket de despacho.",
                )
            )
            notifications.forEach { db.systemNotificationDao.insertNotification(it) }

            // Initial Fuel Receipts
            val receipts = listOf(
                FuelReceipt(
                    fuelType = "Gasoil",
                    totalReceived = 5000.0,
                    usedAmount = 4850.0,
                    wasteAmount = 150.0,
                    operator = "Carlos Pérez"
                ),
                FuelReceipt(
                    fuelType = "Aceite Usado",
                    totalReceived = 2400.0,
                    usedAmount = 2100.0,
                    wasteAmount = 300.0,
                    operator = "Pedro Gómez"
                ),
                FuelReceipt(
                    fuelType = "Gasoil",
                    totalReceived = 3500.0,
                    usedAmount = 3400.0,
                    wasteAmount = 100.0,
                    operator = "Pedro Gómez"
                )
            )
            db.fuelReceiptDao.insertAll(receipts)
        }
    }
}
