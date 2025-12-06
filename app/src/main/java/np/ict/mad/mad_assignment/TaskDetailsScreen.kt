package np.ict.mad.mad_assignment

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import np.ict.mad.mad_assignment.data.DatabaseProvider
import np.ict.mad.mad_assignment.model.Task
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.core.net.toUri
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailsScreen(nav: NavController, taskId: Int) {

    val context = LocalContext.current
    val dao = DatabaseProvider.getDatabase(context).taskDao()

    var task by remember { mutableStateOf<Task?>(null) }
    val scope = rememberCoroutineScope()

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
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f))
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 12.dp),
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

    val priorityColor = when (task.priority) {
        "High" -> Color(0xFFFF6B6B)
        "Medium" -> Color(0xFFFFC46B)
        "Low" -> Color(0xFF6BCB77)
        else -> Color(0xFFB0BEC5)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Title
            Text(
                text = task.title,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            // Description
            if (!task.description.isNullOrBlank()) {
                Text(
                    text = task.description,
                    fontSize = 16.sp
                )
            }

            // Display attached image
            task.imageUri?.let { uriString ->
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(uriString.toUri())
                        .crossfade(true)
                        .build(),
                    contentDescription = "Attached Image for ${task.title}",
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 150.dp, max = 350.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            // Priority + date row
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Priority chip
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(priorityColor.copy(alpha = 0.18f))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Priority: ${task.priority}",
                        fontSize = 14.sp,
                        color = priorityColor,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Due date with icon
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = "Due Date",
                        modifier = Modifier.size(18.dp),
                        tint = Color.Gray
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Due Date: ${task.date}",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

