import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.firebase.firestore.FirebaseFirestore

class DeleteRequestWorker(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {

    override fun doWork(): Result {
        val requestId = inputData.getString("requestId") ?: return Result.failure()
        val db = FirebaseFirestore.getInstance()

        db.collection("requests").document(requestId).delete()
            .addOnSuccessListener {
                // يمكنك إرسال إشعار هنا إذا أردت
            }
            .addOnFailureListener {
                // يمكن إعادة المحاولة لاحقاً
            }

        return Result.success()
    }
}
