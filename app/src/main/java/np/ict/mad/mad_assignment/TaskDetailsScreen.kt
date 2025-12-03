package np.ict.mad.mad_assignment

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import np.ict.mad.mad_assignment.data.DatabaseProvider
import np.ict.mad.mad_assignment.model.Task

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailsScreen(nav: NavController, taskId: Int) {

    val context = LocalContext.current
    val dao = DatabaseProvider.getDatabase(context).taskDao()

    var task by remember { mutableStateOf<Task?>(null) }
    val scope = rememberCoroutineScope()

    // Load task once when screen opens
    LaunchedEffect(taskId) {
        scope.launch(Dispatchers.IO) {
            task = dao.getTaskById(taskId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Task Details") },
                navigationIcon = {
                    IconButton(onClick = { nav.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(20.dp),
            contentAlignment = Alignment.TopStart
        ) {
            if (task == null) {
                Text("Loading...", fontSize = 18.sp)
            } else {
                TaskDetailsContent(task!!)
            }
        }
    }
}

@Composable
fun TaskDetailsContent(task: Task) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = task.title,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )

        if (!task.description.isNullOrBlank()) {
            Text(
                text = task.description,
                fontSize = 18.sp
            )
        }

        // Show priority
        Text(
            text = "Priority: ${task.priority}",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold
        )

        // Show date
        Text(
            text = "Due Date: ${task.date}",
            fontSize = 18.sp
        )
    }
}
