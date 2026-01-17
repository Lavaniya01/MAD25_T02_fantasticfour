package np.ict.mad.mad_assignment

import android.app.DatePickerDialog
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import np.ict.mad.mad_assignment.data.DatabaseProvider
import np.ict.mad.mad_assignment.model.Task
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskScreen(nav: NavController) {

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    // ---------------- PRIORITY DROPDOWN ----------------
    val priorities = listOf("Low", "Medium", "High")
    var expanded by remember { mutableStateOf(false) }
    var selectedPriority by remember { mutableStateOf("Low") }

    // ---------------- IMAGE ATTACHMENT -----------------
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        // If you keep GetContent, persistable permission may fail for some providers.
        // For demo/testing this is fine; ignore SecurityException if it happens.
        uri?.let {
            try {
                context.contentResolver.takePersistableUriPermission(
                    it,
                    android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: SecurityException) { }
        }
        imageUri = uri
    }

    // ---------------- DATE PICKER ----------------------
    var selectedDate by remember { mutableStateOf("Select date") }

    // Real due date storage (epoch millis). In demo mode we will override this.
    var dueAtMillis by remember { mutableStateOf(0L) }

    val calendar = Calendar.getInstance()
    val datePicker = DatePickerDialog(
        context,
        { _, year, month, day ->
            selectedDate = "$day/${month + 1}/$year"

            // Normal behaviour: due at end of selected day (23:59)
            val cal = Calendar.getInstance()
            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.MONTH, month)
            cal.set(Calendar.DAY_OF_MONTH, day)
            cal.set(Calendar.HOUR_OF_DAY, 23)
            cal.set(Calendar.MINUTE, 59)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            dueAtMillis = cal.timeInMillis
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    // ✅ DEMO MODE toggle: set true to make notifications trigger quickly
    val DEMO_MODE = true

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Task") },
                navigationIcon = {
                    IconButton(onClick = { nav.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = null
                        )
                    }
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            // ---------------- TITLE -------------------
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Task Title") },
                modifier = Modifier.fillMaxWidth()
            )

            // ---------------- DESCRIPTION --------------
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                maxLines = 5
            )

            // ---------------- PRIORITY DROPDOWN --------
            Column {
                Text("Priority", style = MaterialTheme.typography.bodyMedium)

                OutlinedTextField(
                    value = selectedPriority,
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Select Priority") },
                    trailingIcon = {
                        IconButton(onClick = { expanded = !expanded }) {
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
                ) {
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

            // ---------------- DATE PICKER BUTTON --------
            Button(
                onClick = { datePicker.show() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(selectedDate)
            }

            // --------------- IMAGE ATTACHMENT BUTTON ----
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = { imagePickerLauncher.launch("image/*") }
                ) {
                    Icon(Icons.Filled.AddAPhoto, contentDescription = "Attach Image")
                    Spacer(Modifier.width(8.dp))
                    Text(if (imageUri == null) "Attach Photo" else "Change Photo")
                }

                if (imageUri != null) {
                    Text("Photo Attached", color = MaterialTheme.colorScheme.primary)
                }
            }

            // ---------------- SAVE BUTTON ----------------
            Button(
                onClick = {
                    if (title.isNotBlank()) {

                        // 🔴 DEMO MODE: force due time to 2 minutes from now
                        val finalDueAtMillis =
                            if (DEMO_MODE) System.currentTimeMillis() + 2 * 60_000L
                            else dueAtMillis

                        val finalDateLabel =
                            if (DEMO_MODE) "Demo: due in 2 min" else selectedDate

                        val newTask = Task(
                            title = title,
                            description = description,
                            priority = selectedPriority,
                            date = finalDateLabel,
                            imageUri = imageUri?.toString(),
                            dueAtMillis = finalDueAtMillis
                        )

                        scope.launch(Dispatchers.IO) {
                            val dao = DatabaseProvider.getDatabase(context).taskDao()
                            val newId = dao.insertTask(newTask) // must return Long in TaskDao

                            // 🔔 Schedule reminder (Demo: 1 min before)
                            ReminderScheduler.scheduleDueReminder(
                                context = context,
                                taskId = newId.toInt(),
                                title = newTask.title,
                                dueAtMillis = newTask.dueAtMillis,
                                remindBeforeMillis = if (DEMO_MODE) 60_000L else 60 * 60 * 1000L
                            )
                        }

                        nav.popBackStack()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.primary)
            ) {
                Text("Save Task")
            }
        }
    }
}
