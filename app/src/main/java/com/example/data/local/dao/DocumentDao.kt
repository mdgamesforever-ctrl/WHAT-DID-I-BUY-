package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.DocumentEntity
import com.example.data.model.DocumentType
import kotlinx.coroutines.flow.Flow

@Dao
interface DocumentDao {
    @Query("SELECT * FROM documents ORDER BY createdAt DESC")
    fun getAllDocuments(): Flow<List<DocumentEntity>>

    @Query("SELECT * FROM documents WHERE purchaseId = :purchaseId ORDER BY createdAt DESC")
    fun getDocumentsForPurchase(purchaseId: Long): Flow<List<DocumentEntity>>

    @Query("SELECT * FROM documents WHERE docType = :type ORDER BY createdAt DESC")
    fun getDocumentsByType(type: DocumentType): Flow<List<DocumentEntity>>

    @Query("SELECT * FROM documents WHERE id = :id")
    suspend fun getDocumentById(id: Long): DocumentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocument(document: DocumentEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocuments(documents: List<DocumentEntity>): List<Long>

    @Update
    suspend fun updateDocument(document: DocumentEntity)

    @Delete
    suspend fun deleteDocument(document: DocumentEntity)

    @Delete
    suspend fun deleteDocuments(documents: List<DocumentEntity>)

    @Query("DELETE FROM documents WHERE purchaseId = :purchaseId")
    suspend fun deleteDocumentsForPurchase(purchaseId: Long)

    @Query("DELETE FROM documents WHERE id = :id")
    suspend fun deleteDocumentById(id: Long)

    @Query("DELETE FROM documents")
    suspend fun deleteAllDocuments()
}
