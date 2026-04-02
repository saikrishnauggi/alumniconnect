import { useEffect, useState } from 'react'
import { profileAPI } from '../api'
import { useAuth } from '../context/AuthContext'

export default function Profile() {
  const { user } = useAuth()
  const isAlumni = user?.role === 'ALUMNI'

  const [profile,  setProfile]  = useState(null)
  const [editing,  setEditing]  = useState(false)
  const [loading,  setLoading]  = useState(true)
  const [saving,   setSaving]   = useState(false)
  const [success,  setSuccess]  = useState(false)
  const [error,    setError]    = useState('')

  const [form, setForm] = useState({
    fullName: '', department: '', graduationYear: '',
    company: '', jobTitle: '', skills: '', bio: '', isAvailable: true
  })

  useEffect(() => {
    profileAPI.getMyProfile()
      .then(r => {
        setProfile(r.data)
        setForm({
          fullName:       r.data.fullName || '',
          department:     r.data.department || '',
          graduationYear: r.data.graduationYear || '',
          company:        r.data.company || '',
          jobTitle:       r.data.jobTitle || '',
          skills:         r.data.skills || '',
          bio:            r.data.bio || '',
          isAvailable:    r.data.isAvailable ?? true,
        })
      })
      .catch(() => setEditing(true))   // No profile yet → go straight to create
      .finally(() => setLoading(false))
  }, [])

  const f = (k) => (e) => setForm(prev => ({ ...prev, [k]: e.target.type === 'checkbox' ? e.target.checked : e.target.value }))

  const save = async () => {
    setSaving(true); setError('')
    try {
      if (profile) {
        const r = await profileAPI.updateProfile(form)
        setProfile(r.data)
      } else {
        const r = await profileAPI.createProfile({ ...form, role: user.role })
        setProfile(r.data)
      }
      setEditing(false)
      setSuccess(true)
      setTimeout(() => setSuccess(false), 3000)
    } catch (err) {
      setError(err.response?.data?.error || 'Failed to save profile')
    } finally {
      setSaving(false)
    }
  }

  if (loading) return <div className="loading-screen"><div className="spinner"/></div>

  const initials = form.fullName?.split(' ').map(n => n[0]).join('').slice(0,2).toUpperCase() || user?.email?.slice(0,2).toUpperCase() || 'ME'
  const skills   = (profile?.skills || '').split(',').filter(Boolean)

  return (
    <div>
      <div className="page-header" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
        <div>
          <h1>My profile</h1>
          <p>How others see you on the platform.</p>
        </div>
        {!editing && (
          <button className="btn btn-secondary" onClick={() => setEditing(true)}>Edit profile</button>
        )}
      </div>

      {success && (
        <div style={{ background: 'var(--green-bg)', color: 'var(--green)', border: '1px solid #BBF7D0',
          borderRadius: 'var(--radius)', padding: '10px 16px', marginBottom: 20, fontSize: 14 }}>
          ✓ Profile saved successfully.
        </div>
      )}

      {!editing ? (
        /* ── View mode ─────────────────────── */
        <>
          <div className="profile-header-card">
            <div className="profile-avatar-lg">{initials}</div>
            <div className="profile-info">
              <h2 className="profile-name">{profile?.fullName || '—'}</h2>
              <p className="profile-sub">
                {profile?.jobTitle && <span>{profile.jobTitle}</span>}
                {profile?.company  && <span> · {profile.company}</span>}
                {profile?.department && <span> · {profile.department}</span>}
                {profile?.graduationYear && <span> · Class of {profile.graduationYear}</span>}
              </p>
              <div className="profile-badges">
                <span className={`badge ${isAlumni ? 'badge-alumni' : 'badge-student'}`}>{user?.role}</span>
                {isAlumni && (
                  <span className={`badge ${profile?.isAvailable ? 'badge-available' : 'badge-unavailable'}`}>
                    {profile?.isAvailable ? 'Open to mentorship' : 'Not available'}
                  </span>
                )}
              </div>
            </div>
          </div>

          {profile?.bio && (
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
        </>
      ) : (
        /* ── Edit mode ─────────────────────── */
        <div className="card" style={{ padding: '28px' }}>
          {error && <div className="error-msg">{error}</div>}

          {!profile && (
            <div style={{ background: 'var(--amber-bg)', color: 'var(--amber)', borderRadius: 'var(--radius)', padding: '10px 16px', marginBottom: 20, fontSize: 13 }}>
              Complete your profile so others can find and connect with you.
            </div>
          )}

          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '0 20px' }}>
            <div className="form-group">
              <label className="form-label">Full name *</label>
              <input className="form-input" value={form.fullName} onChange={f('fullName')} placeholder="Your full name" />
            </div>
            <div className="form-group">
              <label className="form-label">Department</label>
              <input className="form-input" value={form.department} onChange={f('department')} placeholder="e.g. Computer Science" />
            </div>
            <div className="form-group">
              <label className="form-label">Graduation year</label>
              <input className="form-input" value={form.graduationYear} onChange={f('graduationYear')} placeholder="e.g. 2022" />
            </div>
            {isAlumni && <>
              <div className="form-group">
                <label className="form-label">Company</label>
                <input className="form-input" value={form.company} onChange={f('company')} placeholder="Where you work" />
              </div>
              <div className="form-group">
                <label className="form-label">Job title</label>
                <input className="form-input" value={form.jobTitle} onChange={f('jobTitle')} placeholder="e.g. Software Engineer" />
              </div>
            </>}
          </div>

          <div className="form-group">
            <label className="form-label">Skills <span style={{ fontWeight: 400, color: 'var(--ink3)' }}>(comma-separated)</span></label>
            <input className="form-input" value={form.skills} onChange={f('skills')} placeholder="Java, Spring Boot, MySQL, Docker" />
          </div>

          <div className="form-group">
            <label className="form-label">Bio</label>
            <textarea className="form-textarea" rows={4} value={form.bio} onChange={f('bio')}
              placeholder="Tell others about yourself, your experience, and what you can offer..." />
          </div>

          {isAlumni && (
            <div className="form-group" style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
              <input type="checkbox" id="avail" checked={form.isAvailable} onChange={f('isAvailable')}
                style={{ width: 16, height: 16, accentColor: 'var(--accent)', cursor: 'pointer' }} />
              <label htmlFor="avail" style={{ fontSize: 14, cursor: 'pointer', color: 'var(--ink2)' }}>
                I am available to accept mentorship requests
              </label>
            </div>
          )}

          <div style={{ display: 'flex', gap: 10, marginTop: 8 }}>
            <button className="btn btn-primary" onClick={save} disabled={saving}>
              {saving ? <span className="spinner"/> : 'Save profile'}
            </button>
            {profile && <button className="btn btn-secondary" onClick={() => { setEditing(false); setError('') }}>Cancel</button>}
          </div>
        </div>
      )}
    </div>
  )
}
