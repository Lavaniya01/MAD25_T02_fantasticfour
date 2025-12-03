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
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.ActivityNavigatorExtras
import kotlinx.coroutines.withContext


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AppNavigation()
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
fun AppNavigation() {
    val nav = rememberNavController()

    NavHost(navController = nav, startDestination = Routes.Start) {
        composable(Routes.Start) { StartingScreen(nav) }
        composable(Routes.Home) { HomeScreen(nav) }
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

    // Sorted Tasks
    val sortedTasks = remember(tasks) {
        tasks.sortedWith (
            compareByDescending<Task> { priorityToInt(it.priority) }.thenBy { it.id }
        )
    }

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

            if (sortedTasks.isEmpty()) {
                Text(
                    text = "No tasks yet!",
                    style = TextStyle(fontSize = 18.sp, color = Color.Gray)
                )
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(sortedTasks) { task ->
                        TaskCard(
                            task = task,
                            onClick = {nav.navigate("details/${task.id}") },
                            onEdit = {nav.navigate("edit_task/${task.id}")},
                            onDelete = {
                                scope.launch(Dispatchers.IO){
                                    dao.deleteTask(task)
                                }
                            }
                        )
                    }

        }
    }
}}}

// ---------------------------------------------------
// TASK CARD
// ---------------------------------------------------

@Composable
fun TaskCard(
    task: Task,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,) {

    var expanded by remember { mutableStateOf(false)}

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth()
                .padding(start = 16.dp, top = 16.dp, bottom = 16.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(task.title, fontSize = 20.sp, fontWeight = FontWeight.Bold)

                if (task.description != null && task.description.isNotEmpty()) {
                    Text(
                        task.description,
                        fontSize = 16.sp,
                        color = Color.DarkGray,
                        maxLines = 2
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (!task.imageUri.isNullOrEmpty()) {
                    Text(
                        "Photo Attached",
                        fontSize = 14.sp,
                        color = Color(0xFF4CAF50)
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ){
                    task.priority?.let{
                        Text("Priority: $it", fontSize = 14.sp)
                    }

                    task.date?.let {
                        Text("Due Date: $it", fontSize = 14.sp)
                    }
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

        }
    }
}}

