package np.ict.mad.mad_assignment

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import np.ict.mad.mad_assignment.data.DatabaseProvider
import np.ict.mad.mad_assignment.model.Task
import androidx.core.net.toUri
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailsScreen(nav: NavController, taskId: Int) {

    val context = LocalContext.current
    val dao = DatabaseProvider.getDatabase(context).taskDao()
    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
    var task by remember { mutableStateOf<Task?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(taskId) {
        scope.launch(Dispatchers.IO) {
            task = dao.getTaskById(taskId,uid)
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
                // subtle fade + slide animation (same as before)
                var appeared by remember(task!!.id) { mutableStateOf(false) }

                LaunchedEffect(task!!.id) {
                    appeared = true
                }

                val alpha by animateFloatAsState(
                    targetValue = if (appeared) 1f else 0f,
                    label = "detailsAlpha"
                )
                val offsetY by animateDpAsState(
                    targetValue = if (appeared) 0.dp else 16.dp,
                    label = "detailsOffset"
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .alpha(alpha)
                        .offset(y = offsetY),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TaskDetailsContent(task!!)

                    Spacer(modifier = Modifier.width(0.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // ✅ Toggle Mark / Unmark button
                        Button(
                            modifier = Modifier.weight(1f),
                            onClick = {
                                val current = task!!
                                val updated = current.copy(isDone = !current.isDone)
                                task = updated

                                scope.launch(Dispatchers.IO) {
                                    dao.updateTask(updated)

                                    // 🔔 Cancel reminder when done; reschedule when unmarked
                                    if (updated.isDone) {
                                        ReminderScheduler.cancelDueReminder(context, updated.id)
                                    } else {
                                        ReminderScheduler.scheduleDueReminder(
                                            context = context,
                                            taskId = updated.id,
                                            title = updated.title,
                                            dueAtMillis = updated.dueAtMillis
                                        )
                                    }
                                }
                            }
                        ) {
                            Text(text = if (task!!.isDone) "Unmark" else "Mark as done")
                        }

                        // Delete button
                        OutlinedButton(
                            modifier = Modifier.weight(1f),
                            onClick = {
                                val current = task!!
                                scope.launch(Dispatchers.IO) {
                                    // 🔔 Cancel reminder before deleting
                                    ReminderScheduler.cancelDueReminder(context, current.id)
                                    dao.deleteTask(current)
                                }
                                nav.popBackStack()
                            },
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color.Red
                            )
                        ) {
                            Text("Delete")
                        }
                    }
                }
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
            Text(
                text = task.title,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            if (!task.description.isNullOrBlank()) {
                Text(
                    text = task.description,
                    fontSize = 16.sp
                )
            }

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
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )
            }

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
                        tint = MaterialTheme.colorScheme.onSurfaceVariant


                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Due Date: ${task.date}",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant

                    )
                }
            }
        }
    }
}
