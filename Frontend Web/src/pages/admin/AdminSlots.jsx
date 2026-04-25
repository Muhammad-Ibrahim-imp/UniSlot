import React, { useEffect, useState } from 'react'
import {
  getDepartments, getCourses, getProfessors, getProfessorsByDept,
  getAdminSlotsByCourse, createSlot, deleteSlot
} from '../../api/client'

const ALL_DAYS = ['MONDAY','TUESDAY','WEDNESDAY','THURSDAY','FRIDAY','SATURDAY']
const DAY_SHORT = { MONDAY:'Mon', TUESDAY:'Tue', WEDNESDAY:'Wed', THURSDAY:'Thu', FRIDAY:'Fri', SATURDAY:'Sat' }

const emptyLecture = () => ({ dayOfWeek:'MONDAY', startTime:'08:00', endTime:'09:30', venue:'' })

export default function AdminSlots() {
  const [depts, setDepts]         = useState([])
  const [courses, setCourses]     = useState([])
  const [professors, setProfessors] = useState([])
  const [selCourse, setSelCourse] = useState('')
  const [slots, setSlots]         = useState([])
  const [loadingSlots, setLoadingSlots] = useState(false)
  const [modal, setModal]         = useState(false)
  const [saving, setSaving]       = useState(false)
  const [error, setError]         = useState('')
  const [feedback, setFeedback]   = useState('')
  const [form, setForm] = useState({
    courseId: '', professorId: '', deptId: '', slotName: '', maxCapacity: 50,
    schedule: [emptyLecture()]
  })

  useEffect(() => {
    Promise.all([getDepartments(), getCourses(), getProfessors()])
      .then(([d, c, p]) => { setDepts(d); setCourses(c); setProfessors(p) })
  }, [])

  async function handleDeptChange(deptId) {
    setForm(f => ({ ...f, deptId, professorId: '' }))
    if (deptId) { const p = await getProfessorsByDept(deptId); setProfessors(p) }
    else        { const p = await getProfessors();  setProfessors(p) }
  }

  useEffect(() => {
    if (!selCourse) { setSlots([]); return }
    setLoadingSlots(true)
    getAdminSlotsByCourse(selCourse).then(setSlots).finally(() => setLoadingSlots(false))
  }, [selCourse])

  function addLecture()       { setForm(f => ({ ...f, schedule: [...f.schedule, emptyLecture()] })) }
  function removeLecture(idx) { setForm(f => ({ ...f, schedule: f.schedule.filter((_,i) => i !== idx) })) }
  function updateLecture(idx, field, val) {
    setForm(f => {
      const schedule = [...f.schedule]
      schedule[idx] = { ...schedule[idx], [field]: val }
      return { ...f, schedule }
    })
  }

  async function handleSave() {
    if (!form.courseId)    { setError('Select a course.'); return }
    if (!form.professorId) { setError('Select a professor.'); return }
    if (form.schedule.length === 0) { setError('Add at least one lecture.'); return }
    for (const l of form.schedule) {
      if (l.endTime <= l.startTime) { setError(`End time must be after start time for ${l.dayOfWeek}.`); return }
    }
    setSaving(true); setError('')
    try {
      const payload = {
        courseId:    Number(form.courseId),
        professorId: Number(form.professorId),
        slotName:    form.slotName.trim() || undefined,
        maxCapacity: Number(form.maxCapacity),
        schedule:    form.schedule.map(l => ({
          dayOfWeek: l.dayOfWeek,
          startTime: l.startTime,
          endTime:   l.endTime,
          venue:     l.venue || undefined
        }))
      }
      await createSlot(payload)
      const updated = await getAdminSlotsByCourse(form.courseId)
      if (String(selCourse) === String(form.courseId)) setSlots(updated)
      setModal(false)
      setFeedback(`Slot "${form.slotName || 'auto-named'}" created with ${form.schedule.length} lecture(s)!`)
    } catch(e) { setError(e.response?.data?.message || 'Save failed.') }
    finally { setSaving(false) }
  }

  async function handleDelete(code, slotName) {
    if (!confirm(`Delete slot "${slotName}" (${code})? All enrollments will be lost.`)) return
    try {
      await deleteSlot(code)
      if (selCourse) {
        const updated = await getAdminSlotsByCourse(selCourse)
        setSlots(updated)
      }
      setFeedback(`Slot "${slotName}" deleted.`)
    } catch(e) { alert(e.response?.data?.message || 'Delete failed.') }
  }

  const selCourseName = courses.find(c => String(c.id) === String(selCourse))?.name || ''

  return (
    <>
      <div className="page-header">
        <div>
          <h1>Lecture Slots</h1>
          <p>Create course slots — each slot has its own weekly lecture schedule and venue</p>
        </div>
      </div>

      {feedback && (
        <div className="alert alert-success" style={{ marginBottom: 16 }}>
          ✓ {feedback}
          <button onClick={() => setFeedback('')} style={{ marginLeft:'auto', background:'none', border:'none', cursor:'pointer' }}>×</button>
        </div>
      )}

      {/* How it works banner */}
      <div className="alert alert-info" style={{ marginBottom: 16 }}>
        <strong>How slots work:</strong> For 250 students in OOP, create 5 slots each with capacity 50.
        Each slot has its own weekly schedule (e.g., Slot 1: Mon 10–1 Room 101; Slot 2: Tue 9–10 Room 201 + Wed 2–4 Lab 3).
        Students pick <em>one</em> slot and attend all its lectures.
      </div>

      <div className="card" style={{ marginBottom: 20 }}>
        <div style={{ display:'flex', gap:12, alignItems:'flex-end', flexWrap:'wrap' }}>
          <div className="form-group" style={{ flex:1, minWidth:200, margin:0 }}>
            <label>View slots for course</label>
            <select value={selCourse} onChange={e => setSelCourse(e.target.value)}>
              <option value="">— Select a course —</option>
              {courses.map(c => <option key={c.id} value={c.id}>{c.courseCode} — {c.name}</option>)}
            </select>
          </div>
          <button className="btn btn-primary" onClick={() => {
            setForm({ courseId: selCourse||'', professorId:'', deptId:'', slotName:'', maxCapacity:50, schedule:[emptyLecture()] })
            setError(''); setModal(true)
          }}>+ Create Slot</button>
        </div>
      </div>

      {!selCourse ? (
        <div className="card"><div className="empty"><div className="ei">🕐</div><div className="et">Select a course above</div><div className="es">Choose a course to view its slots</div></div></div>
      ) : loadingSlots ? <div className="loading">Loading slots…</div>
      : slots.length === 0 ? (
        <div className="card"><div className="empty"><div className="ei">📅</div><div className="et">No slots for {selCourseName}</div><div className="es">Click "Create Slot" to add the first slot</div></div></div>
      ) : (
        <div className="slot-grid">
          {slots.map(slot => {
            // pct is percentage of seats filled, used for capacity bar and color coding
            const pct = slot.maxCapacity ? (slot.enrolledCount / slot.maxCapacity) * 100 : 0
            return (
              <div key={slot.slotGroupCode} className={`slot-card${slot.isFull ? ' full' : ''}`}>
                <div style={{ display:'flex', justifyContent:'space-between', alignItems:'flex-start', marginBottom:10 }}>
                  <div>
                    <div style={{ fontWeight:700, fontSize:15 }}>{slot.slotName}</div>
                    <div style={{ fontSize:12, color:'var(--text-muted)' }}>
                      {slot.courseName} ({slot.courseCode})
                    </div>
                  </div>
                  <button className="btn btn-danger btn-sm" onClick={() => handleDelete(slot.slotGroupCode, slot.slotName)}>Delete</button>
                </div>

                <div style={{ fontSize:13, marginBottom:8 }}>
                  <strong>👨‍🏫</strong> {slot.professorName}
                </div>

                <div style={{ marginBottom:10, fontSize:12, color:'var(--text-muted)', fontWeight:600, textTransform:'uppercase', letterSpacing:'0.4px' }}>
                  Weekly Schedule ({slot.lectures?.length || 0} lecture{slot.lectures?.length !== 1 ? 's' : ''})
                </div>
                {(slot.lectures || []).map((l, i) => (
                  <div key={i} className="slot-schedule-item">
                    <span className="day-tag">{DAY_SHORT[l.dayOfWeek] || l.dayOfWeek.slice(0,3)}</span>
                    <span>🕐 {l.startTime} – {l.endTime}</span>
                    {l.venue && <span>📍 {l.venue}</span>}
                  </div>
                ))}

                <div className="capacity-bar" style={{ marginTop:10 }}>
                  <div className={`capacity-fill ${pct > 80 ? 'danger' : pct > 60 ? 'warn' : ''}`}
                       style={{ width:`${pct}%` }} />
                </div>
                <div style={{ display:'flex', justifyContent:'space-between', fontSize:12, color:'var(--text-muted)', marginTop:4 }}>
                  <span>{slot.enrolledCount}/{slot.maxCapacity} enrolled</span>
                  {slot.isFull
                    ? <span className="badge badge-red">Full</span>
                    : <span className="badge badge-green">{slot.maxCapacity - slot.enrolledCount} seats left</span>
                  }
                </div>
              </div>
            )
          })}
        </div>
      )}

      {/* Create Slot Modal */}
      {modal && (
        <div className="modal-overlay" onClick={e => e.target === e.currentTarget && setModal(false)}>
          <div className="modal modal-lg">
            <div className="modal-header">
              <div>
                <h3>Create Lecture Slot</h3>
                <p>Define one section students can enrol into, with its full weekly schedule</p>
              </div>
              <button className="modal-close" onClick={() => setModal(false)}>×</button>
            </div>
            <div className="modal-body">
              {error && <div className="alert alert-error">{error}</div>}

              <div className="form-row-2">
                <div className="form-group">
                  <label>Course <span style={{ color:'red' }}>*</span></label>
                  <select value={form.courseId} onChange={e => setForm(f => ({ ...f, courseId:e.target.value }))}>
                    <option value="">— Select course —</option>
                    {courses.map(c => <option key={c.id} value={c.id}>{c.courseCode} — {c.name}</option>)}
                  </select>
                </div>
                <div className="form-group">
                  <label>Slot Name <span style={{ fontSize:11, color:'var(--text-muted)', fontWeight:400 }}>(auto if blank)</span></label>
                  <input type='text' value={form.slotName} onChange={e => setForm(f => ({ ...f, slotName:e.target.value }))}
                    placeholder="e.g. Slot 1, Section A" />
                </div>
              </div>

              <div className="form-row-2">
                <div className="form-group">
                  <label>Department <span style={{ fontSize:11, color:'var(--text-muted)', fontWeight:400 }}>(filter professors)</span></label>
                  <select value={form.deptId} onChange={e => handleDeptChange(e.target.value)}>
                    <option value="">— All departments —</option>
                    {depts.map(d => <option key={d.id} value={d.id}>{d.name} ({d.code})</option>)}
                  </select>
                </div>
                <div className="form-group">
                  <label>Professor <span style={{ color:'red' }}>*</span></label>
                  <select value={form.professorId} onChange={e => setForm(f => ({ ...f, professorId:e.target.value }))}>
                    <option value="">— Select professor —</option>
                    {professors.map(p => <option key={p.id} value={p.id}>{p.name}{p.departmentName ? ` (${p.departmentName})` : ''}</option>)}
                  </select>
                </div>
              </div>

              <div className="form-group">
                <label>Capacity <span style={{ color:'red' }}>*</span></label>
                <input type="number" min={1} max={500} value={form.maxCapacity}
                  onChange={e => setForm(f => ({ ...f, maxCapacity:e.target.value }))}
                  style={{ width:140 }} />
              </div>

              <hr />

              <div style={{ display:'flex', alignItems:'center', justifyContent:'space-between', marginBottom:8 }}>
                <div>
                  <label style={{ marginBottom:0, fontSize:13 }}>📅 Lecture Schedule</label>
                  <p style={{ fontSize:12, color:'var(--text-muted)', marginTop:2 }}>
                    Each row = one class meeting per week. Each can be on a different day, time, and venue.
                  </p>
                </div>
                <button type="button" className="btn btn-outline btn-sm" onClick={addLecture}
                  disabled={form.schedule.length >= 6}>+ Add Lecture</button>
              </div>

              {form.schedule.map((lec, idx) => (
                <div key={idx} className="day-schedule-row">
                  <div>
                    <label>Day</label>
                    <select value={lec.dayOfWeek} onChange={e => updateLecture(idx, 'dayOfWeek', e.target.value)}>
                      {ALL_DAYS.map(d => <option key={d} value={d}>{DAY_SHORT[d]}</option>)}
                    </select>
                  </div>
                  <div>
                    <label>Start</label>
                    <input type="time" value={lec.startTime} onChange={e => updateLecture(idx, 'startTime', e.target.value)} />
                  </div>
                  <div>
                    <label>End</label>
                    <input type="time" value={lec.endTime} onChange={e => updateLecture(idx, 'endTime', e.target.value)} />
                  </div>
                  <div>
                    <label>Venue / Room</label>
                    <input type='text' value={lec.venue} onChange={e => updateLecture(idx, 'venue', e.target.value)}
                      placeholder="e.g. Room 101, Lab 3" />
                  </div>
                  <button type="button" className="btn-remove-day"
                    onClick={() => removeLecture(idx)} disabled={form.schedule.length === 1}>×</button>
                </div>
              ))}
            </div>
            <div className="modal-footer">
              <button className="btn btn-ghost" onClick={() => setModal(false)}>Cancel</button>
              <button className="btn btn-primary" onClick={handleSave} disabled={saving}>
                {saving ? 'Creating…' : `Create Slot (${form.schedule.length} lecture${form.schedule.length !== 1 ? 's' : ''})`}
              </button>
            </div>
          </div>
        </div>
      )}
    </>
  )
}
