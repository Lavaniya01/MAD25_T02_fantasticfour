package np.ict.mad.mad_assignment

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import np.ict.mad.mad_assignment.data.DatabaseProvider
import np.ict.mad.mad_assignment.model.Task
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Image
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.ActivityNavigatorExtras
import kotlinx.coroutines.withContext
import androidx.compose.ui.text.style.TextDecoration
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import android.widget.Toast
import com.google.firebase.FirebaseApp
import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Folder
import coil.compose.AsyncImage
import kotlinx.coroutines.CoroutineScope
import np.ict.mad.mad_assignment.model.Folder
import np.ict.mad.mad_assignment.model.TaskDao


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)

        setContent {

            // ✅ Request notification permission on Android 13+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val permissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
                ) { /* granted/denied - no action needed for now */ }

                LaunchedEffect(Unit) {
                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }

            var isDarkMode by remember { mutableStateOf(false) }

            AppTheme(isDarkTheme = isDarkMode) {
                AppNavigation(
                    isDarkMode = isDarkMode,
                    onToggleTheme = { isDarkMode = !isDarkMode }
                )
            }
        }
    }
}

fun priorityToInt(priority: String?): Int{
    return when (priority) {
        "High" -> 3
        "Medium" -> 2
        "Low" -> 1
        else -> 0
    }
}
@Composable
fun AppNavigation(
    isDarkMode : Boolean ,
    onToggleTheme : () -> Unit
) {
    val nav = rememberNavController()

    NavHost(navController = nav, startDestination = Routes.Start) {
        composable(Routes.Start) { StartingScreen(nav) }
        composable(Routes.Login) { LoginScreen(nav) }
        composable(Routes.Signup) { SignupScreen(nav) }
        composable(Routes.Home) {
            HomeScreen(
                nav = nav,
                isDarkMode = isDarkMode,
                onToggleTheme = onToggleTheme
            )
        }
        composable(Routes.AddTask) {AddTaskScreen(nav) }

        composable("edit_task/{taskId}"){ backStackEntry ->
            val taskId = backStackEntry.arguments?.getString("taskId")?.toIntOrNull()
            if (taskId != null){
                EditTaskScreen(nav, taskId)
            }
        }

        composable("details/{taskId}") { backStackEntry ->
            val id = backStackEntry.arguments?.getString("taskId")?.toInt() ?: 0
            TaskDetailsScreen(nav, id)
        }

        composable(Routes.Settings) {
            SettingsScreen(nav, isDarkMode, onToggleTheme)
        }

        composable(Routes.ChangePassword) {
            ChangePasswordScreen(nav)
        }

        composable(Routes.Folders) {
            FoldersScreen(nav)
        }
    }
}
@Composable
fun AppTheme(isDarkTheme: Boolean, content: @Composable () -> Unit) {
    val colorScheme = if (isDarkTheme) {
        darkColorScheme(
            primary = Color(0xFF90CAF9),
            background = Color(0xFF121212),
            surface = Color(0xFF1E1E1E),
            onPrimary = Color.Black,
            onBackground = Color.White,
            onSurface = Color.White
        )
    } else {
        lightColorScheme(
            primary = Color(0xFF1565C0),
            background = Color.White,
            surface = Color.White,
            onPrimary = Color.White,
            onBackground = Color.Black,
            onSurface = Color.Black
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}

@Composable
fun StartingScreen(nav: NavHostController) {

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5)),
        contentAlignment = Alignment.Center
    ) {

        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(28.dp)
        ) {

            Text(
                text = "SmartTasks",
                style = TextStyle(
                    fontSize = 46.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 2.sp,
                    color = Color(0xFF2A2A2A)
                )
            )

            Text(
                text = "Organize your day.",
                style = TextStyle(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.DarkGray
                ),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    nav.navigate(Routes.Login) {
                        popUpTo(Routes.Start) { inclusive = true }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(55.dp),
                colors = ButtonDefaults.buttonColors(Color(0xFF4CAF50)),
                shape = MaterialTheme.shapes.medium
            ) {
                Text(
                    text = "Login",
                    fontSize = 18.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun LoginScreen(nav: NavHostController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Login", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(20.dp))

        TextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            val auth = FirebaseAuth.getInstance()
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        nav.navigate(Routes.Home) {
                            popUpTo(Routes.Login) { inclusive = true }
                        }
                    } else {
                        errorMessage = task.exception?.localizedMessage
                    }
                }
        }) {
            Text("Login")
        }


        errorMessage?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(it, color = Color.Red)
        }
        Text(
            text = "Don't have an account? Sign Up",
            modifier = Modifier.clickable { nav.navigate(Routes.Signup) },
            color = Color.Blue
        )
    }
}

@Composable
fun SignupScreen(nav: NavHostController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        TextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") }
        )

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                FirebaseAuth.getInstance()
                    .createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(context, "Signup Successful!", Toast.LENGTH_SHORT).show()
                            nav.navigate(Routes.Home) {
                                popUpTo(Routes.Signup) { inclusive = true }
                            }
                        } else {
                            Toast.makeText(context, task.exception?.message ?: "Signup Failed", Toast.LENGTH_SHORT).show()
                        }
                    }
            },
            modifier = Modifier.fillMaxWidth(0.6f)
        ) {
            Text("Sign Up")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Already have an account? Log in",
            modifier = Modifier.clickable { nav.navigate(Routes.Login) }
        )
    }
}
@Composable
fun HomeScreen(
    nav: NavHostController,
    isDarkMode: Boolean,
    onToggleTheme: () -> Unit
) {
    // Rest of HomeScreen UI
    val context = LocalContext.current
    val dao = DatabaseProvider.getDatabase(context).taskDao()
    val tasks by dao.getAllTasksFlow().collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()

    var showFolderDialog by remember { mutableStateOf(false) }
    var newFolderName by remember { mutableStateOf("") }
    val folders by dao.getAllFoldersFlow().collectAsState(initial = emptyList())

    val sortedTasks = remember(tasks) {
        tasks.sortedWith (
            compareByDescending<Task> { priorityToInt(it.priority) }.thenBy { it.id }
        )
    }
    Scaffold(
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(
                title = { Text("SmartTasks") },
                actions = {
                    IconButton(onClick = { showFolderDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.CreateNewFolder,
                            contentDescription = "New Folder"
                        )
                    }

                    IconButton( onClick = { nav.navigate(Routes.Settings) }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        },

        floatingActionButton = {
            FloatingActionButton(
                onClick = { nav.navigate(Routes.AddTask) },
                containerColor = Color(0xFF4CAF50)
            ) {
                Text("+", fontSize = 30.sp, color = Color.White)
            }
        }
    ) { padding ->
        if (showFolderDialog) {
            AlertDialog(
                onDismissRequest = { showFolderDialog = false },
                title = { Text("Create New Folder") },
                text = {
                    OutlinedTextField(
                        value = newFolderName,
                        onValueChange = { newFolderName = it },
                        label = { Text("Folder Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (newFolderName.isNotBlank()) {
                                scope.launch(Dispatchers.IO) {
                                    dao.insertFolder(Folder(name = newFolderName))
                                }
                                newFolderName = ""
                                showFolderDialog = false
                            }
                        }
                    ) { Text("Create") }
                },
                dismissButton = {
                    TextButton(onClick = { showFolderDialog = false }) { Text("Cancel") }
                }
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f))
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                Text(
                    text = "SmartTasks",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (sortedTasks.isEmpty())
                        "No tasks yet"
                    else
                        "${sortedTasks.size} task${if (sortedTasks.size == 1) "" else "s"} total",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            if (sortedTasks.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "No tasks yet!",
                            style = TextStyle(fontSize = 18.sp, color = Color.Gray)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tap + to add your first task",
                            style = TextStyle(fontSize = 14.sp, color = Color.Gray)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(sortedTasks) { task ->
                        TaskCard(
                            task = task,
                            dao = dao,
                            scope = scope,
                            onClick = { nav.navigate("details/${task.id}") },
                            onEdit = { nav.navigate("edit_task/${task.id}") },
                            onDelete = {
                                scope.launch(Dispatchers.IO) {
                                    dao.deleteTask(task)
                                }
                            }
                        )
                    }
                }
            }
        }
        Box(modifier = Modifier.fillMaxSize()) {
            Button(
                onClick = onToggleTheme,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
            ) {
                Text("Toggle Theme")

            }
        }
    }
}

// ---------------------------------------------------
// TASK CARD
// ---------------------------------------------------

@Composable
fun TaskCard(
    task: Task,
    dao: TaskDao,
    scope: CoroutineScope,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {

    val isDone = task.isDone

    val priorityColorBase = when (task.priority) {
        "High" -> Color(0xFFFF6B6B)
        "Medium" -> Color(0xFFFFC46B)
        "Low" -> Color(0xFF6BCB77)
        else -> Color(0xFFB0BEC5)
    }

    // If done, use grey to visually separate from active tasks
    val priorityColor = if (isDone) Color(0xFF9E9E9E) else priorityColorBase

    val titleColor = if (isDone) Color.Gray else Color.Unspecified
    val titleDecoration = if (isDone) TextDecoration.LineThrough else TextDecoration.None
    val descriptionColor = if (isDone) Color.LightGray else Color.DarkGray

    var expanded by remember { mutableStateOf(false) }
    var showMoveDialog by remember { mutableStateOf(false) }
    val folders by dao.getAllFoldersFlow().collectAsState(initial = emptyList())

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min) // so the bar can fill height
                .padding(start = 12.dp, top = 12.dp, bottom = 12.dp, end = 4.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Priority bar (or status bar if done)
            Box(
                modifier = Modifier
                    .padding(vertical = 4.dp)
                    .width(4.dp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(20.dp))
                    .background(priorityColor)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Title
                Text(
                    text = task.title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = titleColor,
                    textDecoration = titleDecoration
                )

                // Description
                if (task.description != null && task.description.isNotEmpty()) {
                    Text(
                        text = task.description,
                        fontSize = 16.sp,
                        color = descriptionColor,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Column (modifier = Modifier.weight(1f)){
                    Text(text = task.title, fontSize = 20.sp, fontWeight = FontWeight.Bold)

                    if (!task.imageUri.isNullOrEmpty()) {
                        AsyncImage(
                            model = task.imageUri,
                            contentDescription = "Task Image",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .padding(vertical = 8.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // ✅ Status / priority chip
                        if (isDone) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(Color(0xFFEEEEEE))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Done",
                                    fontSize = 13.sp,
                                    color = Color(0xFF616161),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        } else {
                            task.priority?.let {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(50))
                                        .background(priorityColor.copy(alpha = 0.18f))
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "Priority: $it",
                                        fontSize = 13.sp,
                                        color = priorityColor,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }

                        // Due date with icon (unchanged)
                        task.date?.let {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CalendarMonth,
                                    contentDescription = "Due Date",
                                    modifier = Modifier.size(16.dp),
                                    tint = Color.Gray
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Due Date: $it",
                                    fontSize = 13.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }
            }

            Box {
                IconButton(onClick = { expanded = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Options")
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Edit") },
                        onClick = {
                            expanded = false
                            onEdit()
                        }
                    )

                    // Move to Folder logic
                    DropdownMenuItem(
                        text = { Text("Move to Folder") },
                        onClick = {
                            expanded = false
                            showMoveDialog = true
                        }
                    )

                    DropdownMenuItem(
                        text = { Text("Delete", color = Color.Red) },
                        onClick = {
                            expanded = false
                            onDelete()
                        }
                    )
                }
            }
        }
    }

    if (showMoveDialog) {
        AlertDialog(
            onDismissRequest = { showMoveDialog = false },
            title = { Text("Move to Folder") },
            text = {
                LazyColumn {
                    // Option to remove from all folders
                    item {
                        ListItem(
                            headlineContent = { Text("No Folder (General)") },
                            modifier = Modifier.clickable {
                                scope.launch(Dispatchers.IO) {
                                    dao.updateTask(task.copy(folderId = null))
                                }
                                showMoveDialog = false
                            }
                        )
                    }
                    // List of existing folders
                    items(folders) { folder ->
                        ListItem(
                            headlineContent = { Text(folder.name) },
                            leadingContent = { Icon(Icons.Default.Folder, null) },
                            modifier = Modifier.clickable {
                                scope.launch(Dispatchers.IO) {
                                    dao.updateTask(task.copy(folderId = folder.id))
                                }
                                showMoveDialog = false
                            }
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showMoveDialog = false }) { Text("Cancel") }
            }
        )
    }
}


