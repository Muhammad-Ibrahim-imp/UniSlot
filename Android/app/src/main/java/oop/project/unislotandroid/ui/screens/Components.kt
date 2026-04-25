package oop.project.unislotandroid.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import oop.project.unislotandroid.ui.theme.GreenBadge
import oop.project.unislotandroid.ui.theme.RedBadge
import oop.project.unislotandroid.ui.theme.YellowBadge
import oop.project.unislotandroid.ui.theme.BlueBadge

// Opt-in annotation: allows usage of experimental Material 3 APIs
@OptIn(ExperimentalMaterial3Api::class)

// Marks this function as a Composable (used to build UI in Jetpack Compose)
@Composable

// Reusable Top App Bar component
// - title: text shown in the center/left
// - onMenuClick: callback triggered when menu (hamburger) icon is pressed
fun AppTopBar(title: String, onMenuClick: () -> Unit) {

    // Material 3 Top App Bar component
    TopAppBar(

        // Title section of the app bar
        title = {
            Text(
                title,                         // dynamic title passed from parent
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp
            )
        },

        // Left-side icon (hamburger menu)
        navigationIcon = {

            // Clickable icon button
            IconButton(onClick = onMenuClick) {

                // Menu icon (☰)
                Icon(
                    Icons.Default.Menu,
                    contentDescription = "Menu" // accessibility description
                )
            }
        },

        // Styling for the app bar
        colors = TopAppBarDefaults.topAppBarColors(

            // Background color of the top bar
            containerColor = MaterialTheme.colorScheme.primary,

            // Color of the title text
            titleContentColor = Color.White,

            // Color of the navigation (menu) icon
            navigationIconContentColor = Color.White
        )
    )
}
//In Material Design TopAppBar, this slot is predefined:
//
//navigationIcon = LEFT side(menu/back)
//title          = CENTER/LEFT
//actions        = RIGHT side(search, profile)

//====================================================================================================
@Composable
// Optional color for the value text (defaults to theme color)
fun StatCard(label: String, value: String, color: Color = MaterialTheme.colorScheme.onSurface, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors   = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 0.5.sp)
            Text(value, fontSize = 26.sp, fontWeight = FontWeight.Bold, color = color,
                modifier = Modifier.padding(top = 4.dp))
        }
    }
}

//====================================================================================================
// Reusable composable that shows a colored status badge (pill-shaped label)
// Example: "PAID", "UNPAID", "FULL", etc.

@Composable
fun StatusBadge(
    text: String,    // Text shown inside the badge
    status: String   // Status used to determine colors
) {

    // Decide background (bg) and text color (fg) based on status
    val (bg, fg) = when (status.uppercase()) {

        // Light green background + green text
        "PAID" -> Color(0xFFD1FAE5) to GreenBadge

        // Light red background + red text
        "UNPAID", "FULL" -> Color(0xFFFDE8E8) to RedBadge

        // Light yellow background + yellow text
        "PARTIAL" -> Color(0xFFFEF3C7) to YellowBadge

        // Default case (unknown status)
        else -> Color(0xFFDBEAFE) to BlueBadge
    }

    // Container for the badge
    Box(
        modifier = Modifier

            // Applies background color with fully rounded corners (pill shape)
            .background(
                bg,
                RoundedCornerShape(999.dp) // very large radius → pill shape
            )

            // Inner spacing around the text
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {

        // Text inside the badge
        Text(
            text = text,

            // Small font size (badge style)
            fontSize = 11.sp,

            // Text color based on status
            color = fg,

            // Semi-bold for better visibility
            fontWeight = FontWeight.SemiBold
        )
    }
}
//====================================================================================================
@Composable
fun ErrorBanner(message: String) {
    // Early return: avoid rendering UI if there is no message
    if (message.isBlank()) return
    Box(
        Modifier
            .fillMaxWidth()
            .background(Color(0xFFFDE8E8), /*RoundedCornerShape(8.dp)*/)
            .padding(12.dp)
    ) {
        Text(message, color = Color(0xFF9B1C1C), fontSize = 13.sp)
    }
}

//====================================================================================================
@Composable
fun SuccessBanner(message: String) {
    if (message.isBlank()) return
    Box(
        Modifier
            .fillMaxWidth()
            .background(Color(0xFFD1FAE5), RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Text(message, color = Color(0xFF064E3B), fontSize = 13.sp)
    }
}

//====================================================================================================
@Composable
fun SectionHeader(text: String) {
    Text(
        text, fontWeight = FontWeight.Bold, fontSize = 15.sp,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

//====================================================================================================
@Composable
fun CapacityBar(filled: Int, total: Int) {
    val pct = if (total > 0) filled.toFloat() / total else 0f
    val color = if (pct > 0.8f) RedBadge else MaterialTheme.colorScheme.primary
    Column {
        // Progress bar representing capacity
        LinearProgressIndicator(
            // Progress must be between 0f and 1f
            progress = { pct },
            // Full width, thin bar
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp),
            // Fill color (dynamic based on capacity)
            color = color,
            // Background track color
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
        Spacer(Modifier.height(4.dp))
        Text("$filled/$total seats filled", fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

//====================================================================================================
@Composable
fun InfoRow(label: String, value: String) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        // - First item goes to the START (left)
        // - Last item goes to the END (right)
        // - Remaining items are spaced evenly in between
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp,
            modifier = Modifier.weight(0.4f))
        Text(value, fontWeight = FontWeight.Medium, fontSize = 13.sp,
            modifier = Modifier.weight(0.6f))
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
}

//====================================================================================================
// Full-screen loading UI shown while data is being fetched or processed

@Composable
fun LoadingScreen() {

    // Box fills entire screen and allows content alignment
    Box(
        modifier = Modifier.fillMaxSize(),

        // Centers everything inside the Box (both horizontally & vertically)
        contentAlignment = Alignment.Center
    ) {

        // Column arranges items vertically (spinner + text)
        Column(

            // Center items horizontally inside the column
            horizontalAlignment = Alignment.CenterHorizontally,

            // Add vertical spacing between items
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // Circular loading spinner
            CircularProgressIndicator()

            // Loading text below spinner
            Text(
                "Loading…",

                // Subtle text color from theme
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

//====================================================================================================
@Composable
fun EmptyState(message: String) {
    Box(Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
        Text(message, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
    }
}
