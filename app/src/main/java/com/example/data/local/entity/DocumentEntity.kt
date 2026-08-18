package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.data.model.DocumentType

@Entity(
    tableName = "documents",
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
data class DocumentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val purchaseId: Long? = null,
    val title: String,
    val docType: DocumentType = DocumentType.OTHER,
    val filePath: String = "",
    val uriString: String = "",
    val notes: String = "",
    val expiryDate: Long? = null,
    val fileSize: Long = 0L,
    val createdAt: Long = System.currentTimeMillis()
)
