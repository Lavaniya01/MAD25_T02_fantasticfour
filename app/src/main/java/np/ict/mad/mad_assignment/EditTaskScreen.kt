package np.ict.mad.mad_assignment

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import np.ict.mad.mad_assignment.data.DatabaseProvider
import np.ict.mad.mad_assignment.model.Task

// ---------------------------------------------------
// EDIT TASK SCREEN
// ---------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTaskScreen(navController: NavHostController, taskId: Int) {
    val context = LocalContext.current
    val dao = DatabaseProvider.getDatabase(context).taskDao()
    val scope = rememberCoroutineScope()

    // Load existing data
    var task by remember { mutableStateOf<Task?>(null) }
    var isLoaded by remember { mutableStateOf(false) }

    // Load task once
    LaunchedEffect(taskId) {
        val t = withContext(Dispatchers.IO) {
            dao.getTaskById(taskId)
        }
        task = t
        isLoaded = true
    }

    // State for the text fields
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    // Image Attachment
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
    }

    //Update text fields when task loads from DB (using LaunchedEffect)
    LaunchedEffect(task) {
        task?.let {
            title = it.title
            description = it.description ?: ""
            imageUri = it.imageUri?.let { Uri.parse(it) }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Task: ${task?.title ?: ""}") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Task Title") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth().height(120.dp),
                maxLines = 5
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = { imagePickerLauncher.launch("image/*")}
                ) {
                    Icon(Icons.Filled.AddAPhoto, contentDescription = "Attach Image")
                    Spacer(Modifier.width(8.dp))
                    Text(
                        if (imageUri == null){
                            "Attach Photo"
                        } else {
                            "Change Photo"
                        }
                    )
                }

                if (imageUri != null) {
                    Text("Photo Attached", color = MaterialTheme.colorScheme.primary)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (title.isNotBlank() && task != null) {
                        //Create a copy of the original task with updated values
                        val updatedTask = task!!.copy(title = title, description = description)
                        scope.launch(Dispatchers.IO) {
                            dao.updateTask(updatedTask)
                        }
                        navController.popBackStack()
                    }
                },

                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(Color(0xFF2196F3))
            ) {
                Text(
                    "Update Task",
                    fontSize = 18.sp,
                    color = Color.White
                )
            }
        }

    }
}