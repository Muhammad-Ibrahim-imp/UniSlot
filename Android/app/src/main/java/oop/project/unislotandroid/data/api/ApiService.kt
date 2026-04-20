package oop.project.unislotandroid.data.api

import oop.project.unislotandroid.data.model.*
import retrofit2.http.*

interface ApiService {

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): ApiResponse<AuthResponse>

    // ── Departments ───────────────────────────────────────────────────────────
    @GET("api/admin/departments")
    suspend fun getDepartments(): ApiResponse<List<DepartmentResponse>>
    @POST("api/admin/departments")
    suspend fun createDepartment(@Body r: CreateDepartmentRequest): ApiResponse<DepartmentResponse>
    @PUT("api/admin/departments/{id}")
    suspend fun updateDepartment(@Path("id") id: Long, @Body r: CreateDepartmentRequest): ApiResponse<DepartmentResponse>
    @DELETE("api/admin/departments/{id}")
    suspend fun deleteDepartment(@Path("id") id: Long): ApiResponse<Void>

    // ── Degrees ───────────────────────────────────────────────────────────────
    @GET("api/admin/degrees/by-department/{deptId}")
    suspend fun getDegreesByDepartment(@Path("deptId") deptId: Long): ApiResponse<List<DegreeResponse>>
    @GET("api/admin/degrees/{id}")
    suspend fun getDegreeById(@Path("id") id: Long): ApiResponse<DegreeDetailResponse>
    @POST("api/admin/degrees")
    suspend fun createDegree(@Body r: CreateDegreeRequest): ApiResponse<DegreeResponse>
    @POST("api/admin/degrees/add-course")
    suspend fun addCourseToDegree(@Body r: AddCourseToDegreeRequest): ApiResponse<Void>
    @DELETE("api/admin/degrees/{degreeId}/courses/{courseId}")
    suspend fun removeCourseFromDegree(@Path("degreeId") degreeId: Long, @Path("courseId") courseId: Long): ApiResponse<Void>

    // ── Courses ───────────────────────────────────────────────────────────────
    @GET("api/admin/courses")
    suspend fun getCourses(): ApiResponse<List<CourseResponse>>
    @POST("api/admin/courses")
    suspend fun createCourse(@Body r: CreateCourseRequest): ApiResponse<CourseResponse>

    // ── Professors ────────────────────────────────────────────────────────────
    @GET("api/admin/professors")
    suspend fun getProfessors(): ApiResponse<List<ProfessorResponse>>
    @GET("api/admin/professors/by-department/{deptId}")
    suspend fun getProfessorsByDept(@Path("deptId") deptId: Long): ApiResponse<List<ProfessorResponse>>
    @POST("api/admin/professors")
    suspend fun createProfessor(@Body r: CreateProfessorRequest): ApiResponse<ProfessorResponse>
    @GET("api/admin/professors/evaluation-ranking")
    suspend fun getEvaluationRanking(): ApiResponse<List<ProfessorResponse>>

    // ── Slots (new model: one slot = one section with lectures list) ───────────
    @GET("api/admin/slots/by-course/{courseId}")
    suspend fun getAdminSlotsByCourse(@Path("courseId") courseId: Long): ApiResponse<List<LectureSlotResponse>>
    @POST("api/admin/slots")
    suspend fun createSlot(@Body r: CreateSlotRequest): ApiResponse<LectureSlotResponse>
    @DELETE("api/admin/slots/group/{code}")
    suspend fun deleteSlot(@Path("code") code: String): ApiResponse<Void>

    // ── Students ──────────────────────────────────────────────────────────────
    @GET("api/admin/students")
    suspend fun getStudents(): ApiResponse<List<StudentResponse>>
    @POST("api/admin/students")
    suspend fun createStudent(@Body r: CreateStudentRequest): ApiResponse<StudentResponse>
    @PATCH("api/admin/students/fee-status")
    suspend fun updateFeeStatus(@Body r: UpdateFeeStatusRequest): ApiResponse<StudentResponse>

    // ── Student self-service ──────────────────────────────────────────────────
    @GET("api/students/me/profile")
    suspend fun getMyProfile(): ApiResponse<StudentResponse>
    @GET("api/students/me/courses")
    suspend fun getMyCourses(): ApiResponse<List<CourseResponse>>
    @GET("api/students/me/courses/{courseId}/available-slots")
    suspend fun getAvailableSlots(@Path("courseId") courseId: Long): ApiResponse<List<LectureSlotResponse>>
    @POST("api/students/me/enrollments")
    suspend fun selectSlot(@Body r: SelectSlotRequest): ApiResponse<List<EnrollmentResponse>>
    @DELETE("api/students/me/enrollments/{code}")
    suspend fun dropSlot(@Path("code") code: String): ApiResponse<Void>
    @GET("api/students/me/enrollments")
    suspend fun getMyEnrollments(): ApiResponse<List<EnrollmentResponse>>

    @Streaming
    @GET("api/students/me/timetable/pdf")
    suspend fun downloadTimetablePdf(): okhttp3.ResponseBody
}
