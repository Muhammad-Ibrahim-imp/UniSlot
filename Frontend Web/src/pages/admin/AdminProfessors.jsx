import React, { useEffect, useState } from 'react'
import { getProfessors, createProfessor, getDepartments } from '../../api/client'

export default function AdminProfessors() {
  const [items, setItems]       = useState([])
  const [depts, setDepts]       = useState([])
  const [loading, setLoading]   = useState(true)
  const [modal, setModal]       = useState(false)
  const [form, setForm]         = useState({ name: '', email: '', qualification: '', departmentId: '' })
  const [saving, setSaving]     = useState(false)
  const [error, setError]       = useState('')
  const [filterDept, setFilter] = useState('')

  const load = () =>
    Promise.all([getProfessors(), getDepartments()])
      .then(([profs, d]) => { setItems(profs); setDepts(d) })
      .finally(() => setLoading(false))

      // we are not using await when calling load() because we want to load professors and departments in parallel, and set loading to false only after both are done
      {/*Component renders
        useEffect runs
        load() starts API calls
        React does NOT wait
        UI renders (maybe with loading state)
        When data arrives → setItems, setDepts
        React re-renders automatically 
        */}
  useEffect(() => { load() }, []) 

  const filtered = filterDept
    ? items.filter(p => p.departmentName === filterDept)
    : items

  async function handleSave() {
    if (!form.name.trim() || !form.email.trim()) {
       setError('Name and email are required.'); 
       return
     }
    setSaving(true);
    setError('');
    try {
      await createProfessor({ ...form, departmentId: form.departmentId ? Number(form.departmentId) : undefined })
      await load()
      setModal(false)
    } catch (e) {
      setError(e.response?.data?.message || 'Save failed.')
    } finally {
      setSaving(false)
    }
  }

  function fillRate(p) {
    if (!p.totalSeatsOffered) return 0
    return ((p.totalSeatsFilled / p.totalSeatsOffered) * 100).toFixed(1)
  }

  return (
    <>
      <div className="page-header">
        <h1>Professors</h1>
        <p>Faculty roster and slot fill-rate analytics</p>
      </div>

      <div className="card">
        <div className="toolbar">
          <select style={{ width: 200 }} value={filterDept} onChange={e => setFilter(e.target.value)}>
            <option value="">All Departments</option>
            {depts.map(d => <option key={d.id} value={d.name}>{d.name}</option>)}
          </select>
          <div className="toolbar-spacer" />
          <button className="btn btn-primary" onClick={() => { setForm({ name:'', email:'', qualification:'', departmentId:'' }); setError(''); setModal(true) }}>
            + New Professor
          </button>
        </div>

        {loading ? <div className="loading">Loading…</div> : (
          <div className="table-wrap">
            <table>
              <thead>
                <tr><th>Name</th><th>Email</th><th>Department</th><th>Qualification</th><th>Fill Rate</th></tr>
              </thead>
              <tbody>
                {filtered.length === 0
                  ? <tr><td colSpan={5}><div className="empty">No professors found</div></td></tr>
                  : filtered.map(p => {
                    const rate = parseFloat(fillRate(p))
                    return (
                      <tr key={p.id}>
                        <td><strong>{p.name}</strong></td>
                        <td style={{ fontSize: 13, color: '#6b7280' }}>{p.email}</td>
                        <td>{p.departmentName || '—'}</td>
                        <td style={{ fontSize: 12 }}>{p.qualification || '—'}</td>
                        <td>
                          <span className={`badge ${rate >= 80 ? 'badge-green' : rate >= 50 ? 'badge-yellow' : 'badge-gray'}`}>
                            {p.totalSeatsFilled}/{p.totalSeatsOffered} ({rate}%)
                          </span>
                        </td>
                      </tr>
                    )
                  })}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {modal && (
        <div className="modal-overlay" onClick={e => e.target === e.currentTarget && setModal(false)}>
          <div className="modal">
            <div className="modal-header">
              <h3>New Professor</h3>
              <button className="modal-close" onClick={() => setModal(false)}>×</button>
            </div>
            <div className="modal-body">
              {error && <div className="alert alert-error">{error}</div>}
              <div className="form-group">
                <label>Full Name</label>
                <input type="text" value={form.name} onChange={e => setForm(f => ({ ...f, name: e.target.value }))} placeholder="Dr. Jane Smith" />
              </div>
              <div className="form-group">
                <label>Email</label>
                <input type="email" value={form.email} onChange={e => setForm(f => ({ ...f, email: e.target.value }))} placeholder="j.smith@university.edu" />
              </div>
              <div className="form-group">
                <label>Qualification (optional)</label>
                <input type="text" value={form.qualification} onChange={e => setForm(f => ({ ...f, qualification: e.target.value }))} placeholder="PhD Computer Science, MIT" />
              </div>
              <div className="form-group">
                <label>Department (optional)</label>
                <select value={form.departmentId} onChange={e => setForm(f => ({ ...f, departmentId: e.target.value }))}>
                  <option value="">— Select department —</option>
                  {depts.map(d => <option key={d.id} value={d.id}>{d.name}</option>)}
                </select>
              </div>
            </div>
            <div className="modal-footer">
              <button className="btn btn-outline" onClick={() => setModal(false)}>Cancel</button>
              <button className="btn btn-primary" onClick={handleSave} disabled={saving}>
                {saving ? 'Saving…' : 'Create Professor'}
              </button>
            </div>
          </div>
        </div>
      )}
    </>
  )
}