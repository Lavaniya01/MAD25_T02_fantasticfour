package np.ict.mad.mad_assignment

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import androidx.core.content.edit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordScreen(nav: NavHostController) {
    val context = LocalContext.current
    val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
    val sharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    // States
    var currentPasswordInput by remember { mutableStateOf("") }

    var newPasswordInput by remember { mutableStateOf("") }
    var confirmPasswordInput by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    val actualCurrentPassword = sharedPreferences.getString("password", "") ?: ""

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Change Password") },
                navigationIcon = {
                    // 3. EXIT BACK BUTTON
                    IconButton(onClick = { nav.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            PasswordField(
                value = currentPasswordInput,
                onValueChange = { currentPasswordInput = it },
                label = "Current Password"
            )

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            PasswordField(
                value = newPasswordInput,
                onValueChange = { newPasswordInput = it },
                label = "New Password"
            )

            PasswordField(
                value = confirmPasswordInput,
                onValueChange = { confirmPasswordInput = it },
                label = "Confirm New Password"
            )
            if (errorMessage.isNotEmpty()) {
                Text(errorMessage, color = MaterialTheme.colorScheme.error)
            }

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    val user = auth.currentUser

                    when {
                        currentPasswordInput != actualCurrentPassword -> {
                            errorMessage = "Current password is incorrect"
                        }
                        newPasswordInput == actualCurrentPassword -> {
                            errorMessage = "New password cannot be the same as the old one"
                        }
                        newPasswordInput.length < 6 -> {
                            errorMessage = "Password must be at least 6 characters"
                        }
                        user != null -> {
                            // UPDATE FIREBASE
                            user.updatePassword(newPasswordInput)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        // UPDATE LOCAL PREFS
                                        sharedPreferences.edit {
                                            putString(
                                                "password",
                                                newPasswordInput
                                            )
                                        }
                                        Toast.makeText(context, "Password updated!", Toast.LENGTH_SHORT).show()
                                        nav.popBackStack()
                                    } else {
                                        errorMessage = "Firebase update failed: ${task.exception?.message}"
                                    }
                                }
                        }
                    }
                }
            ) {
                Text("Update Password")
            }
        }
    }
}