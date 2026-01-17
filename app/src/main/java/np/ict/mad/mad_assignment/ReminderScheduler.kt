package np.ict.mad.mad_assignment

import android.content.Context
import androidx.core.app.NotificationManagerCompat
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import java.util.concurrent.TimeUnit

object ReminderScheduler {

    fun scheduleDueReminder(
        context: Context,
        taskId: Int,
        title: String,
        dueAtMillis: Long,
        remindBeforeMillis: Long = 60 * 60 * 1000L
    ) {
        if (dueAtMillis <= 0L) return

        val triggerAt = dueAtMillis - remindBeforeMillis
        val now = System.currentTimeMillis()

        val delay =
            if (triggerAt <= now) {
                // Missed reminder time → notify soon
                10_000L
            } else {
                triggerAt - now
            }

        val data = workDataOf(
            "taskId" to taskId,
            "title" to title
        )

        val request = OneTimeWorkRequestBuilder<DueReminderWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(data)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            uniqueName(taskId),
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    fun cancelDueReminder(context: Context, taskId: Int) {
        WorkManager.getInstance(context).cancelUniqueWork(uniqueName(taskId))
        NotificationManagerCompat.from(context).cancel(taskId)
    }

    private fun uniqueName(taskId: Int) = "due_reminder_$taskId"
}
