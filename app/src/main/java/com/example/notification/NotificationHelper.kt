package com.example.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.data.local.entity.PurchaseEntity
import com.example.data.local.entity.ReminderEntity
import com.example.data.local.entity.ReminderType

object NotificationHelper {

    const val CHANNEL_ID = "what_did_i_buy_alerts"
    const val CHANNEL_NAME = "Deadlines & Warranty Alerts"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = "Reminders for purchase return deadlines and warranty expirations"
                enableLights(true)
                enableVibration(true)
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun generateRemindersForPurchase(purchase: PurchaseEntity): List<ReminderEntity> {
        val reminders = mutableListOf<ReminderEntity>()
        val now = System.currentTimeMillis()

        // 1. Return deadline reminders (7 days, 3 days, 1 day before)
        purchase.returnEndDate?.let { retEnd ->
            if (retEnd > now) {
                val intervals = listOf(
                    Pair(7L * 86400000L, "Return window closes in 7 days"),
                    Pair(3L * 86400000L, "Return window closes in 3 days"),
                    Pair(1L * 86400000L, "Return window closes tomorrow")
                )
                for ((offset, title) in intervals) {
                    val triggerTime = retEnd - offset
                    if (triggerTime > now) {
                        reminders.add(
                            ReminderEntity(
                                purchaseId = purchase.id,
                                title = title,
                                message = "${purchase.productName} (${purchase.purchasePrice} ${purchase.currency}) return deadline is approaching.",
                                triggerTimestamp = triggerTime,
                                type = ReminderType.RETURN_DEADLINE
                            )
                        )
                    }
                }
            }
        }

        // 2. Warranty expiration reminders (30 days, 14 days, 7 days, 1 day before)
        purchase.warrantyEndDate?.let { warEnd ->
            if (warEnd > now) {
                val intervals = listOf(
                    Pair(30L * 86400000L, "Warranty expires in 30 days"),
                    Pair(14L * 86400000L, "Warranty expires in 14 days"),
                    Pair(7L * 86400000L, "Warranty expires in 7 days"),
                    Pair(1L * 86400000L, "Warranty expires tomorrow")
                )
                for ((offset, title) in intervals) {
                    val triggerTime = warEnd - offset
                    if (triggerTime > now) {
                        reminders.add(
                            ReminderEntity(
                                purchaseId = purchase.id,
                                title = title,
                                message = "Warranty for ${purchase.productName} is ending soon. Check device condition.",
                                triggerTimestamp = triggerTime,
                                type = ReminderType.WARRANTY_EXPIRATION
                            )
                        )
                    }
                }
            }
        }

        return reminders
    }

    fun showNotification(context: Context, notificationId: Int, title: String, message: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, notification)
    }
}
