import { useEffect, useState } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { profileAPI, connectionAPI } from '../api'
import { useAuth } from '../context/AuthContext'

export default function AlumniProfile() {
  const { id }   = useParams()
  const { user } = useAuth()
  const navigate = useNavigate()
  const [profile,  setProfile]  = useState(null)
  const [loading,  setLoading]  = useState(true)
  const [message,  setMessage]  = useState('')
  const [sending,  setSending]  = useState(false)
  const [sent,     setSent]     = useState(false)
  const [error,    setError]    = useState('')

  useEffect(() => {
    profileAPI.getById(id)
      .then(r => setProfile(r.data))
      .catch(() => navigate('/alumni'))
      .finally(() => setLoading(false))
  }, [id])

  const sendRequest = async () => {
    setSending(true); setError('')
    try {
      await connectionAPI.sendRequest({ alumniId: profile.userId, message })
      setSent(true)
    } catch (err) {
      setError(err.response?.data?.error || 'Failed to send request')
    } finally {
      setSending(false)
    }
  }

  if (loading) return <div className="loading-screen"><div className="spinner"/></div>
  if (!profile) return null

  const initials = profile.fullName?.split(' ').map(n => n[0]).join('').slice(0,2).toUpperCase() || 'AL'
  const skills   = profile.skills?.split(',').filter(Boolean) || []
  const isOwn    = user?.role === 'ALUMNI'

  return (
    <div>
      <button className="btn btn-ghost btn-sm" onClick={() => navigate(-1)} style={{ marginBottom: 20 }}>
        ← Back
      </button>

      <div className="profile-header-card">
        <div className="profile-avatar-lg">{initials}</div>
        <div className="profile-info">
          <h1 className="profile-name">{profile.fullName}</h1>
          <p className="profile-sub">
            {profile.jobTitle && <span>{profile.jobTitle}</span>}
            {profile.company  && <span> · {profile.company}</span>}
            {profile.department && <span> · {profile.department}</span>}
            {profile.graduationYear && <span> · Class of {profile.graduationYear}</span>}
          </p>
          <div className="profile-badges">
            <span className="badge badge-alumni">Alumni</span>
            <span className={`badge ${profile.isAvailable ? 'badge-available' : 'badge-unavailable'}`}>
              {profile.isAvailable ? 'Open to mentorship' : 'Not available'}
            </span>
          </div>
        </div>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 340px', gap: 20 }}>
        <div>
          {profile.bio && (
            <div className="profile-section">
              <h3>About</h3>
              <p style={{ fontSize: 14, color: 'var(--ink2)', lineHeight: 1.7 }}>{profile.bio}</p>
            </div>
          )}
          {skills.length > 0 && (
            <div className="profile-section">
              <h3>Skills</h3>
              <div>{skills.map(s => <span key={s} className="skill-chip">{s.trim()}</span>)}</div>
            </div>
          )}
        </div>

        {/* Connect panel — only for students */}
        {!isOwn && (
          <div className="profile-section" style={{ alignSelf: 'flex-start' }}>
            <h3>Send a request</h3>
            {sent
              ? <div style={{ background: 'var(--green-bg)', color: 'var(--green)', borderRadius: 'var(--radius)', padding: '12px 14px', fontSize: 14 }}>
                  ✓ Request sent! You'll be notified when they respond.
                </div>
              : profile.isAvailable
                ? <>
                    {error && <div className="error-msg">{error}</div>}
                    <div className="form-group">
                      <label className="form-label">Message</label>
                      <textarea className="form-textarea" rows={5}
                        placeholder="Introduce yourself..."
                        value={message} onChange={e => setMessage(e.target.value)} />
                    </div>
                    <button className="btn btn-primary btn-full" onClick={sendRequest} disabled={sending}>
                      {sending ? <span className="spinner"/> : 'Send mentorship request'}
                    </button>
                  </>
                : <p style={{ fontSize: 14, color: 'var(--ink3)' }}>This alumni is not currently accepting requests.</p>
            }
          </div>
        )}
      </div>
    </div>
  )
}
