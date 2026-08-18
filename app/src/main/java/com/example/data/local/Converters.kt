package com.example.data.local

import androidx.room.TypeConverter
import com.example.data.local.entity.ReminderType
import com.example.data.model.DocumentType
import com.example.data.model.PurchaseCategory
import com.example.data.model.ReturnStatus
import com.example.data.model.WarrantyStatus

class Converters {
    @TypeConverter
    fun fromPurchaseCategory(value: PurchaseCategory?): String {
        return value?.name ?: PurchaseCategory.OTHER.name
    }

    @TypeConverter
    fun toPurchaseCategory(value: String?): PurchaseCategory {
        return value?.let {
            try {
                PurchaseCategory.valueOf(it)
            } catch (e: Exception) {
                PurchaseCategory.OTHER
            }
        } ?: PurchaseCategory.OTHER
    }

    @TypeConverter
    fun fromReturnStatus(value: ReturnStatus?): String {
        return value?.name ?: ReturnStatus.UNKNOWN.name
    }

    @TypeConverter
    fun toReturnStatus(value: String?): ReturnStatus {
        return value?.let {
            try {
                ReturnStatus.valueOf(it)
            } catch (e: Exception) {
                ReturnStatus.UNKNOWN
            }
        } ?: ReturnStatus.UNKNOWN
    }

    @TypeConverter
    fun fromWarrantyStatus(value: WarrantyStatus?): String {
        return value?.name ?: WarrantyStatus.UNKNOWN.name
    }

    @TypeConverter
    fun toWarrantyStatus(value: String?): WarrantyStatus {
        return value?.let {
            try {
                WarrantyStatus.valueOf(it)
            } catch (e: Exception) {
                WarrantyStatus.UNKNOWN
            }
        } ?: WarrantyStatus.UNKNOWN
    }

    @TypeConverter
    fun fromDocumentType(value: DocumentType?): String {
        return value?.name ?: DocumentType.OTHER.name
    }

    @TypeConverter
    fun toDocumentType(value: String?): DocumentType {
        return value?.let {
            try {
                DocumentType.valueOf(it)
            } catch (e: Exception) {
                DocumentType.OTHER
            }
        } ?: DocumentType.OTHER
    }

    @TypeConverter
    fun fromReminderType(value: ReminderType?): String {
        return value?.name ?: ReminderType.CUSTOM.name
    }

    @TypeConverter
    fun toReminderType(value: String?): ReminderType {
        return value?.let {
            try {
                ReminderType.valueOf(it)
            } catch (e: Exception) {
                ReminderType.CUSTOM
            }
        } ?: ReminderType.CUSTOM
    }
}
