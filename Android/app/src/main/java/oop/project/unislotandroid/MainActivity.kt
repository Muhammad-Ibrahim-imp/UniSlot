package oop.project.unislotandroid

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.Login
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.*
import oop.project.unislotandroid.data.api.RetrofitClient
import oop.project.unislotandroid.ui.screens.*
import oop.project.unislotandroid.ui.theme.UniSlotAndroidTheme
import oop.project.unislotandroid.viewmodel.MainViewModel
import kotlinx.coroutines.launch
import oop.project.unislotandroid.ui.screens.AppTopBar

// MainActivity is the entry point (first screen launcher) of the Android app
// It inherits from ComponentActivity which is the base class for Compose-based activities
class MainActivity : ComponentActivity() {//This means: “MainActivity is an Android Activity that supports Jetpack Compose"

    private val vm: MainViewModel by viewModels() //This line creates a lifecycle-aware ViewModel tied to the Activity,
    // ensuring data survives configuration changes and is shared safely across UI recompositions.

    // Called when the Activity is first created
    // This is the main lifecycle entry point where UI setup happens
    //Bundle is a special Android class used to:
    //Store and pass small pieces of data between Android components (like Activities, Fragments, and system callbacks).
    override fun onCreate(savedInstanceState: Bundle?) {
        // A Bundle that holds previously saved UI state of the Activity
        // It is used to restore the Activity after it was destroyed and recreated
        // (e.g., screen rotation, process kill by system)
        // Android passes it automatically


        // Calls the parent class implementation of onCreate()
        // This is REQUIRED so Android can properly initialize the Activity lifecycle
        // It restores saved state (if any) and sets up the base system behavior
        super.onCreate(savedInstanceState)


// Initializes Retrofit client and related dependencies
// Uses applicationContext to ensure a global, non-leaking context
// Typically sets up:
// - Base URL
// - OkHttp client
// - Interceptors (like auth token)
// - DataStore / token storage access
        RetrofitClient.init(applicationContext)

        // Entry point for Jetpack Compose UI in this Activity
        setContent {

            // Applies the app's Material theme (colors, typography, shapes)
            // Everything inside this block will follow SlotSelectorTheme styling
            UniSlotAndroidTheme {

                // Root composable of the entire app UI
                // vm (ViewModel) is passed so UI can:
                // - observe state (StateFlow)
                // - trigger actions (login, load data, etc.)
                AppRoot(vm)
            }
        }
    }
}

// Represents a single item in the app's navigation (e.g., Bottom Navigation or Drawer)
// Each item defines:
// - route: navigation destination identifier
// - label: text shown in UI
// - icon: visual icon shown in UI
data class NavItem(val route: String, val label: String, val icon: ImageVector)

private val adminNavItems = listOf(
    NavItem(Screen.AdminDashboard.route,   "Dashboard",    Icons.Default.Home),
    NavItem(Screen.AdminDepartments.route, "Departments",  Icons.Default.AccountBalance),
    NavItem(Screen.AdminDegrees.route,     "Degrees",      Icons.Default.School),
    NavItem(Screen.AdminCourses.route,     "Courses",      Icons.Default.MenuBook),
    NavItem(Screen.AdminProfessors.route,  "Professors",   Icons.Default.Person),
    NavItem(Screen.AdminSlots.route,       "Slots",        Icons.Default.Schedule),
    NavItem(Screen.AdminStudents.route,    "Students",     Icons.Default.Group),
)

private val studentNavItems = listOf(
    NavItem(Screen.StudentDashboard.route, "Dashboard",    Icons.Default.Home),
    NavItem(Screen.StudentCourses.route,   "My Courses",   Icons.Default.MenuBook),
    NavItem(Screen.StudentSchedule.route,  "My Schedule",  Icons.Default.CalendarMonth),
)

// Opt-in annotation used to allow usage of experimental APIs
// In this case, it enables experimental Material 3 components in Jetpack Compose
@OptIn(ExperimentalMaterial3Api::class)
// Marks a function as a Jetpack Compose UI function
// This tells the compiler that this function builds UI instead of returning a value
@Composable
fun AppRoot(vm: MainViewModel) {
    // Observes the token StateFlow from ViewModel and converts it into Compose State
    // Whenever token changes, this composable will automatically recompose
    val token = vm.token.collectAsState().value

    // Observes the user role (e.g. ADMIN / STUDENT) from ViewModel as Compose State
    val role = vm.role.collectAsState().value

    // Creates and remembers a NavController for handling navigation between screens
    // rememberNavController() ensures the controller survives recompositions
    val nav = rememberNavController()

    // Determine start destination
    val start = when {
        token == null -> Screen.Login.route
        role == "ADMIN" -> Screen.AdminDashboard.route
        else -> Screen.StudentDashboard.route
    }

    //A Drawer is a side panel menu that slides in from the left (or sometimes right) of the screen.
    // Creates and remembers the state of the Navigation Drawer (open/closed)
    // DrawerValue.Closed means the drawer starts in closed position
    // rememberDrawerState ensures the state survives recompositions
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    // Creates a CoroutineScope tied to the lifecycle of this composable
    // Used to launch coroutines (async tasks) safely inside Jetpack Compose
    // remember ensures the scope survives recompositions
    val scope = rememberCoroutineScope()
    //A back stack is a history of screens (or pages) the user has visited, stored in order.
    //currentBackStackEntry = current page
    val currentBackStack by nav.currentBackStackEntryAsState()
    // Extracts the current screen's route (string) from the navigation back stack
    // Safe calls (?.) prevent crashes if any part is null
    val currentRoute = currentBackStack?.destination?.route

    // Decide whether to show the drawer
    val showDrawer = token != null
    val navItems   = if (role == "ADMIN") adminNavItems else studentNavItems

    val mainContent = @Composable {
        // A Material Design layout component that provides a basic screen structure
        // It handles common UI elements like TopBar, BottomBar, Drawer, FAB, etc.
        Scaffold(
            // topBar = { ... },
            // bottomBar = { ... },
            // floatingActionButton = { ... },
            // drawerContent = { ... },
            // content = { padding -> ... }
            topBar = {
                if (showDrawer) {
                    val title = navItems.find { it.route == currentRoute }?.label
                        ?: if (role == "ADMIN") "Admin" else "Student"
                    // Custom Top App Bar composable
                    // - Displays the screen title with an emoji prefix
                    // - Accepts a lambda (callback) that is triggered when the menu icon is clicked
                    AppTopBar(title = "🎓 $title") {

                        // Launch a coroutine in UI scope to perform a suspend operation
                        // drawerState.open() is a suspend function → cannot be called directly
                        // So we use scope.launch { ... }
                            scope.launch {
                                drawerState.open()   // Opens the navigation drawer
                            }
                            //launch            → manual coroutine (you control when it runs)
                            //LaunchedEffect    → lifecycle-aware side-effect (Compose controls when it runs)
                    }
                }
            }
        ) { innerPadding ->
            NavHost(
                navController    = nav,
                startDestination = start,
                modifier         = Modifier.padding(innerPadding)
            ) {
                composable(Screen.Login.route) {
                    LoginScreen(vm) { r ->
                        val dest = if (r == "ADMIN") Screen.AdminDashboard.route
                        else Screen.StudentDashboard.route
                        nav.navigate(dest) {
                            //popUpTo = delete history up to a point
                            //inclusive = also delete that point or not
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                }

                // ── Admin ──────────────────────────────────────────────────
                composable(Screen.AdminDashboard.route)   { AdminDashboardScreen(vm) }
                composable(Screen.AdminDepartments.route) { AdminDepartmentsScreen(vm) }
                composable(Screen.AdminDegrees.route)     { AdminDegreesScreen(vm) }
                composable(Screen.AdminCourses.route)     { AdminCoursesScreen(vm) }
                composable(Screen.AdminProfessors.route)  { AdminProfessorsScreen(vm) }
                composable(Screen.AdminSlots.route)       { AdminSlotsScreen(vm) }
                composable(Screen.AdminStudents.route)    { AdminStudentsScreen(vm) }

                // ── Student ────────────────────────────────────────────────
                // StudentDashboardScreen is different from other screens because it is a navigation hub.
                // Instead of only displaying UI, it also triggers navigation to other student-related screens.
                //
                // The lambda { route -> nav.navigate(route) } is passed as a callback:
                // - It allows this screen to request navigation without directly depending on NavController inside the UI.
                // - When the user clicks an action (e.g., "My Courses", "Schedule"),
                //   the screen calls this lambda with a route string.
                //
                // Flow:
                // StudentDashboardScreen → calls lambda(route) → NavHost performs nav.navigate(route)
                //
                // This keeps UI decoupled from navigation logic and follows clean architecture principles
                composable(Screen.StudentDashboard.route) {
                    StudentDashboardScreen(vm) { route -> nav.navigate(route) }
                }
                composable(Screen.StudentCourses.route)  { StudentCoursesScreen(vm) }
                composable(Screen.StudentSchedule.route) { StudentScheduleScreen(vm) }
            }
        }
    }

    if (showDrawer) {
        ModalNavigationDrawer(
            drawerState   = drawerState,
            drawerContent = {
                ModalDrawerSheet(modifier = Modifier.width(260.dp)) {
                    // Header
                    Column(Modifier.padding(20.dp)) {
                        Text("🎓 UniSlot", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Text(
                            if (role == "ADMIN") "Admin Panel" else "Student Portal",
                            fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            vm.email.collectAsState().value ?: "",
                            fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    // HorizontalDivider draws a thin horizontal line
                    // Used to visually separate sections inside the drawer (e.g., header vs menu items)
                    HorizontalDivider()

                    // Spacer adds empty space between UI elements
                    // Here it creates vertical spacing of 8.dp to improve layout readability
                    Spacer(Modifier.height(8.dp))

                    // Loop through all navigation items (Admin or Student menu list)
                    // Each item represents one screen in the app (Dashboard, Courses, etc.)
                    navItems.forEach { item ->

                        // Creates a clickable item inside the Navigation Drawer
                        NavigationDrawerItem(

                            // Icon shown next to the label (e.g., Home, Courses, etc.)
                            icon = { Icon(item.icon, null) },

                            // Text label displayed in the drawer (e.g., "Dashboard")
                            label = { Text(item.label) },

                            // Highlights the currently selected item based on active route
                            // If currentRoute matches this item's route → it becomes selected (highlighted)
                            selected = currentRoute == item.route,

                            // Action when user clicks on a drawer item
                            onClick = {

                                // First close the drawer (runs in coroutine because it's a suspend function)
                                scope.launch { drawerState.close() }

                                // Navigate to selected screen
                                nav.navigate(item.route) {

                                    // Ensures only one instance of the destination exists in back stack
                                    launchSingleTop = true

                                    // Restores previous state if user returns to this screen later
                                    restoreState = true
                                }
                            },

                            // Adds horizontal padding so items don't touch drawer edges
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                    }

                    // Spacer is an invisible composable used to create flexible empty space in layouts.
                    //
                    // Modifier.weight(1f) tells the Spacer to take all remaining available space
                    // in the parent Column/Row.
                    //
                    // This is commonly used to push content apart (e.g., push logout button to bottom).
                    Spacer(Modifier.weight(1f))
                    HorizontalDivider()
                    NavigationDrawerItem(
                        icon     = { Icon(Icons.Default.Logout, null) },
                        label    = { Text("Sign out") },
                        selected = false,
                        // Action when user clicks "Sign out"
                        onClick = {

                            // Close the drawer (must be inside coroutine because it's suspend)
                            scope.launch { drawerState.close() }

                            // Clear user session (token, role, email, etc.)
                            vm.logout()

                            // Navigate back to Login screen
                            nav.navigate(Screen.Login.route) {

                                // popUpTo(0) clears the entire back stack
                                // so user cannot go back to previous screens using back button
                                popUpTo(0) { inclusive = true }
                            }
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
                    )
                }
            }
        ) {
            // Calls and renders the composable UI stored in the mainContent lambda.
            //
            // mainContent is a reusable composable function variable that contains:
            // - Scaffold (layout structure)
            // - NavHost (navigation system)
            // - TopBar / Drawer / Screens
            //
            // Calling mainContent() actually triggers Jetpack Compose to build and display the entire UI tree.
            mainContent()
        }
    } else {
        mainContent()
    }
}
