import React, { useEffect, useState } from 'react'
import { getMyCourses, getAvailableSlots, selectSlot, getMyEnrollments } from '../../api/client'

const DAY_SHORT = { MONDAY:'Mon', TUESDAY:'Tue', WEDNESDAY:'Wed', THURSDAY:'Thu', FRIDAY:'Fri', SATURDAY:'Sat' }
const DAY_ORDER = ['MONDAY','TUESDAY','WEDNESDAY','THURSDAY','FRIDAY','SATURDAY']

export default function StudentCourses() {
  const [courses, setCourses]         = useState([])
  const [enrollments, setEnrollments] = useState([])
  const [loading, setLoading]         = useState(true)
  const [selCourse, setSelCourse]     = useState(null)
  const [slots, setSlots]             = useState([])
  const [loadingSlots, setLoadingSlots] = useState(false)
  const [selecting, setSelecting]     = useState(null)
  const [error, setError]             = useState('')
  const [success, setSuccess]         = useState('')

  const load = () => Promise.all([getMyCourses(), getMyEnrollments()])
    .then(([c, e]) => { setCourses(c); setEnrollments(e) })
    .finally(() => setLoading(false))

  useEffect(() => { load() }, [])

  async function loadSlots(course) {
    setSelCourse(course); setSlots([]); setError(''); setSuccess('')
    setLoadingSlots(true)
    try { setSlots(await getAvailableSlots(course.id)) }
    catch { setError('Could not load slots.') }
    finally { setLoadingSlots(false) }
  }

  async function handleSelect(slotGroupCode, slotName) {
    setSelecting(slotGroupCode); setError(''); setSuccess('')
    try {
      await selectSlot(slotGroupCode)
      setSuccess(`✓ Enrolled in "${slotName}"!`)
      await load()
      if (selCourse) setSlots(await getAvailableSlots(selCourse.id))
    } catch(e) { setError(e.response?.data?.message || 'Could not select slot.') }
    finally { setSelecting(null) }
  }

  const enrolledCodes       = new Set(enrollments.map(e => e.slotGroupCode)) // Set of slotGroupCodes the student is currently enrolled in (including dropped ones, since we want to show them as enrolled for reference)
  const enrolledCourseCodes = new Set(enrollments.filter(e => !e.dropped).map(e => e.courseCode))// Set of courseCodes the student is currently enrolled in (excluding dropped courses, since we want to show them as not enrolled if they dropped all slots of that course)

  const sortLectures = lectures => [...(lectures || [])].sort((a,b) => DAY_ORDER.indexOf(a.dayOfWeek) - DAY_ORDER.indexOf(b.dayOfWeek))

  if (loading) return <div className="loading">Loading your courses…</div>

  return (
    <>
      <div className="page-header">
        <div><h1>My Courses</h1><p>Select a course to view available slots and enrol</p></div>
      </div>

      {error   && <div className="alert alert-error"   style={{ marginBottom:16 }}>{error}</div>}
      {success && <div className="alert alert-success" style={{ marginBottom:16 }}>{success}</div>}

      <div style={{ display:'grid', gridTemplateColumns:'280px 1fr', gap:20, alignItems:'start' }}>
        {/* Course list */}
        <div>
          <div className="card" style={{ padding:'10px 14px', marginBottom:8 }}>
            <div style={{ fontSize:12, color:'var(--text-muted)', fontWeight:600 }}>
              {courses.length} course{courses.length !== 1 ? 's' : ''} this semester
            </div>
          </div>
          {courses.length === 0 ? (
            <div className="card"><div className="empty"><div className="es">No courses for your semester</div></div></div>
          ) : courses.map(c => {
            const enrolled = enrolledCourseCodes.has(c.courseCode) // checking whether the enrolled slots match this course code, to show the course as enrolled if the student is enrolled in at least one slot of that course (even if they dropped some other slots of the same course, we still want to show it as enrolled for reference)
            return (
              <div key={c.id}
                className={`course-card${selCourse?.id === c.id ? ' selected' : ''}${enrolled ? ' enrolled' : ''}`}
                onClick={() => loadSlots(c)} style={{ marginBottom:8 }}>
                <div className="cc-code">{c.courseCode}</div>
                <div className="cc-name">{c.name}</div>
                <div className="cc-meta">{c.creditHours} credit hour{c.creditHours !== 1 ? 's' : ''}</div>
                <div style={{ marginTop:8 }}>
                  {enrolled ? <span className="badge badge-green">✓ Enrolled</span>
                  : c.availableSlotGroups === 0 ? <span className="badge badge-gray">No slots open</span>
                  : <span className="badge badge-purple">{c.availableSlotGroups} slot{c.availableSlotGroups !== 1 ? 's' : ''} available</span>}
                </div>
              </div>
            )
          })}
        </div>

        {/* Slot picker */}
        <div>
          {!selCourse ? (
            <div className="card"><div className="empty"><div className="ei">📚</div><div className="et">Select a course</div><div className="es">Click a course on the left to see available slots</div></div></div>
          ) : loadingSlots ? <div className="loading">Loading slots…</div>
          : slots.length === 0 ? (
            <div className="card"><div className="empty"><div className="ei">😔</div><div className="et">No open slots</div><div className="es">No available slots for <strong>{selCourse.name}</strong> right now</div></div></div>
          ) : (
            <>
              <div className="card" style={{ padding:'12px 16px', marginBottom:14 }}>
                <div style={{ fontWeight:700, fontSize:15 }}>{selCourse.name}</div>
                <div style={{ fontSize:12, color:'var(--text-muted)' }}>
                  {selCourse.courseCode} · {selCourse.creditHours} credits · {slots.length} slot{slots.length !== 1 ? 's' : ''} available
                </div>
              </div>

              <div style={{ display:'flex', flexDirection:'column', gap:14 }}>
                {slots.map(slot => {
                  const alreadyIn = enrolledCodes.has(slot.slotGroupCode)
                  const pct = slot.maxCapacity ? (slot.enrolledCount / slot.maxCapacity) * 100 : 0
                  const lectures = sortLectures(slot.lectures)
                  return (
                    <div key={slot.slotGroupCode} className={`slot-card${slot.isFull ? ' full' : ''}`}>
                      <div style={{ display:'flex', justifyContent:'space-between', alignItems:'flex-start', marginBottom:10 }}>
                        <div>
                          <div style={{ fontWeight:700, fontSize:15 }}>{slot.slotName}</div>
                          <div style={{ fontSize:12, color:'var(--text-muted)' }}>
                            👨‍🏫 {slot.professorName}
                          </div>
                        </div>
                        {alreadyIn
                          ? <span className="badge badge-green">✓ Enrolled</span>
                          : slot.isFull ? <span className="badge badge-red">Full</span>
                          : <span className="badge badge-blue">{slot.availableSeats} left</span>
                        }
                      </div>

                      {/* Lectures */}
                      <div style={{ marginBottom:10 }}>
                        <div style={{ fontSize:11, fontWeight:700, color:'var(--text-muted)', textTransform:'uppercase', letterSpacing:'0.4px', marginBottom:5 }}>
                          Weekly Schedule ({lectures.length} lecture{lectures.length !== 1 ? 's' : ''})
                        </div>
                        {lectures.map((l,i) => (
                          <div key={i} className="slot-schedule-item">
                            <span className="day-tag">{DAY_SHORT[l.dayOfWeek] || l.dayOfWeek.slice(0,3)}</span>
                            <span>🕐 {l.startTime} – {l.endTime}</span>
                            {l.venue && <span>📍 {l.venue}</span>}
                          </div>
                        ))}
                      </div>

                      <div className="capacity-bar">
                        <div className={`capacity-fill ${pct>80?'danger':pct>60?'warn':''}`} style={{ width:`${pct}%` }} />
                      </div>
                      <div style={{ display:'flex', justifyContent:'space-between', alignItems:'center', marginTop:8 }}>
                        <span style={{ fontSize:12, color:'var(--text-muted)' }}>{slot.enrolledCount}/{slot.maxCapacity} enrolled</span>
                        {!alreadyIn && !slot.isFull && (
                          <button className="btn btn-primary btn-sm"
                            onClick={() => handleSelect(slot.slotGroupCode, slot.slotName)}
                            disabled={selecting === slot.slotGroupCode}>
                            {selecting === slot.slotGroupCode ? 'Enrolling…' : 'Enrol in this slot'}
                          </button>
                        )}
                      </div>
                    </div>
                  )
                })}
              </div>
            </>
          )}
        </div>
      </div>
    </>
  )
}
