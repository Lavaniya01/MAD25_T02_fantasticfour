package np.ict.mad.mad_assignment

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.content.MediaType.Companion.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Folder
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
import androidx.compose.foundation.Image
import coil.compose.rememberAsyncImagePainter
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.material.icons.filled.Category
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskScreen(nav: NavController, initialFolderId: Int? = null) {

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val dao = DatabaseProvider.getDatabase(context).taskDao()
    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    // ---------------- TITLE ----------------

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    // ---------------- PRIORITY ----------------
    val priorities = listOf("Low", "Medium", "High")
    var folderExpanded by remember { mutableStateOf(false) }
    var priorityExpanded by remember { mutableStateOf(false) }
    var selectedPriority by remember { mutableStateOf("Low") }

    // ---------------- CATEGORY ----------------
    var selectedCategory by remember { mutableStateOf(PREDEFINED_CATEGORIES.first()) }
    var categoryExpanded by remember { mutableStateOf(false) }

    // ---------------- IMAGE ----------------
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        imageUri = uri
    }
    // ---------------- TIME ----------------
    // New state
    var selectedHour by remember { mutableStateOf(23) }
    var selectedMinute by remember { mutableStateOf(59) }
    // ---------------- DATE ----------------
    var selectedDate by remember { mutableStateOf("Select date") }
    var dueAtMillis by remember { mutableStateOf(0L) }

    val calendar = Calendar.getInstance()
    val timePicker = TimePickerDialog(
        context,
        { _, hourOfDay, minute ->
            selectedHour = hourOfDay
            selectedMinute = minute

            // Updated dueAtMillis combining selectedDate + selected time
            val cal = Calendar.getInstance()
            val parts = selectedDate.split("/").map { it.toIntOrNull() ?: 0 }
            if (parts.size == 3) {
                val (day, month, year) = parts
                cal.set(Calendar.YEAR, year)
                cal.set(Calendar.MONTH, month - 1) // Calendar months are 0-based
                cal.set(Calendar.DAY_OF_MONTH, day)
                cal.set(Calendar.HOUR_OF_DAY, selectedHour)
                cal.set(Calendar.MINUTE, selectedMinute)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                dueAtMillis = cal.timeInMillis
            }
        },
        selectedHour,
        selectedMinute,
        true // 24-hour format
    )

    val datePicker = DatePickerDialog(
        context,
        { _, year, month, day ->
            selectedDate = "$day/${month + 1}/$year"
            // After picking the date, immediately show time picker
            timePicker.show()
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )



    // --------------- FOLDER ---------------
    val folders by dao.getAllFoldersFlow(uid).collectAsState(initial = emptyList())
    var selectedFolderId by remember { mutableStateOf(initialFolderId) }
    var folderMenuExpanded by remember { mutableStateOf(false) }
    val selectedFolderName = folders.find { it.id == selectedFolderId }?.name ?: "No Folder (General)"

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

            // -------- TITLE --------
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Task Title") },
                modifier = Modifier.fillMaxWidth()
            )

            // -------- DESCRIPTION --------
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                maxLines = 5
            )

            // -------- PRIORITY --------
            Column {
                Text("Priority")

                OutlinedTextField(
                    value = selectedPriority,
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        IconButton(onClick = { priorityExpanded = !priorityExpanded }) {
                            Icon(Icons.Filled.ArrowDropDown, null)
                        }
                    }
                )

                DropdownMenu(expanded = priorityExpanded, onDismissRequest = { priorityExpanded = false }) {
                    priorities.forEach {
                        DropdownMenuItem(
                            text = { Text(it) },
                            onClick = {
                                selectedPriority = it
                                priorityExpanded = false
                            }
                        )
                    }
                }
            }

            // -------- CATEGORY --------
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

                DropdownMenu(
                    expanded = categoryExpanded,
                    onDismissRequest = { categoryExpanded = false }
                ) {
                    PREDEFINED_CATEGORIES.forEach { cat ->
                        DropdownMenuItem(
                            text = { Text(cat) },
                            onClick = {
                                selectedCategory = cat
                                categoryExpanded = false
                            }
                        )
                    }
                }
            }

            // -------- DATE PICKER --------
            Button(
                onClick = { datePicker.show() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(selectedDate)
            }


            // -------- FOLDER --------
            ExposedDropdownMenuBox(
                expanded = folderExpanded,
                onExpandedChange = { folderExpanded = !folderExpanded }
            ) {
                OutlinedTextField(
                    value = selectedFolderName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Select Folder") },
                    leadingIcon = { Icon(Icons.Default.Folder, null) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = folderExpanded) },
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                    modifier = Modifier
                        .menuAnchor() // Important: Anchors the menu to the text field
                        .fillMaxWidth()
                )

                ExposedDropdownMenu(
                    expanded = folderExpanded,
                    onDismissRequest = { folderExpanded = false }
                ) {
                    // Option for no folder
                    DropdownMenuItem(
                        text = { Text("No Folder (General)") },
                        onClick = {
                            selectedFolderId = null
                            folderExpanded = false
                        }
                    )
                    // Options for existing folders
                    folders.forEach { folder ->
                        DropdownMenuItem(
                            text = { Text(folder.name) },
                            onClick = {
                                selectedFolderId = folder.id
                                folderExpanded = false
                            }
                        )
                    }
                }
            }


            Spacer(modifier = Modifier.height(16.dp))

            // -------- IMAGE --------
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(onClick = { imagePickerLauncher.launch("image/*") }) {
                    Icon(Icons.Filled.AddAPhoto, null)
                    Spacer(Modifier.width(8.dp))
                    Text(if (imageUri == null) "Attach Photo" else "Change Photo")
                }

                if (imageUri != null) {
                    Image(
                        painter = rememberAsyncImagePainter(imageUri),
                        contentDescription = null,
                        modifier = Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                }

                Button(onClick = { imagePickerLauncher.launch("image/*")}, modifier = Modifier.padding(vertical = 8.dp)) {
                    Icon(Icons.Default.AddAPhoto, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Update Photo")
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // -------- SAVE BUTTON --------
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
                        val newTask = Task(
                            title = title,
                            description = description,
                            priority = selectedPriority,
                            category = selectedCategory,
                            date = "$selectedDate ${selectedHour.toString().padStart(2,'0')}:${selectedMinute.toString().padStart(2,'0')}",
                            imageUri = imageUri?.toString(),
                            dueAtMillis = dueAtMillis,
                            folderId = selectedFolderId,
                            userId = uid
                        )

                        //actual code
                       /* val newTask = Task(
                            title = title,
                            description = description,
                            priority = selectedPriority,
                            category = selectedCategory,
                            date = selectedDate,
                            imageUri = imageUri?.toString(),
                            dueAtMillis = dueAtMillis
                        )*/

                        scope.launch(Dispatchers.IO) {

                            val dao = DatabaseProvider.getDatabase(context).taskDao()
                            val newId = dao.insertTask(newTask)
                            val firestoreTask = newTask.copy(id = newId.toInt())

                            Firebase.firestore
                                .collection("users")
                                .document(uid)
                                .collection("tasks")
                                .document(newId.toString())
                                .set(firestoreTask)

                            // STEP 5 — reminder
                            ReminderScheduler.scheduleDueReminder(
                                context = context,
                                taskId = newId.toInt(),
                                title = firestoreTask.title,
                                dueAtMillis = firestoreTask.dueAtMillis
                            )
                        }


                        nav.popBackStack()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Task")
            }
        }
    }
}
