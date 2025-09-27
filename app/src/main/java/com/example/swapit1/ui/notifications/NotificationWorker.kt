package com.example.swapit1.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.swapit1.NotificationHelper
import com.example.swapit1.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class NotificationWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override suspend fun doWork(): Result {
        try {
            // جلب UID المستخدم الحالي
            val currentUserId = auth.currentUser?.uid ?: return Result.failure()

            // جلب الإشعارات الجديدة لهذا المستخدم فقط
            val notificationsSnap = firestore.collection("notifications")
                .whereEqualTo("seen", false)
                .whereEqualTo("userId", currentUserId)
                .get()
                .await()

            for (doc in notificationsSnap.documents) {
                val title = doc.getString("title") ?: "إشعار"
                val message = doc.getString("message") ?: ""

                // عرض الإشعار على الجهاز الحالي
                NotificationHelper.showNotification(
                    applicationContext,
                    title,
                    message,
                    System.currentTimeMillis().toInt(),
                            R.drawable.swapit
                )

                // تحديث الحالة إلى 'تمت المشاهدة'
                doc.reference.update("seen", true)
            }

            return Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            return Result.failure()
        }
    }
}
