import React, { useEffect, useState } from 'react'
import { getStudents, createStudent, updateFeeStatus, getDepartments, getDegreesByDept } from '../../api/client'

export default function AdminStudents() {
  const [items, setItems]         = useState([])
  const [depts, setDepts]         = useState([])
  const [loading, setLoading]     = useState(true)
  const [modal, setModal]         = useState(false)
  const [search, setSearch]       = useState('')
  const [feeFilter, setFeeFilter] = useState('')
  const [deptFilter, setDeptFilter] = useState('')
  const [saving, setSaving]       = useState(false)
  const [error, setError]         = useState('')
  const [feedback, setFeedback]   = useState('')
  const [degrees, setDegrees]     = useState([])
  const [form, setForm] = useState({ name:'', rollNumber:'', email:'', departmentId:'', degreeId:'', currentSemester:1 })

  const load = () => Promise.all([getStudents(), getDepartments()])
    .then(([s, d]) => { setItems(s); setDepts(d) })
    .finally(() => setLoading(false))

  useEffect(() => { load() }, [])

  async function loadDegrees(deptId) {
    setDegrees([])
    setForm(f => ({ ...f, degreeId: '' }))
    if (deptId) {
      const d = await getDegreesByDept(deptId)
      setDegrees(d)
    }
  }

  const filtered = items.filter(s => {
    const q = search.toLowerCase()
    const matchSearch = !search || s.name.toLowerCase().includes(q) ||
      s.rollNumber.toLowerCase().includes(q) || s.email.toLowerCase().includes(q)
    const matchFee  = !feeFilter  || s.feeStatus === feeFilter
    const matchDept = !deptFilter || String(s.departmentCode) === deptFilter
    return matchSearch && matchFee && matchDept
  })

  async function handleFeeToggle(s) {
    const ns = s.feeStatus === 'PAID' ? 'UNPAID' : 'PAID'
    try { await updateFeeStatus(s.id, ns);
       await load();
        setFeedback(`Fee status updated for ${s.name}.`) }
    catch(e) { alert(e.response?.data?.message || 'Update failed.') }
  }

  async function handleSave() {
    if (!form.name || !form.rollNumber || !form.email || !form.departmentId || !form.degreeId)
      { setError('All fields are required.'); return }
    setSaving(true); setError('')
    try {
      await createStudent({
        ...form, departmentId: Number(form.departmentId),
        degreeId: Number(form.degreeId), currentSemester: Number(form.currentSemester)
      })
      await load(); setModal(false)
      setFeedback(`Student ${form.name} registered. Default password = roll number.`)
    } catch(e) { setError(e.response?.data?.message || 'Save failed.') }
    finally { setSaving(false) }
  }

  const paid   = items.filter(s => s.feeStatus === 'PAID').length
  const unpaid = items.length - paid

  return (
    <>
      <div className="page-header">
        <div>
          <h1>Students</h1>
          <p>Manage student accounts and fee status</p>
        </div>
        <button className="btn btn-primary" onClick={() => {
          setForm({ name:'', rollNumber:'', email:'', departmentId:'', degreeId:'', currentSemester:1 })
          setDegrees([]); setError(''); setModal(true)
        }}>+ Register Student</button>
      </div>

      {feedback && (
        <div className="alert alert-success" style={{ marginBottom: 16 }}>
          ✓ {feedback}
          <button onClick={() => setFeedback('')} style={{ marginLeft: 'auto', background: 'none', border: 'none', cursor: 'pointer', fontSize: 14 }}>×</button>
        </div>
      )}

      {/* Summary */}
      <div className="stats-grid" style={{ gridTemplateColumns: 'repeat(4,1fr)', marginBottom: 20 }}>
        <div className="stat-card" style={{ '--sc':'var(--c-primary)' }}>
          <div className="s-icon">👩‍🎓</div><div className="s-label">Total</div>
          <div className="s-value" style={{ color:'var(--c-primary)' }}>{items.length}</div>
        </div>
        <div className="stat-card" style={{ '--sc':'var(--c-success)' }}>
          <div className="s-icon">✅</div><div className="s-label">Fee Paid</div>
          <div className="s-value" style={{ color:'var(--c-success)' }}>{paid}</div>
        </div>
        <div className="stat-card" style={{ '--sc':'var(--c-danger)' }}>
          <div className="s-icon">⚠️</div><div className="s-label">Fee Unpaid</div>
          <div className="s-value" style={{ color:'var(--c-danger)' }}>{unpaid}</div>
        </div>
        <div className="stat-card" style={{ '--sc':'var(--c-warning)' }}>
          <div className="s-icon">📅</div><div className="s-label">Slots Selected</div>
          <div className="s-value" style={{ color:'var(--c-warning)' }}>{items.reduce((a,s) => a + s.slotsSelected, 0)}</div>
        </div>
      </div>

      <div className="card">
        <div className="toolbar">
          <input type='text' style={{ width: 220 }} placeholder="Search name, roll no, email…"
            value={search} onChange={e => setSearch(e.target.value)} />
          <select style={{ width: 130 }} value={feeFilter} onChange={e => setFeeFilter(e.target.value)}>
            <option value="">All Fees</option>
            <option value="PAID">Paid</option>
            <option value="UNPAID">Unpaid</option>
          </select>
          <select style={{ width: 160 }} value={deptFilter} onChange={e => setDeptFilter(e.target.value)}>
            <option value="">All Departments</option>
            {depts.map(d => <option key={d.id} value={d.code}>{d.code}</option>)}
          </select>
          <div className="toolbar-spacer" />
          <span style={{ fontSize: 13, color: 'var(--text-muted)' }}>{filtered.length} student{filtered.length !== 1 ? 's' : ''}</span>
        </div>

        {loading ? <div className="loading">Loading…</div> : (
          <div className="table-wrap">
            <table>
              <thead>
                <tr><th>Student</th><th>Roll No</th><th>Degree</th><th>Semester</th><th>Slots</th><th>Fee</th><th>Action</th></tr>
              </thead>
              <tbody>
                {filtered.length === 0 ? (
                  <tr><td colSpan={7}><div className="empty"><div className="es">No students found</div></div></td></tr>
                ) : filtered.map(s => (
                  <tr key={s.id}>
                    <td>
                      <div style={{ fontWeight: 600 }}>{s.name}</div>
                      <div style={{ fontSize: 12, color: 'var(--text-muted)' }}>{s.email}</div>
                    </td>
                    <td><code style={{ fontSize: 12, background: 'var(--c-neutral-l)', padding: '1px 6px', borderRadius: 4 }}>{s.rollNumber}</code></td>
                    <td>
                      <span className="badge badge-purple">{s.degreeCode}</span>
                      <div style={{ fontSize: 11, color: 'var(--text-muted)', marginTop: 2 }}>{s.departmentCode}</div>
                    </td>
                    <td><span className="badge badge-blue">Sem {s.currentSemester}</span></td>
                    <td>
                      <span style={{ fontWeight: 700 }}>{s.slotsSelected}</span>
                      <span style={{ color: 'var(--text-muted)', fontSize: 11 }}> selected</span>
                    </td>
                    <td><span className={`badge ${s.feeStatus === 'PAID' ? 'badge-green' : 'badge-red'}`}>{s.feeStatus}</span></td>
                    <td>
                      <button
                        className={`btn btn-sm ${s.feeStatus === 'PAID' ? 'btn-ghost' : 'btn-success'}`}
                        onClick={() => handleFeeToggle(s)}
                        title={s.feeStatus === 'PAID' ? 'Mark as unpaid' : 'Mark as paid — enables slot selection'}
                      >
                        {s.feeStatus === 'PAID' ? 'Mark Unpaid' : '✓ Mark Paid'}
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* Register Student Modal */}
      {modal && (
        <div className="modal-overlay" onClick={e => e.target === e.currentTarget && setModal(false)}>
          <div className="modal">
            <div className="modal-header">
              <div><h3>Register Student</h3><p>Default password = roll number</p></div>
              <button className="modal-close" onClick={() => setModal(false)}>×</button>
            </div>
            <div className="modal-body">
              {error && <div className="alert alert-error">{error}</div>}
              <div className="alert alert-info">Student will login using their email. Initial password equals their roll number.</div>
              <div className="form-group">
                <label>Full Name <span style={{ color: 'red' }}>*</span></label>
                <input type='text' value={form.name} onChange={e => setForm(f => ({ ...f, name: e.target.value }))} placeholder="Ali Hassan" />
              </div>
              <div className="form-row-2">
                <div className="form-group">
                  <label>Roll Number <span style={{ color: 'red' }}>*</span></label>
                  <input type='text' value={form.rollNumber} onChange={e => setForm(f => ({ ...f, rollNumber: e.target.value }))} placeholder="CS-2024-001" />
                </div>
                <div className="form-group">
                  <label>Email <span style={{ color: 'red' }}>*</span></label>
                  <input type="email" value={form.email} onChange={e => setForm(f => ({ ...f, email: e.target.value }))} placeholder="ali@university.edu" />
                </div>
              </div>
              <div className="form-row-2">
                <div className="form-group">
                  <label>Department <span style={{ color: 'red' }}>*</span></label>
                  <select value={form.departmentId} onChange={e => {
                    setForm(f => ({ ...f, departmentId: e.target.value }))
                    loadDegrees(e.target.value)
                  }}>
                    <option value="">— Select —</option>
                    {depts.map(d => <option key={d.id} value={d.id}>{d.name}</option>)}
                  </select>
                </div>
                <div className="form-group">
                  <label>Degree <span style={{ color: 'red' }}>*</span></label>
                  <select value={form.degreeId} onChange={e => setForm(f => ({ ...f, degreeId: e.target.value }))} disabled={!form.departmentId}>
                    <option value="">— Select dept first —</option>
                    {degrees.map(d => <option key={d.id} value={d.id}>{d.name} ({d.code})</option>)}
                  </select>
                </div>
              </div>
              <div className="form-group">
                <label>Current Semester <span style={{ color: 'red' }}>*</span></label>
                <select value={form.currentSemester} onChange={e => setForm(f => ({ ...f, currentSemester: Number(e.target.value) }))}>
                  {[1,2,3,4,5,6,7,8,9,10,11,12].map(n => <option key={n} value={n}>Semester {n}</option>)}
                </select>
              </div>
            </div>
            <div className="modal-footer">
              <button className="btn btn-ghost" onClick={() => setModal(false)}>Cancel</button>
              <button className="btn btn-primary" onClick={handleSave} disabled={saving}>
                {saving ? 'Registering…' : 'Register Student'}
              </button>
            </div>
          </div>
        </div>
      )}
    </>
  )
}
