package np.ict.mad.mad_assignment

import android.app.DatePickerDialog
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
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.core.net.toUri
import java.util.Calendar
import java.util.Locale
import java.text.SimpleDateFormat

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

    // Priority Dropdown
    val priorities = listOf("Low", "Medium", "High")
    var expanded by remember { mutableStateOf(false) }
    var selectedPriority by remember { mutableStateOf("Low") }

    // Date Picker
    val initialCalender = Calendar.getInstance()
    var selectedDate by remember { mutableStateOf("Select Date") }

    var calendarYear by remember { mutableStateOf(initialCalender.get(Calendar.YEAR)) }
    var calendarMonth by remember { mutableStateOf(initialCalender.get(Calendar.MONTH)) }
    var calendarDay by remember { mutableStateOf(initialCalender.get(Calendar.DAY_OF_MONTH)) }

    val dateFormat = remember { SimpleDateFormat("d/M/yyyy", Locale.getDefault()) }

    // Image Attachment
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            context.contentResolver
                .takePersistableUriPermission(it, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION )
        }
        imageUri = uri
    }

    //Update text fields when task loads from DB (using LaunchedEffect)
    LaunchedEffect(taskId) {
        val t = withContext(Dispatchers.IO) {
            dao.getTaskById(taskId)
        }

        task = t

        t?.let {
            title = it.title
            description = it.description ?: ""
            selectedPriority = it.priority ?: "Low"
            selectedDate = it.date ?: "Select date"
            imageUri = it.imageUri?.let { Uri.parse(it) }

            if (selectedDate != "Select Date" && selectedDate.isNotBlank()) {
                try {
                    val dateObject = dateFormat.parse(selectedDate)
                    dateObject?.let { date ->
                        val cal = Calendar.getInstance()
                        cal.time = date
                        calendarYear = cal.get(Calendar.YEAR)
                        calendarMonth = cal.get(Calendar.MONTH)
                        calendarDay = cal.get(Calendar.DAY_OF_MONTH)
                    }
                } catch (e: Exception) {

                }
            }
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
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),

            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Title Input
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Task Title") },
                modifier = Modifier.fillMaxWidth()
            )

            // Description Input
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth().height(120.dp),
                maxLines = 5
            )

            // Priority Dropdown
            Column{
                Text("Priority", style = MaterialTheme.typography.bodyMedium)

                OutlinedTextField(
                    value = selectedPriority,
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Select Priority") },
                    trailingIcon = {
                        IconButton(onClick = { expanded = !expanded }){
                            Icon(
                                imageVector = Icons.Filled.ArrowDropDown,
                                contentDescription = null
                            )
                        }
                    }
                )

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ){
                    priorities.forEach { p ->
                        DropdownMenuItem(
                            text = { Text(p) },
                            onClick = {
                                selectedPriority = p
                                expanded = false
                            }
                        )
                    }
                }


            }

            // Date Picker
            Button(
                onClick = {
                    DatePickerDialog(
                        context,
                        { _, year, month, day ->
                            selectedDate = "$day/${month + 1}/$year"
                            calendarYear = year
                            calendarMonth = month
                            calendarDay = day

                        },
                        calendarYear,
                        calendarMonth,
                        calendarDay
                    ).show()
                },
                modifier = Modifier.fillMaxWidth()

            ){
                Text(selectedDate)
            }

            // Image Attachment
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

            // Update Button
            Button(
                onClick = {
                    if (title.isNotBlank() && task != null) {
                        //Create a copy of the original task with updated values
                        val updatedTask = task!!.copy(
                            title = title,
                            description = description,
                            priority = selectedPriority,
                            date = selectedDate,
                            imageUri = imageUri?.toString()
                        )
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