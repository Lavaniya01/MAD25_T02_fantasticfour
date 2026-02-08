package np.ict.mad.mad_assignment.model

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import np.ict.mad.mad_assignment.model.Folder

@Dao
interface TaskDao {

    // Tasks

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task): Long

    @Query("SELECT * FROM tasks WHERE userId = :userId ORDER BY id DESC")
    fun getAllTasksFlow(userId: String): Flow<List<Task>>

    @Update
    suspend fun updateTask(task: Task)

    @Delete
    suspend fun deleteTask(task: Task)

    @Query("SELECT * FROM tasks WHERE id = :taskId AND userId = :userId")
    suspend fun getTaskById(taskId: Int, userId: String): Task?

    @Query("SELECT * FROM tasks WHERE folderId = :folderId AND userId = :userId")
    fun getTasksByFolder(folderId: Int, userId: String): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE folderId IS NULL AND userId = :userId")
    fun getUnfolderedTasks(userId: String): Flow<List<Task>>

    @Query("DELETE FROM tasks WHERE folderId = :folderId AND userId = :userId")
    suspend fun deleteTasksByFolder(folderId: Int, userId: String)

    @Query("UPDATE tasks SET folderId = NULL WHERE folderId = :folderId AND userId = :userId")
    suspend fun setTasksFolderToNull(folderId: Int, userId: String)

    // Folder Operations

    @Query("SELECT * FROM folders")
    fun getAllFoldersFlow(): Flow<List<Folder>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFolder(folder: Folder)

    @Update
    suspend fun updateFolder(folder: Folder)

    @Delete
    suspend fun deleteFolder(folder: Folder)
}
