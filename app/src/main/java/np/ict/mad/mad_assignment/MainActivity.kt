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
import android.content.Context
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import np.ict.mad.mad_assignment.model.Folder
import np.ict.mad.mad_assignment.model.TaskDao
import androidx.core.content.edit
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Clear
//hello


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

val PREDEFINED_CATEGORIES = listOf(
    "School",
    "Work",
    "Personal",
    "Health",
    "Finance",
    "Others"
)
private const val PREFS_NAME = "category_prefs"
private const val KEY_PREFIX = "cat_expanded_"

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

        composable("${Routes.AddTask}?folderId={folderId}") { backStackEntry ->
            val folderId = backStackEntry.arguments?.getString("folderId")?.toIntOrNull()
            AddTaskScreen(nav, folderId)
        }

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

        composable("folder_detail/{folderId}") { backStackEntry ->
            val folderId = backStackEntry.arguments?.getString("folderId")?.toInt() ?: 0
            FolderDetailScreen(nav, folderId)
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
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )

        Spacer(modifier = Modifier.height(12.dp))

        PasswordField(
            value = password,
            onValueChange = { password = it },
            label = "Password",
            modifier = Modifier.fillMaxWidth()
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
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )

        Spacer(modifier = Modifier.height(16.dp))

        PasswordField(
            value = password,
            onValueChange = { password = it },
            label = "Password",
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                FirebaseAuth.getInstance()
                    .createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            // 1. SAVE TO SHARED PREFERENCES (for your Change Password screen)
                            val sharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                            sharedPreferences.edit {
                                putString("password", password)
                            }

                            // 2. SHOW SUCCESS MESSAGE
                            android.widget.Toast.makeText(
                                context,
                                "Signup Successful! Welcome aboard.",
                                android.widget.Toast.LENGTH_LONG
                            ).show()

                            // 3. NAVIGATE TO HOME
                            nav.navigate(Routes.Home)

                            // Show error if signup fails
                            val error = task.exception?.message ?: "Signup failed"
                            android.widget.Toast.makeText(context, error, android.widget.Toast.LENGTH_SHORT).show()
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
    val context = LocalContext.current
    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
    val dao = DatabaseProvider.getDatabase(context).taskDao()
    val tasks by dao.getAllTasksFlow(uid).collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    var showCreateDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var folderNameInput by remember { mutableStateOf("") }
    val folders by dao.getAllFoldersFlow().collectAsState(initial = emptyList())

    val sortedTasks = remember(tasks) {
        tasks.sortedWith (
            compareByDescending<Task> { priorityToInt(it.priority) }.thenBy { it.id }
        )
    }

    // Search-filtered tasks
    val filteredTasks = remember(sortedTasks, searchQuery) {
        val q = searchQuery.trim()
        if (q.isBlank()) {
            sortedTasks
        } else {
            sortedTasks.filter { task ->
                task.title.contains(q, ignoreCase = true) ||
                        (task.description?.contains(q, ignoreCase = true) == true)
            }
        }
    }

    val activeTasks = remember(filteredTasks) { filteredTasks.filter { !it.isDone } }
    val completedTasks = remember(filteredTasks) { filteredTasks.filter { it.isDone } }
    val COMPLETED_CATEGORY = "Completed"

    // Group tasks into predefined categories (anything unknown -> Others)
    val tasksByCategory = remember(activeTasks) {
        val map = PREDEFINED_CATEGORIES.associateWith { mutableListOf<Task>() }.toMutableMap()

        activeTasks.forEach { t ->
            val raw = t.category?.trim()
            val key = if (raw != null && raw in PREDEFINED_CATEGORIES) raw else "Others"
            map.getValue(key).add(t)
        }
        map
    }

    val prefs = remember {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    // Expanded state per category (default = true)
    val expandedMap = remember {
        mutableStateMapOf<String, Boolean>().apply {
            (PREDEFINED_CATEGORIES + COMPLETED_CATEGORY).forEach { cat ->
                val saved = prefs.getBoolean(KEY_PREFIX + cat, true)
                this[cat] = saved
            }
        }
    }


    Scaffold(
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(
                title = { Text(
                    "SmartTasks",
                    fontWeight = FontWeight.Bold,
                    fontSize = 40.sp,
                    fontFamily = FontFamily.Cursive
                )
                        },
                actions = {
                    // New Folder
                    IconButton(onClick = {
                        folderNameInput = ""
                        showCreateDialog = true
                    }) {
                        Icon(
                            imageVector = Icons.Default.CreateNewFolder,
                            contentDescription = "New Folder"
                        )
                    }

                    // Navigation to Folders Screen
                    IconButton(onClick = { nav.navigate(Routes.Folders) }) {
                        Icon(
                            imageVector = Icons.Default.Folder,
                            contentDescription = "View Folders"
                        )
                    }

                    // Navigation to Settings
                    IconButton(onClick = { nav.navigate(Routes.Settings) }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings"
                        )
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f))
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Search Bar UI
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("Search tasks") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                Text(
                    text = if (searchQuery.isBlank()) {
                        if (sortedTasks.isEmpty()) "No tasks yet"
                        else "${sortedTasks.size} task${if (sortedTasks.size == 1) "" else "s"} total"
                    } else {
                        if (filteredTasks.isEmpty()) "No results for \"${searchQuery.trim()}\""
                        else "${filteredTasks.size} result${if (filteredTasks.size == 1) "" else "s"} found"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
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
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (folderNameInput.isNotBlank()) {
                                    scope.launch(Dispatchers.IO) {
                                        dao.insertFolder(Folder(name = folderNameInput))
                                        withContext(Dispatchers.Main) {
                                            Toast.makeText(
                                                context,
                                                "Folder created: $folderNameInput",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                    showCreateDialog = false
                                }
                            }
                        ) { Text("Create") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showCreateDialog = false }) { Text("Cancel") }
                    }
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            val isSearching = searchQuery.isNotBlank()

            if (filteredTasks.isEmpty()) {
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
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    PREDEFINED_CATEGORIES.forEach { category ->
                        val catTasks = tasksByCategory[category].orEmpty()

                        if (catTasks.isNotEmpty()) {

                            // Header
                            item {
                                val catExpanded =
                                    if (isSearching) true else (expandedMap[category] == true)

                                CategorySectionHeader(
                                    title = category,
                                    count = catTasks.size,
                                    expanded = catExpanded,
                                    onToggle = {
                                        if (!isSearching) {
                                            val newValue = !(expandedMap[category] == true)
                                            expandedMap[category] = newValue
                                            prefs.edit()
                                                .putBoolean(KEY_PREFIX + category, newValue)
                                                .apply()
                                        }

                                    }
                                )
                            }

                            // Tasks
                            item {
                                val catExpanded =
                                    if (isSearching) true else (expandedMap[category] == true)

                                AnimatedVisibility(
                                    visible = catExpanded,
                                    enter = expandVertically() + fadeIn(),
                                    exit = shrinkVertically() + fadeOut()
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        catTasks.forEach { task ->
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
                        }
                    }
                    // Completed section always at bottom (only if there are completed tasks)
                    if (completedTasks.isNotEmpty()) {

                        item {
                            val completedExpanded =
                                if (isSearching) true else (expandedMap[COMPLETED_CATEGORY] == true)

                            CategorySectionHeader(
                                title = COMPLETED_CATEGORY,
                                count = completedTasks.size,
                                expanded = completedExpanded,
                                onToggle = {
                                    if (!isSearching) {
                                        val newValue = !(expandedMap[COMPLETED_CATEGORY] == true)
                                        expandedMap[COMPLETED_CATEGORY] = newValue
                                        prefs.edit()
                                            .putBoolean(KEY_PREFIX + COMPLETED_CATEGORY, newValue)
                                            .apply()
                                    }
                                },
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                iconTint = Color(0xFF3949AB)
                            )
                        }

                        item {
                            val completedExpanded =
                                if (isSearching) true else (expandedMap[COMPLETED_CATEGORY] == true)

                            AnimatedVisibility(
                                visible = completedExpanded,
                                enter = expandVertically() + fadeIn(),
                                exit = shrinkVertically() + fadeOut()
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    completedTasks.forEach { task ->
                                        TaskCard(
                                            task = task,
                                            dao = dao,
                                            scope = scope,
                                            onClick = { nav.navigate("details/${task.id}") },
                                            onEdit = { nav.navigate("edit_task/${task.id}") },
                                            onDelete = {
                                                scope.launch(Dispatchers.IO) { dao.deleteTask(task) }
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CategorySectionHeader(
    title: String,
    count: Int,
    expanded: Boolean,
    onToggle: () -> Unit,
    containerColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    iconTint: Color = MaterialTheme.colorScheme.primary
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
    Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Folder,
                contentDescription = null,
                tint = iconTint
            )

        Spacer(Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "$count task${if (count == 1) "" else "s"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = if (expanded) "Collapse" else "Expand"
            )
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
    var expanded by remember { mutableStateOf(false) }

    val isDone = task.isDone

    val priorityColorBase = when (task.priority) {
        "High" -> Color(0xFFFF6B6B)
        "Medium" -> Color(0xFFFFC46B)
        "Low" -> Color(0xFF6BCB77)
        else -> Color(0xFFB0BEC5)
    }

    // If done, use grey to visually separate from active tasks
    val priorityColor = if (isDone) Color(0xFF9E9E9E) else priorityColorBase

    val titleColor =
        if (isDone)
            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        else
            MaterialTheme.colorScheme.onSurface

    val titleDecoration =
        if (isDone) TextDecoration.LineThrough else TextDecoration.None

    val descriptionColor =
        if (isDone)
            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        else
            MaterialTheme.colorScheme.onSurfaceVariant


    var showMoveDialog by remember { mutableStateOf(false) }

    // 1. Collect all folders to find the name of the folder this task belongs to
    val folders by dao.getAllFoldersFlow().collectAsState(initial = emptyList())
    val parentFolder = folders.find { it.id == task.folderId }

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

                //Folder
                if (parentFolder != null) {
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Folder,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = parentFolder.name,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }

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

                Column {
                    if (!task.imageUri.isNullOrEmpty()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Image,
                                contentDescription = "Photo Attached",
                                modifier = Modifier.size(16.dp),
                                tint = Color(0xFF4CAF50)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "Photo Attached",
                                fontSize = 14.sp,
                                color = Color(0xFF4CAF50)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
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
                        if (task.dueAtMillis > 0) {
                            val dateFormat = remember {
                                SimpleDateFormat(
                                    "d/M/yyyy HH:mm",
                                    Locale.getDefault()
                                )
                            }
                            val formattedDate = dateFormat.format(Date(task.dueAtMillis))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CalendarMonth,
                                    contentDescription = "Due Date",
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant


                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Due Date: $formattedDate",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant

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

                    DropdownMenuItem(
                        text = { Text("Move to...") },
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

                if (showMoveDialog) {
                    AlertDialog(
                        onDismissRequest = { showMoveDialog = false },
                        title = { Text("Move to Folder") },
                        text = {
                            LazyColumn {
                                item {
                                    Text(
                                        text = "No Folder (General)",
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                scope.launch(Dispatchers.IO) {
                                                    dao.updateTask(task.copy(folderId = null))
                                                }
                                                showMoveDialog = false
                                            }
                                            .padding(16.dp)
                                    )
                                }
                                items(folders) { folder ->
                                    Text(
                                        text = folder.name,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                scope.launch(Dispatchers.IO) {
                                                    dao.updateTask(task.copy(folderId = folder.id))
                                                }
                                                showMoveDialog = false
                                            }
                                            .padding(16.dp)
                                    )
                                }
                            }
                        },
                        confirmButton = {},
                        dismissButton = {
                            TextButton(onClick = { showMoveDialog = false }) {
                                Text("Cancel")


                                if (task.dueAtMillis > 0) {
                                    val dateFormat = remember { SimpleDateFormat("d/M/yyyy HH:mm", Locale.getDefault()) }
                                    val formattedDate = dateFormat.format(Date(task.dueAtMillis))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.CalendarMonth,
                                            contentDescription = "Due Date",
                                            modifier = Modifier.size(16.dp),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(Modifier.width(4.dp))
                                        Text(
                                            text = "Due: $formattedDate",
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                            }
                        }
                    )
                }
            }
        }
    }
}


