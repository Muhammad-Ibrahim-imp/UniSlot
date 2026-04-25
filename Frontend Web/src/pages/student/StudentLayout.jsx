import React from 'react'
import { Outlet, NavLink, useNavigate } from 'react-router-dom'
import { useAuth } from '../../contexts/AuthContext'

const links = [
  { to: '/student',          label: 'Dashboard',   icon: '🏠', end: true },
  { to: '/student/courses',  label: 'My Courses',  icon: '📚' },
  { to: '/student/schedule', label: 'My Schedule', icon: '📅' },
]

export default function StudentLayout() {
  const { user, logout } = useAuth()
  const navigate = useNavigate()
  return (
    <div className="layout">
      <aside className="sidebar">
        <div className="sidebar-brand">
          <div className="sidebar-brand-logo">
            <div className="sidebar-brand-icon">🎓</div>
            <div>
              <div className="sidebar-brand-name">UniSlot</div>
              <div className="sidebar-brand-sub">Student Portal</div>
            </div>
          </div>
        </div>
        <div className="sidebar-section">Menu</div>
        <nav>
          {links.map(l => (
            <NavLink key={l.to} to={l.to} end={l.end}
              className={({ isActive }) => isActive ? 'active' : ''}>
              <span className="nav-icon">{l.icon}</span>{l.label}
            </NavLink>
          ))}
        </nav>
        <div className="sidebar-footer">
          <div className="sidebar-user">
            <div className="sidebar-avatar">{user?.email?.[0]?.toUpperCase()}</div>
            <div className="sidebar-user-info">
              <div className="name">{user?.email?.split('@')[0]}</div>
              <div className="role">Student</div>
            </div>
          </div>
          <button className="sidebar-logout" onClick={() => { logout(); navigate('/login') }}>🚪 Sign out</button>
        </div>
      </aside>
      <main className="main-content"><Outlet /></main>
    </div>
  )
}
