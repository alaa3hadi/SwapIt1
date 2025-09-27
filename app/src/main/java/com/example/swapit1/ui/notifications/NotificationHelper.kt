package com.example.swapit1

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.BitmapFactory
import android.os.Build
import androidx.core.app.NotificationCompat

object NotificationHelper {

    /**
     * @param context سياق التطبيق
     * @param title عنوان الإشعار
     * @param message نص الإشعار
     * @param id رقم فريد لكل إشعار
     * @param largeIconRes أيقونة كبيرة للإشعار داخل التطبيق (ليست Small Icon)
     */
    fun showNotification(
        context: Context,
        title: String,
        message: String,
        id: Int,
        largeIconRes: Int
    ) {
        // 🔹 تحقق من حالة السويتش
        val prefs = context.getSharedPreferences("notifications_prefs", Context.MODE_PRIVATE)
        val notificationsEnabled = prefs.getBoolean("global", true)
        if (!notificationsEnabled) return  // إذا السويتش مطفأ، تجاهل الإشعار

        val channelId = "offer_channel"
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "العروض الجديدة",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                enableVibration(true)
                description = "إشعارات العروض الجديدة"
            }
            notificationManager.createNotificationChannel(channel)
        }

        // إنشاء البت ماب من Large Icon
        val largeIconBitmap = BitmapFactory.decodeResource(context.resources, largeIconRes)

        val notification = NotificationCompat.Builder(context, channelId)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.swapit) // ➤ Small Icon ثابتة للشريط العلوي
            .setLargeIcon(largeIconBitmap)   // ➤ Large Icon خاصة لكل إشعار
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(id, notification)
    }
}
