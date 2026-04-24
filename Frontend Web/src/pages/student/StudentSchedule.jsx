import React, { useEffect, useState } from 'react'
import { getMyEnrollments, dropSlot, downloadTimetablePdf } from '../../api/client'

const DAYS = ['MONDAY','TUESDAY','WEDNESDAY','THURSDAY','FRIDAY','SATURDAY']
const DAY_SHORT = { MONDAY:'Mon', TUESDAY:'Tue', WEDNESDAY:'Wed', THURSDAY:'Thu', FRIDAY:'Fri', SATURDAY:'Sat' }

export default function StudentSchedule() {
  const [enrollments, setEnrollments] = useState([])
  const [loading, setLoading]         = useState(true)
  const [dropping, setDropping]       = useState(null)
  const [downloading, setDownloading] = useState(false)
  const [feedback, setFeedback]       = useState('')
  const [viewMode, setViewMode]       = useState('list')

  const load = () => getMyEnrollments().then(setEnrollments).finally(() => setLoading(false))
  useEffect(() => { load() }, [])

  async function handleDrop(code, slotName) {
    if (!confirm(`Drop "${slotName}"?`)) return
    setDropping(code)
    try { await dropSlot(code); setFeedback(`Dropped ${slotName}`); await load() }
    catch(e) { alert(e.response?.data?.message || 'Drop failed.') }
    finally { setDropping(null) }
  }

  async function handlePdf() {
    setDownloading(true)
    try {
      const res = await downloadTimetablePdf()
      const url = URL.createObjectURL(res.data)
      const a = document.createElement('a'); a.href = url; a.download = 'timetable.pdf'; a.click()
      URL.revokeObjectURL(url)
    } catch(e) { alert(e.response?.data?.message || 'PDF failed.') }
    finally { setDownloading(false) }
  }

  const sortLectures = lecs => [...(lecs||[])].sort((a,b) => DAYS.indexOf(a.dayOfWeek) - DAYS.indexOf(b.dayOfWeek))

  // Build timetable: collect all unique time windows across all enrollments' lectures
  const allLectures = enrollments.flatMap(e =>
    sortLectures(e.lectures).map(l => ({ ...l, enrollment: e }))
  )
  const timeKeys = [...new Set(allLectures.map(l => `${l.startTime}|${l.endTime}`))]
    .sort((a,b) => a.localeCompare(b))

  const timetableMap = {}
  allLectures.forEach(l => {
    const tk = `${l.startTime}|${l.endTime}`
    if (!timetableMap[tk]) timetableMap[tk] = {}
    timetableMap[tk][l.dayOfWeek] = l
  })

  if (loading) return <div className="loading">Loading your schedule…</div>

  return (
    <>
      <div className="page-header">
        <div><h1>My Schedule</h1><p>All enrolled slots and their lecture times</p></div>
        <div style={{ display:'flex', gap:10 }}>
          <button className="btn btn-success" onClick={handlePdf}
            disabled={downloading || enrollments.length === 0}>
            {downloading ? '⏳…' : '⬇️ PDF'}
          </button>
        </div>
      </div>

      {feedback && (
        <div className="alert alert-success" style={{ marginBottom:16 }}>
          ✓ {feedback}
          <button onClick={() => setFeedback('')} style={{ marginLeft:'auto', background:'none', border:'none', cursor:'pointer' }}>×</button>
        </div>
      )}

      <div className="tabs">
        <button className={`tab ${viewMode==='list' ? 'active' : ''}`} onClick={() => setViewMode('list')}>📋 By Slot</button>
        <button className={`tab ${viewMode==='timetable' ? 'active' : ''}`} onClick={() => setViewMode('timetable')}>🗓️ Weekly Grid</button>
      </div>

      {enrollments.length === 0 ? (
        <div className="card"><div className="empty"><div className="ei">📅</div><div className="et">No slots enrolled yet</div><div className="es">Go to "My Courses" to browse and enrol</div></div></div>
      ) : viewMode === 'list' ? (
        <div style={{ display:'flex', flexDirection:'column', gap:14 }}>
          {enrollments.map(e => {
            const lectures = sortLectures(e.lectures)
            return (
              <div key={e.slotGroupCode} className="slot-card">
                <div style={{ display:'flex', justifyContent:'space-between', alignItems:'flex-start', marginBottom:10 }}>
                  <div>
                    <div style={{ fontWeight:700, fontSize:15 }}>{e.slotName}</div>
                    <div style={{ fontSize:13, color:'var(--text-muted)' }}>
                      {e.courseName} ({e.courseCode}) · {e.creditHours} cr · 👨‍🏫 {e.professorName}
                    </div>
                  </div>
                  <button className="btn btn-danger btn-sm" onClick={() => handleDrop(e.slotGroupCode, e.slotName)}
                    disabled={dropping === e.slotGroupCode}>
                    {dropping === e.slotGroupCode ? '…' : 'Drop'}
                  </button>
                </div>
                <div style={{ fontSize:11, fontWeight:700, color:'var(--text-muted)', textTransform:'uppercase', letterSpacing:'0.4px', marginBottom:6 }}>
                  Lectures ({lectures.length})
                </div>
                {lectures.map((l,i) => (
                  <div key={i} className="slot-schedule-item">
                    <span className="day-tag">{DAY_SHORT[l.dayOfWeek]}</span>
                    <span>🕐 {l.startTime} – {l.endTime}</span>
                    {l.venue && <span>📍 {l.venue}</span>}
                  </div>
                ))}
              </div>
            )
          })}
        </div>
      ) : (
        <div className="card" style={{ padding:0, overflow:'hidden' }}>
          <div className="timetable-wrap">
            <div className="timetable">
              <div className="tt-head">
                <div className="tt-cell" />
                {DAYS.map(d => <div key={d} className="tt-cell" style={{ textAlign:'center' }}>{DAY_SHORT[d]}</div>)}
              </div>
              {timeKeys.length === 0 ? null : timeKeys.map(tk => {
                const [start, end] = tk.split('|')
                return (
                  <div key={tk} className="tt-row">
                    <div className="tt-cell tt-time">{start}<br/><span style={{ fontSize:10 }}>{end}</span></div>
                    {DAYS.map(day => {
                      const cell = timetableMap[tk]?.[day]
                      return (
                        <div key={day} className="tt-cell" style={{ padding:6 }}>
                          {cell ? (
                            <div className="tt-slot">
                              <div className="cn">{cell.enrollment.slotName}</div>
                              <div className="pn">{cell.enrollment.courseCode} · {cell.enrollment.professorName}</div>
                              {cell.venue && <div className="vn">📍 {cell.venue}</div>}
                            </div>
                          ) : null}
                        </div>
                      )
                    })}
                  </div>
                )
              })}
            </div>
          </div>
        </div>
      )}
    </>
  )
}
