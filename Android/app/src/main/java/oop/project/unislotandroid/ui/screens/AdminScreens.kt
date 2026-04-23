package oop.project.unislotandroid.ui.screens

import androidx.compose.runtime.Composable
import oop.project.unislotandroid.viewmodel.MainViewModel
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import oop.project.unislotandroid.ui.theme.GreenBadge
import oop.project.unislotandroid.ui.theme.RedBadge
import oop.project.unislotandroid.viewmodel.UiState
import kotlin.compareTo

// ─────────────────────────────────────────────────────────────────────────────
// ADMIN DASHBOARD
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun AdminDashboardScreen(vm: MainViewModel) {
    // Collecting UI state from ViewModel StateFlows and converting them into Compose State.
    // This ensures the UI automatically recomposes whenever the underlying data changes.
    val studentsState  by vm.students.collectAsState()
    val profsState     by vm.professors.collectAsState()
    val deptsState     by vm.departments.collectAsState()
    val coursesState   by vm.courses.collectAsState()

    // Trigger initial data loading when the composable first enters composition.
    // LaunchedEffect(Unit) ensures these API calls run only once and not on recomposition.
    LaunchedEffect(Unit) {
        vm.loadStudents(); vm.loadProfessors()
        vm.loadDepartments(); vm.loadCourses()
    }

    //as? → safe type casting
    //?. → safe access
    //?: → fallback default value
    val students = (studentsState as? UiState.Success)?.data ?: emptyList()
    val professors = (profsState     as? UiState.Success)?.data ?: emptyList()
    val depts      = (deptsState     as? UiState.Success)?.data ?: emptyList()
    val courses    = (coursesState   as? UiState.Success)?.data ?: emptyList()
    val paid       = students.count { it.feeStatus == "PAID" } //Count how many items in the list satisfy a condition”
    //So instead of looping manually, Kotlin gives you count.

    LazyColumn(
        contentPadding     = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Overview", fontWeight = FontWeight.Bold, fontSize = 18.sp,
                modifier = Modifier.padding(bottom = 4.dp))
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard("Departments", "${depts.size}", modifier = Modifier.weight(1f))
                StatCard("Courses",     "${courses.size}", modifier = Modifier.weight(1f))
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard("Professors", "${professors.size}", modifier = Modifier.weight(1f))
                StatCard("Students",   "${students.size}", modifier = Modifier.weight(1f))
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard("Fee Paid",   "$paid",
                    color = GreenBadge, modifier = Modifier.weight(1f))
                StatCard("Unpaid",     "${students.size - paid}",
                    color = RedBadge,  modifier = Modifier.weight(1f))
            }
        }
        if (professors.isNotEmpty()) {
            //item {} → single composable
            //items {} → multiple composables from a list.It takes your list and loops over it, creating UI for each element.
            item { SectionHeader("Top Professors by Fill Rate") }
            //“Take professors → sort them by fill rate (highest first) → keep top 5 → display them in LazyColumn”
            items(professors.sortedByDescending { it.fillRatePercent }.take(5)) { p ->
                Card(Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(1.dp)) {
                    Row(
                        Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(p.name, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                            Text(p.departmentName ?: "—", fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        StatusBadge("${String.format("%.1f", p.fillRatePercent)}%", //“Convert fillRatePercent into a 1-decimal percentage string"
                            if (p.fillRatePercent >= 80) "PAID" else if (p.fillRatePercent >= 50) "PARTIAL" else "UNPAID")
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// ADMIN DEPARTMENTS
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun AdminDepartmentsScreen(x0: MainViewModel) {
    TODO("Not yet implemented")
}

@Composable
private fun DepartmentDialog(
    initial: oop.project.unislotandroid.data.model.DepartmentResponse?,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var code by remember { mutableStateOf(initial?.code ?: "") }

    //AlertDialog is a prebuilt UI component in Jetpack Compose used to show a popup dialog on top of the screen.
    // Basic Structure:
    //AlertDialog(
    //    onDismissRequest = { }, Called when:User taps outside dialog. Presses back button. Used to close dialog
    //    title = { }, Displays heading of dialog
    //    text = { }, Main content area. Can contain: 1) Text 2) Column 3) Input fields (like our form)
    //    confirmButton = { }, Positive action button Examples: 1)Save 2)OK 3)Delete
    //    dismissButton = { } Cancel / negative action Examples: 1)Cancel 2)No
    //)

    AlertDialog(
        onDismissRequest = onDismiss,
        title  = { Text(if (initial == null) "New Department" else "Edit Department") },
        text   = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,                    // current state
                    onValueChange = { name = it },   // updates state
                    label = { Text("Department Name") }, // UI label
                    singleLine = true,               // restrict to one line
                    modifier = Modifier.fillMaxWidth()   // layout width
                )
                OutlinedTextField(value = code, onValueChange = { code = it.uppercase() },
                    label = { Text("Code (e.g. CS)") }, singleLine = true,
                    modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(onClick = { if (name.isNotBlank() && code.isNotBlank()) onSave(name.trim(), code.trim()) }) {
                Text("Save")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
// ─────────────────────────────────────────────────────────────────────────────
// ADMIN COURSES
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun AdminCoursesScreen(x0: MainViewModel) {
    TODO("Not yet implemented")
}

@Composable
private fun CourseDialog(onDismiss: () -> Unit, onSave: (String, String, Int, String?) -> Unit) {
    /**
     * by keyword
     *
     * Kotlin delegation
     *
     * Instead of:
     *
     * val creditsState = remember { mutableStateOf("3") }
     * creditsState.value = "4"
     *
     * You write:
     *
     * var credits by remember { mutableStateOf("3") }
     *
     * Now you can use it like a normal variable
     */
    var name    by remember { mutableStateOf("") }
    var code    by remember { mutableStateOf("") }
    var credits by remember { mutableStateOf("3") } //Initial value = "3"
    var desc    by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Course") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it },
                    label = { Text("Course Name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = code, onValueChange = { code = it.uppercase() },
                    label = { Text("Code (e.g. CS301)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = credits, onValueChange = { credits = it },
                    label = { Text("Credit Hours") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = desc, onValueChange = { desc = it },
                    label = { Text("Description (optional)") }, modifier = Modifier.fillMaxWidth(), maxLines = 3)
            }
        },
        confirmButton = {
            Button(onClick = {
                val cr = credits.toIntOrNull() ?: 3 //“Convert credits(String) to integer safely; if it fails, use 3 as default”
                if (name.isNotBlank() && code.isNotBlank()) onSave(name.trim(), code.trim(), cr, desc.ifBlank { null }/*Converts empty input → null*/)
            }) { Text("Create") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
// ─────────────────────────────────────────────────────────────────────────────
// ADMIN PROFESSORS
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun AdminProfessorsScreen(vm: MainViewModel) {
    val state by vm.professors.collectAsState()
    val depts by vm.departments.collectAsState()
    LaunchedEffect(Unit) { vm.loadProfessors(); vm.loadDepartments() }

    var showDialog  by remember { mutableStateOf(false) }
    var feedbackMsg by remember { mutableStateOf("") }
    var isError     by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize()) {
        if (feedbackMsg.isNotBlank()) {
            if (isError) ErrorBanner(feedbackMsg) else SuccessBanner(feedbackMsg)
        }
        when (state) {
            is UiState.Loading -> LoadingScreen()
            is UiState.Success -> {
                val items = (state as UiState.Success).data
                LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    item {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically) {
                            Text("${items.size} Professor(s)", fontWeight = FontWeight.SemiBold)
                            FloatingActionButton(onClick = { showDialog = true },
                                modifier = Modifier.size(40.dp), containerColor = MaterialTheme.colorScheme.primary) {
                                Icon(Icons.Default.Add, null, tint = Color.White)
                            }
                        }
                    }
                    items(items) { prof ->
                        Card(Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(1.dp)) {
                            Row(Modifier.padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Text(prof.name, fontWeight = FontWeight.Medium)
                                    Text(prof.email, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    if (prof.departmentName != null)
                                        Text(prof.departmentName, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                StatusBadge(
                                    "${String.format("%.0f", prof.fillRatePercent)}%", //Convert a decimal number into a rounded percentage string.
                                    if (prof.fillRatePercent >= 80) "PAID" else "PARTIAL"
                                )
                            }
                        }
                    }
                }
            }
            else -> {}
        }
    }

    if (showDialog) {
        val deptList = (depts as? UiState.Success)?.data ?: emptyList()
        oop.project.unislotandroid.ui.screens.ProfessorDialog(
            departments = deptList,
            onDismiss = { showDialog = false },
            onSave = { name, email, qual, deptId ->
                vm.createProfessor(name, email, qual, deptId) { ok, msg ->
                    isError = !ok; feedbackMsg = msg
                }
                showDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfessorDialog(
    departments: List<oop.project.unislotandroid.data.model.DepartmentResponse>,
    onDismiss: () -> Unit,
    onSave: (String, String, String?, Long?) -> Unit
) {
    // Local UI state for form inputs
    var name    by remember { mutableStateOf("") }
    var email   by remember { mutableStateOf("") }
    var qual    by remember { mutableStateOf("") }
    var deptId  by remember { mutableStateOf<Long?>(null) }
    var expanded by remember { mutableStateOf(false) } // controls dropdown visibility
    // Derived value: finds the department object based on selected deptId
    // Recomputed on every recomposition (not stored state)
    val selectedDept = departments.find { it.id == deptId }

    AlertDialog(
        onDismissRequest = onDismiss,// Called when user taps outside or presses back → closes dialog
        title = { Text("New Professor") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it },
                    label = { Text("Full Name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = email, onValueChange = { email = it },
                    label = { Text("Email") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = qual, onValueChange = { qual = it },
                    label = { Text("Qualification (optional)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                //Simple mental structure
                //ExposedDropdownMenuBox
                //    ├── OutlinedTextField (anchor / clickable field)
                //    └── ExposedDropdownMenu (dropdown list)
                //            ├── DropdownMenuItem
                //            └── DropdownMenuItem (loop)
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded/*(// toggles dropdown open/close when field is clicked)*/ = it }) {
                    // Anchor field (looks like input but acts like dropdown)
                    OutlinedTextField(
                        value = selectedDept?.name
                            ?: "Select department (optional)",
                        // Shows selected department name OR placeholder

                        onValueChange = {}, // no typing allowed
                        readOnly = true,    // prevents keyboard

                        label = { Text("Department") },

                        // Dropdown arrow icon (rotates automatically)
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded)
                        },

                        modifier = Modifier
                            .menuAnchor()   // attaches menu to this field
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        DropdownMenuItem(text = { Text("None") }, onClick = { deptId = null; expanded = false })
                        departments.forEach { d ->
                            DropdownMenuItem(
                                text    = { Text(d.name) },
                                onClick = { deptId = d.id; expanded = false }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                if (name.isNotBlank() && email.isNotBlank())
                    onSave(name.trim(), email.trim(), qual.ifBlank { null }, deptId)
            }) { Text("Create") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}


