package np.ict.mad.mad_assignment

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class DueReminderWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val taskId = inputData.getInt("taskId", -1)
        val title = inputData.getString("title") ?: "Task due soon"

        if (taskId == -1) return Result.failure()

        // Open app when user taps the notification
        val launchIntent = applicationContext.packageManager
            .getLaunchIntentForPackage(applicationContext.packageName)

        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            taskId,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(applicationContext, "due_channel")
            .setSmallIcon(R.drawable.ic_launcher_foreground) // change later if you want
            .setContentTitle(title)
            .setContentText("Due soon. Tap to open SmartTasks.")
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        // ✅ Android 13+ requires POST_NOTIFICATIONS permission
        val canPostNotifications =
            Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                    ContextCompat.checkSelfPermission(
                        applicationContext,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) == PackageManager.PERMISSION_GRANTED

        if (canPostNotifications) {
            try {
                NotificationManagerCompat.from(applicationContext)
                    .notify(taskId, notification)
            } catch (_: SecurityException) {
                // Permission revoked / OEM issue - do nothing
            }
        }

        return Result.success()
    }
}
