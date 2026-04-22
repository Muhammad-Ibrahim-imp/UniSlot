package oop.project.unislotandroid.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import oop.project.unislotandroid.ui.theme.*
import oop.project.unislotandroid.viewmodel.MainViewModel

// ─────────────────────────────────────────────────────────────────────────────
// STUDENT DASHBOARD
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun StudentDashboardScreen(vm: MainViewModel, onNavigate: (String) -> Unit) {
    // Collects a StateFlow (or Flow) from the ViewModel and converts it into
    // Compose State so the UI can react to updates automatically.
    //
    // "by" is Kotlin property delegation → lets you use the value directly
    // instead of writing .value every time.
    val profile by vm.myProfile.collectAsState()
    val enrollments by vm.myEnrollments.collectAsState()
    LaunchedEffect(Unit) { vm.loadMyProfile(); vm.loadMyEnrollments() }

    if (profile == null) { LoadingScreen(); return }
    // "!!" means: "I am 100% sure this is NOT null"
    // If profile IS null → app will CRASH (NullPointerException)
    val p    = profile!!
    val paid = p.feeStatus == "PAID"

    // LazyColumn is a vertically scrollable container that efficiently displays a list of items.
    // It only composes (renders) the items that are currently visible on the screen,
    // making it suitable for large or dynamic datasets (like RecyclerView in old Android).
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // item { ... } is used to add a single static element inside the LazyColumn.
        // It is typically used for headers, footers, or standalone components
        // that are not part of a repeating list.
        item {
            Text("Welcome, ${p.name.split(" ").first()} 👋",
                fontWeight = FontWeight.Bold, fontSize = 20.sp)
            Text("Student Portal", fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp))
        }
        if (!paid) {
            item { ErrorBanner("⚠️ Fee UNPAID — Contact finance to enable slot selection") }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard("Degree", p.degreeCode ?: "—", modifier = Modifier.weight(1f))
                StatCard("Semester", "Sem ${p.currentSemester}", modifier = Modifier.weight(1f))
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard("Slots Enrolled", "${p.slotsSelected}",
                    color = MaterialTheme.colorScheme.primary, modifier = Modifier.weight(1f))
                StatCard("Fee", p.feeStatus,
                    color = if (paid) GreenBadge else RedBadge, modifier = Modifier.weight(1f))
            }
        }
        item { SectionHeader("Profile") }
        item {
            Card(elevation = CardDefaults.cardElevation(1.dp)) {
                Column(Modifier.padding(16.dp)) {
                    InfoRow("Name",       p.name)
                    InfoRow("Roll No",    p.rollNumber)
                    InfoRow("Email",      p.email)
                    InfoRow("Department", p.departmentName ?: "—")
                    InfoRow("Degree",     "${p.degreeName} (${p.degreeCode})")
                    InfoRow("Semester",   "Semester ${p.currentSemester}")
                }
            }
        }
        item { SectionHeader("Quick Actions") }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick  = { onNavigate(Screen.StudentCourses.route) },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    enabled = paid,
                    colors   = ButtonDefaults.buttonColors(
                        containerColor = if (paid) MaterialTheme.colorScheme.primary else Color.Gray)
                ) {
                    Text(if (paid) "📚 Browse & Select Slots" else "🔒 Slots Locked (Fee Unpaid)",
                        fontWeight = FontWeight.Medium)
                }
                OutlinedButton(onClick = { onNavigate(Screen.StudentSchedule.route) },
                    modifier = Modifier.fillMaxWidth().height(48.dp)) {
                    Text("📅 View My Schedule")
                }
            }
        }
    }
}
@Composable
fun StudentScheduleScreen(x0: MainViewModel) {
    TODO("Not yet implemented")
}

@Composable
fun StudentCoursesScreen(x0: MainViewModel) {
    TODO("Not yet implemented")
}


