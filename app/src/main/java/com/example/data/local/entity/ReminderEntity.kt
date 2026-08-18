package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class ReminderType {
    RETURN_DEADLINE,
    WARRANTY_EXPIRATION,
    DOCUMENT_EXPIRATION,
    CUSTOM
}

@Entity(
    tableName = "reminders",
    foreignKeys = [
        ForeignKey(
            entity = PurchaseEntity::class,
            parentColumns = ["id"],
            childColumns = ["purchaseId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["purchaseId"])]
)
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val purchaseId: Long,
    val title: String,
    val message: String,
    val triggerTimestamp: Long,
    val type: ReminderType,
    val isTriggered: Boolean = false,
    val isDismissed: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
