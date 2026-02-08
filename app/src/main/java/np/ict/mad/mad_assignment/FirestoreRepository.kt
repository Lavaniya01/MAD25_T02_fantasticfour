package np.ict.mad.mad_assignment

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import np.ict.mad.mad_assignment.model.Task
import np.ict.mad.mad_assignment.model.TaskDao

object FirestoreRepository {

    private val db = FirebaseFirestore.getInstance()

    private fun userTasks(uid: String) =
        db.collection("users")
            .document(uid)
            .collection("tasks")

    suspend fun uploadTask(uid: String, task: Task) {
        userTasks(uid)
            .document(task.id.toString())
            .set(task)
            .await()
    }

    suspend fun deleteTask(uid: String, taskId: Int) {
        userTasks(uid)
            .document(taskId.toString())
            .delete()
            .await()
    }
    suspend fun downloadTasks(uid: String, dao: TaskDao) {
        try {
            // Get all tasks from Firestore
            val snapshot = userTasks(uid).get().await()

            snapshot.documents.forEach { doc ->
                val task = doc.toObject(Task::class.java) ?: return@forEach
                // Ensure userId is correct
                val taskWithUid = task.copy(userId = uid)
                // Insert or replace locally
                dao.insertTask(taskWithUid) // Make sure your DAO uses OnConflictStrategy.REPLACE
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    fun listenTasks(
        uid: String,
        onChange: (List<Task>) -> Unit
    ) {
        userTasks(uid)
            .addSnapshotListener { snapshot, _ ->
                val tasks = snapshot?.toObjects(Task::class.java) ?: emptyList()
                onChange(tasks)
            }
    }
}
