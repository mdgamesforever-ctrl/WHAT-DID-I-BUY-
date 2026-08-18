package com.example.data.repository

import com.example.data.local.dao.PurchaseDao
import com.example.data.local.entity.PurchaseEntity
import kotlinx.coroutines.flow.Flow

class PurchaseRepository(private val purchaseDao: PurchaseDao) {
    val allPurchases: Flow<List<PurchaseEntity>> = purchaseDao.getAllPurchases()
    val purchaseCount: Flow<Int> = purchaseDao.getPurchaseCount()

    fun getPurchaseById(id: Long): Flow<PurchaseEntity?> = purchaseDao.getPurchaseById(id)

    suspend fun getPurchaseByIdSync(id: Long): PurchaseEntity? = purchaseDao.getPurchaseByIdSync(id)

    suspend fun getPurchaseCountSync(): Int = purchaseDao.getPurchaseCountSync()

    fun searchPurchases(query: String): Flow<List<PurchaseEntity>> = purchaseDao.searchPurchases(query)

    suspend fun findSimilarPurchases(productName: String): List<PurchaseEntity> =
        purchaseDao.findSimilarPurchases(productName)

    suspend fun insertPurchase(purchase: PurchaseEntity): Long =
        purchaseDao.insertPurchase(purchase)

    suspend fun insertPurchases(purchases: List<PurchaseEntity>): List<Long> =
        purchaseDao.insertPurchases(purchases)

    suspend fun updatePurchase(purchase: PurchaseEntity) =
        purchaseDao.updatePurchase(purchase)

    suspend fun deletePurchase(purchase: PurchaseEntity) =
        purchaseDao.deletePurchase(purchase)

    suspend fun deletePurchaseById(id: Long) =
        purchaseDao.deletePurchaseById(id)

    suspend fun deleteAll() =
        purchaseDao.deleteAllPurchases()
}
