package oop.project.unislotandroid.ui.screens

// Sealed class representing all navigation destinations (screens) in the app
// Each screen has a unique "route" string used by NavController for navigation
// Defines a closed set of all screens (navigation destinations) in the app
// route → unique string used by NavController (e.g., "admin/dashboard")
// Sealed class ensures:
// ✔ No invalid screens can be added outside this file
// ✔ Compiler knows all possible screen types
// ✔ Safer and more maintainable than raw strings
// ✔ More flexible than enum (can support arguments later)
sealed class Screen(val route: String) {

    // ── Authentication ─────────────────────────────────────────────

    // Login screen → shown when user is not authenticated
    object Login : Screen("login")


    // ── Admin Screens ──────────────────────────────────────────────

    // Admin Dashboard → main overview screen for admin
    object AdminDashboard : Screen("admin/dashboard")

    // Manage departments
    object AdminDepartments : Screen("admin/departments")

    // Manage degrees
    object AdminDegrees : Screen("admin/degrees")

    // Manage courses
    object AdminCourses : Screen("admin/courses")

    // Manage professors
    object AdminProfessors : Screen("admin/professors")

    // Manage time slots / schedule slots
    object AdminSlots : Screen("admin/slots")

    // Manage students
    object AdminStudents : Screen("admin/students")


    // ── Student Screens ────────────────────────────────────────────

    // Student Dashboard → main screen for student
    object StudentDashboard : Screen("student/dashboard")

    // View enrolled courses
    object StudentCourses : Screen("student/courses")

    // View personal timetable/schedule
    object StudentSchedule : Screen("student/schedule")
}
