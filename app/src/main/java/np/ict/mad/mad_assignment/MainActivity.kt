package np.ict.mad.mad_assignment

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.runtime.rememberCoroutineScope


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AppNavigation()
        }
    }
}

@Composable
fun AppNavigation() {
    val nav = rememberNavController()

    NavHost(navController = nav, startDestination = Routes.Start) {

        composable(Routes.Start) {
            StartingScreen(nav)
        }

    NavHost(
        navController = nav, startDestination = Routes.Start
    ) {
        composable(Routes.Start) { StartingScreen(nav) }
        composable(Routes.Home) { HomeScreen(nav) }
        composable(Routes.AddTask) {AddTaskScreen(nav) }

        composable("edit_task/{taskId}"){ backStackEntry ->
            val taskId = backStackEntry.arguments?.getString("taskId")?.toIntOrNull()
            if (taskId != null){
                EditTaskScreen(nav, id)
            }
        }
    }


        composable("details/{taskId}") { backStackEntry ->
            val id = backStackEntry.arguments?.getString("taskId")?.toInt() ?: 0
            TaskDetailsScreen(nav, id)
        }
    }
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
                    nav.navigate(Routes.Home) {
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
                    text = "Let's Get Started",
                    fontSize = 18.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}


@Composable
fun HomeScreen(nav: NavHostController) {
    val context = LocalContext.current
    val dao = DatabaseProvider.getDatabase(context).taskDao()
    val tasks by dao.getAllTasksFlow().collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()

    Scaffold(
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
                .padding(padding)
                .padding(16.dp)
        ) {

            Text(
                text = "Your Tasks",
                style = TextStyle(fontSize = 28.sp, fontWeight = FontWeight.Bold)
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (tasks.isEmpty()) {
                Text(
                    text = "No tasks yet!",
                    style = TextStyle(fontSize = 18.sp, color = Color.Gray)
                )
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(tasks) { task ->
                        TaskCard(
                            task = task,
                            onEdit = { navController.navigate("edit_task/${task.id}")},
                            onDelete = {
                                scope.launch(Dispatchers.IO){
                                    dao.deleteTask(task)
                                }
                            }
                        )
                    }
            LazyColumn {
                items(tasks) { task ->
                    TaskCard(task, nav)
                }
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
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    nav: NavHostController) {

    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable { nav.navigate("details/${task.id}") },
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(task.title, fontSize = 20.sp, fontWeight = FontWeight.Bold)

                if (task.description != null && task.description.isNotEmpty()) {
                    Text(
                        task.description,
                        fontSize = 16.sp,
                        color = Color.DarkGray,
                        maxLines = 2
                    )
                }
            }

            Box{
                IconButton(onClick = { expanded = true}) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Options")
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    DropdownMenuItem(
                        text = {Text("Edit")},
                        onClick = {
                            expanded = false
                            onEdit()
                        }
                    )

                    DropdownMenuItem(
                        text = {Text("Delete", color = Color.Red)},
                        onClick = {
                            expanded = false
                            onDelete()
                        }
                    )
                }
            // SAFE description check
            task.description?.let {
                Text(it, fontSize = 16.sp, color = Color.Gray)
            }

            task.priority?.let {
                Text("Priority: $it", fontSize = 14.sp)
            }

            task.date?.let {
                Text("Due: $it", fontSize = 14.sp)
            }
        }
    }
}


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
    val taskState = dao.getTaskById(taskId).collectAsState(initial = null)
    val task = taskState.value

    // State for the text fields
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    //Update text fields when task loads from DB (using LaunchedEffect)
    LaunchedEffect(task) {
        task?.let {
            title = it.title
            description = it.description ?: ""
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

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (title.isNotBlank() && task != null) {
                        //Create a copy of the original task with updated values
                        val updatedTask = task.copy(title = title, description = description)
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

