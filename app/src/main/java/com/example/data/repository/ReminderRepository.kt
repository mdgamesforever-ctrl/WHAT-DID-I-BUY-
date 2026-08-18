package com.example.data.repository

import com.example.data.local.dao.ReminderDao
import com.example.data.local.entity.ReminderEntity
import kotlinx.coroutines.flow.Flow

class ReminderRepository(private val reminderDao: ReminderDao) {
    val activeReminders: Flow<List<ReminderEntity>> = reminderDao.getActiveReminders()

    fun getRemindersForPurchase(purchaseId: Long): Flow<List<ReminderEntity>> =
        reminderDao.getRemindersForPurchase(purchaseId)

    suspend fun getDueReminders(currentTimestamp: Long): List<ReminderEntity> =
        reminderDao.getDueReminders(currentTimestamp)

    suspend fun insertReminder(reminder: ReminderEntity): Long =
        reminderDao.insertReminder(reminder)

    suspend fun insertReminders(reminders: List<ReminderEntity>): List<Long> =
        reminderDao.insertReminders(reminders)

    suspend fun updateReminder(reminder: ReminderEntity) =
        reminderDao.updateReminder(reminder)

    suspend fun dismissReminder(id: Long) =
        reminderDao.dismissReminder(id)

    suspend fun markTriggered(id: Long) =
        reminderDao.markTriggered(id)

    suspend fun deleteReminder(reminder: ReminderEntity) =
        reminderDao.deleteReminder(reminder)

    suspend fun deleteRemindersForPurchase(purchaseId: Long) =
        reminderDao.deleteRemindersForPurchase(purchaseId)

    suspend fun deleteAll() =
        reminderDao.deleteAllReminders()
}
