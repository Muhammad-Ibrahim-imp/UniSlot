package oop.project.unislotandroid.ui.screens

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.size
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

// ─────────────────────────────────────────────────────────────────────────────
// STUDENT SCHEDULE
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun StudentScheduleScreen(vm: MainViewModel) {
    val enrollments by vm.myEnrollments.collectAsState()
    val slotOpState by vm.slotOpState.collectAsState() //slotOpState exists to control UI behavior during and after slot operations (loading, success, error).
    val pdfState    by vm.pdfState.collectAsState()
    val context     = LocalContext.current //This line is used in Jetpack Compose to get the Android Context.
    LaunchedEffect(Unit) { vm.loadMyEnrollments() }

    var feedbackMsg by remember { mutableStateOf("") }
    var isError     by remember { mutableStateOf(false) }

    // Handle slot drop result
    LaunchedEffect(slotOpState) {
        when (slotOpState) {
            is UiState.Success -> { feedbackMsg = "Slot dropped."; isError = false; vm.resetSlotOp() }
            is UiState.Error   -> { feedbackMsg = (slotOpState as UiState.Error).message; isError = true; vm.resetSlotOp() }
            else -> {}
        }
    }

    // Handle PDF download result
    LaunchedEffect(pdfState) {
        when (pdfState) {
            is UiState.Success -> {
                val bytes = (pdfState as UiState.Success<ByteArray>).data
                savePdfAndOpen(context, bytes)
                feedbackMsg = "Timetable PDF saved to Downloads"
                isError = false
                vm.resetPdfState()
            }
            is UiState.Error -> {
                feedbackMsg = (pdfState as UiState.Error).message
                isError = true
                vm.resetPdfState()
            }
            else -> {}
        }
    }

    val active = enrollments.filter { !it.dropped }

    Column(Modifier.fillMaxSize()) {
        // PDF download button row
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                if (active.isEmpty()) "No slots enrolled"
                else "${active.size} Course${if (active.size != 1) "s" else ""} Enrolled",
                fontWeight = FontWeight.SemiBold, fontSize = 14.sp
            )
            Button(
                onClick = { vm.downloadTimetablePdf() },
                enabled = active.isNotEmpty() && pdfState !is UiState.Loading,
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
            ) {
                if (pdfState is UiState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = Color.White, strokeWidth = 2.dp
                    )
                    Spacer(Modifier.width(6.dp))
                    Text("Downloading…", fontSize = 12.sp)
                } else {
                    Icon(Icons.Default.PictureAsPdf, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Download PDF", fontSize = 12.sp)
                }
            }
        }

        if (feedbackMsg.isNotBlank()) {
            Box(Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                if (isError) ErrorBanner(feedbackMsg) else SuccessBanner(feedbackMsg)
            }
        }

        if (active.isEmpty()) {
            EmptyState("No slots enrolled yet.\nGo to My Courses to get started.")
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(active) { enrollment ->
                    EnrollmentCard(
                        enrollment = enrollment,
                        dropping   = slotOpState is UiState.Loading,
                        onDrop     = { vm.dropSlot(enrollment.slotGroupCode) }
                    )
                }
            }
        }
    }
}

/**
 * Save PDF bytes to Downloads folder and open it with the device's PDF viewer.
 */
private fun savePdfAndOpen(context: Context, bytes: ByteArray) {
    try {
        // Generate a unique file name using current time
        val fileName = "timetable_${System.currentTimeMillis()}.pdf"

        // Check Android version
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // ================================
            // ANDROID 10+ (Scoped Storage)
            // ================================

            // Create metadata for the file (name, type, status)
            val values = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, fileName) // File name
                put(MediaStore.Downloads.MIME_TYPE, "application/pdf") // File type
                put(MediaStore.Downloads.IS_PENDING, 1) // Mark as "being written"
            }

            // Get system service to interact with MediaStore
            val resolver = context.contentResolver

            // Ask Android to create a file entry in Downloads and return its URI
            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)

            uri?.let {
                // Open output stream and write PDF bytes into the file
                resolver.openOutputStream(it)?.use { out ->
                    out.write(bytes)
                }

                // Clear old metadata and update only IS_PENDING
                values.clear()
                values.put(MediaStore.Downloads.IS_PENDING, 0) // Mark file as finished

                // Tell Android: "File writing is complete, make it visible"
                resolver.update(it, values, null, null)

                // Create intent to open the PDF
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(it, "application/pdf") // File URI + type
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) // Allow other apps to read
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) // Required if not in Activity
                }

                // Show chooser so user can pick PDF viewer app
                context.startActivity(
                    Intent.createChooser(intent, "Open Timetable PDF")
                )
            }

        } else {
            // ================================
            // ANDROID 9 AND BELOW (Legacy)
            // ================================

            @Suppress("DEPRECATION")
            // Get the public Downloads directory path
            val dir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS
            )

            // Create directory if it doesn't exist
            dir.mkdirs()

            // Create file inside Downloads folder
            val file = File(dir, fileName)

            // Write PDF bytes directly into the file
            FileOutputStream(file).use {
                it.write(bytes)
            }

            // Convert file path to secure content URI using FileProvider
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider", // Must match manifest
                file
            )

            // Create intent to open the PDF
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf") // File URI + type
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) // Allow access to other apps
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) // Required if not in Activity
            }

            // Launch chooser to open PDF
            context.startActivity(
                Intent.createChooser(intent, "Open Timetable PDF")
            )
        }

    } catch (e: Exception) {
        // Print error if something goes wrong
        e.printStackTrace()
    }
}

@Composable
private fun EnrollmentCard(
    enrollment: EnrollmentResponse,
    dropping: Boolean,
    onDrop: () -> Unit
) {
    Card(Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(2.dp)) {
        Column(Modifier.padding(14.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top) {
                Column(Modifier.weight(1f)) {
                    Text(enrollment.slotName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text(enrollment.courseName, fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${enrollment.courseCode} · ${enrollment.creditHours} cr",
                        fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                StatusBadge("${enrollment.creditHours} cr", "INFO")
            }
            Spacer(Modifier.height(8.dp))
            Text("👨‍🏫 ${enrollment.professorName}", fontSize = 13.sp, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(8.dp))

            Text("Lectures (${enrollment.lectures.size})",
                fontSize = 11.sp, fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(4.dp))

            enrollment.lectures.sortedBy { com.university.slotselector.ui.screens.DAYS_ORDER.indexOf(it.dayOfWeek) }.forEach { lec ->
                Row(Modifier.fillMaxWidth().padding(vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(color = MaterialTheme.colorScheme.primaryContainer,
                        shape = MaterialTheme.shapes.small) {
                        Text(
                            com.university.slotselector.ui.screens.DAY_S[lec.dayOfWeek] ?: lec.dayOfWeek.take(3),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            fontSize = 11.sp, fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                    Text("🕐 ${lec.startTime}–${lec.endTime}", fontSize = 12.sp)
                    if (!lec.venue.isNullOrBlank())
                        Text("📍 ${lec.venue}", fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(Modifier.height(10.dp))
            OutlinedButton(onClick = onDrop, enabled = !dropping,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = RedBadge),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = androidx.compose.ui.graphics.SolidColor(RedBadge))
                //Compose uses Brush internally so the same API can support both simple colors and advanced graphics like gradients.
            ) { Text(if (dropping) "Dropping…" else "Drop Slot") }
        }
    }
}

//private fun androidx.compose.foundation.BorderStroke.copy(brush: androidx.compose.ui.graphics.Brush) =
//    androidx.compose.foundation.BorderStroke(this.width, brush)




