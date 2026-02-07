package np.ict.mad.mad_assignment.model

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Task::class, Folder::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
}
