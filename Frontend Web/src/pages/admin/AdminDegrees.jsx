import React, { useEffect, useState, useCallback } from 'react'
import {
  getDepartments, getDegreesByDept, getDegreeById, createDegree,
  getCourses, addCourseToDegree, removeCourseFromDegree,deleteDegree, updateDegree
} from '../../api/client'

export default function AdminDegrees() {
  const [depts, setDepts]           = useState([])
  const [selDept, setSelDept]       = useState('')
  const [degrees, setDegrees]       = useState([])
  const [fullDegrees, setFullDegrees] = useState({}) // id -> full degree with semesters
  const [courses, setCourses]       = useState([])
  const [loading, setLoading]       = useState(false)

  // Modals
  const [showNewDegree, setShowNewDegree] = useState(false)
  const [showAddCourse, setShowAddCourse] = useState(null) // degreeId
  const [editDegreeTarget, setEditDegreeTarget] = useState(null) // edit: track which degree is being edited
  const [degreeForm, setDegreeForm] = useState({ name: '', code: '', durationYears: 4 })
  const [courseForm, setCourseForm] = useState({ courseId: '', semesterNumber: 1, isCompulsory: true })
  const [saving, setSaving]         = useState(false)
  const [error, setError]           = useState('')
  const [feedback, setFeedback]     = useState('')
  const [isErr, setIsErr]           = useState(false)

  useEffect(() => {
    Promise.all([getDepartments(), getCourses()])
      .then(([d, c]) => { setDepts(d); setCourses(c) })
  }, [])

  const loadDegrees = useCallback(async (deptId) => {
    if (!deptId) { setDegrees([]); setFullDegrees({}); return }
    setLoading(true)
    try {
      const list = await getDegreesByDept(deptId)
      setDegrees(list)
      // Load full details for each degree
      const full = {}
      await Promise.all(list.map(async d => {
        try { full[d.id] = await getDegreeById(d.id) } catch { full[d.id] = d }
      }))
      setFullDegrees(full)
    } finally { setLoading(false) }
  }, [])

  useEffect(() => { loadDegrees(selDept) }, [selDept, loadDegrees])

  // edit: open create modal blank
  function openCreateDegree() {
    setEditDegreeTarget(null)
    setDegreeForm({ name: '', code: '', durationYears: 4 })
    setError('')
    setShowNewDegree(true)
  }
  // edit: open edit modal pre-filled
  function openEditDegree(deg) {
    setEditDegreeTarget(deg)
    setDegreeForm({ name: deg.name, code: deg.code, durationYears: deg.durationYears })
    setError('')
    setShowNewDegree(true)
  }

  async function handleCreateDegree() {
    if (!degreeForm.name.trim() || !degreeForm.code.trim() || (!selDept && !editDegreeTarget)) {
      setError('Name, code and department are required.'); return
    }
    setSaving(true); setError('')
    try {
      if (editDegreeTarget) {
        // edit: update instead of create when editing — department cannot be changed via edit
        await updateDegree(editDegreeTarget.id, {
          name: degreeForm.name.trim(),
          code: degreeForm.code.trim().toUpperCase(),
          departmentId: editDegreeTarget.departmentId || Number(selDept), // edit: keep existing dept
          durationYears: Number(degreeForm.durationYears)
        })
        setFeedback(`"${degreeForm.name}" updated!`); setIsErr(false)
      }
       else {
        await createDegree({
          name: degreeForm.name.trim(),
          code: degreeForm.code.trim().toUpperCase(),
          departmentId: Number(selDept),
          durationYears: Number(degreeForm.durationYears)
        })
        setFeedback(`Degree "${degreeForm.name}" created!`); setIsErr(false)
      }
      setShowNewDegree(false)
      setDegreeForm({ name: '', code: '', durationYears: 4 })
      await loadDegrees(selDept)
    } catch(e) {
      setError(e.response?.data?.message || 'Failed to create degree.')
    } finally { setSaving(false) }
  }



  async function handleAddCourse() {
    if (!courseForm.courseId) { setError('Please select a course.'); return }
    setSaving(true); setError('')
    try {
      await addCourseToDegree({

        //Number conerts string into number,
        // which is needed for the API. 
        // The form values are strings because
        // they come from <select> elements
        degreeId: Number(showAddCourse),
        courseId: Number(courseForm.courseId),
        semesterNumber: Number(courseForm.semesterNumber),
        isCompulsory: courseForm.isCompulsory
      })
      setShowAddCourse(null)
      setCourseForm({ courseId: '', semesterNumber: 1, isCompulsory: true })
      // Refresh full degree
      const updated = await getDegreeById(showAddCourse)
      // it is done so that added course must appear in the degree after adding.
      setFullDegrees(prev => ({ ...prev, [showAddCourse]: updated }))
      setFeedback('Course added to degree!')
    } catch(e) {
      setError(e.response?.data?.message || 'Failed to add course.')
    } 
    finally{ 
      setSaving(false)
     }
  }

  async function handleRemoveCourse(degreeId, courseId, courseName) {
    if (!confirm(`Remove "${courseName}" from this degree?`)) return
    try {
      await removeCourseFromDegree(degreeId, courseId)
      const updated = await getDegreeById(degreeId)
      setFullDegrees(prev => ({ ...prev, [degreeId]: updated }))
      setFeedback('Course removed.')
    } catch(e) {
      alert(e.response?.data?.message || 'Failed to remove course.')
    }
  }

  // Build semester groups from fullDegree.semesters
  function getSemesters(degree) {
    const fd = fullDegrees[degree.id]
    if (!fd || !fd.semesters) return []
    return fd.semesters.filter(s => s.courses && s.courses.length > 0)
  }

  // List courses not yet assigned to this degree in this semester
  function getAvailableCourses(degreeId) {
    const fd = fullDegrees[degreeId]
    if (!fd || !fd.semesters) return courses
    const assigned = new Set(fd.semesters.flatMap(s => (s.courses || []).map(c => c.id)))
    return courses.filter(c => !assigned.has(c.id))
  }

  const deptObj = depts.find(d => String(d.id) === String(selDept))

  return (
    <>
      <div className="page-header">
        <div>
          <h1>Degrees & Courses</h1>
          <p>Manage degree programs and assign courses to semesters</p>
        </div>
      </div>

      {feedback && (
        <div className="alert alert-success" style={{ marginBottom: 16 , display:'flex', justifyContent:'space-between', alignItems:'center'}}>
          ✓ {feedback}
          <button onClick={() => setFeedback('')} style={{ paddingRight:'3%', background: 'none', border: 'none', cursor: 'pointer', fontSize: 20 }}>×</button>
        </div>
      )}

      {/* Department Selector */}
      <div className="card" style={{ marginBottom: 20 }}>
        <div className="toolbar" style={{ margin: 0 }}>
          <div className="form-group" style={{ margin: 0, flex: 1 }}>
            <label>Select Department</label>
            <select value={selDept} onChange={e => setSelDept(e.target.value)}>
              <option value="">— Choose a department to view its degrees —</option>
              {depts.map(d => <option key={d.id} value={d.id}>{d.name} ({d.code})</option>)}
            </select>
          </div>
          {selDept && (
            <div style={{ alignSelf: 'flex-end' }}>
              <button className="btn btn-primary" onClick={() => { setShowNewDegree(true); setError('') }}>
                + New Degree
              </button>
            </div>
          )}
        </div>
      </div>

      {/* Degree Cards */}
      {!selDept ? (
        <div className="card">
          <div className="empty">
            <div className="ei">🎓</div>
            <div className="et">Select a department to begin</div>
            <div className="es">Choose a department above to view and manage its degree programs</div>
          </div>
        </div>
      ) : loading ? <div className="loading">Loading degrees…</div>
      : degrees.length === 0 ? (
        <div className="card">
          <div className="empty">
            <div className="ei">📋</div>
            <div className="et">No degrees yet in {deptObj?.name}</div>
            <div className="es">Click "New Degree" to create the first degree program</div>
          </div>
        </div>
      ) : (
        <div className="degree-grid">
          {degrees.map(deg => {
            const semesters = getSemesters(deg)
            const fd = fullDegrees[deg.id]
            return (
              <div key={deg.id} className="degree-card">
                <div className="degree-card-head">
                  <div>
                    <div className="degree-name">{deg.name}</div>
                    <div className="degree-meta">
                      <span className="badge badge-purple" style={{ marginRight: 6 }}>{deg.code}</span>
                      {deg.durationYears}‑year program · {fd?.semesters?.reduce((acc, s) => acc + (s.courses?.length || 0), 0) || 0} courses
                    </div>
                  </div>
                  <button
                    className="btn btn-primary btn-sm"
                    onClick={() => { setShowAddCourse(deg.id); setCourseForm({ courseId: '', semesterNumber: 1, isCompulsory: true }); setError('') }}
                  >
                    + Add Course
                  </button>
                    {/* EDIT */}
                        <button
                        className="btn btn-outline btn-sm"
                        onClick={() => openEditDegree(deg)}
                        >
                    Edit
                  </button>

                  {/* DELETE */}
                  <button
                    className="btn btn-danger btn-sm"
                    onClick={async () => {
                      if (!confirm(`Delete degree "${deg.name}" (${deg.code})?`)) return
                      try {
                        await deleteDegree(deg.id)
                        await loadDegrees(selDept)
                        setFeedback(`"${deg.name}" deleted.`)
                        setIsErr(false)
                      } catch (e) {
                        setFeedback(e.response?.data?.message || 'Delete failed.')
                        setIsErr(true)
                      }
                    }}
                  >
                    Delete
                  </button>
                    

                </div>
                <div className="degree-card-body">
                  {semesters.length === 0 ? (
                    <p style={{ fontSize: 12.5, color: 'var(--text-muted)', fontStyle: 'italic' }}>
                      No courses assigned yet. Click "+ Add Course" to start building this degree's curriculum.
                    </p>//.sort() arranges array elements based on a comparison function. .sort() reorders array elements// compare function controls order// a - b → ascending// b - a → descending// modifies original array
                  ) : semesters.sort((a, b) => a.semesterNumber - b.semesterNumber).map(sem => (
                    <div key={sem.semesterNumber} className="semester-block">
                      <div className="semester-label">Semester {sem.semesterNumber}</div>
                      <div>
                        {(sem.courses || []).map(c => (
                          <span key={c.id} className="course-chip">
                            {c.courseCode} — {c.name}
                            <span className="rm" title="Remove course"
                              onClick={() => handleRemoveCourse(deg.id, c.id, c.name)}>×</span>
                          </span>
                        ))}
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            )
          })}
        </div>
      )}

      {/* New Degree Modal */}
      {showNewDegree && (
        <div className="modal-overlay" onClick={e => e.target === e.currentTarget && setShowNewDegree(false)}>
          <div className="modal">
            <div className="modal-header">
              <div><h3>{editDegreeTarget ? 'Edit Degree' : 'Create New Degree'}</h3><p>Under {deptObj?.name}</p></div> {/* edit: dynamic title */}
              <button className="modal-close" onClick={() => setShowNewDegree(false)}>×</button>
            </div>
            <div className="modal-body">
              {error && <div className="alert alert-error">{error}</div>}
              <div className="form-group">
                <label>Degree Name <span style={{ color: 'red' }}>*</span></label>
                <input  type="text" value={degreeForm.name}
                  onChange={e => setDegreeForm(f => ({ ...f, name: e.target.value }))}
                  placeholder="e.g. BS Computer Science" />
              </div>
              <div className="form-row-2">
                <div className="form-group">
                  <label>Code <span style={{ color: 'red' }}>*</span></label>
                  <input  type="text" value={degreeForm.code}
                    onChange={e => setDegreeForm(f => ({ ...f, code: e.target.value.toUpperCase() }))}
                    placeholder="e.g. BSCS" maxLength={20} />
                </div>
                <div className="form-group">
                  <label>Duration (years)</label>
                  <select value={degreeForm.durationYears}
                    onChange={e => setDegreeForm(f => ({ ...f, durationYears: e.target.value }))}>
                    {[2,3,4,5,6].map(n => <option key={n} value={n}>{n} years</option>)}
                  </select>
                </div>
              </div>
              <div className="alert alert-info" style={{ marginTop: 4 }}>
                A {degreeForm.durationYears}-year degree has {degreeForm.durationYears * 2} semesters (S1–S{degreeForm.durationYears * 2})
              </div>
            </div>
            <div className="modal-footer">
              <button className="btn btn-ghost" onClick={() => setShowNewDegree(false)}>Cancel</button>
              <button className="btn btn-primary" onClick={handleCreateDegree} disabled={saving}>
               {saving ? 'Saving…' : editDegreeTarget ? 'Save Changes' : 'Create Degree'} {/* edit: dynamic button label */}
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Add Course to Degree Modal */}
      {showAddCourse && (() => {
        const deg = degrees.find(d => d.id === showAddCourse) || {}
        const available = getAvailableCourses(showAddCourse)
        const maxSem = (deg.durationYears || 4) * 2
        return (
          <div className="modal-overlay" onClick={e => e.target === e.currentTarget && setShowAddCourse(null)}>
            <div className="modal">
              <div className="modal-header">
                <div><h3>Add Course to {deg.name}</h3><p>Assign a course to a specific semester</p></div>
                <button className="modal-close" onClick={() => setShowAddCourse(null)}>×</button>
              </div>
              <div className="modal-body">
                {error && <div className="alert alert-error">{error}</div>}
                <div className="form-group">
                  <label>Course <span style={{ color: 'red' }}>*</span></label>
                  <select value={courseForm.courseId}
                    onChange={e => setCourseForm(f => ({ ...f, courseId: e.target.value }))}>
                    <option value="">— Select a course —</option>
                    {available.map(c => (
                      <option key={c.id} value={c.id}>{c.courseCode} — {c.name} ({c.creditHours} cr)</option>
                    ))}
                  </select>
                  {available.length === 0 && (
                    <p style={{ fontSize: 12, color: 'var(--text-muted)', marginTop: 4 }}>All courses are already assigned to this degree.</p>
                  )}
                </div>
                <div className="form-row-2">
                  <div className="form-group">
                    <label>Semester <span style={{ color: 'red' }}>*</span></label>
                    <select value={courseForm.semesterNumber}
                      onChange={e => setCourseForm(f => ({ ...f, semesterNumber: Number(e.target.value) }))}>
                      {/*
                        maxSem = 4
                        Array.from({ length: 4 })
                        Result:
                        [undefined, undefined, undefined, undefined] 

                        (_, i) => i + 1
                           Parameters:
                          _ → unused value (just placeholder)
                          i → index (0, 1, 2, 3...)
                          So this becomes:
                          Array.from({ length: 4 }, (_, i) => i + 1)
                          Result:
                          [1, 2, 3, 4]
                           Now you have semester numbers*/}
                      {Array.from({ length: maxSem }, (_, i) => i + 1).map(n => (
                        <option key={n} value={n}>Semester {n}</option>
                      ))}
                    </select>
                  </div>
                  <div className="form-group">
                    <label>Type</label>
                    <select value={courseForm.isCompulsory}
                      onChange={e => setCourseForm(f => ({ ...f, isCompulsory: e.target.value === 'true' }))}>
                      <option value="true">Compulsory</option>
                      <option value="false">Elective</option>
                    </select>
                  </div>
                </div>
              </div>
              <div className="modal-footer">
                <button className="btn btn-ghost" onClick={() => setShowAddCourse(null)}>Cancel</button>
                <button className="btn btn-primary" onClick={handleAddCourse} disabled={saving || !courseForm.courseId}>
                  {saving ? 'Adding…' : 'Add Course'}
                </button>
              </div>
            </div>
          </div>
        )
      })()}
    </>
  )
}
