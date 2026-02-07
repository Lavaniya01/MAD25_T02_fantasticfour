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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoldersScreen(nav: NavHostController){
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val dao = DatabaseProvider.getDatabase(context).taskDao()

    val folders by dao.getAllFoldersFlow().collectAsState(initial = emptyList())

    var showCreateDialog by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf<Folder?>(null) }
    var folderNameInput by remember { mutableStateOf("") }

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
            FloatingActionButton(onClick = {
                folderNameInput = "" // Clear input before opening
                showCreateDialog = true
            }) {
                Icon(Icons.Default.CreateNewFolder, "Create Folder")
            }
        }

    ){ padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            if (folders.isEmpty()) {
                Box(Modifier.fillMaxSize().padding(top = 50.dp), contentAlignment = Alignment.Center) {
                    Text("No folders found. Tap + to create one.", color = Color.Gray)
                }
            } else {
                folders.forEach { folder ->
                    var menuExpanded by remember { mutableStateOf(false) }

                    ListItem(
                        headlineContent = {
                            // Using a forced black/white color to ensure it isn't "invisible"
                            Text(
                                text = folder.name,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        },
                        leadingContent = {
                            Icon(Icons.Default.Folder, null, tint = Color(0xFFFFA000))
                        },
                        trailingContent = {
                            Box {
                                IconButton(onClick = { menuExpanded = true }) {
                                    Icon(Icons.Default.MoreVert, "Options")
                                }
                                DropdownMenu(
                                    expanded = menuExpanded,
                                    onDismissRequest = { menuExpanded = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Rename") },
                                        onClick = {
                                            menuExpanded = false
                                            folderNameInput = folder.name
                                            showRenameDialog = folder
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Delete", color = Color.Red) },
                                        onClick = {
                                            menuExpanded = false
                                            scope.launch(Dispatchers.IO) {
                                                dao.deleteFolder(folder)
                                            }
                                        }
                                    )
                                }
                            }
                        },
                        modifier = Modifier.clickable {
                            nav.navigate("folder_detail/${folder.id}")
                        }
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                }
            }
        }

        // --- 1. CREATE DIALOG LOGIC ---
        if (showCreateDialog) {
            AlertDialog(
                onDismissRequest = { showCreateDialog = false },
                title = { Text("Create New Folder") },
                text = {
                    OutlinedTextField(
                        value = folderNameInput,
                        onValueChange = { folderNameInput = it },
                        label = { Text("Enter Folder Name") },
                        singleLine = true
                    )
                },
                confirmButton = {
                    Button(onClick = {
                        if (folderNameInput.isNotBlank()) {
                            val newName = folderNameInput // Capture the string
                            scope.launch(Dispatchers.IO) {
                                dao.insertFolder(Folder(name = newName))
                            }
                            folderNameInput = ""
                            showCreateDialog = false
                        }
                    }) { Text("Create") }
                },
                dismissButton = {
                    TextButton(onClick = { showCreateDialog = false }) { Text("Cancel") }
                }
            )
        }

        // Rename Folder
        if (showRenameDialog != null) {
            AlertDialog(
                onDismissRequest = { showRenameDialog = null },
                title = { Text("Rename Folder") },
                text = {
                    OutlinedTextField(
                        value = folderNameInput,
                        onValueChange = { folderNameInput = it },
                        label = { Text("New Name") }
                    )
                },
                confirmButton = {
                    Button(onClick = {
                        val folderToRename = showRenameDialog
                        if (folderToRename != null && folderNameInput.isNotBlank()) {
                            scope.launch(Dispatchers.IO) {
                                // Update the folder in DB. Task cards will update automatically.
                                dao.insertFolder(folderToRename.copy(name = folderNameInput))
                            }
                            folderNameInput = ""
                            showRenameDialog = null
                        }
                    }) { Text("Update") }
                },
                dismissButton = {
                    TextButton(onClick = { showRenameDialog = null }) { Text("Cancel") }
                }
            )
        }

        // --- FOLDER LIST ---
        LazyColumn(modifier = Modifier.padding(padding).fillMaxSize()) {
            items(folders, key = { it.id }) { folder ->
                var menuExpanded by remember { mutableStateOf(false) }

                ListItem(
                    headlineContent = {
                        // We use a Column to ensure the text has enough vertical space
                        Column {
                            Text(
                                text = folder.name,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface, // Ensures visibility
                                maxLines = 1
                            )
                        }
                    },
                    leadingContent = {
                        Icon(Icons.Default.Folder, null, tint = MaterialTheme.colorScheme.primary)
                    },
                    trailingContent = {
                        Box {
                            IconButton(onClick = { menuExpanded = true }) {
                                Icon(Icons.Default.MoreVert, contentDescription = "Options")
                            }
                            DropdownMenu(
                                expanded = menuExpanded,
                                onDismissRequest = { menuExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Rename") },
                                    onClick = {
                                        menuExpanded = false
                                        folderNameInput = folder.name
                                        showRenameDialog = folder
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Delete", color = Color.Red) },
                                    onClick = {
                                        menuExpanded = false
                                        scope.launch(Dispatchers.IO) { dao.deleteFolder(folder) }
                                    }
                                )
                            }
                        }
                    },
                    modifier = Modifier.clickable { nav.navigate("folder_detail/${folder.id}") }
                )
                HorizontalDivider()
            }
        }
    }
}