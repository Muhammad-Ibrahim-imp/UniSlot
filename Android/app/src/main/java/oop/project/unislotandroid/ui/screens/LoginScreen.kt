package oop.project.unislotandroid.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import oop.project.unislotandroid.viewmodel.MainViewModel
import oop.project.unislotandroid.viewmodel.UiState

@Composable
fun LoginScreen(vm: MainViewModel, onLoginSuccess: (String) -> Unit) {
    var email    by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPass by remember { mutableStateOf(false) }

    val loginState by vm.loginState.collectAsState()

    // Runs a side-effect whenever loginState changes.
    // Used to react to a successful login and trigger navigation.
    LaunchedEffect(loginState) {
        // Check if login was successful
        if (loginState is UiState.Success) {
            // Cast loginState to UiState.Success so we can access its data,
            // then extract the user's role from the response (e.g., "ADMIN" or "STUDENT")
            val role = (loginState as UiState.Success).data.role
            // Reset login state back to Idle
            // This prevents the effect from running again on recomposition
            vm.resetLogin()
            // Trigger navigation (passed from parent composable)
            // e.g., navigate to AdminDashboard or StudentDashboard
            onLoginSuccess(role)

        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .// Applies a background to the composable using a linear gradient brush.
                //
                // Brush.linearGradient(...) creates a smooth color transition between colors.
                // Here it blends from a dark blue/gray to a brighter blue.
                //
                // Color(0xFF1E2A3A) → start color (top-left by default)
                // Color(0xFF1A56DB) → end color (bottom-right by default)
                //
                // The gradient is drawn across the entire background of the composable.
            background(
                Brush.linearGradient(
                    listOf(
                        Color(0xFF282A2D), // dark tone
                        Color(0xFF1A3262)  // blue tone
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier  = Modifier.padding(24.dp).fillMaxWidth(),
            shape     = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(16.dp)
        ) {
            Column(
                modifier              = Modifier.padding(28.dp),
                horizontalAlignment   = Alignment.CenterHorizontally,
                verticalArrangement   = Arrangement.spacedBy(16.dp)
            ) {
                Text("🎓", fontSize = 40.sp)
                Text("UniSlot", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text(
                    "University Slot Selection System",
                    fontSize = 13.sp,
                    color    = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(4.dp))

                // Error
                // If the current login state represents an error,
                // show an ErrorBanner with the error message.
                if (loginState is UiState.Error) {

                    // Cast loginState to UiState.Error so we can access its message
                    // and pass it to the UI component that displays the error.
                    ErrorBanner((loginState as UiState.Error).message)
                }

                // Email input field using Material Design OutlinedTextField.
                // Allows user to type their email with proper keyboard and UI behavior.

                OutlinedTextField(

                    // Current value of the text field (state variable)
                    value = email,

                    // Called whenever user types → updates the email state
                    onValueChange = { email = it },

                    // Label shown inside the field (floats up when typing)
                    label = { Text("Email address") },

                    // Icon displayed at the start (left side) of the field
                    leadingIcon = { Icon(Icons.Default.Email, null) },

                    // Configures keyboard behavior
                    keyboardOptions = KeyboardOptions(

                        // Shows email-optimized keyboard (includes @, .com, etc.)
                        keyboardType = KeyboardType.Email,

                        // "Next" button on keyboard → moves to next input field
                        imeAction = ImeAction.Next
                    ),

                    // Restricts input to a single line (no multi-line expansion)
                    singleLine = true,

                    // Makes the field take full available width
                    modifier = Modifier.fillMaxWidth()
                )

                // Password field
                OutlinedTextField(
                    value         = password,
                    onValueChange = { password = it },
                    label         = { Text("Password") },
                    leadingIcon   = { Icon(Icons.Default.Lock, null) },
                    // Controls how the text inside the TextField is visually displayed (not the actual value).
                    //
                    // If showPass = true  → show plain text (visible password)
                    // If showPass = false → mask the text (hide password with dots •••)
                    //
                    // VisualTransformation.None → no transformation (show actual text)
                    // PasswordVisualTransformation() → replaces characters with dots for security
                    visualTransformation =
                        if (showPass) VisualTransformation.None
                        else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        // Specifies the action button shown on the soft keyboard (IME = Input Method Editor).
                        // ImeAction.Done means the keyboard will show a "Done" button.
                        //
                        // When the user taps "Done", it usually indicates input is complete
                        // (e.g., submit form, close keyboard, trigger login).
                        imeAction = ImeAction.Done
                    ),
                    // trailingIcon defines the UI shown at the END (right side) of the TextField.
// Here, it adds a clickable "Show / Hide" button to toggle password visibility.

                    trailingIcon = {

                        // TextButton is a clickable button with text (no background)
                        TextButton(

                            // When clicked → toggle showPass state
                            // true  → show password
                            // false → hide password
                            onClick = { showPass = !showPass }
                        ) {

                            // Button text changes dynamically based on current state
                            // If password is visible → show "Hide"
                            // If password is hidden  → show "Show"
                            Text(
                                if (showPass) "Hide" else "Show",

                                // Smaller font size so it fits nicely inside the TextField
                                fontSize = 12.sp
                            )
                        }
                    },
                    singleLine = true,
                    modifier   = Modifier.fillMaxWidth()
                )

                Button(
                    onClick  = { vm.login(email.trim(), password) },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    // Controls whether a UI element (e.g., Button) is clickable/enabled.
                    //
                    // The button is enabled ONLY if:
                    // 1. Email is not empty
                    // 2. Password is not empty
                    // 3. Login is NOT currently in Loading state (i.e., no API call in progress)
                    enabled =
                        email.isNotBlank() &&          // user has entered email
                                password.isNotBlank() &&       // user has entered password
                                loginState !is UiState.Loading, // prevent multiple clicks during loading
                    shape    = RoundedCornerShape(8.dp)
                ) {

                    // Shows different UI inside a button based on login state.
                    //
                    // If login is in Loading state → show a small spinner
                    // Otherwise → show the "Sign in" text
                    if (loginState is UiState.Loading) {
                        // CircularProgressIndicator = loading spinner
                        CircularProgressIndicator(
                            // Makes spinner small so it fits inside button
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            // Thickness of the spinner stroke
                            strokeWidth = 2.dp
                        )
                    } else {
                        // Default button text when not loading
                        Text(
                            "Sign in",
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Text(
                    "Default admin: admin@university.edu / admin123",
                    fontSize = 11.sp,
                    color    = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}


