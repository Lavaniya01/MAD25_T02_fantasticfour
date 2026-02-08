package np.ict.mad.mad_assignment

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material3.TextButton
import androidx.wear.compose.material3.TextButtonDefaults
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import np.ict.mad.mad_assignment.model.TaskDao


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoldersScreen(nav: NavHostController){
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val dao = DatabaseProvider.getDatabase(context).taskDao()
    val folders by dao.getAllFoldersFlow().collectAsState(initial = emptyList())
    val tasks by dao.getAllTasksFlow().collectAsState(initial = emptyList())
    val folderCounts by remember { derivedStateOf { tasks.groupBy { it.folderId }.mapValues { it.value.size } } }

    var showCreateDialog by remember { mutableStateOf(false) }
    var folderNameInput by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Folders") },
                navigationIcon = {
                    IconButton( onClick = { nav.popBackStack() }){
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        },

        floatingActionButton = {
            FloatingActionButton(onClick = {
                folderNameInput = ""
                showCreateDialog = true
            }) {
                Icon(Icons.Default.CreateNewFolder, "New Folder")
            }
        }
    ){ padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ){

            if(folders.isEmpty()) {
                Box( Modifier.fillMaxSize(), contentAlignment = Alignment.Center ) {
                    Text("No Folders Found", color = Color.Gray )
                }
            } else {
                folders.forEach { folder ->
                    FolderCard(
                        folder = folder,
                        dao = dao,
                        scope = scope,
                        onClick = { nav.navigate("folder_detail/${folder.id}") },
                        count = folderCounts[folder.id] ?: 0
                    )

                    Spacer( modifier = Modifier.height(8.dp) )
                }
            }

        }

        if (showCreateDialog) {
            AlertDialog(
                onDismissRequest = { showCreateDialog = false },
                title = { Text("New Folder") },
                text = {
                    OutlinedTextField(
                        value = folderNameInput,
                        onValueChange = { folderNameInput = it },
                        label = { Text("Folder Name") },
                        singleLine = true,
                    )

                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (folderNameInput.isNotBlank()) {
                                scope.launch(Dispatchers.IO) {
                                    dao.insertFolder(Folder(name = folderNameInput))
                                }
                                showCreateDialog = false
                            }
                        }) { Text("Create") }

                }

            )
        }

    }

}


@Composable
fun FolderCard(
    folder: Folder,
    dao: TaskDao,
    scope: CoroutineScope,
    onClick: () -> Unit,
    count: Int
){

    var menuExpanded by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ){
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ){

            Icon(
                imageVector = Icons.Default.Folder,
                contentDescription = "Folder Icon",
                modifier = Modifier.size(40.dp),
                tint = Color(0xFF2196F3)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = folder.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "$count tasks",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }

            Spacer( modifier = Modifier.weight(1f))

            Box {
                IconButton( onClick = { menuExpanded = true }) {
                    Icon(Icons.Default.MoreVert, "Options")
                }

                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ){
                    DropdownMenuItem(
                        text = { Text("Rename") },
                        onClick = {
                            menuExpanded = false
                            showRenameDialog = true
                        }
                    )

                    DropdownMenuItem(
                        text = { Text("Delete") },
                        onClick = {
                            menuExpanded = false
                            showDeleteDialog = true
                        }
                    )

                }
            }
        }
    }

    if (showRenameDialog) {
        var newName by remember { mutableStateOf(folder.name) }
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title = { Text("Rename Folder") },
            text = {
                OutlinedTextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text("Folder Name") },
                    singleLine = true,
                )
            },

            confirmButton = {
                Button(
                    onClick = {
                        if (newName.isNotBlank()) {
                            scope.launch(Dispatchers.IO) {
                                dao.updateFolder(folder.copy(name = newName))
                            }
                            showRenameDialog = false
                        }
                    }
                ) { Text("Rename") }
            },

            dismissButton = {
                TextButton(onClick = { showRenameDialog = false }) {
                    Text("Cancel")
                }

            }
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Folder") },
            text = { Text("All tasks in this folder will be moved to the genral list.")},
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch(Dispatchers.IO) {
                            dao.setTasksFolderToNull(folder.id)
                            dao.deleteFolder(folder)
                        }
                        showDeleteDialog = false
                    },
                    colors = TextButtonDefaults.textButtonColors(contentColor = Color.Red)
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            }
        )
    }

}