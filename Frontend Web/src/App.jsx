import React from 'react'
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { AuthProvider, useAuth } from './contexts/AuthContext'
import LoginPage        from './pages/LoginPage'
import AdminLayout      from './pages/admin/AdminLayout'
import AdminDashboard   from './pages/admin/AdminDashboard'
import AdminDepartments from './pages/admin/AdminDepartments'
import AdminDegrees     from './pages/admin/AdminDegrees'
import AdminCourses     from './pages/admin/AdminCourses'
import AdminProfessors  from './pages/admin/AdminProfessors'
import AdminSlots       from './pages/admin/AdminSlots'
import AdminStudents    from './pages/admin/AdminStudents'
import StudentLayout    from './pages/student/StudentLayout'
import StudentDashboard from './pages/student/StudentDashboard'
import StudentCourses   from './pages/student/StudentCourses'
import StudentSchedule  from './pages/student/StudentSchedule'

function RequireAuth({ children, role }) {
  const { user } = useAuth()
  if (!user) return <Navigate to="/login" replace />
  if (role && user.role !== role) return <Navigate to="/login" replace />
  return children
}

export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          <Route path="/login" element={<LoginPage />} />
          <Route path="/admin" element={<RequireAuth role="ADMIN"><AdminLayout /></RequireAuth>}>
            <Route index            element={<AdminDashboard />} />
            <Route path="departments" element={<AdminDepartments />} />
            <Route path="degrees"     element={<AdminDegrees />} />
            <Route path="courses"     element={<AdminCourses />} />
            <Route path="professors"  element={<AdminProfessors />} />
            <Route path="slots"       element={<AdminSlots />} />
            <Route path="students"    element={<AdminStudents />} />
          </Route>
          <Route path="/student" element={<RequireAuth role="STUDENT"><StudentLayout /></RequireAuth>}>
            <Route index          element={<StudentDashboard />} />
            <Route path="courses" element={<StudentCourses />} />
            <Route path="schedule" element={<StudentSchedule />} />
          </Route>
          <Route path="*" element={<RootRedirect />} />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  )
}

function RootRedirect() {
  const { user } = useAuth()
  if (!user) return <Navigate to="/login" replace />
  if (user.role === 'ADMIN') return <Navigate to="/admin" replace />
  return <Navigate to="/student" replace />
}
