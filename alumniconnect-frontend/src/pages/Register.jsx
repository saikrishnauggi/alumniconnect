import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { authAPI } from '../api'
import { useAuth } from '../context/AuthContext'

export default function Register() {
  const [email, setEmail]       = useState('')
  const [password, setPassword] = useState('')
  const [role, setRole]         = useState('STUDENT')
  const [error, setError]       = useState('')
  const [loading, setLoading]   = useState(false)
  const { login } = useAuth()
  const navigate  = useNavigate()

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError(''); setLoading(true)
    try {
      const { data } = await authAPI.register({ email, password, role })
      login({ userId: data.userId, email: data.email, role: data.role }, data.token)
      navigate('/profile')
    } catch (err) {
      setError(err.response?.data?.error || 'Registration failed. Please try again.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="auth-shell">
      <div className="auth-left">
        <h1 className="auth-left-title">Join the<br/><em>Alumni</em><br/>network.</h1>
        <p className="auth-left-sub">Whether you're a student seeking guidance or an alumni ready to give back — this is your space.</p>
        <div className="auth-features">
          <div className="auth-feature"><span className="auth-feature-dot"/><span>Students: find mentors in your field</span></div>
          <div className="auth-feature"><span className="auth-feature-dot"/><span>Alumni: manage your availability</span></div>
          <div className="auth-feature"><span className="auth-feature-dot"/><span>Build meaningful long-term connections</span></div>
        </div>
      </div>

      <div className="auth-right">
        <div className="auth-form-box">
          <h2 className="auth-form-title">Create account</h2>
          <p className="auth-form-sub">Choose your role to get started</p>

          {error && <div className="error-msg">{error}</div>}

          <div className="role-toggle">
            <button type="button" className={`role-btn${role==='STUDENT'?' active':''}`} onClick={() => setRole('STUDENT')}>
              STUDENT
            </button>
            <button type="button" className={`role-btn${role==='ALUMNI'?' active':''}`} onClick={() => setRole('ALUMNI')}>
              ALUMNI
            </button>
          </div>

          <form onSubmit={handleSubmit}>
            <div className="form-group">
              <label className="form-label">Email address</label>
              <input className="form-input" type="email" placeholder="you@college.edu"
                value={email} onChange={e => setEmail(e.target.value)} required />
            </div>
            <div className="form-group">
              <label className="form-label">Password</label>
              <input className="form-input" type="password" placeholder="At least 6 characters"
                value={password} onChange={e => setPassword(e.target.value)} required minLength={6} />
            </div>
            <button className="btn btn-primary btn-full" type="submit" disabled={loading}>
              {loading ? <span className="spinner"/> : `Create ${role.toLowerCase()} account`}
            </button>
          </form>

          <p className="auth-switch">
            Already have an account? <a onClick={() => navigate('/login')}>Sign in</a>
          </p>
        </div>
      </div>
    </div>
  )
}
