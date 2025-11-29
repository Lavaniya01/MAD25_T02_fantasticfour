package np.ict.mad.mad_assignment

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import np.ict.mad.mad_assignment.data.DatabaseProvider
import np.ict.mad.mad_assignment.model.Task



class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppNavigation()
        }
    }
}

// ---------------------------------------------------
// NAVIGATION
// ---------------------------------------------------

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.Start
    ) {
        composable(Routes.Start) { StartingScreen(navController) }
        composable(Routes.Home) { HomeScreen(navController) }
        composable(Routes.AddTask) { AddTaskScreen(navController) }
    }
}

// ---------------------------------------------------
// STARTING SCREEN
// ---------------------------------------------------

@Composable
fun StartingScreen(navController: NavHostController) {

    LaunchedEffect(Unit) {
        delay(1500)
        navController.navigate(Routes.Home) {
            popUpTo(Routes.Start) { inclusive = true }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5)),
        contentAlignment = Alignment.Center
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
    }
}

// ---------------------------------------------------
// HOME SCREEN — SHOW LIST OF TASKS
// ---------------------------------------------------

@Composable
fun HomeScreen(navController: NavHostController) {

    val context = LocalContext.current
    val dao = DatabaseProvider.getDatabase(context).taskDao()

    val tasks by dao.getAllTasksFlow().collectAsState(initial = emptyList())

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Routes.AddTask) },
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
                style = TextStyle(
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (tasks.isEmpty()) {
                Text(
                    text = "No tasks yet!",
                    style = TextStyle(fontSize = 18.sp, color = Color.Gray)
                )
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(tasks) { task ->
                        TaskCard(task = task)
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------
// TASK CARD UI
// ---------------------------------------------------

@Composable
fun TaskCard(task: Task) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        colors = CardDefaults.cardColors(Color.White),
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(task.title, fontSize = 20.sp, fontWeight = FontWeight.Bold)

            if (task.description != null && task.description.isNotEmpty()) {
                Text(
                    task.description,
                    fontSize = 16.sp,
                    color = Color.DarkGray
                )
            }
        }
    }
}

// ---------------------------------------------------
// ADD TASK SCREEN — SAVES INTO ROOM
// ---------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskScreen(navController: NavHostController) {

    val context = LocalContext.current
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Task") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
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
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                maxLines = 5
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        val newTask = Task(
                            title = title,
                            description = description
                        )

                        scope.launch(Dispatchers.IO) {
                            DatabaseProvider.getDatabase(context)
                                .taskDao()
                                .insertTask(newTask)
                        }

                        navController.popBackStack()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(Color(0xFF4CAF50))
            ) {
                Text("Save Task", fontSize = 18.sp, color = Color.White)
            }
        }
    }
}
