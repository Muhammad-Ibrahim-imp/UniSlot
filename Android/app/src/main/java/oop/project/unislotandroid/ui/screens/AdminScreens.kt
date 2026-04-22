package oop.project.unislotandroid.ui.screens

import androidx.compose.runtime.Composable
import oop.project.unislotandroid.viewmodel.MainViewModel
import androidx.compose.foundation.layout.*
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

@Composable
fun AdminProfessorsScreen(x0: MainViewModel) {
    TODO("Not yet implemented")
}

@Composable
fun AdminCoursesScreen(x0: MainViewModel) {
    TODO("Not yet implemented")
}

@Composable
fun AdminDepartmentsScreen(x0: MainViewModel) {
    TODO("Not yet implemented")
}

