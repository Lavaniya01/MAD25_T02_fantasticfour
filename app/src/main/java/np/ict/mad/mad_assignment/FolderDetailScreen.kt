package np.ict.mad.mad_assignment

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import np.ict.mad.mad_assignment.data.DatabaseProvider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FolderDetailScreen(nav: NavController, folderId: Int) {
    val context = LocalContext.current
    val dao = DatabaseProvider.getDatabase(context).taskDao()
    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""


    // Fetch only tasks belonging to this folder
    val tasks by dao.getTasksByFolder(folderId, userId = uid).collectAsState(initial = emptyList())
    val folders by dao.getAllFoldersFlow().collectAsState(initial = emptyList())
    val currentFolder = folders.find { it.id == folderId }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(currentFolder?.name ?: "Folder") },
                navigationIcon = {
                    IconButton(onClick = { nav.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { nav.navigate("${Routes.AddTask}?folderId=$folderId") }) {
                Icon(Icons.Default.Add, contentDescription = "Add Task to Folder")
            }
        }
    ) { padding ->
        if (tasks.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("This folder is empty")
            }
        } else {
            LazyColumn(modifier = Modifier.padding(padding).fillMaxSize()) {
                items(tasks) { task ->
                    TaskCard(
                        task = task,
                        dao = dao,
                        scope = rememberCoroutineScope(),
                        onClick = { nav.navigate("details/${task.id}") },
                        onEdit = { nav.navigate("edit_task/${task.id}") },
                        onDelete = { /* Add delete logic */ }
                    )
                }
            }
        }
    }
}