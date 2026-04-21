package oop.project.unislotandroid.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import oop.project.unislotandroid.data.api.RetrofitClient
import oop.project.unislotandroid.data.model.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
// A sealed class is a restricted class hierarchy mechanism in programming (notably Java and Kotlin) that limits which other classes can extend or inherit from it
// out means covariance. In simple words: “This class will only PRODUCE (return) values of type T, not consume them.”
// A sealed class representing different UI states
// 'out T' means this class only PRODUCES values of type T (covariant)
sealed class UiState<out T> {

    // Represents an "idle" state:
    // - Nothing is happening yet (initial state)
    // - No data is associated with this state
    // - 'Nothing' means this state never holds any value
    object Idle : UiState<Nothing>()

    // Represents a loading state:
    // - Typically used when an API call or background task is in progress
    // - No data is available yet
    object Loading : UiState<Nothing>()

    // Represents a success state:
    // - Contains actual data of type T
    // - Example: API response, list of items, etc.
    data class Success<T>(val data: T) : UiState<T>()

    // Represents an error state:
    // - Contains an error message explaining what went wrong
    // - Does NOT hold data, so we use Nothing
    data class Error(val message: String) : UiState<Nothing>()
}

class MainViewModel(app: Application) : AndroidViewModel(app) {

    // Get the API service instance (Retrofit interface)
// This is used to make network calls (login, fetch data, etc.)
    private val api = RetrofitClient.api

    // Get token storage (likely backed by DataStore or SharedPreferences)
// This handles saving and retrieving auth-related data
    private val tokens = RetrofitClient.getTokenStorage()

    // Convert token Flow into StateFlow
// - tokenFlow: emits token updates (Flow<String?>)
// - stateIn: converts Flow → StateFlow (hot stream, holds latest value)
// - viewModelScope: lifecycle-aware (auto cancels when ViewModel is cleared)
// - SharingStarted.Eagerly: starts collecting immediately
// - null: initial value before first emission
    val token = tokens.tokenFlow.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        null
    )

    // Same pattern for user role (e.g., "admin", "student")
    val role = tokens.roleFlow.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        null
    )

    // Same pattern for user email
    val email = tokens.emailFlow.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        null
    )
    //Cold stream = starts producing data only when someone collects it(Flow)
    //Hot stream  = produces data regardless of collectors(StateFlow)

    // ── Auth ──────────────────────────────────────────────────────────────────
    // Internal mutable state (ONLY the ViewModel can change this)
    private val _loginState = MutableStateFlow<UiState<AuthResponse>>(UiState.Idle)

    // Public read-only state (UI can only observe, not modify)
    val loginState: StateFlow<UiState<AuthResponse>> = _loginState

    // Function to perform login
// Runs inside a coroutine tied to ViewModel lifecycle
    fun login(email: String, password: String) = viewModelScope.launch {

        // Step 1: Update UI state → show loading indicator
        _loginState.value = UiState.Loading

        try {
            // Step 2: Make API request using Retrofit
            // Sends email & password to backend
            val res = api.login(LoginRequest(email, password))

            // Step 3: Check if API response indicates success
            if (res.success && res.data != null) {

                // Save token and user info locally (DataStore / persistence)
                // So user stays logged in
                tokens.save(res.data.token, res.data.email, res.data.role)

                // Step 4: Update UI state → success with data
                _loginState.value = UiState.Success(res.data)

            } else {
                // API responded but login failed (e.g., wrong credentials)
                _loginState.value = UiState.Error(
                    res.message ?: "Login failed"
                )//?: is known as the Elvis operator. It provides a concise way to handle null safety by specifying a fallback value if an expression evaluates to null
            }

        } catch (e: Exception) {

            // Step 5: Handle exceptions (network issues, timeout, etc.)
            _loginState.value = UiState.Error(
                e.message ?: "Network error"
            )
        }
    }
    // Clears stored user session (token, email, role, etc.)
    // Used when user logs out
    fun logout() = viewModelScope.launch { tokens.clear() }
    // Resets login UI state back to initial (Idle)
    // Used after logout or navigation reset
    fun resetLogin() { _loginState.value = UiState.Idle }

    // ── Departments ───────────────────────────────────────────────────────────
    private val _departments = MutableStateFlow<UiState<List<DepartmentResponse>>>(UiState.Idle)
    val departments: StateFlow<UiState<List<DepartmentResponse>>> = _departments

    fun loadDepartments() = viewModelScope.launch {
        _departments.value = UiState.Loading
        // runCatching wraps the API call in a try-catch internally
        runCatching { api.getDepartments() }
            .onSuccess { _departments.value = UiState.Success(it.data ?: emptyList()) }
            .onFailure { _departments.value = UiState.Error(it.message ?: "Error") }
    }
    fun createDepartment(name: String, code: String, onDone: (Boolean, String) -> Unit) = viewModelScope.launch {
        //“Give me a function that takes (Boolean, String) and returns nothing.”
        //
        //So onDone is just a function parameter.
        runCatching { api.createDepartment(CreateDepartmentRequest(name, code)) }
            .onSuccess { loadDepartments(); onDone(true, "Department created") }
            .onFailure { onDone(false, it.message ?: "Error") }
    }
    fun updateDepartment(id: Long, name: String, code: String, onDone: (Boolean, String) -> Unit) = viewModelScope.launch {
        runCatching { api.updateDepartment(id, CreateDepartmentRequest(name, code)) }
            .onSuccess { loadDepartments(); onDone(true, "Updated") }
            .onFailure { onDone(false, it.message ?: "Error") }
    }
    fun deleteDepartment(id: Long, onDone: (Boolean, String) -> Unit) = viewModelScope.launch {
        runCatching { api.deleteDepartment(id) }
            .onSuccess { loadDepartments(); onDone(true, "Deleted") }
            .onFailure { onDone(false, it.message ?: "Error") }
    }

    // ── Degrees ───────────────────────────────────────────────────────────────
    private val _degrees = MutableStateFlow<List<DegreeResponse>>(emptyList())
    val degrees: StateFlow<List<DegreeResponse>> = _degrees
    private val _degreeDetails = MutableStateFlow<Map<Long, DegreeDetailResponse>>(emptyMap())
    val degreeDetails: StateFlow<Map<Long, DegreeDetailResponse>> = _degreeDetails

    fun loadDegrees(deptId: Long) = viewModelScope.launch {
        runCatching { api.getDegreesByDepartment(deptId) }.onSuccess { res ->
            val list = res.data ?: emptyList(); _degrees.value = list
            val map = _degreeDetails.value.toMutableMap()
            list.forEach { deg -> runCatching { api.getDegreeById(deg.id) }.onSuccess { r -> r.data?.let { map[deg.id] = it } } }
            //This line safely stores API response data into a map only when the data is non-null, preventing null pointer crashes and keeping code concise
            _degreeDetails.value = map
        }
    }
    fun createDegree(name: String, code: String, deptId: Long, years: Int, onDone: (Boolean, String) -> Unit) = viewModelScope.launch {
        runCatching { api.createDegree(CreateDegreeRequest(name, code, deptId, years)) }
            .onSuccess { loadDegrees(deptId); onDone(true, "Degree created") }
            .onFailure { onDone(false, it.message ?: "Error") }
    }
    fun addCourseToDegree(degreeId: Long, courseId: Long, semester: Int, compulsory: Boolean, deptId: Long, onDone: (Boolean, String) -> Unit) = viewModelScope.launch {
        runCatching { api.addCourseToDegree(AddCourseToDegreeRequest(degreeId, courseId, semester, compulsory)) }
            .onSuccess {
                runCatching { api.getDegreeById(degreeId) }.onSuccess { r -> r.data?.let { d -> _degreeDetails.value = _degreeDetails.value.toMutableMap().also { it[degreeId] = d } } }
                onDone(true, "Course added")
            }.onFailure { onDone(false, it.message ?: "Error") }
    }
    fun removeCourseFromDegree(degreeId: Long, courseId: Long, onDone: (Boolean, String) -> Unit) = viewModelScope.launch {
        runCatching { api.removeCourseFromDegree(degreeId, courseId) }
            .onSuccess {
                runCatching { api.getDegreeById(degreeId) }.onSuccess { r -> r.data?.let { d -> _degreeDetails.value = _degreeDetails.value.toMutableMap().also { it[degreeId] = d } } }
                onDone(true, "Course removed")
            }.onFailure { onDone(false, it.message ?: "Error") }
    }

    // ── Courses ───────────────────────────────────────────────────────────────
    private val _courses = MutableStateFlow<UiState<List<CourseResponse>>>(UiState.Idle)
    val courses: StateFlow<UiState<List<CourseResponse>>> = _courses

    fun loadCourses() = viewModelScope.launch {
        _courses.value = UiState.Loading
        runCatching { api.getCourses() }
            .onSuccess { _courses.value = UiState.Success(it.data ?: emptyList()) }
            .onFailure { _courses.value = UiState.Error(it.message ?: "Error") }
    }
    fun createCourse(name: String, code: String, credits: Int, desc: String?, onDone: (Boolean, String) -> Unit) = viewModelScope.launch {
        runCatching { api.createCourse(CreateCourseRequest(name, code, credits, desc)) }
            .onSuccess { loadCourses(); onDone(true, "Course created") }
            .onFailure { onDone(false, it.message ?: "Error") }
    }

    // ── Professors ────────────────────────────────────────────────────────────
    private val _professors = MutableStateFlow<UiState<List<ProfessorResponse>>>(UiState.Idle)
    val professors: StateFlow<UiState<List<ProfessorResponse>>> = _professors
    private val _deptProfessors = MutableStateFlow<List<ProfessorResponse>>(emptyList())
    val deptProfessors: StateFlow<List<ProfessorResponse>> = _deptProfessors

    fun loadProfessors() = viewModelScope.launch {
        _professors.value = UiState.Loading
        runCatching { api.getProfessors() }
            .onSuccess { _professors.value = UiState.Success(it.data ?: emptyList()) }
            .onFailure { _professors.value = UiState.Error(it.message ?: "Error") }
    }
    fun loadProfessorsByDept(deptId: Long) = viewModelScope.launch {
        runCatching { api.getProfessorsByDept(deptId) }.onSuccess { _deptProfessors.value = it.data ?: emptyList() }
    }
    fun loadAllProfessorsIntoDept() = viewModelScope.launch {
        runCatching { api.getProfessors() }.onSuccess { _deptProfessors.value = it.data ?: emptyList() }
    }
    fun createProfessor(name: String, email: String, qual: String?, deptId: Long?, onDone: (Boolean, String) -> Unit) = viewModelScope.launch {
        runCatching { api.createProfessor(CreateProfessorRequest(name, email, qual, deptId)) }
            .onSuccess { loadProfessors(); onDone(true, "Professor created") }
            .onFailure { onDone(false, it.message ?: "Error") }
    }

    // ── Admin Slots ───────────────────────────────────────────────────────────
    private val _adminSlots = MutableStateFlow<List<LectureSlotResponse>>(emptyList())
    val adminSlots: StateFlow<List<LectureSlotResponse>> = _adminSlots

    fun loadAdminSlots(courseId: Long) = viewModelScope.launch {
        runCatching { api.getAdminSlotsByCourse(courseId) }
            .onSuccess { _adminSlots.value = it.data ?: emptyList() }
            .onFailure { _adminSlots.value = emptyList() }
    }
    fun createSlot(req: CreateSlotRequest, onDone: (Boolean, String) -> Unit) = viewModelScope.launch {
        runCatching { api.createSlot(req) }
            .onSuccess { loadAdminSlots(req.courseId); onDone(true, "Slot created") }
            .onFailure { onDone(false, it.message ?: "Error") }
    }
    fun deleteSlot(code: String, courseId: Long, onDone: (Boolean, String) -> Unit) = viewModelScope.launch {
        runCatching { api.deleteSlot(code) }
            .onSuccess { loadAdminSlots(courseId); onDone(true, "Slot deleted") }
            .onFailure { onDone(false, it.message ?: "Error") }
    }

    // ── Students ──────────────────────────────────────────────────────────────
    private val _students = MutableStateFlow<UiState<List<StudentResponse>>>(UiState.Idle)
    val students: StateFlow<UiState<List<StudentResponse>>> = _students

    fun loadStudents() = viewModelScope.launch {
        _students.value = UiState.Loading
        runCatching { api.getStudents() }
            .onSuccess { _students.value = UiState.Success(it.data ?: emptyList()) }
            .onFailure { _students.value = UiState.Error(it.message ?: "Error") }
    }
    fun createStudent(req: CreateStudentRequest, onDone: (Boolean, String) -> Unit) = viewModelScope.launch {
        runCatching { api.createStudent(req) }
            .onSuccess { loadStudents(); onDone(true, "Student registered") }
            .onFailure { onDone(false, it.message ?: "Error") }
    }
    fun updateFeeStatus(studentId: Long, status: String, onDone: (Boolean, String) -> Unit) = viewModelScope.launch {
        runCatching { api.updateFeeStatus(UpdateFeeStatusRequest(studentId, status)) }
            .onSuccess { loadStudents(); onDone(true, "Fee status updated") }
            .onFailure { onDone(false, it.message ?: "Error") }
    }

    // ── Student self-service ──────────────────────────────────────────────────
    private val _myProfile      = MutableStateFlow<StudentResponse?>(null)
    val myProfile: StateFlow<StudentResponse?> = _myProfile
    private val _myCourses      = MutableStateFlow<UiState<List<CourseResponse>>>(UiState.Idle)
    val myCourses: StateFlow<UiState<List<CourseResponse>>> = _myCourses
    private val _availableSlots = MutableStateFlow<List<LectureSlotResponse>>(emptyList())
    val availableSlots: StateFlow<List<LectureSlotResponse>> = _availableSlots
    private val _myEnrollments  = MutableStateFlow<List<EnrollmentResponse>>(emptyList())
    val myEnrollments: StateFlow<List<EnrollmentResponse>> = _myEnrollments
    private val _slotOpState    = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val slotOpState: StateFlow<UiState<Unit>> = _slotOpState

    fun loadMyProfile() = viewModelScope.launch {
        runCatching { api.getMyProfile() }.onSuccess { _myProfile.value = it.data }
    }
    fun loadMyCourses() = viewModelScope.launch {
        _myCourses.value = UiState.Loading
        runCatching { api.getMyCourses() }
            .onSuccess { _myCourses.value = UiState.Success(it.data ?: emptyList()) }
            .onFailure { _myCourses.value = UiState.Error(it.message ?: "Error") }
    }
    fun loadAvailableSlots(courseId: Long) = viewModelScope.launch {
        runCatching { api.getAvailableSlots(courseId) }
            .onSuccess { _availableSlots.value = it.data ?: emptyList() }
            .onFailure { _availableSlots.value = emptyList() }
    }
    fun loadMyEnrollments() = viewModelScope.launch {
        runCatching { api.getMyEnrollments() }.onSuccess { _myEnrollments.value = it.data ?: emptyList() }
    }
    fun selectSlot(code: String) = viewModelScope.launch {
        _slotOpState.value = UiState.Loading
        runCatching { api.selectSlot(SelectSlotRequest(code)) }
            .onSuccess { loadMyEnrollments(); loadMyProfile(); _slotOpState.value = UiState.Success(Unit) }
            .onFailure { _slotOpState.value = UiState.Error(it.message ?: "Error") }
    }
    fun dropSlot(code: String) = viewModelScope.launch {
        _slotOpState.value = UiState.Loading
        runCatching { api.dropSlot(code) }
            .onSuccess { loadMyEnrollments(); loadMyProfile(); _slotOpState.value = UiState.Success(Unit) }
            .onFailure { _slotOpState.value = UiState.Error(it.message ?: "Error") }
    }
    fun resetSlotOp() { _slotOpState.value = UiState.Idle }

    // ── PDF Download ──────────────────────────────────────────────────────────
    private val _pdfState = MutableStateFlow<UiState<ByteArray>>(UiState.Idle)
    val pdfState: StateFlow<UiState<ByteArray>> = _pdfState

    fun downloadTimetablePdf() = viewModelScope.launch {
        _pdfState.value = UiState.Loading
        runCatching { api.downloadTimetablePdf().bytes() }
            .onSuccess { _pdfState.value = UiState.Success(it) }
            .onFailure { _pdfState.value = UiState.Error(it.message ?: "PDF download failed") }
    }
    fun resetPdfState() { _pdfState.value = UiState.Idle }
}
