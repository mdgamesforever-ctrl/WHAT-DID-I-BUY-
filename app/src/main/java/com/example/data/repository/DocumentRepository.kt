package com.example.data.repository

import com.example.data.local.dao.DocumentDao
import com.example.data.local.entity.DocumentEntity
import com.example.data.model.DocumentType
import kotlinx.coroutines.flow.Flow

class DocumentRepository(private val documentDao: DocumentDao) {
    val allDocuments: Flow<List<DocumentEntity>> = documentDao.getAllDocuments()

    fun getDocumentsForPurchase(purchaseId: Long): Flow<List<DocumentEntity>> =
        documentDao.getDocumentsForPurchase(purchaseId)

    fun getDocumentsByType(type: DocumentType): Flow<List<DocumentEntity>> =
        documentDao.getDocumentsByType(type)

    suspend fun getDocumentById(id: Long): DocumentEntity? =
        documentDao.getDocumentById(id)

    suspend fun insertDocument(document: DocumentEntity): Long =
        documentDao.insertDocument(document)

    suspend fun insertDocuments(documents: List<DocumentEntity>): List<Long> =
        documentDao.insertDocuments(documents)

    suspend fun updateDocument(document: DocumentEntity) =
        documentDao.updateDocument(document)

    suspend fun deleteDocument(document: DocumentEntity) =
        documentDao.deleteDocument(document)

    suspend fun deleteDocumentById(id: Long) =
        documentDao.deleteDocumentById(id)

    suspend fun deleteAll() =
        documentDao.deleteAllDocuments()
}
