package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.PurchaseCategory
import com.example.data.model.ReturnStatus
import com.example.data.model.WarrantyStatus

@Entity(tableName = "purchases")
data class PurchaseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val productName: String,
    val brand: String = "",
    val model: String = "",
    val category: PurchaseCategory = PurchaseCategory.OTHER,
    val customCategory: String = "",
    val store: String = "",
    val purchaseDate: Long = System.currentTimeMillis(),
    val purchasePrice: Double = 0.0,
    val currency: String = "JOD",
    val quantity: Int = 1,
    val serialNumber: String = "",
    val orderNumber: String = "",
    val receiptNumber: String = "",
    val warrantyStartDate: Long? = null,
    val warrantyEndDate: Long? = null,
    val warrantyDurationMonths: Int = 0,
    val warrantyStatus: WarrantyStatus = WarrantyStatus.UNKNOWN,
    val returnStartDate: Long? = null,
    val returnEndDate: Long? = null,
    val returnPeriodDays: Int = 0,
    val returnStatus: ReturnStatus = ReturnStatus.UNKNOWN,
    val notes: String = "",
    val isGift: Boolean = false,
    val giftRecipient: String = "",
    val giftDate: Long? = null,
    val isBorrowed: Boolean = false,
    val isLent: Boolean = false,
    val contactName: String = "",
    val primaryImageUri: String = "",
    val receiptImageUri: String = "",
    val itemsSummary: String = "", // Comma separated items from receipt
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
