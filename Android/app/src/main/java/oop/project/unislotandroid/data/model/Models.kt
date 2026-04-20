package oop.project.unislotandroid.data.model

import com.google.gson.annotations.SerializedName

data class ApiResponse<T>(val success: Boolean, val message: String?, val data: T?)

// ── Auth ──────────────────────────────────────────────────────────────────────
data class LoginRequest(val email: String, val password: String)
data class AuthResponse(val token: String, val email: String, val role: String, val expiresInSeconds: Long)

// ── Department ────────────────────────────────────────────────────────────────
data class DepartmentResponse(val id: Long, val name: String, val code: String, val degreeCount: Int, val studentCount: Int)
data class CreateDepartmentRequest(val name: String, val code: String)

// ── Degree ────────────────────────────────────────────────────────────────────
data class DegreeResponse(val id: Long, val name: String, val code: String, val durationYears: Int, val departmentName: String?, val departmentCode: String?)
data class CreateDegreeRequest(val name: String, val code: String, val departmentId: Long, val durationYears: Int)
data class SemesterCourses(val semesterNumber: Int, val courses: List<CourseResponse>)
data class DegreeDetailResponse(val id: Long, val name: String, val code: String, val durationYears: Int, val departmentName: String?, val semesters: List<SemesterCourses>?)
data class AddCourseToDegreeRequest(val degreeId: Long, val courseId: Long, val semesterNumber: Int, val isCompulsory: Boolean)

// ── Course ────────────────────────────────────────────────────────────────────
data class CourseResponse(
    val id: Long, val name: String, val courseCode: String,
    val creditHours: Int, val description: String?,
    val availableSlotGroups: Int,
    val offeredInDegrees: List<String>?,
    val semesterNumber: Int? = null
)
data class CreateCourseRequest(val name: String, val courseCode: String, val creditHours: Int, val description: String?)

// ── Professor ─────────────────────────────────────────────────────────────────
data class ProfessorResponse(
    val id: Long, val name: String, val email: String,
    val qualification: String?, val departmentName: String?, val departmentId: Long?,
    val totalSeatsOffered: Int, val totalSeatsFilled: Int, val fillRatePercent: Double
)
data class CreateProfessorRequest(val name: String, val email: String, val qualification: String?, val departmentId: Long?)

// ── Slot Lecture (one class meeting within a slot) ────────────────────────────
data class SlotLectureResponse(
    val id: Long,
    val dayOfWeek: String,
    val startTime: String,
    val endTime: String,
    val venue: String?
)

/** One entry in the schedule list when creating a slot */
data class LectureScheduleEntry(
    val dayOfWeek: String,
    val startTime: String,
    val endTime: String,
    val venue: String?
)

/** UI-only helper for building the schedule form */
data class DayScheduleEntry(
    val day: String = "MONDAY",
    val startTime: String = "08:00",
    val endTime: String = "09:30",
    val venue: String = ""
)

// ── Lecture Slot (the pickable slot — one per section) ────────────────────────
/**
 * A slot is one section a student can enroll into (e.g., "OOP Slot 1").
 * It contains a list of lectures (the actual weekly class meetings).
 */
data class LectureSlotResponse(
    val id: Long,
    val slotGroupCode: String,
    val slotName: String,
    val courseName: String,
    val courseCode: String,
    val professorName: String,
    val professorEmail: String?,
    val maxCapacity: Int,
    val enrolledCount: Int,
    val availableSeats: Int,
    @SerializedName("full") val isFull: Boolean,
    val slotOpenedAt: String?,
    val lectures: List<SlotLectureResponse>
)

data class CreateSlotRequest(
    val courseId: Long,
    val professorId: Long,
    val slotName: String?,
    val maxCapacity: Int,
    val schedule: List<LectureScheduleEntry>
)

// ── Student ───────────────────────────────────────────────────────────────────
data class StudentResponse(
    val id: Long, val name: String, val rollNumber: String, val email: String,
    val departmentName: String?, val departmentCode: String?,
    val degreeName: String?, val degreeCode: String?,
    val currentSemester: Int, val feeStatus: String,
    val feePaidAt: String?, val slotsSelected: Int
)
data class CreateStudentRequest(val name: String, val rollNumber: String, val email: String, val departmentId: Long, val degreeId: Long, val currentSemester: Int)
data class UpdateFeeStatusRequest(val studentId: Long, val feeStatus: String)

// ── Enrollment ────────────────────────────────────────────────────────────────
/**
 * One enrolled slot in the student's timetable.
 * Contains the full list of lectures (weekly schedule).
 */
data class EnrollmentResponse(
    val enrollmentId: Long,
    val slotGroupCode: String,
    val slotName: String,
    val courseName: String,
    val courseCode: String,
    val creditHours: Int,
    val professorName: String,
    val lectures: List<SlotLectureResponse>,
    val enrolledAt: String?,
    val dropped: Boolean
)

data class SelectSlotRequest(val slotGroupCode: String)
