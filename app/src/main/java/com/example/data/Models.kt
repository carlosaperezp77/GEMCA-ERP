package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "users")
data class User(
    @PrimaryKey val email: String,
    val name: String,
    val role: String, // "ADMINISTRADOR", "ALMACENISTA", "TÉCNICO"
    val token: String,
    val imageUrl: String
) : Serializable

@Entity(tableName = "inventory_items")
data class InventoryItem(
    @PrimaryKey val sku: String,
    val name: String,
    val location: String,
    val stockActual: Int,
    val totalStockRequired: Int, // for progress indicator
    val abcGrade: String, // "A", "B", "C"
    val vedGrade: String, // "V", "E", "D"
    val statusText: String, // "Saludable", "Stock Bajo", "Exceso"
    val dsiDays: String, // "12 d", "45 d", "156 d"
    val iconName: String // "settings_input_component", "oil_barrel", "bolt", etc.
) : Serializable

@Entity(tableName = "work_orders")
data class WorkOrder(
    @PrimaryKey val otId: String,
    val type: String, // "Correctiva de Emergencia", "Preventiva Programada", "Completada"
    val title: String,
    val location: String,
    val assignedTo: String,
    val scheduledTime: String,
    val status: String, // "PENDIENTE", "POR_INICIAR", "COMPLETADA", "BORRADOR"
    val readingHours: Double = 0.0,
    val mileage: Int = 0,
    val evidenceBefore: String? = null, // base64 or photo info
    val evidenceDuring: String? = null,
    val evidenceAfter: String? = null,
    val signaturePoints: String? = null, // Encoded signature representation
    val signatureAuthor: String? = null,
    val isEmergency: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
) : Serializable

@Entity(tableName = "dispatch_logs")
data class DispatchLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val activeId: String, // "GEN-022", "VOL-118", "VOL-442"
    val operator: String,
    val capacity: String,
    val imageUrl: String,
    val observedVolume: Double,
    val correctedVolume: Double,
    val timeStr: String,
    val isOffline: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
) : Serializable

@Entity(tableName = "system_notifications")
data class SystemNotification(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val isPush: Boolean = true
) : Serializable

@Entity(tableName = "fuel_receipts")
data class FuelReceipt(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fuelType: String, // "Aceite Usado", "Gasoil"
    val totalReceived: Double,
    val usedAmount: Double, // cuánto se aprovechó / aprovechable
    val wasteAmount: Double, // "Residuo para disposición final"
    val operator: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isOffline: Boolean = false
) : Serializable
