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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import oop.project.unislotandroid.data.model.*
import oop.project.unislotandroid.ui.theme.GreenBadge
import oop.project.unislotandroid.ui.theme.RedBadge
import oop.project.unislotandroid.viewmodel.MainViewModel
import oop.project.unislotandroid.viewmodel.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDegreesScreen(vm: MainViewModel) {
    val deptsState   by vm.departments.collectAsState()
    val degreesList  by vm.degrees.collectAsState()
    val degreeDetails by vm.degreeDetails.collectAsState()
    val coursesState by vm.courses.collectAsState()

    LaunchedEffect(Unit) { vm.loadDepartments(); vm.loadCourses() }

    val depts   = (deptsState   as? UiState.Success)?.data ?: emptyList()
    val courses = (coursesState as? UiState.Success)?.data ?: emptyList()

    var selDept      by remember { mutableStateOf<DepartmentResponse?>(null) }
    var deptExpanded by remember { mutableStateOf(false) }
    var showNewDegree by remember { mutableStateOf(false) }
    var showAddCourse by remember { mutableStateOf<DegreeResponse?>(null) }
    var feedbackMsg  by remember { mutableStateOf("") }
    var isError      by remember { mutableStateOf(false) }

    LaunchedEffect(selDept) { selDept?.let { vm.loadDegrees(it.id) } }

    Column(Modifier.fillMaxSize()) {
        if (feedbackMsg.isNotBlank()) {
            if (isError) ErrorBanner(feedbackMsg) else SuccessBanner(feedbackMsg)
        }

        // Department picker
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically) {
                ExposedDropdownMenuBox(expanded = deptExpanded, onExpandedChange = { deptExpanded = it },
                    modifier = Modifier.weight(1f)) {
                    OutlinedTextField(
                        value = selDept?.let { "${it.name} (${it.code})" } ?: "Select Department…",
                        onValueChange = {}, readOnly = true,
                        label = { Text("Department") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(deptExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = deptExpanded, onDismissRequest = { deptExpanded = false }) {
                        depts.forEach { d ->
                            DropdownMenuItem(text = { Text("${d.name} (${d.code})") }, onClick = {
                                selDept = d; deptExpanded = false
                            })
                        }
                    }
                }
                if (selDept != null) {
                    FloatingActionButton(onClick = { showNewDegree = true },
                        modifier = Modifier.size(48.dp), containerColor = MaterialTheme.colorScheme.primary) {
                        Icon(Icons.Default.Add, null, tint = Color.White)
                    }
                }
            }
        }

        if (selDept == null) {
            EmptyState("Select a department to manage its degree programs")
            return@Column
        }

        if (degreesList.isEmpty()) {
            EmptyState("No degrees yet.\nTap + to create the first degree program.")
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(degreesList) { degree ->
                    val detail = degreeDetails[degree.id]
                    DegreeCard(
                        degree  = degree,
                        detail  = detail,
                        onAddCourse    = { showAddCourse = degree },
                        onRemoveCourse = { courseId, courseName ->
                            vm.removeCourseFromDegree(degree.id, courseId) { ok, msg ->
                                isError = !ok; feedbackMsg = msg
                            }
                        }
                    )
                }
                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }

    // New Degree Dialog
    if (showNewDegree && selDept != null) {
        NewDegreeDialog(
            departmentName = selDept!!.name,
            onDismiss = { showNewDegree = false },
            onSave = { name, code, years ->
                vm.createDegree(name, code, selDept!!.id, years) { ok, msg ->
                    isError = !ok; feedbackMsg = msg
                }
                showNewDegree = false
            }
        )
    }

    // Add Course to Degree Dialog
    showAddCourse?.let { deg ->
        val detail = degreeDetails[deg.id]
        val assignedIds =
            detail?.semesters                      // safely access semesters (null if detail is null)
                ?.flatMap { s ->
                    s.courses.map { it.id }        // for each semester, extract course IDs
                }                                  // flatMap flattens all lists into one list of IDs
                ?.toSet()                          // convert to Set to remove duplicates
                ?: emptySet()                      // if detail was null → return empty set
        val available = courses.filter { it.id !in assignedIds }
        AddCourseDialog(
            degree      = deg,
            courses     = available,
            onDismiss   = { showAddCourse = null },
            onSave      = { courseId, semester, compulsory ->
                vm.addCourseToDegree(deg.id, courseId, semester, compulsory, selDept!!.id) { ok, msg ->
                    isError = !ok; feedbackMsg = msg
                }
                showAddCourse = null
            }
        )
    }
}

@Composable
private fun DegreeCard(
    degree: DegreeResponse,
    detail: DegreeDetailResponse?,
    onAddCourse: () -> Unit,
    onRemoveCourse: (Long, String) -> Unit
) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.padding(16.dp)) {
            // Header
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(degree.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 3.dp)) {
                        StatusBadge(degree.code, "INFO")
                        Text("${degree.durationYears}-year · ${detail?.semesters?.sumOf { it.courses.size } ?: 0} courses",
                            fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                OutlinedButton(onClick = onAddCourse, contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)) {
                    Icon(Icons.Default.Add, null, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Add Course", fontSize = 12.sp)
                }
            }

            if (detail?.semesters != null && detail.semesters.isNotEmpty()) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))
                val sortedSems = detail.semesters.filter { it.courses.isNotEmpty() }.sortedBy { it.semesterNumber }
                sortedSems.forEach { sem ->
                    Text("Semester ${sem.semesterNumber}", fontSize = 11.sp, fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 5.dp, top = if (sem == sortedSems.first()) 0.dp else 8.dp))
                    sem.courses.forEach { c ->
                        Row(
                            Modifier.fillMaxWidth().padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("• ${c.courseCode} — ${c.name}", fontSize = 12.5.sp,
                                modifier = Modifier.weight(1f))
                            IconButton(
                                onClick = { onRemoveCourse(c.id, c.name) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Default.Delete, null, tint = RedBadge, modifier = Modifier.size(14.dp))
                            }
                        }
                    }
                }
            } else if (detail != null) {
                Spacer(Modifier.height(8.dp))
                Text("No courses assigned yet. Tap \"Add Course\" to build this degree's curriculum.",
                    fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
            }
        }
    }
}

@Composable
private fun NewDegreeDialog(
    departmentName: String,
    onDismiss: () -> Unit,
    onSave: (String, String, Int) -> Unit
) {
    var name  by remember { mutableStateOf("") }
    var code  by remember { mutableStateOf("") }
    var years by remember { mutableStateOf("4") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Column { Text("New Degree"); Text(departmentName, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) } },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it },
                    label = { Text("Degree Name *") }, placeholder = { Text("e.g. BS Computer Science") },
                    singleLine = true, modifier = Modifier.fillMaxWidth())
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(value = code, onValueChange = { code = it.uppercase() },
                        label = { Text("Code *") }, placeholder = { Text("BSCS") },
                        singleLine = true, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = years, onValueChange = { years = it },
                        label = { Text("Years") }, singleLine = true, modifier = Modifier.weight(1f))
                }

                //no need for this line though
                Text("A ${years.toIntOrNull() ?: 4}-year degree has ${(years.toIntOrNull() ?: 4) * 2} semesters",
                    fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
            }
        },
        confirmButton = {
            Button(onClick = {
                if (name.isNotBlank() && code.isNotBlank())
                    onSave(name.trim(), code.trim(), years.toIntOrNull() ?: 4)
            }) { Text("Create Degree") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddCourseDialog(
    degree: DegreeResponse,
    courses: List<CourseResponse>,
    onDismiss: () -> Unit,
    onSave: (Long, Int, Boolean) -> Unit
) {
    var selCourse   by remember { mutableStateOf<CourseResponse?>(null) }
    var semester    by remember { mutableStateOf("1") }
    var compulsory  by remember { mutableStateOf(true) }
    var cExpanded   by remember { mutableStateOf(false) }
    val maxSem = degree.durationYears * 2

    //spacedBy = fixed gaps
    //SpaceBetween = push items to edges
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Column { Text("Add Course"); Text("to ${degree.name}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) } },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                ExposedDropdownMenuBox(expanded = cExpanded, onExpandedChange = { cExpanded = it }) {
                    OutlinedTextField(
                        value = selCourse?.let { "${it.courseCode} — ${it.name}" } ?: "Select Course *",
                        onValueChange = {}, readOnly = true,
                        label = { Text("Course") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(cExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = cExpanded, onDismissRequest = { cExpanded = false }) {
                        if (courses.isEmpty()) {
                            DropdownMenuItem(text = { Text("All courses already assigned", color = MaterialTheme.colorScheme.onSurfaceVariant) }, onClick = {})
                        } else {
                            courses.forEach { c ->
                                DropdownMenuItem(
                                    text = { Text("${c.courseCode} — ${c.name} (${c.creditHours} cr)") },
                                    onClick = { selCourse = c; cExpanded = false }
                                )
                            }
                        }
                    }
                }
                OutlinedTextField(
                    value = semester,
                    onValueChange = {
                        val n = it.toIntOrNull()
                        if (n == null || (n in 1..maxSem)) semester = it
                    },
                    label = { Text("Semester (1–$maxSem) *") },
                    singleLine = true, modifier = Modifier.fillMaxWidth()
                )
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Checkbox(checked = compulsory, onCheckedChange = { compulsory = it })
                    Text("Compulsory course", fontSize = 13.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val c = selCourse ?: return@Button
                    val s = semester.toIntOrNull() ?: return@Button
                    if (s in 1..maxSem) onSave(c.id, s, compulsory)
                },
                enabled = selCourse != null && semester.toIntOrNull()?.let { it in 1..maxSem } == true
            ) { Text("Add Course") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
