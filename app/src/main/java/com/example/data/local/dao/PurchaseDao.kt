package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.PurchaseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PurchaseDao {
    @Query("SELECT * FROM purchases ORDER BY purchaseDate DESC")
    fun getAllPurchases(): Flow<List<PurchaseEntity>>

    @Query("SELECT * FROM purchases WHERE id = :id")
    fun getPurchaseById(id: Long): Flow<PurchaseEntity?>

    @Query("SELECT * FROM purchases WHERE id = :id")
    suspend fun getPurchaseByIdSync(id: Long): PurchaseEntity?

    @Query("SELECT COUNT(*) FROM purchases")
    fun getPurchaseCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM purchases")
    suspend fun getPurchaseCountSync(): Int

    @Query("""
        SELECT * FROM purchases 
        WHERE productName LIKE '%' || :query || '%' 
           OR brand LIKE '%' || :query || '%'
           OR model LIKE '%' || :query || '%'
           OR store LIKE '%' || :query || '%'
           OR serialNumber LIKE '%' || :query || '%'
           OR notes LIKE '%' || :query || '%'
           OR itemsSummary LIKE '%' || :query || '%'
        ORDER BY purchaseDate DESC
    """)
    fun searchPurchases(query: String): Flow<List<PurchaseEntity>>

    @Query("""
        SELECT * FROM purchases 
        WHERE productName LIKE '%' || :productName || '%'
        ORDER BY purchaseDate DESC
    """)
    suspend fun findSimilarPurchases(productName: String): List<PurchaseEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPurchase(purchase: PurchaseEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPurchases(purchases: List<PurchaseEntity>): List<Long>

    @Update
    suspend fun updatePurchase(purchase: PurchaseEntity)

    @Delete
    suspend fun deletePurchase(purchase: PurchaseEntity)

    @Query("DELETE FROM purchases WHERE id = :id")
    suspend fun deletePurchaseById(id: Long)

    @Query("DELETE FROM purchases")
    suspend fun deleteAllPurchases()
}
