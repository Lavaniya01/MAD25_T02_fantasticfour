package np.ict.mad.mad_assignment

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build

class SmartTasksApp : Application() {

    override fun onCreate() {
        super.onCreate()
        createDueDateNotificationChannel()
    }

    private fun createDueDateNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "due_channel",
                "Due Date Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifies when a task is approaching its due date"
            }

            val notificationManager =
                getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
}
