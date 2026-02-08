package np.ict.mad.mad_assignment

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun PasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier
){
    // State to toggle between hidden (dots) and visible (text)
    var passwordVisible by remember { mutableStateOf(false) }

    // States for revealing the last typed character
    var revealLastChar by remember { mutableStateOf(false) }
    var previousLength by remember { mutableIntStateOf(value.length) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(value) {
        if (value.length > previousLength) {
            revealLastChar = true
            scope.launch {
                delay(1000L) // Show for 1 second (change to 2000L for 2s)
                revealLastChar = false
            }
        }
        previousLength = value.length
    }

    val visualTransformation = if (passwordVisible) {
        VisualTransformation.None
    } else if (revealLastChar) {
        object : VisualTransformation {
            override fun filter(text: AnnotatedString): TransformedText {
                if (text.isEmpty()) return TransformedText(text, OffsetMapping.Identity)

                val maskChar = '\u2022'
                val maskedString = String(CharArray(text.length - 1) { maskChar }) + text.last()

                return TransformedText(AnnotatedString(maskedString), object : OffsetMapping {
                    override fun originalToTransformed(offset: Int): Int = maskedString.length
                    override fun transformedToOriginal(offset: Int): Int = text.length
                })
            }
        }
    } else {
        PasswordVisualTransformation()
    }

    TextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        // ensures it matches the width and spacing of the email field
        modifier = modifier.fillMaxWidth(),
        singleLine = true,
        visualTransformation = visualTransformation,
        trailingIcon = {
            val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
            val description = if (passwordVisible) "Hide password" else "Show password"

            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                Icon(imageVector = image, contentDescription = description)
            }
        }
    )
}