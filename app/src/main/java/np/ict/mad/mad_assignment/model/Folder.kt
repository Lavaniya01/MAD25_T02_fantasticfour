package np.ict.mad.mad_assignment.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "folders")
data class Folder(
    @PrimaryKey(true) val id: Int = 0,
    val name: String
)