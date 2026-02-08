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
                        onClick = { nav.navigate("folder_detail/${folder.id}") }
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
    onClick: () -> Unit
){

    var menuExpanded by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {  }
    ){

    }

}