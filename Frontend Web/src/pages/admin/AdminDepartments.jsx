import React, { useEffect, useState } from 'react'
import { getDepartments, createDepartment, updateDepartment, deleteDepartment } from '../../api/client'

export default function AdminDepartments() {
  const [items, setItems]     = useState([])
  const [loading, setLoading] = useState(true)
  const [modal, setModal]     = useState(null) // null | 'create' | dept object
  const [form, setForm]       = useState({ name: '', code: '' })
  const [saving, setSaving]   = useState(false)
  const [error, setError]     = useState('')

  const load = () => getDepartments().then(setItems).finally(() => setLoading(false))
  useEffect(() => { load() }, [])

  function openCreate() { 
    setForm({ name: '', code: '' });
    setModal('create');
    setError('') }

  function openEdit(d){ 
    setForm({ name: d.name, code: d.code }); 
    setModal(d); 
    setError('') 
  }

  function close(){ 
      setModal(null)
   }

  async function handleSave() {
    //.trim() remmoves white space from both ends, preventing names/codes that are just spaces
    if (!form.name.trim() || !form.code.trim()){ 
      setError('Both fields are required.'); 
      return 
    }
    setSaving(true);
    setError('')
    try {
      if (modal === 'create') {
        await createDepartment(form)
      } else {
        await updateDepartment(modal.id, form)
      }
      await load()
      close()
    } catch (e) {
      setError(e.response?.data?.message || 'Save failed.')
    } finally {
      setSaving(false)
    }
  }

  async function handleDelete(d) {
    if (!confirm(`Delete department "${d.name}"? This cannot be undone.`)) return
    try {
      await deleteDepartment(d.id)
      await load()
    } catch (e) {
      alert(e.response?.data?.message || 'Delete failed.')
    }
  }

  return (
    <>
      <div className="page-header">
        <h1>Departments</h1>
        <p>Manage university departments</p>
      </div>

      <div className="card">
        <div className="toolbar">
          <span style={{ fontWeight: 600 }}>{items.length} department{items.length !== 1 ? 's' : ''}</span>
          <div className="toolbar-spacer" />
          <button className="btn btn-primary" onClick={openCreate}>+ New Department</button>
        </div>

        {loading ? <div className="loading">Loading…</div> : (
          <div className="table-wrap">
            <table>
              <thead>
                <tr><th>Name</th><th>Code</th><th>Degrees</th><th>Students</th><th>Actions</th></tr>
              </thead>
              <tbody>
                {items.length === 0
                  ? <tr><td colSpan={5}><div className="empty">No departments yet</div></td></tr>
                  : items.map(d => (
                    <tr key={d.id}>
                      <td><strong>{d.name}</strong></td>
                      <td><span className="badge badge-blue">{d.code}</span></td>
                      <td>{d.degreeCount}</td>
                      <td>{d.studentCount}</td>
                      <td>
                        <div className="actions">
                          <button className="btn btn-outline btn-sm" onClick={() => openEdit(d)}>Edit</button>
                          <button className="btn btn-danger btn-sm" onClick={() => handleDelete(d)}>Delete</button>
                        </div>
                      </td>
                    </tr>
                  ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {modal && (
        <div className="modal-overlay" onClick={e => e.target === e.currentTarget && close()}>
          <div className="modal">
            <div className="modal-header">
              <h3>{modal === 'create' ? 'New Department' : 'Edit Department'}</h3>
              <button className='modal-close' onClick={close}>×</button>
            </div>
            <div className="modal-body">
              {error && <div className="alert alert-error">{error}</div>}
              <div className="form-group">
                <label>Department Name</label>
                <input type='text' value={form.name} onChange={e => setForm(f => ({ ...f, name: e.target.value }))}
                  placeholder="e.g. Department of Computer Science" />
              </div>
              <div className="form-group">
                <label>Code (uppercase)</label>
                <input type="text" value={form.code} onChange={e => setForm(f => ({ ...f, code: e.target.value.toUpperCase() }))}
                  placeholder="e.g. CS" maxLength={10} />
              </div>
            </div>
            <div className="modal-footer">
              <button className="btn btn-outline" onClick={close}>Cancel</button>
              <button className="btn btn-primary" onClick={handleSave} disabled={saving}>
                {saving ? 'Saving…' : 'Save'}
              </button>
            </div>
          </div>
        </div>
      )}
    </>
  )
}
