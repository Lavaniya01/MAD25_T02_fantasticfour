package np.ict.mad.mad_assignment.data

import android.content.Context
import androidx.room.Room
import np.ict.mad.mad_assignment.model.AppDatabase

object DatabaseProvider {
    private var INSTANCE: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase {
        return INSTANCE ?: synchronized(this) {
            val instance = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "tasks_db"
            ).build()
            INSTANCE = instance
            instance
        }
    }
}
