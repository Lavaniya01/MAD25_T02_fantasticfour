package np.ict.mad.mad_assignment.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val description: String? = null,
    val date: String? = null,
    val priority: String? = null,
    val imageUri: String? = null,
    val isDone: Boolean = false,
    // store as epoch millis (Long) to avoid timezone parsing issues
    val dueAtMillis: Long = 0L,
    val folderId: Int? = null,
    val category: String? = null,
    val userId : String
)


