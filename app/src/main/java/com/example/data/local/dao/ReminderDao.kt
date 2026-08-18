package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.ReminderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderDao {
    @Query("SELECT * FROM reminders WHERE isDismissed = 0 ORDER BY triggerTimestamp ASC")
    fun getActiveReminders(): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders WHERE purchaseId = :purchaseId")
    fun getRemindersForPurchase(purchaseId: Long): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders WHERE triggerTimestamp <= :currentTimestamp AND isTriggered = 0 AND isDismissed = 0")
    suspend fun getDueReminders(currentTimestamp: Long): List<ReminderEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: ReminderEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminders(reminders: List<ReminderEntity>): List<Long>

    @Update
    suspend fun updateReminder(reminder: ReminderEntity)

    @Query("UPDATE reminders SET isDismissed = 1 WHERE id = :id")
    suspend fun dismissReminder(id: Long)

    @Query("UPDATE reminders SET isTriggered = 1 WHERE id = :id")
    suspend fun markTriggered(id: Long)

    @Delete
    suspend fun deleteReminder(reminder: ReminderEntity)

    @Query("DELETE FROM reminders WHERE purchaseId = :purchaseId")
    suspend fun deleteRemindersForPurchase(purchaseId: Long)

    @Query("DELETE FROM reminders")
    suspend fun deleteAllReminders()
}
