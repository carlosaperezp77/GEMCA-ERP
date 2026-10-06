package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class FuelManagerViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val repository = FuelRepository(db)

    // Auth & Session State
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _loginError = MutableStateFlow<String?>(null)
    val loginError: StateFlow<String?> = _loginError.asStateFlow()

    // Connection Mode State (Online/Offline)
    private val _isOnline = MutableStateFlow(true)
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    // Real-time toast alerts
    private val _notificationToast = MutableSharedFlow<String>()
    val notificationToast: SharedFlow<String> = _notificationToast.asSharedFlow()

    // Global Statistics State
    val allInventory: StateFlow<List<InventoryItem>> = repository.allInventoryItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allOrders: StateFlow<List<WorkOrder>> = repository.allWorkOrders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allDispatches: StateFlow<List<DispatchLog>> = repository.allDispatchLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allNotifications: StateFlow<List<SystemNotification>> = repository.allNotifications
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allFuelReceipts: StateFlow<List<FuelReceipt>> = repository.allFuelReceipts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active state elements
    private val _selectedWorkOrder = MutableStateFlow<WorkOrder?>(null)
    val selectedWorkOrder: StateFlow<WorkOrder?> = _selectedWorkOrder.asStateFlow()

    val searchQuery = MutableStateFlow("")
    val selectedAbcFilter = MutableStateFlow("TODOS")
    val selectedVedFilter = MutableStateFlow("TODOS")
    val selectedOtTab = MutableStateFlow("Todas")

    // Dispatch form state
    val dispatchActiveId = MutableStateFlow("VOL-442")
    val dispatchObservedVolume = MutableStateFlow("250.00")
    val vcfFactor = MutableStateFlow(0.9923)

    init {
        // Automatically set initial selection of WorkOrder when list loads
        viewModelScope.launch {
            allOrders.collectLatest { list ->
                if (list.isNotEmpty() && _selectedWorkOrder.value == null) {
                    _selectedWorkOrder.value = list.firstOrNull { it.otId == "OT-2024-892" } ?: list.first()
                }
            }
        }
    }

    fun selectWorkOrder(order: WorkOrder) {
        _selectedWorkOrder.value = order
    }

    // Toggle Online/Offline simulation
    fun toggleConnection() {
        val next = !_isOnline.value
        _isOnline.value = next
        viewModelScope.launch {
            if (next) {
                _notificationToast.emit("Modo Online: Conexión recuperada y datos sincronizados.")
                // Automatically push/sync any offline dispatches
                db.dispatchLogDao.getAllDispatchLogs().first().forEach { log ->
                    if (log.isOffline) {
                        // Simulate sync
                        _notificationToast.emit("Sincronizando despacho offline #${log.id}...")
                    }
                }
            } else {
                _notificationToast.emit("Modo Offline activo: Datos se almacenarán localmente.")
            }
        }
    }

    // Simulated authentic token login based on Firebase profiles
    fun login(email: String) {
        viewModelScope.launch {
            _loginError.value = null
            val user = repository.getUserByEmail(email.trim().lowercase())
            if (user != null) {
                _currentUser.value = user
                _notificationToast.emit("Sesión protegida por Token iniciada: ${user.name} (${user.role})")
            } else {
                // If not found in database, allow fallback simulation for user experience
                val fallbackRole = when {
                    email.contains("carlos") || email.contains("admin") -> "ADMINISTRADOR"
                    email.contains("tecnico") || email.contains("juan") -> "TÉCNICO"
                    else -> "ALMACENISTA"
                }
                val fallbackUser = User(
                    email = email,
                    name = email.substringBefore("@").replaceFirstChar { it.uppercase() },
                    role = fallbackRole,
                    token = "token_simulated_${UUID.randomUUID()}",
                    imageUrl = ""
                )
                repository.insertUser(fallbackUser)
                _currentUser.value = fallbackUser
                _notificationToast.emit("Token Firebase generado para nuevo perfil: $email")
            }
        }
    }

    fun logout() {
        _currentUser.value = null
        _loginError.value = null
        viewModelScope.launch {
            _notificationToast.emit("Sesión finalizada de manera segura.")
        }
    }

    fun register(email: String, name: String, role: String) {
        viewModelScope.launch {
            _loginError.value = null
            val trimmedEmail = email.trim().lowercase()
            if (trimmedEmail.isEmpty() || name.trim().isEmpty()) {
                _loginError.value = "Por favor, complete todos los campos."
                _notificationToast.emit("Error: Nombre y email son requeridos.")
                return@launch
            }
            val existing = repository.getUserByEmail(trimmedEmail)
            if (existing != null) {
                _loginError.value = "La cuenta ya existe."
                _notificationToast.emit("Error: El correo $trimmedEmail ya está registrado.")
                return@launch
            }
            val newlyCreated = User(
                email = trimmedEmail,
                name = name.trim(),
                role = role,
                token = "token_simulated_${UUID.randomUUID()}",
                imageUrl = ""
            )
            repository.insertUser(newlyCreated)
            _currentUser.value = newlyCreated
            _notificationToast.emit("¡Cuenta creada exitosamente! Bienvenido, ${newlyCreated.name}.")
        }
    }

    // Trigger sync with Central ERP system
    fun syncERP() {
        viewModelScope.launch {
            _notificationToast.emit("Sincronizando con ERP Central...")
            // Simulating a delay of network
            kotlinx.coroutines.delay(1200)
            _notificationToast.emit("Indicadores KPIs y Balance de materiales actualizados con el ERP.")
            repository.insertNotification(
                SystemNotification(
                    title = "RECONCILIACIÓN ERP EXCEL",
                    message = "Balances de materiales consolidados de forma satisfactoria."
                )
            )
        }
    }

    // Export analytics report as PDF or Excel
    fun exportReport(format: String) {
        viewModelScope.launch {
            _notificationToast.emit("Generando reporte $format...")
            kotlinx.coroutines.delay(1500)
            _notificationToast.emit("¡Reporte $format exportado correctamente! Guardado en descargas.")
            repository.insertNotification(
                SystemNotification(
                    title = "REPORTE DESCARGADO",
                    message = "El informe de gestión y rendimiento en formato $format se encuentra listo."
                )
            )
        }
    }

    // Add receipt of stock item manually
    fun addReceipt(sku: String, name: String, location: String, amount: Int, abc: String, ved: String) {
        viewModelScope.launch {
            val existing = allInventory.value.find { it.sku.uppercase() == sku.uppercase() }
            if (existing != null) {
                val updated = existing.copy(
                    stockActual = existing.stockActual + amount,
                    totalStockRequired = maxOf(existing.totalStockRequired, existing.stockActual + amount)
                )
                repository.insertInventoryItem(updated)
            } else {
                val newItem = InventoryItem(
                    sku = sku.uppercase(),
                    name = name,
                    location = location,
                    stockActual = amount,
                    totalStockRequired = amount + 5,
                    abcGrade = abc,
                    vedGrade = ved,
                    statusText = "Saludable",
                    dsiDays = "10 d",
                    iconName = "settings_input_component"
                )
                repository.insertInventoryItem(newItem)
            }
            _notificationToast.emit("Recepción registrada: Sku $sku sumado +$amount unidades.")
        }
    }

    // Simulated IA Object Scan and Auto-Count
    fun runIaCount(sku: String, countedAmount: Int) {
        viewModelScope.launch {
            val target = allInventory.value.find { it.sku.uppercase() == sku.uppercase() }
            if (target != null) {
                val updated = target.copy(stockActual = countedAmount)
                repository.insertInventoryItem(updated)
                _notificationToast.emit("Escaneo IA: Se autodectó $countedAmount unidades de SKU $sku")
            }
        }
    }

    // Save a draft of Work Order locally
    fun saveWorkOrderDraft(otId: String, reading: Double, mileage: Int, signaturePoints: String?) {
        viewModelScope.launch {
            val current = db.workOrderDao.getOrderById(otId)
            if (current != null) {
                val updated = current.copy(
                    readingHours = reading,
                    mileage = mileage,
                    signaturePoints = signaturePoints,
                    status = "BORRADOR"
                )
                repository.insertWorkOrder(updated)
                _selectedWorkOrder.value = updated
                _notificationToast.emit("Borrador de OT-$otId guardado localmente.")
            }
        }
    }

    // Finalize Work Order (transitions to completada and alerts mobile assigned tech)
    fun finalizeWorkOrder(otId: String, reading: Double, mileage: Int, signaturePoints: String?) {
        viewModelScope.launch {
            val current = db.workOrderDao.getOrderById(otId)
            if (current != null) {
                val updated = current.copy(
                    readingHours = reading,
                    mileage = mileage,
                    signaturePoints = signaturePoints,
                    status = "COMPLETADA"
                )
                repository.insertWorkOrder(updated)
                _selectedWorkOrder.value = updated
                _notificationToast.emit("¡OT-$otId cerrada y consolidada! Enviada alerta push al supervisor.")
                
                // Add a notification for security audit and supervisor dashboard
                repository.insertNotification(
                    SystemNotification(
                        title = "ALERTA PUSH: ORDEN FINALIZADA",
                        message = "La OT-$otId (${current.title}) ha sido cerrada exitosamente por el técnico Juan Pérez."
                    )
                )
            }
        }
    }

    // Create a new Corrective Emergency OT instantly from FAB
    fun createEmergencyCorrective(title: String, location: String) {
        viewModelScope.launch {
            val newOtId = "OT-2024-${Random().nextInt(899) + 100}"
            val newOt = WorkOrder(
                otId = newOtId,
                type = "Correctiva de Emergencia",
                title = title,
                location = location,
                assignedTo = "J. Pérez",
                scheduledTime = "Hace 1 min",
                status = "PENDIENTE",
                isEmergency = true
            )
            repository.insertWorkOrder(newOt)
            _selectedWorkOrder.value = newOt
            
            // Trigger push notifications
            _notificationToast.emit("🚨 Alerta Push enviada al Técnico: $title en $location")
            repository.insertNotification(
                SystemNotification(
                    title = "NUEVA ALERTA PUSH",
                    message = "Falla de Emergencia '$title' asignada directamente a J. Pérez."
                )
            )
        }
    }

    // Registry dispatch fuel transaction
    fun dispatchFuel() {
        val observed = dispatchObservedVolume.value.toDoubleOrNull() ?: 0.0
        val corrected = observed * vcfFactor.value
        val activeIdVal = dispatchActiveId.value
        val isOff = !_isOnline.value

        viewModelScope.launch {
            val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            val timeString = sdf.format(Date())

            val newLog = DispatchLog(
                activeId = activeIdVal,
                operator = _currentUser.value?.name ?: "Operador",
                capacity = "450L",
                imageUrl = "https://images.unsplash.com/photo-1621259182978-f09e5e2ae1bd?w=120",
                observedVolume = observed,
                correctedVolume = corrected,
                timeStr = timeString,
                isOffline = isOff
            )
            repository.insertDispatchLog(newLog)

            _notificationToast.emit("Despacho registrado: $observed L (${String.format("%.2f", corrected)} L Corregidos VCF)")

            if (isOff) {
                repository.insertNotification(
                    SystemNotification(
                        title = "DESPACHO OFFLINE ALMACENADO",
                        message = "Vehículo $activeIdVal abastecido con ${observed}L de combustible sin conexión."
                    )
                )
            } else {
                repository.insertNotification(
                    SystemNotification(
                        title = "DESPACHO COMBUSTIBLE",
                        message = "Combustible despachado exitosamente a unidad $activeIdVal: ${observed}L."
                    )
                )
            }
        }
    }

    fun recordFuelReceipt(fuelType: String, totalReceived: Double, usedAmount: Double, wasteAmount: Double) {
        viewModelScope.launch {
            val op = _currentUser.value?.name ?: "Operador"
            val receipt = FuelReceipt(
                fuelType = fuelType,
                totalReceived = totalReceived,
                usedAmount = usedAmount,
                wasteAmount = wasteAmount,
                operator = op,
                isOffline = !_isOnline.value
            )
            repository.insertFuelReceipt(receipt)
            _notificationToast.emit("Recepción registrada: $fuelType | Aprovechado: ${usedAmount}L | Residuo: ${wasteAmount}L")

            val alertMessage = "Recepción de $fuelType de ${totalReceived}L. Se aprovechó ${usedAmount}L (" +
                    "${String.format("%.1f", (usedAmount / totalReceived) * 100)}%) | Residuo: ${wasteAmount}L (" +
                    "${String.format("%.1f", (wasteAmount / totalReceived) * 100)}%)"
            repository.insertNotification(
                SystemNotification(
                    title = "RECEPCIÓN: $fuelType".uppercase(),
                    message = alertMessage
                )
            )
        }
    }
}
