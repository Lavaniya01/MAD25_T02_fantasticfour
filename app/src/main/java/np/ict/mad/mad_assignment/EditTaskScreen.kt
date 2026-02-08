package np.ict.mad.mad_assignment

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.net.Uri
import android.content.Intent
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import np.ict.mad.mad_assignment.data.DatabaseProvider
import np.ict.mad.mad_assignment.model.Task
import java.util.Calendar
import java.util.Locale
import java.text.SimpleDateFormat
import androidx.compose.material.icons.filled.Category
import com.google.firebase.auth.FirebaseAuth

// ---------------------------------------------------
// EDIT TASK SCREEN
// ---------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTaskScreen(navController: NavHostController, taskId: Int) {
    val context = LocalContext.current
    val dao = DatabaseProvider.getDatabase(context).taskDao()
    val scope = rememberCoroutineScope()

    // Load existing task
    var task by remember { mutableStateOf<Task?>(null) }

    // Text field states
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    // Priority
    val priorities = listOf("Low", "Medium", "High")
    var expanded by remember { mutableStateOf(false) }
    var selectedPriority by remember { mutableStateOf("Low") }

    // Category
    var selectedCategory by remember { mutableStateOf(PREDEFINED_CATEGORIES.first()) }
    var categoryExpanded by remember { mutableStateOf(false) }

    // Date & Time
    val initialCalendar = Calendar.getInstance()
    var selectedDate by remember { mutableStateOf("Select Date") }
    var selectedTime by remember { mutableStateOf("Select Time") }

    var calendarYear by remember { mutableStateOf(initialCalendar.get(Calendar.YEAR)) }
    var calendarMonth by remember { mutableStateOf(initialCalendar.get(Calendar.MONTH)) }
    var calendarDay by remember { mutableStateOf(initialCalendar.get(Calendar.DAY_OF_MONTH)) }
    var calendarHour by remember { mutableStateOf(initialCalendar.get(Calendar.HOUR_OF_DAY)) }
    var calendarMinute by remember { mutableStateOf(initialCalendar.get(Calendar.MINUTE)) }

    val dateFormat = remember { SimpleDateFormat("d/M/yyyy", Locale.getDefault()) }
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }

    // Epoch millis
    var dueAtMillis by remember { mutableStateOf(0L) }

    // Image
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            context.contentResolver.takePersistableUriPermission(
                it,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        }
        imageUri = uri
    }

    // Load task once
    LaunchedEffect(taskId) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@LaunchedEffect
        val t = withContext(Dispatchers.IO) { dao.getTaskById(taskId, uid) }
        task = t

        t?.let {
            title = it.title
            description = it.description ?: ""
            selectedPriority = it.priority ?: "Low"
            selectedCategory = it.category ?: PREDEFINED_CATEGORIES.first()
            selectedDate = it.date ?: "Select Date"
            imageUri = it.imageUri?.let { s -> Uri.parse(s) }
            dueAtMillis = it.dueAtMillis

            if (dueAtMillis > 0) {
                val cal = Calendar.getInstance().apply { timeInMillis = dueAtMillis }
                calendarYear = cal.get(Calendar.YEAR)
                calendarMonth = cal.get(Calendar.MONTH)
                calendarDay = cal.get(Calendar.DAY_OF_MONTH)
                calendarHour = cal.get(Calendar.HOUR_OF_DAY)
                calendarMinute = cal.get(Calendar.MINUTE)
                selectedDate = dateFormat.format(cal.time)
                selectedTime = timeFormat.format(cal.time)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (task == null) "Loading..." else "Edit Task") },
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
            // Title
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Task Title") },
                modifier = Modifier.fillMaxWidth()
            )

            // Description
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                maxLines = 5
            )

            // Priority Dropdown
            Column {
                Text("Priority", style = MaterialTheme.typography.bodyMedium)
                OutlinedTextField(
                    value = selectedPriority,
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        IconButton(onClick = { expanded = !expanded }) {
                            Icon(Icons.Filled.ArrowDropDown, contentDescription = null)
                        }
                    }
                )
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    priorities.forEach { p ->
                        DropdownMenuItem(text = { Text(p) }, onClick = {
                            selectedPriority = p
                            expanded = false
                        })
                    }
                }
            }

            // Category Dropdown
            Column {
                Text("Category", style = MaterialTheme.typography.bodyMedium)
                OutlinedTextField(
                    value = selectedCategory,
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        IconButton(onClick = { categoryExpanded = !categoryExpanded }) {
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                    }
                )
                DropdownMenu(expanded = categoryExpanded, onDismissRequest = { categoryExpanded = false }) {
                    PREDEFINED_CATEGORIES.forEach { cat ->
                        DropdownMenuItem(text = { Text(cat) }, onClick = {
                            selectedCategory = cat
                            categoryExpanded = false
                        })
                    }
                }
            }

            // Date Picker
            Button(onClick = {
                DatePickerDialog(
                    context,
                    { _, year, month, day ->
                        calendarYear = year
                        calendarMonth = month
                        calendarDay = day
                        selectedDate = "$day/${month + 1}/$year"
                    },
                    calendarYear,
                    calendarMonth,
                    calendarDay
                ).show()
            }, modifier = Modifier.fillMaxWidth()) {
                Text(selectedDate)
            }

            // Time Picker
            Button(onClick = {
                TimePickerDialog(
                    context,
                    { _, hour, minute ->
                        calendarHour = hour
                        calendarMinute = minute
                        selectedTime = String.format("%02d:%02d", hour, minute)
                    },
                    calendarHour,
                    calendarMinute,
                    true
                ).show()
            }, modifier = Modifier.fillMaxWidth()) {
                Text(selectedTime)
            }

            // Image Attachment
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(onClick = { imagePickerLauncher.launch("image/*") }) {
                    Icon(Icons.Filled.AddAPhoto, contentDescription = "Attach Image")
                    Spacer(Modifier.width(8.dp))
                    Text(if (imageUri == null) "Attach Photo" else "Change Photo")
                }
                if (imageUri != null) {
                    Text("Photo Attached", color = MaterialTheme.colorScheme.primary)
                }
            }

            // Update Button
            Button(
                onClick = {
                    val currentTask = task ?: return@Button
                    if (title.isBlank()) return@Button

                    // Combine date & time into dueAtMillis
                    val cal = Calendar.getInstance()
                    cal.set(Calendar.YEAR, calendarYear)
                    cal.set(Calendar.MONTH, calendarMonth)
                    cal.set(Calendar.DAY_OF_MONTH, calendarDay)
                    cal.set(Calendar.HOUR_OF_DAY, calendarHour)
                    cal.set(Calendar.MINUTE, calendarMinute)
                    cal.set(Calendar.SECOND, 0)
                    cal.set(Calendar.MILLISECOND, 0)
                    dueAtMillis = cal.timeInMillis

                    val updatedTask = currentTask.copy(
                        title = title,
                        description = description,
                        priority = selectedPriority,
                        category = selectedCategory,
                        date = selectedDate,
                        imageUri = imageUri?.toString(),
                        dueAtMillis = dueAtMillis
                    )

                    scope.launch(Dispatchers.IO) {
                        dao.updateTask(updatedTask)

                        // 🔔 Schedule reminder
                        ReminderScheduler.scheduleDueReminder(
                            context = context,
                            taskId = updatedTask.id,
                            title = updatedTask.title,
                            dueAtMillis = updatedTask.dueAtMillis
                        )
                    }

                    navController.popBackStack()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(Color(0xFF2196F3))
            ) {
                Text("Update Task", fontSize = 18.sp, color = Color.White)
            }
        }
    }
}