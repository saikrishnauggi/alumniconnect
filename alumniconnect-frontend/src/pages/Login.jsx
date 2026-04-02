import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { authAPI } from '../api'
import { useAuth } from '../context/AuthContext'

export default function Login() {
  const [email, setEmail]       = useState('')
  const [password, setPassword] = useState('')
  const [error, setError]       = useState('')
  const [loading, setLoading]   = useState(false)
  const { login } = useAuth()
  const navigate  = useNavigate()

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError(''); setLoading(true)
    try {
      const { data } = await authAPI.login({ email, password })
      login({ userId: data.userId, email: data.email, role: data.role }, data.token)
      navigate('/dashboard')
    } catch (err) {
      setError(err.response?.data?.error || 'Invalid email or password')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="auth-shell">
      <div className="auth-left">
        <h1 className="auth-left-title">Connect with<br/><em>alumni</em> who<br/>shaped careers.</h1>
        <p className="auth-left-sub">Find mentors from your college, get guidance, and build connections that last a lifetime.</p>
        <div className="auth-features">
          <div className="auth-feature"><span className="auth-feature-dot"/><span>Browse alumni by skill and industry</span></div>
          <div className="auth-feature"><span className="auth-feature-dot"/><span>Send mentorship requests instantly</span></div>
          <div className="auth-feature"><span className="auth-feature-dot"/><span>Get notified on every update</span></div>
        </div>
      </div>

      <div className="auth-right">
        <div className="auth-form-box">
          <h2 className="auth-form-title">Welcome back</h2>
          <p className="auth-form-sub">Sign in to your account</p>

          {error && <div className="error-msg">{error}</div>}

          <form onSubmit={handleSubmit}>
            <div className="form-group">
              <label className="form-label">Email address</label>
              <input className="form-input" type="email" placeholder="you@college.edu"
                value={email} onChange={e => setEmail(e.target.value)} required />
            </div>
            <div className="form-group">
              <label className="form-label">Password</label>
              <input className="form-input" type="password" placeholder="••••••••"
                value={password} onChange={e => setPassword(e.target.value)} required />
            </div>
            <button className="btn btn-primary btn-full" type="submit" disabled={loading}>
              {loading ? <span className="spinner"/> : 'Sign in'}
            </button>
          </form>

          <p className="auth-switch">
            No account? <a onClick={() => navigate('/register')}>Create one</a>
          </p>
        </div>
      </div>
    </div>
  )
}
