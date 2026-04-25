import React, { useEffect, useState } from 'react'
import { getDepartments, getCourses, getProfessors, getStudents, getEvaluationRanking } from '../../api/client'

export default function AdminDashboard() {
  const [stats, setStats] = useState()
  const [ranking, setRanking] = useState([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    Promise.all([getDepartments(), getCourses(), getProfessors(), getStudents(), getEvaluationRanking()])
      .then(([depts, courses, profs, students, rank]) => {
        setStats({
          departments: depts.length,
          courses: courses.length,
          professors: profs.length,
          students: students.length,
          paid: students.filter(s => s.feeStatus === 'PAID').length,
          unpaid: students.filter(s => s.feeStatus !== 'PAID').length,
        })
        setRanking(rank.slice(0, 6))
      }).finally(() => setLoading(false))
  }, [])

  if (loading) return <div className="loading">Loading dashboard…</div>

  return (
    <>
      <div className="page-header">
        <div>
          <h1>Dashboard</h1>
          <p>University Slot Selection System — overview</p>
        </div>
      </div>

      <div className="stats-grid">
        {[
          { label: 'Departments',  value: stats.departments, icon: '🏛️', color: '#6366f1' },
          { label: 'Courses',      value: stats.courses,     icon: '📚', color: '#0284c7' },
          { label: 'Professors',   value: stats.professors,  icon: '👨‍🏫', color: '#059669' },
          { label: 'Students',     value: stats.students,    icon: '👩‍🎓', color: '#d97706' },
          { label: 'Fee Paid',     value: stats.paid,        icon: '✅', color: '#059669' },
          { label: 'Fee Unpaid',   value: stats.unpaid,      icon: '⚠️', color: '#dc2626' },
        ].map(s => (
          <div key={s.label} className="stat-card" style={{ '--sc': s.color }}>
            <div className="s-icon">{s.icon}</div>
            <div className="s-label">{s.label}</div>
            <div className="s-value" style={{ color: s.color }}>{s.value}</div>
          </div>
        ))}
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 20 }}>
        <div className="card">
          <div className="card-header">
            <span className="card-title">🏆 Professor Fill-Rate Ranking</span>
          </div>
          {ranking.length === 0
            ? <div className="empty"><div className="es">No slot data yet</div></div>
            : <div className="table-wrap">
                <table>
                  <thead><tr><th>#</th><th>Professor</th><th>Seats</th><th>Rate</th></tr></thead>
                  <tbody>
                    {ranking.map((p, i) => (
                      <tr key={p.id}>
                        <td><span className="badge badge-purple">{i + 1}</span></td>
                        <td>
                          <div style={{ fontWeight: 600 }}>{p.name}</div>
                          <div style={{ fontSize: 11, color: 'var(--text-muted)' }}>{p.departmentName || '—'}</div>
                        </td>
                        <td style={{ fontSize: 12 }}>{p.totalSeatsFilled}/{p.totalSeatsOffered}</td>
                        <td>
                          <span className={`badge ${p.fillRatePercent >= 80 ? 'badge-green' : p.fillRatePercent >= 50 ? 'badge-yellow' : 'badge-gray'}`}>
                            {p.fillRatePercent?.toFixed(1)}%
                          </span>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
          }
        </div>

        <div className="card">
          <div className="card-header">
            <span className="card-title">📋 Quick Setup Guide</span>
          </div>
          <div style={{ fontSize: 13, color: 'var(--text-muted)' }}>
            {[
              ['1', 'Create Departments', '/admin/departments'],
              ['2', 'Create Degrees & link courses', '/admin/degrees'],
              ['3', 'Add Courses to catalogue', '/admin/courses'],
              ['4', 'Register Professors', '/admin/professors'],
              ['5', 'Create Lecture Slots', '/admin/slots'],
              ['6', 'Register Students', '/admin/students'],
              ['7', 'Mark Student Fees as PAID', '/admin/students'],
            ].map(([n, label, href]) => (
              <div key={n} style={{ display: 'flex', alignItems: 'center', gap: 10, padding: '8px 0', borderBottom: '1px solid var(--border)' }}>
                <span style={{ width: 22, height: 22, borderRadius: '50%', background: 'var(--c-primary-l)', color: 'var(--c-primary)', fontSize: 11, fontWeight: 700, display: 'flex', alignItems: 'center', justifyContent: 'center', flexShrink: 0 }}>{n}</span>
                <a href={href} style={{ color: 'var(--text)', fontWeight: 500, textDecoration: 'none' }}
                  onMouseOver={e => e.target.style.color = 'var(--c-primary)'}
                  onMouseOut={e => e.target.style.color = 'var(--text)'}>
                  {label}
                </a>
              </div>
            ))}
          </div>
        </div>
      </div>
    </>
  )
}
