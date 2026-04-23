package oop.project.unislotandroid.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import oop.project.unislotandroid.data.model.*
import oop.project.unislotandroid.ui.theme.GreenBadge
import oop.project.unislotandroid.ui.theme.RedBadge
import oop.project.unislotandroid.viewmodel.MainViewModel
import oop.project.unislotandroid.viewmodel.UiState

private val ALL_DAYS = listOf("MONDAY","TUESDAY","WEDNESDAY","THURSDAY","FRIDAY","SATURDAY")
private val DAY_SHORT = mapOf(
    "MONDAY" to "Mon","TUESDAY" to "Tue","WEDNESDAY" to "Wed",
    "THURSDAY" to "Thu","FRIDAY" to "Fri","SATURDAY" to "Sat"
)
// ─────────────────────────────────────────────────────────────────────────────
// ADMIN SLOTS
// ─────────────────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminSlotsScreen(vm: MainViewModel) {
    val coursesState by vm.courses.collectAsState()
    val adminSlots   by vm.adminSlots.collectAsState()
    // Runs a side-effect coroutine when the composable first enters the composition.
    // Using Unit as the key means this block executes only once and will NOT re-run on recomposition.
    // If a variable is used as the key, the block will re-run whenever that variable changes.
    // Used here to trigger initial data loading from the ViewModel (API calls).
    LaunchedEffect(Unit) {
        vm.loadCourses()
        vm.loadDepartments()
    }

    // Safely extracts the course list from UiState.Success if available;
    // if the state is not Success (Loading/Error), returns an empty list to avoid crashes.
    val courses = (coursesState as? UiState.Success)?.data ?: emptyList()
    var selCourse   by remember { mutableStateOf<CourseResponse?>(null) }
    var cExpanded   by remember { mutableStateOf(false) }
    var showDialog  by remember { mutableStateOf(false) }
    var feedbackMsg by remember { mutableStateOf("") }
    var isError     by remember { mutableStateOf(false) }

    LaunchedEffect(selCourse) { selCourse?.let { vm.loadAdminSlots(it.id) } }

    Column(Modifier.fillMaxSize()) {
        if (feedbackMsg.isNotBlank()) {
            if (isError) ErrorBanner(feedbackMsg) else SuccessBanner(feedbackMsg)
        }

        // Info banner
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFE0F2FE))
        ) {
            Text(
                "Each slot = one section students enroll into. " +
                        "Add multiple slots per course for different sections (e.g., OOP Slot 1, Slot 2…). " +
                        "Each slot has its own weekly schedule and capacity.",
                modifier = Modifier.padding(12.dp),
                fontSize = 12.sp, color = Color(0xFF075985)
            )
        }

        Column(Modifier.padding(horizontal = 16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically) {
                ExposedDropdownMenuBox(expanded = cExpanded, onExpandedChange = { cExpanded = it },
                    modifier = Modifier.weight(1f)) {
                    OutlinedTextField(
                        value = selCourse?.let { "${it.courseCode} — ${it.name}" } ?: "Select course…",
                        onValueChange = {}, readOnly = true,
                        label = { Text("Course") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(cExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = cExpanded, onDismissRequest = { cExpanded = false }) {
                        courses.forEach { c ->
                            DropdownMenuItem(text = { Text("${c.courseCode} — ${c.name}") },
                                onClick = { selCourse = c; cExpanded = false })
                        }
                    }
                }
                FloatingActionButton(onClick = { showDialog = true },
                    modifier = Modifier.size(48.dp), containerColor = MaterialTheme.colorScheme.primary) {
                    Icon(Icons.Default.Add, null, tint = Color.White)
                }
            }
            Spacer(Modifier.height(12.dp))
        }

        if (selCourse == null) {
            EmptyState("Select a course to view its slots")
        } else if (adminSlots.isEmpty()) {
            EmptyState("No slots yet for ${selCourse!!.courseCode}.\nTap + to create the first slot.")
        } else {
            LazyColumn(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)) {
                item {
                    Text("${adminSlots.size} slot${if (adminSlots.size != 1) "s" else ""}",
                        fontWeight = FontWeight.SemiBold, fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                items(adminSlots) { slot ->
                    SlotCard(
                        slot = slot,
                        onDelete = {
                            vm.deleteSlot(slot.slotGroupCode, selCourse!!.id) { ok, msg ->
                                isError = !ok; feedbackMsg = msg
                            }
                        }
                    )
                }
                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }

    if (showDialog) {
        val profsState by vm.deptProfessors.collectAsState()
        val deptsState by vm.departments.collectAsState()
        val depts = (deptsState as? UiState.Success)?.data ?: emptyList()
        LaunchedEffect(Unit) { vm.loadAllProfessorsIntoDept() } //it will update profsState
        //In Compose, UI updates depend on state observation, not where the update is triggered.

        CreateSlotDialog(
            courses     = courses,
            professors  = profsState,
            depts       = depts,
            preselected = selCourse,
            onDeptSelected = { deptId ->
                if (deptId != null) vm.loadProfessorsByDept(deptId)
                else vm.loadAllProfessorsIntoDept()
            },
            onDismiss   = { showDialog = false },
            onSave      = { req ->
                vm.createSlot(req) { ok, msg -> isError = !ok; feedbackMsg = msg }
                showDialog = false
            }
        )
    }
}

@Composable
private fun SlotCard(slot: LectureSlotResponse, onDelete: () -> Unit) {
    val pct = if (slot.maxCapacity > 0) slot.enrolledCount.toFloat() / slot.maxCapacity else 0f
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
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Delete, null, tint = RedBadge, modifier = Modifier.size(18.dp))
                }
            }

            Spacer(Modifier.height(10.dp))
            Text("Lectures (${slot.lectures.size})",
                fontSize = 11.sp, fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(4.dp))

            // Sorts the list of lectures based on the order of days defined in ALL_DAYS,
            // so that lectures appear in proper weekly sequence (e.g., Mon → Tue → Wed),
            // then iterates through each lecture to render its UI.
            slot.lectures.sortedBy { oop.project.unislotandroid.ui.screens.ALL_DAYS.indexOf(it.dayOfWeek) }.forEach { lec ->
                Row(
                    Modifier.fillMaxWidth().padding(vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(color = MaterialTheme.colorScheme.primaryContainer,
                        shape = MaterialTheme.shapes.small) {
                        // Tries to get a short form of the day from the DAY_SHORT map (e.g., "MONDAY" -> "Mon");
                        // if not found, falls back to taking the first 3 characters of the day string.
                        oop.project.unislotandroid.ui.screens.DAY_SHORT[lec.dayOfWeek] ?: lec.dayOfWeek.take(3)
                        Text(
                            oop.project.unislotandroid.ui.screens.DAY_SHORT[lec.dayOfWeek] ?: lec.dayOfWeek.take(3),
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
            CapacityBar(slot.enrolledCount, slot.maxCapacity)
            if (slot.isFull) {
                Spacer(Modifier.height(4.dp))
                StatusBadge("FULL", "UNPAID")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateSlotDialog(
    courses: List<CourseResponse>,
    professors: List<ProfessorResponse>,
    depts: List<DepartmentResponse>,
    preselected: CourseResponse?,
    onDeptSelected: (Long?) -> Unit,
    onDismiss: () -> Unit,
    onSave: (CreateSlotRequest) -> Unit
) {
    var selCourse  by remember { mutableStateOf(preselected) }
    var selProf    by remember { mutableStateOf<ProfessorResponse?>(null) }
    var selDept    by remember { mutableStateOf<DepartmentResponse?>(null) }
    var slotName   by remember { mutableStateOf("") }
    var capacity   by remember { mutableStateOf("50") }
    var schedule   by remember { mutableStateOf(listOf(DayScheduleEntry())) }
    var cExpanded  by remember { mutableStateOf(false) }
    var pExpanded  by remember { mutableStateOf(false) }
    var dExpanded  by remember { mutableStateOf(false) }
    var errorMsg   by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Lecture Slot") },
        text = {
            Column(
                Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (errorMsg.isNotBlank())
                    Text(errorMsg, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)

                // Course
                ExposedDropdownMenuBox(expanded = cExpanded, onExpandedChange = { cExpanded = it }) {
                    OutlinedTextField(
                        // If a course is selected (selCourse != null), format and display its code and name
                        // (e.g., "CS101 — Data Structures"); otherwise, show placeholder text "Course *".
                        value = selCourse?.let { "${it.courseCode} — ${it.name}" } ?: "Course *",
                        onValueChange = {}, readOnly = true,
                        label = { Text("Course") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(cExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = cExpanded, onDismissRequest = { cExpanded = false }) {
                        courses.forEach { c -> DropdownMenuItem(
                            text = { Text("${c.courseCode} — ${c.name}") },
                            onClick = { selCourse = c; cExpanded = false }) }
                    }
                }

                OutlinedTextField(value = slotName, onValueChange = { slotName = it },
                    label = { Text("Slot Name (optional, auto if blank)") },
                    placeholder = { Text("e.g. Slot 1, Section A") },
                    singleLine = true, modifier = Modifier.fillMaxWidth())

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Dept filter
                    ExposedDropdownMenuBox(expanded = dExpanded, onExpandedChange = { dExpanded = it },
                        modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = selDept?.code ?: "Dept",
                            onValueChange = {}, readOnly = true,
                            label = { Text("Dept filter") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(dExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(expanded = dExpanded, onDismissRequest = { dExpanded = false }) {
                            DropdownMenuItem(text = { Text("All") }, onClick = {
                                selDept = null; selProf = null; onDeptSelected(null); dExpanded = false })
                            depts.forEach { d -> DropdownMenuItem(text = { Text(d.code) },
                                onClick = { selDept = d; selProf = null; onDeptSelected(d.id); dExpanded = false }) }
                        }
                    }
                    OutlinedTextField(value = capacity, onValueChange = { capacity = it },
                        label = { Text("Capacity") }, singleLine = true, modifier = Modifier.weight(1f))
                }

                // Professor
                ExposedDropdownMenuBox(expanded = pExpanded, onExpandedChange = { pExpanded = it }) {
                    OutlinedTextField(
                        value = selProf?.let { "${it.name}${it.departmentName?.let { d -> " ($d)" } ?: ""}" } ?: "Professor *",
                        onValueChange = {}, readOnly = true,
                        label = { Text("Professor") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(pExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = pExpanded, onDismissRequest = { pExpanded = false }) {
                        professors.forEach { p -> DropdownMenuItem(
                            text = { Text("${p.name}${p.departmentName?.let { " ($it)" } ?: ""}") },
                            onClick = { selProf = p; pExpanded = false }) }
                    }
                }

                HorizontalDivider()

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text("Lecture Schedule", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        Text("Each row = one class meeting per week",
                            fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    TextButton(onClick = { schedule = schedule + DayScheduleEntry() },
                        enabled = schedule.size < 6) {
                        Text("+ Add", fontSize = 12.sp)
                    }
                }

                // Iterates through the schedule list with index, displaying each entry using ScheduleEntryCard.
                // Provides update and remove actions:
                // - onUpdate: replaces the item at the current index with the updated entry
                // - onRemove: removes the item at the current index (only if more than one item exists)
                schedule.forEachIndexed { idx, entry ->
                    ScheduleEntryCard(
                        entry = entry, //entry → passes the current schedule item (data object) to the card
                        index = idx, //idx   → passes the position of this item in the list
                        canRemove = schedule.size > 1, // allow removal only if list has more than one item

                        onUpdate = { updated ->
                            // create a new list, replace item at idx, and assign back to trigger recomposition
                            //also is a scope function
                            //It lets you perform operations on an object and then returns the same object
                            schedule = schedule.toMutableList().also { it[idx] = updated }
                        },

                        onRemove = {
                            // create a new list, remove item at idx, and assign back to trigger recomposition
                            schedule = schedule.toMutableList().also { it.removeAt(idx) }
                        }
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                // If a course is selected, assign it to 'c'.
                // Otherwise, show an error message and exit the Button's onClick lambda early.
                val c = selCourse ?: run { errorMsg = "Select a course"; return@Button } //return@Button Exits only the Button’s onClick lambda not the composable
                //run is a scope function
                //What it does:
                //Executes a block of code
                //Returns the last expression
                /**
                 * Why Elvis (?:) needs run
                 * Elvis operator needs:
                 * LEFT ?: VALUE
                 * VALUE must be:
                 * a real value
                 * or something that produces a value
                 */
                val p = selProf   ?: run { errorMsg = "Select a professor"; return@Button }
                if (schedule.isEmpty()) { errorMsg = "Add at least one lecture"; return@Button }
                //Wrong Logic though (overlappting time ki bases pr krna chahye tha), phir bhi:
                // Extracts all days from the schedule, groups them by day,
                // filters only those days that appear more than once (duplicates),
                // and returns the set of duplicate day values.
                val dupDays = schedule
                    .map { it.day }                 // get list of days from schedule entries
                    .groupBy { it }                 // group same days together
                    .filter { it.value.size > 1 }   // keep only days with more than one entry
                    .keys                           // extract the duplicate day names
                if (dupDays.isNotEmpty()) { errorMsg = "Duplicate day: ${dupDays.first()}"; return@Button }
                onSave(CreateSlotRequest(
                    courseId    = c.id,
                    professorId = p.id,
                    slotName    = slotName.ifBlank { null },
                    maxCapacity = capacity.toIntOrNull() ?: 50,
                    // Creates a new list by transforming each existing schedule entry into a new LectureScheduleEntry object,
                    // keeping the same day, start time, and end time, while converting blank venue values into null,
                    // then reassigns the updated list to trigger recomposition in Compose.
                    /**
                     *map is a transformation function for collections (lists).
                     *
                     * It means:
                     *
                     * “Take every item in the list, transform it, and return a NEW list.”
                     *
                     */
                    schedule = schedule.map {
                        LectureScheduleEntry(
                            it.day,
                            it.startTime,
                            it.endTime,
                            it.venue.ifBlank { null }
                        )
                    }


                ))
            }) { Text("Create Slot") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ScheduleEntryCard(
    entry: DayScheduleEntry,
    index: Int,
    canRemove: Boolean,
    onUpdate: (DayScheduleEntry) -> Unit,
    onRemove: () -> Unit
) {
    var dayExpanded by remember { mutableStateOf(false) }
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Text("Lecture ${index + 1}", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                if (canRemove) IconButton(onClick = onRemove, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Delete, null, tint = RedBadge, modifier = Modifier.size(16.dp))
                }
            }
            ExposedDropdownMenuBox(expanded = dayExpanded, onExpandedChange = { dayExpanded = it }) {
                OutlinedTextField(
                    value = oop.project.unislotandroid.ui.screens.DAY_SHORT[entry.day] ?: entry.day,
                    onValueChange = {}, readOnly = true,
                    label = { Text("Day") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(dayExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    textStyle = LocalTextStyle.current.copy(fontSize = 13.sp)
                )
                // Dropdown menu that appears when 'dayExpanded' is true.
                // It lists all available days (ALL_DAYS) and allows the user to select one.
                // When a day is selected:
                // - The current entry is copied with the updated 'day' value
                // - The parent is notified via onUpdate()
                // - The dropdown is closed by setting 'dayExpanded' to false
                ExposedDropdownMenu(expanded = dayExpanded, onDismissRequest = { dayExpanded = false }) {

                    oop.project.unislotandroid.ui.screens.ALL_DAYS.forEach { d ->
                        DropdownMenuItem(
                            text = { Text(oop.project.unislotandroid.ui.screens.DAY_SHORT[d] ?: d) }, // shows short day name or fallback full name
                            onClick = {
                                onUpdate(entry.copy(day = d)) // update entry with new day (immutable update)
                                dayExpanded = false // close dropdown after selection
                            }
                        )
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                //Need to add constraint for  time format
                OutlinedTextField(value = entry.startTime, onValueChange = { onUpdate(entry.copy(startTime = it)) },
                    label = { Text("Start") }, placeholder = { Text("08:00") },
                    singleLine = true, modifier = Modifier.weight(1f),
                    textStyle = LocalTextStyle.current.copy(fontSize = 13.sp))
                OutlinedTextField(value = entry.endTime, onValueChange = { onUpdate(entry.copy(endTime = it)) },
                    label = { Text("End") }, placeholder = { Text("09:30") },
                    singleLine = true, modifier = Modifier.weight(1f),
                    textStyle = LocalTextStyle.current.copy(fontSize = 13.sp))
            }
            OutlinedTextField(value = entry.venue, onValueChange = { onUpdate(entry.copy(venue = it)) },
                label = { Text("Venue / Room") }, placeholder = { Text("e.g. Room 101, Lab 3") },
                singleLine = true, modifier = Modifier.fillMaxWidth(),
                textStyle = LocalTextStyle.current.copy(fontSize = 13.sp))
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// ADMIN STUDENTS
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun AdminStudentsScreen(x0: MainViewModel) {
    TODO("Not yet implemented")
}