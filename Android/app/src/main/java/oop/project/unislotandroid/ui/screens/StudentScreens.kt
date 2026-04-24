package oop.project.unislotandroid.ui.screens

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import oop.project.unislotandroid.data.model.CourseResponse
import oop.project.unislotandroid.data.model.EnrollmentResponse
import oop.project.unislotandroid.data.model.LectureSlotResponse
import oop.project.unislotandroid.ui.theme.GreenBadge
import oop.project.unislotandroid.ui.theme.RedBadge
import oop.project.unislotandroid.viewmodel.MainViewModel
import oop.project.unislotandroid.viewmodel.UiState
import java.io.File
import java.io.FileOutputStream

private val DAYS_ORDER = listOf("MONDAY","TUESDAY","WEDNESDAY","THURSDAY","FRIDAY","SATURDAY")
private val DAY_S = mapOf(
    "MONDAY" to "Mon","TUESDAY" to "Tue","WEDNESDAY" to "Wed",
    "THURSDAY" to "Thu","FRIDAY" to "Fri","SATURDAY" to "Sat"
)

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
// ─────────────────────────────────────────────────────────────────────────────
// STUDENT COURSES  (pick a slot from the available ones)
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun StudentCoursesScreen(vm: MainViewModel) {
    val coursesState  by vm.myCourses.collectAsState()
    val availSlots    by vm.availableSlots.collectAsState()
    val enrollments   by vm.myEnrollments.collectAsState()
    val slotOpState   by vm.slotOpState.collectAsState()
    LaunchedEffect(Unit) { vm.loadMyCourses(); vm.loadMyEnrollments() }

    var selectedCourse by remember { mutableStateOf<CourseResponse?>(null) }
    var feedbackMsg    by remember { mutableStateOf("") }
    var isError        by remember { mutableStateOf(false) }

    LaunchedEffect(slotOpState) {
        when (slotOpState) {
            is UiState.Success -> { feedbackMsg = "✓ Enrolled!"; isError = false; vm.resetSlotOp() }
            is UiState.Error   -> { feedbackMsg = (slotOpState as UiState.Error).message; isError = true; vm.resetSlotOp() }
            else -> {}
        }
    }

    val enrolledCodes       = enrollments.map { it.slotGroupCode }.toSet()
    val enrolledCourseCodes = enrollments.filter { !it.dropped }.map { it.courseCode }.toSet()

    Column(Modifier.fillMaxSize()) {
        if (feedbackMsg.isNotBlank()) {
            Box(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                if (isError) ErrorBanner(feedbackMsg) else SuccessBanner(feedbackMsg)
            }
        }

        when (coursesState) {
            is UiState.Loading -> LoadingScreen()
            is UiState.Error   -> ErrorBanner((coursesState as UiState.Error).message)
            is UiState.Success -> {
                val courses = (coursesState as UiState.Success).data

                if (selectedCourse == null) {
                    // Course list
                    LazyColumn(contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        item { Text("My Courses", fontWeight = FontWeight.SemiBold) }
                        if (courses.isEmpty()) {
                            item { EmptyState("No courses available for your semester.") }
                        } else {
                            items(courses) { course ->
                                val enrolled = course.courseCode in enrolledCourseCodes
                                Card(
                                    onClick = { selectedCourse = course; vm.loadAvailableSlots(course.id) },
                                    modifier = Modifier.fillMaxWidth(),
                                    elevation = CardDefaults.cardElevation(2.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (enrolled) Color(0xFFF0FDF4)
                                        else MaterialTheme.colorScheme.surface)
                                ) {
                                    Row(Modifier.padding(14.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically) {
                                        Column(Modifier.weight(1f)) {
                                            Text(course.name, fontWeight = FontWeight.Medium)
                                            Text("${course.courseCode} · ${course.creditHours} cr",
                                                fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        when {
                                            enrolled -> StatusBadge("✓ Enrolled", "PAID")
                                            course.availableSlotGroups == 0 -> StatusBadge("No slots", "UNPAID")
                                            else -> StatusBadge("${course.availableSlotGroups} slot${if (course.availableSlotGroups != 1) "s" else ""}", "INFO")
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // Slot list for selected course
                    Column(Modifier.fillMaxSize()) {
                        Row(Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { selectedCourse = null }) {
                                Icon(Icons.Default.ArrowBack, null)
                            }
                            Column {
                                Text(selectedCourse!!.name, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                                Text("${availSlots.size} slot${if (availSlots.size != 1) "s" else ""} available",
                                    fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        HorizontalDivider()

                        if (availSlots.isEmpty()) {
                            EmptyState("No available slots for this course right now.")
                        } else {
                            LazyColumn(contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                items(availSlots) { slot ->
                                    AvailableSlotCard(
                                        slot         = slot,
                                        isEnrolled   = slot.slotGroupCode in enrolledCodes,
                                        isSelecting  = slotOpState is UiState.Loading,
                                        onSelect     = { vm.selectSlot(slot.slotGroupCode) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
            else -> {}
        }
    }
}

@Composable
private fun AvailableSlotCard(
    slot: LectureSlotResponse,
    isEnrolled: Boolean,
    isSelecting: Boolean,
    onSelect: () -> Unit
) {
    Card(Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(2.dp)) {
        Column(Modifier.padding(14.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f)) {
                    Text(slot.slotName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text("👨‍🏫 ${slot.professorName}", fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 2.dp))
                }
                when {
                    isEnrolled -> StatusBadge("✓ Enrolled", "PAID")
                    slot.isFull -> StatusBadge("Full", "UNPAID")
                    else -> StatusBadge("${slot.availableSeats} left", "INFO")
                }
            }

            Spacer(Modifier.height(10.dp))
            Text("Lectures (${slot.lectures.size})",
                fontSize = 11.sp, fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(4.dp))

            slot.lectures.sortedBy { DAYS_ORDER.indexOf(it.dayOfWeek) }.forEach { lec ->
                Row(Modifier.fillMaxWidth().padding(vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            DAY_S[lec.dayOfWeek] ?: lec.dayOfWeek.take(3),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            fontSize = 11.sp, fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Text("🕐 ${lec.startTime}–${lec.endTime}", fontSize = 12.sp)
                    if (!lec.venue.isNullOrBlank())
                        Text("📍 ${lec.venue}", fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(Modifier.height(10.dp))
            CapacityBar(slot.enrolledCount, slot.maxCapacity)

            if (!isEnrolled && !slot.isFull) {
                Spacer(Modifier.height(10.dp))
                Button(onClick = onSelect, enabled = !isSelecting,
                    modifier = Modifier.fillMaxWidth()) {
                    Text(if (isSelecting) "Enrolling…" else "Enrol in ${slot.slotName}")
                }
            }
        }
    }
}




