import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { profileAPI, connectionAPI } from '../api'

const SearchIcon = () => (
  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" width="16" height="16">
    <circle cx="11" cy="11" r="8"/><path d="m21 21-4.35-4.35"/>
  </svg>
)

function AlumniCard({ alumni, onConnect }) {
  const initials = alumni.fullName?.split(' ').map(n => n[0]).join('').slice(0,2).toUpperCase() || 'AL'
  const skills   = alumni.skills?.split(',').filter(Boolean) || []

  return (
    <div className="alumni-card">
      <div className="alumni-card-header">
        <div className="alumni-avatar">{initials}</div>
        <div style={{ flex: 1 }}>
          <div className="alumni-name">{alumni.fullName}</div>
          <div className="alumni-title">
            {alumni.jobTitle && alumni.company
              ? `${alumni.jobTitle} · ${alumni.company}`
              : alumni.jobTitle || alumni.company || 'Alumni'}
          </div>
          {alumni.department && (
            <div style={{ fontSize: 12, color: 'var(--ink3)', marginTop: 2 }}>{alumni.department} · {alumni.graduationYear}</div>
          )}
        </div>
        <span className={`badge ${alumni.isAvailable ? 'badge-available' : 'badge-unavailable'}`}>
          {alumni.isAvailable ? 'Available' : 'Busy'}
        </span>
      </div>

      {alumni.bio && (
        <p style={{ fontSize: 13, color: 'var(--ink3)', lineHeight: 1.6,
          display: '-webkit-box', WebkitLineClamp: 2, WebkitBoxOrient: 'vertical', overflow: 'hidden' }}>
          {alumni.bio}
        </p>
      )}

      <div className="alumni-skills">
        {skills.slice(0, 4).map(s => <span key={s} className="skill-chip">{s.trim()}</span>)}
        {skills.length > 4 && <span className="skill-chip">+{skills.length - 4}</span>}
      </div>

      <div className="alumni-card-footer">
        <button className="btn btn-ghost btn-sm" onClick={() => onConnect('view', alumni)}>View profile</button>
        {alumni.isAvailable && (
          <button className="btn btn-primary btn-sm" onClick={() => onConnect('request', alumni)}>
            Connect
          </button>
        )}
      </div>
    </div>
  )
}

function RequestModal({ alumni, onClose, onSent }) {
  const [message, setMessage] = useState('')
  const [loading, setLoading] = useState(false)
  const [error,   setError]   = useState('')

  const send = async () => {
    setLoading(true); setError('')
    try {
      await connectionAPI.sendRequest({ alumniId: alumni.userId, message })
      onSent()
    } catch (err) {
      setError(err.response?.data?.error || 'Failed to send request')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-box" onClick={e => e.stopPropagation()}>
        <h2 className="modal-title">Connect with {alumni.fullName}</h2>
        <p className="modal-sub">{alumni.jobTitle} {alumni.company ? `at ${alumni.company}` : ''}</p>
        {error && <div className="error-msg">{error}</div>}
        <div className="form-group">
          <label className="form-label">Message (optional)</label>
          <textarea className="form-textarea" rows={4}
            placeholder="Introduce yourself and explain what you're hoping to learn..."
            value={message} onChange={e => setMessage(e.target.value)} />
        </div>
        <div className="modal-footer">
          <button className="btn btn-secondary" onClick={onClose}>Cancel</button>
          <button className="btn btn-primary" onClick={send} disabled={loading}>
            {loading ? <span className="spinner"/> : 'Send request'}
          </button>
        </div>
      </div>
    </div>
  )
}

export default function AlumniBrowse() {
  const navigate  = useNavigate()
  const [alumni,  setAlumni]  = useState([])
  const [keyword, setKeyword] = useState('')
  const [loading, setLoading] = useState(true)
  const [modal,   setModal]   = useState(null)  // { type: 'request', alumni }
  const [success, setSuccess] = useState(false)

  const load = async (kw = '') => {
    setLoading(true)
    try {
      const { data } = kw.trim()
        ? await profileAPI.searchAlumni(kw)
        : await profileAPI.getAllAlumni()
      setAlumni(data)
    } catch {}
    setLoading(false)
  }

  useEffect(() => { load() }, [])

  const handleSearch = (e) => {
    e.preventDefault(); load(keyword)
  }

  const handleAction = (type, alumni) => {
    if (type === 'view')    navigate(`/alumni/${alumni.id}`)
    if (type === 'request') setModal({ type, alumni })
  }

  const handleSent = () => {
    setModal(null); setSuccess(true)
    setTimeout(() => setSuccess(false), 3000)
  }

  return (
    <div>
      <div className="page-header">
        <h1>Browse alumni</h1>
        <p>Find mentors by name, skill, company, or department.</p>
      </div>

      {success && (
        <div style={{ background: 'var(--green-bg)', color: 'var(--green)', border: '1px solid #BBF7D0',
          borderRadius: 'var(--radius)', padding: '10px 16px', marginBottom: 20, fontSize: 14 }}>
          ✓ Mentorship request sent! You'll be notified when they respond.
        </div>
      )}

      <form className="search-bar" onSubmit={handleSearch}>
        <div className="search-input-wrap" style={{ flex: 1 }}>
          <SearchIcon />
          <input className="form-input search-input" placeholder="Search by name, skill, company..."
            value={keyword} onChange={e => setKeyword(e.target.value)} />
        </div>
        <button className="btn btn-primary" type="submit">Search</button>
        {keyword && <button className="btn btn-secondary" type="button" onClick={() => { setKeyword(''); load() }}>Clear</button>}
      </form>

      {loading
        ? <div className="loading-screen"><div className="spinner"/></div>
        : alumni.length === 0
          ? <div className="empty-state card" style={{ padding: '60px 20px' }}>
              <h3>No alumni found</h3>
              <p>Try a different keyword or <button className="btn btn-ghost btn-sm" onClick={() => { setKeyword(''); load() }}>view all</button></p>
            </div>
          : <>
              <div style={{ fontSize: 13, color: 'var(--ink3)', marginBottom: 16 }}>
                {alumni.length} alumni found
              </div>
              <div className="alumni-grid">
                {alumni.map(a => <AlumniCard key={a.id} alumni={a} onConnect={handleAction}/>)}
              </div>
            </>
      }

      {modal?.type === 'request' && (
        <RequestModal alumni={modal.alumni} onClose={() => setModal(null)} onSent={handleSent} />
      )}
    </div>
  )
}
