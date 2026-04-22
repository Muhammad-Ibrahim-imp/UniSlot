import axios from 'axios'
const api = axios.create({ baseURL: '/api', headers: { 'Content-Type': 'application/json' } })
api.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})
api.interceptors.response.use(res => res, err => {
  if (err.response?.status === 401) {
    localStorage.removeItem('token');
    localStorage.removeItem('role');
    localStorage.removeItem('email')
    window.location.href = '/login'
  }
  return Promise.reject(err)
})
export default api
export const login = (email, password) => api.post('/auth/login', { email, password }).then(r => r.data.data)
export const getDepartments   = () => api.get('/admin/departments').then(r => r.data.data)
export const createDepartment = d => api.post('/admin/departments', d).then(r => r.data.data)
export const updateDepartment = (id, d) => api.put(`/admin/departments/${id}`, d).then(r => r.data.data)
export const deleteDepartment = id => api.delete(`/admin/departments/${id}`)
export const getDegreesByDept = deptId => api.get(`/admin/degrees/by-department/${deptId}`).then(r => r.data.data)
export const getDegreeById    = id => api.get(`/admin/degrees/${id}`).then(r => r.data.data)
export const createDegree     = d => api.post('/admin/degrees', d).then(r => r.data.data)
export const addCourseToDegree = d => api.post('/admin/degrees/add-course', d).then(r => r.data)
export const removeCourseFromDegree = (degreeId, courseId) => api.delete(`/admin/degrees/${degreeId}/courses/${courseId}`)
export const getCourses    = () => api.get('/admin/courses').then(r => r.data.data)
export const createCourse  = d => api.post('/admin/courses', d).then(r => r.data.data)
export const searchCourses = (query) => api.get(`/admin/courses/search`, { params: { query } }).then(r => r.data.data)
export const getProfessors = () => api.get('/admin/professors').then(r => r.data.data)
export const createProfessor = d => api.post('/admin/professors', d).then(r => r.data.data)
export const getProfessorsByDept = deptId => api.get(`/admin/professors/by-department/${deptId}`).then(r => r.data.data)
export const getEvaluationRanking = () => api.get('/admin/professors/evaluation-ranking').then(r => r.data.data)
// Slots — new model: one slot per call, with schedule[] array
export const getAdminSlotsByCourse = courseId => api.get(`/admin/slots/by-course/${courseId}`).then(r => r.data.data)
export const createSlot    = data => api.post('/admin/slots', data).then(r => r.data.data)
export const deleteSlot    = code => api.delete(`/admin/slots/group/${code}`)
// Students
export const getStudents     = () => api.get('/admin/students').then(r => r.data.data)
export const createStudent   = d => api.post('/admin/students', d).then(r => r.data.data)
export const getUnpaidStudents = () => api.get('/admin/students/unpaid').then(r => r.data.data)
export const updateFeeStatus = (studentId, feeStatus) => api.patch('/admin/students/fee-status', { studentId, feeStatus }).then(r => r.data.data)
// Student self-service
export const getMyProfile     = () => api.get('/students/me/profile').then(r => r.data.data)
export const getMyCourses     = () => api.get('/students/me/courses').then(r => r.data.data)
export const getAvailableSlots = courseId => api.get(`/students/me/courses/${courseId}/available-slots`).then(r => r.data.data)
export const selectSlot       = slotGroupCode => api.post('/students/me/enrollments', { slotGroupCode }).then(r => r.data.data)
export const dropSlot         = slotGroupCode => api.delete(`/students/me/enrollments/${slotGroupCode}`)
export const getMyEnrollments = () => api.get('/students/me/enrollments').then(r => r.data.data)
export const downloadTimetablePdf = () => api.get('/students/me/timetable/pdf', { responseType: 'blob' })
