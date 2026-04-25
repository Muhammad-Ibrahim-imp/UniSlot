import React, { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../contexts/AuthContext'

export default function LoginPage() {
  const { login } = useAuth()
  const navigate  = useNavigate()
  const [email, setEmail]       = useState('')
  const [password, setPassword] = useState('')
  const [showPass, setShowPass] = useState(false)
  const [error, setError]       = useState('')
  const [loading, setLoading]   = useState(false)

  async function handleSubmit(e) {
    e.preventDefault(); setError(''); setLoading(true)
    try {
      const role = await login(email, password)
      navigate(role === 'ADMIN' ? '/admin' : '/student', { replace: true })
    } catch(err) {
      setError(err.response?.data?.message || 'Invalid email or password.')
    } finally { setLoading(false) }
  }

  return (
    <div className="login-page">
      <div style={{ width: '100%', maxWidth: 420 }}>
        <div className="login-card">
          <div style={{ textAlign: 'center', marginBottom: 32 }}>
            <div className="login-icon">🎓</div>
            <h1 style={{ fontSize: 24, fontWeight: 800, marginBottom: 4 }}>UniSlot</h1>
            <p style={{ color: 'var(--text-muted)', fontSize: 13.5 }}>University Slot Selection System</p>
          </div>

          {error && <div className="alert alert-error">{error}</div>}

          <form onSubmit={handleSubmit}>
            <div className="form-group">
              <label>Email address</label>
              <input type="email" value={email} required autoFocus
                onChange={e => setEmail(e.target.value)} placeholder="you@university.edu" />
            </div>
            <div className="form-group">
              <label>Password</label>
              <div style={{ position: 'relative' }}>
                <input type={showPass ? 'text' : 'password'} value={password} required
                  onChange={e => setPassword(e.target.value)} placeholder="••••••••"
                  style={{ paddingRight: 56 }} />
                <button type="button" onClick={() => setShowPass(s => !s)}
                  style={{ position: 'absolute', right: 10, top: '50%', transform: 'translateY(-50%)',
                    background: 'none', border: 'none', cursor: 'pointer', fontSize: 12, color: 'var(--text-muted)' }}>
                  {showPass ? 'Hide' : 'Show'}
                </button>
              </div>
            </div>
            <button className="btn btn-primary btn-full btn-lg" type="submit" disabled={loading}
              style={{ marginTop: 4 }}>
              {loading ? 'Signing in…' : 'Sign in →'}
            </button>
          </form>

          <p style={{ marginTop: 20, fontSize: 12, color: 'var(--text-subtle)', textAlign: 'center',
            padding: '10px', background: 'var(--bg)', borderRadius: 6 }}>
            Default admin: <strong>admin@university.edu</strong> / <strong>admin123</strong>
          </p>
        </div>
      </div>
    </div>
  )
}
