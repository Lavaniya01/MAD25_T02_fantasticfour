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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task): Long

    @Query("SELECT * FROM tasks ORDER BY id DESC")
    fun getAllTasksFlow(): Flow<List<Task>>

    @Update
    suspend fun updateTask(task:Task)

    @Delete
    suspend fun deleteTask(task: Task)

    @Query("SELECT * FROM tasks WHERE id = :taskId")
    suspend fun getTaskById(taskId: Int): Task?

    @Query("SELECT * FROM tasks WHERE folderId = :folderId")
    fun getTasksByFolder(folderId: Int): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE folderId IS NULL")
    fun getUnfolderedTasks(): Flow<List<Task>>

    @Query("SELECT * FROM folders")
    fun getAllFoldersFlow(): Flow<List<Folder>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFolder(folder: Folder)

    @Update
    suspend fun updateFolder(folder: Folder)

    @Delete
    suspend fun deleteFolder(folder: Folder)

    @Query("DELETE FROM tasks WHERE folderId = :folderId")
    suspend fun deleteTasksByFolder(folderId: Int)

    @Query("UPDATE tasks SET folderId = NULL WHERE folderId = :folderId")
    suspend fun setTasksFolderToNull(folderId: Int)

}

