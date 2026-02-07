package np.ict.mad.mad_assignment

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import kotlinx.coroutines.Dispatchers
import np.ict.mad.mad_assignment.model.Folder
import java.util.Collections.emptyList
import androidx.compose.runtime.rememberCoroutineScope
import np.ict.mad.mad_assignment.data.DatabaseProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoldersScreen(nav: NavHostController){
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val dao = DatabaseProvider.getDatabase(context).taskDao()
    val folders by dao.getAllFoldersFlow().collectAsState(initial = emptyList())
    var showDialog by remember { mutableStateOf(false) }
    var folderName by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Folders") },
                navigationIcon = {
                    IconButton(onClick = { nav.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },

        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.CreateNewFolder, contentDescription = "New Folder")
            }
        }
    ){ padding ->
        if(showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("New Folder") },
                text = {
                    OutlinedTextField(value = folderName,
                        onValueChange = { newValue ->
                            folderName = newValue
                        },
                        label = { Text("Folder Name") },
                        singleLine = true
                    )
                       },
                confirmButton = {
                    Button(
                        onClick = {
                            if (folderName.isNotBlank()) {
                                scope.launch(Dispatchers.IO) {
                                    dao.insertFolder(Folder(name = folderName))
                                }
                                folderName = ""
                                showDialog = false
                            }
                        }
                    ){
                        Text("Create")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Folder List Display
        LazyColumn(modifier = Modifier.padding(padding).fillMaxSize()) {
            items(folders) { folder ->
                ListItem(
                    // Displaying the folder name clearly
                    headlineContent = { Text(folder.name) },
                    leadingContent = { Icon(Icons.Default.Folder, null) },
                    trailingContent = {
                        // Shortcut to add task directly to this specific folder
                        IconButton(onClick = { nav.navigate("${Routes.AddTask}?folderId=${folder.id}") }) {
                            Icon(Icons.Default.Add, contentDescription = "Add Task")
                        }
                    },
                    modifier = Modifier.clickable {
                        // Logic to open the folder details
                        nav.navigate("folder_detail/${folder.id}")
                    }
                )
                HorizontalDivider()
            }

        }
    }
}