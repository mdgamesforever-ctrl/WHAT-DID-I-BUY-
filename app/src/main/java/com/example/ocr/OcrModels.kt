package com.example.ocr

import com.example.data.model.PurchaseCategory
import com.example.data.model.ReturnStatus
import com.example.data.model.WarrantyStatus

data class OcrItem(
    val name: String,
    val quantity: Int = 1,
    val price: Double = 0.0
)

data class OcrDraftPurchase(
    val storeName: String = "",
    val isConfidentStore: Boolean = true,
    val purchaseDateMillis: Long = System.currentTimeMillis(),
    val isConfidentDate: Boolean = true,
    val totalPrice: Double = 0.0,
    val currency: String = "JOD",
    val isConfidentTotal: Boolean = true,
    val detectedItems: List<OcrItem> = emptyList(),
    val isConfidentItems: Boolean = true,
    val primaryProductName: String = "",
    val suggestedCategory: PurchaseCategory = PurchaseCategory.OTHER,
    val serialNumber: String = "",
    val invoiceNumber: String = "",
    val returnPeriodDays: Int = 14,
    val returnDeadlineMillis: Long? = null,
    val warrantyDurationMonths: Int = 12,
    val warrantyExpirationMillis: Long? = null,
    val returnPolicyText: String = "",
    val warrantyPolicyText: String = "",
    val rawText: String = "",
    val imageUri: String = ""
)
