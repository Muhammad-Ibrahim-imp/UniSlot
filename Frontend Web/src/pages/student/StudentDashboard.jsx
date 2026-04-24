import React, { useEffect, useState } from 'react'
import { getMyProfile, getMyEnrollments, downloadTimetablePdf } from '../../api/client'
import { useNavigate } from 'react-router-dom'

export default function StudentDashboard() {
  const [profile, setProfile]         = useState(null)
  const [enrollments, setEnrollments] = useState([])
  const [loading, setLoading]         = useState(true)
  const [downloading, setDownloading] = useState(false)
  const navigate = useNavigate()

  useEffect(() => {
    Promise.all([getMyProfile(), getMyEnrollments()])
      .then(([p, e]) => { setProfile(p); setEnrollments(e) })
      .finally(() => setLoading(false))
  }, [])  

  async function handlePdf() {
    setDownloading(true)
    try {
      const res = await downloadTimetablePdf()
      const url = URL.createObjectURL(res.data) //Converts binary data into a temporary browser URL
      const a = document.createElement('a'); a.href = url // Create a temporary anchor element to trigger download
      a.download = `timetable-${profile?.rollNumber || 'me'}.pdf` // Set default filename using roll number or fallback to 'me'
      a.click();// Programmatically click the anchor to start download
      URL.revokeObjectURL(url)// Clean up the temporary URL after download is triggered
    } catch(e) { // catch errors (e.g. network issues, server errors, or if no slots are selected which leads to a specific error response)
      alert(e.response?.data?.message || 'PDF generation failed. Select at least one slot first.')
    } finally { setDownloading(false) }
  }

  if (loading) return <div className="loading">Loading your profile…</div>

  const paid = profile?.feeStatus === 'PAID'

  return (
    <>
      <div className="page-header">
        <div>
          <h1>Welcome back, {profile?.name?.split(' ')[0]} 👋</h1>
          <p>Here's your academic overview</p>
        </div>
      </div>

      {!paid && (
        <div className="alert alert-error" style={{ marginBottom: 24 }}>
          <span>⚠️</span>
          <span><strong>Fee Unpaid</strong> — You cannot select lecture slots until your semester fee is marked as PAID by the finance office.</span>
        </div>
      )}

      <div className="stats-grid" style={{ gridTemplateColumns: 'repeat(4,1fr)', marginBottom: 24 }}>
        <div className="stat-card" style={{ '--sc': paid ? 'var(--c-success)' : 'var(--c-danger)' }}>
          <div className="s-icon">{paid ? '✅' : '⚠️'}</div>
          <div className="s-label">Fee Status</div>
          <div className="s-value" style={{ fontSize: 18, color: paid ? 'var(--c-success)' : 'var(--c-danger)', marginTop: 6 }}>
            {paid ? 'PAID' : 'UNPAID'}
          </div>
        </div>
        <div className="stat-card" style={{ '--sc': 'var(--c-primary)' }}>
          <div className="s-icon">🎓</div>
          <div className="s-label">Degree</div>
          <div className="s-value" style={{ fontSize: 18, color: 'var(--c-primary)', marginTop: 6 }}>{profile?.degreeCode || '—'}</div>
          <div className="s-sub">{profile?.degreeName}</div>
        </div>
        <div className="stat-card" style={{ '--sc': 'var(--c-info)' }}>
          <div className="s-icon">📖</div>
          <div className="s-label">Semester</div>
          <div className="s-value" style={{ color: 'var(--c-info)' }}>{profile?.currentSemester}</div>
        </div>
        <div className="stat-card" style={{ '--sc': 'var(--c-warning)' }}>
          <div className="s-icon">📅</div>
          <div className="s-label">Slots Selected</div>
          <div className="s-value" style={{ color: 'var(--c-warning)' }}>{profile?.slotsSelected}</div>
        </div>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 20 }}>
        <div className="card">
          <div className="card-header"><span className="card-title">👤 My Profile</span></div>
          {[
            ['Name', profile?.name],
            ['Roll Number', profile?.rollNumber],
            ['Email', profile?.email],
            ['Department', profile?.departmentName],
            ['Degree', `${profile?.degreeName} (${profile?.degreeCode})`],
            ['Current Semester', `Semester ${profile?.currentSemester}`],
          ].map(([l, v]) => (
            <div key={l} style={{ display: 'flex', padding: '8px 0', borderBottom: '1px solid var(--border)', fontSize: 13.5 }}>
              <span style={{ width: 130, color: 'var(--text-muted)', fontWeight: 500 }}>{l}</span>
              <span style={{ fontWeight: 600 }}>{v || '—'}</span>
            </div>
          ))}
        </div>

        <div className="card">
          <div className="card-header"><span className="card-title">⚡ Quick Actions</span></div>
          <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
            <button className="btn btn-primary btn-full" onClick={() => navigate('/student/courses')} disabled={!paid}>
              {paid ? '📚 Browse & Select Lecture Slots' : '🔒 Slots Locked — Fee Not Paid'}
            </button>
            <button className="btn btn-outline btn-full" onClick={() => navigate('/student/schedule')}>
              📅 View My Weekly Schedule
            </button>
            <button className="btn btn-success btn-full" onClick={handlePdf}
              disabled={downloading || enrollments.length === 0}>
              {downloading ? '⏳ Generating PDF…' : '⬇️ Download Timetable PDF'}
            </button>
            {enrollments.length === 0 && (
              <p style={{ fontSize: 12, color: 'var(--text-subtle)', textAlign: 'center' }}>
                Select at least one slot to enable PDF download
              </p>
            )}
          </div>
        </div>
      </div>
    </>
  )
}
