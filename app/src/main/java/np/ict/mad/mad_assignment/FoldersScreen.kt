package np.ict.mad.mad_assignment

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

@Composable
fun FoldersScreen(nav: NavHostController){
    val folderList = listOf("Work", "Personal", "Shopping", "Health")

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Folders", style = MaterialTheme.typography.headlineLarge)

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(folderList) { folder ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable{}
                ){
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ){
                        Icon(Icons.Default.Folder, contentDescription = null)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(text = folder, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        }
    }
}