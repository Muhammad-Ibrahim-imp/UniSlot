import React, { useEffect, useState } from 'react'
import { getCourses, createCourse, searchCourses, deleteCourse } from '../../api/client'

export default function AdminCourses() {
  const [courses, setcourses]     = useState([])
  const [loading, setLoading] = useState(true)
  const [search, setSearch]   = useState('')
  const [modal, setModal]     = useState(false)
  const [form, setForm]       = useState({ name: '', courseCode: '', creditHours: 3, description: '' })
  const [saving, setSaving]   = useState(false)
  const [error, setError]     = useState('')
  const [editTarget, setEditTarget] = useState(null)   // edit: track which course is being edited
  const [isErr, setIsErr]       = useState(false)
  const [feedback, setFeedback] = useState('')

  const load = () => getCourses().then(setcourses).finally(() => setLoading(false))
  useEffect(() => { load() }, [])

  const filtered =  courses.filter(c =>
    !search || c.name.toLowerCase().includes(search.toLowerCase()) ||
    c.courseCode.toLowerCase().includes(search.toLowerCase()))

  function openCreate() {
    setEditTarget(null)
    setForm({ name:'', courseCode:'', creditHours:3, description:'' })
    setError('')
    setModal(true)
  }

   function openEdit(course) {
    setEditTarget(course)
    setForm({ name: course.name, courseCode: course.courseCode, creditHours: course.creditHours, description: course.description || '' })
    setError('')
    setModal(true)
  }

  async function handleSave() {
    if (!form.name.trim() || !form.courseCode.trim()) { setError('Name and code are required.'); return }
    setSaving(true); setError('')
    try {
     if (editTarget) {
        // edit: call update instead of create when editing
        await updateCourse(editTarget.id, { ...form, creditHours: Number(form.creditHours) })
        setFeedback(`"${form.name}" updated!`); setIsErr(false)
      } else {
        await createCourse({ ...form, creditHours: Number(form.creditHours) })
        setFeedback('Course created!'); setIsErr(false)
      }
      await load(); setModal(false)
    } catch (e) {
      setError(e.response?.data?.message || 'Save failed.')
    } finally {
      setSaving(false)
    }
  }
  async function handleDelete(course) {
    if (!confirm(`Delete course "${course.name}" (${course.courseCode})?\nThis will fail if the course has active slots.`)) return // edit: delete confirmation
    try {
      await deleteCourse(course.id); await load() // edit: delete then reload
      setFeedback(`"${course.name}" deleted.`); setIsErr(false)
    } catch(e) { setFeedback(e.response?.data?.message || 'Delete failed.'); setIsErr(true) }
  }

  return (
    <>
      <div className="page-header">
        <h1>Courses</h1>
        <p>Global course catalogue — assign courses to degrees after creation</p>
      </div>

      <div className="card">
        <div className="toolbar">
          <input
              type="text"
            style={{ width: 260 }}
            placeholder="Search by name or code…"
            value={search}
            onChange={e => setSearch(e.target.value)}
          />
          <div className="toolbar-spacer" />
          <button className="btn btn-primary" onClick={openCreate}>+ New Course</button>
        </div>

        {feedback && <div className={`alert alert-${isErr?'error':'success'}`} style={{ marginBottom:16 }}>
        {feedback} <button className='modal-close' onClick={() => setFeedback('')} style={{ marginLeft:'auto', background:'none', border:'none', cursor:'pointer' }}>×</button>
      </div>}

        {loading ? <div className="loading">Loading…</div> : (
          <div className="table-wrap">
            <table>
              <thead>
                <tr><th>Code</th><th>Name</th><th>Credits</th><th>Slot Groups</th><th>Offered In</th></tr>
              </thead>
              <tbody>
                {filtered.length === 0
                  ? <tr><td colSpan={5}><div className="empty">No courses found</div></td></tr>
                  : filtered.map(c => (
                    <tr key={c.id}>
                      <td><code style={{ background: '#f3f4f6', padding: '2px 6px', borderRadius: 4 }}>{c.courseCode}</code></td>
                      <td><strong>{c.name}</strong>{c.description && <div style={{ fontSize: 12, color: '#6b7280', marginTop: 2 }}>{c.description.slice(0, 60)}{c.description.length > 60 ? '…' : ''}</div>}</td>
                      <td><span className="badge badge-blue">{c.creditHours} cr</span></td>
                      <td style={{ fontSize: 12 }}>{c.offeredInDegrees?.join(', ') || '—'}</td>
                      <td>{c.availableSlotGroups > 0 ? <span className="badge badge-green">{c.availableSlotGroups} slots</span> : <span className="badge badge-gray">No slots</span>}</td>
                      <td style={{ display:'flex', gap:6 }}>
                    <button className="btn btn-outline btn-sm" onClick={() => openEdit(c)}>Edit</button> {/* edit: edit button */}
                    <button className="btn btn-danger btn-sm" onClick={() => handleDelete(c)}>Delete</button> {/* edit: delete button */}
                     </td>
                    </tr>
                  ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {modal && (
        <div className="modal-overlay" onClick={e => e.target === e.currentTarget && setModal(false)}>
          <div className="modal">
            <div className="modal-header">
               <h3>{editTarget ? 'Edit Course' : 'Add Course'}</h3> {/* edit: dynamic title */}
              <button className='modal-close' onClick={() => setModal(false)}>×</button>
            </div>
            <div className="modal-body">
              {error && <div className="alert alert-error">{error}</div>}
              <div className="form-group">
                <label>Course Name</label>
                <input type='text' value={form.name} onChange={e => setForm(f => ({ ...f, name: e.target.value }))}
                  placeholder="e.g. Database Systems" />
              </div>
              <div className="form-group">
                <label>Course Code</label>
                <input type='text' value={form.courseCode} onChange={e => setForm(f => ({ ...f, courseCode: e.target.value.toUpperCase() }))}
                  placeholder="e.g. CS301" maxLength={20} />
              </div>
              <div className="form-group">
                <label>Credit Hours</label>
                <select value={form.creditHours} onChange={e => setForm(f => ({ ...f, creditHours: e.target.value }))}>
                  {[1,2,3,4].map(n => <option key={n} value={n}>{n}</option>)}
                </select>
              </div>
              <div className="form-group">
                <label>Description (optional)</label>
                <textarea value={form.description} rows={3}
                  onChange={e => setForm(f => ({ ...f, description: e.target.value }))}
                  placeholder="Brief course description…" />
              </div>
            </div>
            <div className="modal-footer">
              <button className="btn btn-outline" onClick={() => setModal(false)}>Cancel</button>
              <button className="btn btn-primary" onClick={handleSave} disabled={saving}>
                {saving ? 'Saving…' : editTarget ? 'Save Changes' : 'Create Course'} {/* edit: dynamic button label */}
              </button>
            </div>
          </div>
        </div>
      )}
    </>
  )
}
