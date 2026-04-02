import { useEffect, useState } from 'react'
import { connectionAPI } from '../api'
import { useAuth } from '../context/AuthContext'

function StatusBadge({ status }) {
  return <span className={`badge badge-${status.toLowerCase()}`}>{status}</span>
}

function ConnectionRow({ conn, isAlumni, onAction }) {
  return (
    <div className="connection-card" style={{ marginBottom: 10 }}>
      <div style={{
        width: 42, height: 42, borderRadius: '50%',
        background: 'var(--cream2)', border: '2px solid var(--border)',
        display: 'flex', alignItems: 'center', justifyContent: 'center',
        fontSize: 14, fontWeight: 600, color: 'var(--accent)', flexShrink: 0
      }}>
        {isAlumni ? 'ST' : 'AL'}
      </div>

      <div className="connection-info">
        <div className="connection-name">
          {isAlumni ? `Student #${conn.studentId}` : `Alumni #${conn.alumniId}`}
        </div>
        <div className="connection-meta">
          Requested {new Date(conn.requestedAt).toLocaleDateString('en-IN', { day: 'numeric', month: 'short', year: 'numeric' })}
        </div>
        {conn.message && <div className="connection-msg">"{conn.message}"</div>}
      </div>

      <StatusBadge status={conn.status} />

      {isAlumni && conn.status === 'PENDING' && (
        <div className="connection-actions">
          <button className="btn btn-success btn-sm" onClick={() => onAction(conn.id, 'ACCEPTED')}>Accept</button>
          <button className="btn btn-danger  btn-sm" onClick={() => onAction(conn.id, 'DECLINED')}>Decline</button>
        </div>
      )}
    </div>
  )
}

export default function Connections() {
  const { user } = useAuth()
  const isAlumni = user?.role === 'ALUMNI'
  const [tab, setTab]         = useState(isAlumni ? 'pending' : 'all')
  const [data, setData]       = useState([])
  const [loading, setLoading] = useState(true)

  const load = async (t) => {
    setLoading(true)
    try {
      let res
      if (isAlumni) {
        res = t === 'pending' ? await connectionAPI.getPending() : await connectionAPI.getIncoming()
      } else {
        res = t === 'connections' ? await connectionAPI.getMyConnections() : await connectionAPI.getMyRequests()
      }
      setData(res.data)
    } catch {}
    setLoading(false)
  }

  useEffect(() => { load(tab) }, [tab])

  const handleAction = async (id, status) => {
    try {
      await connectionAPI.updateStatus(id, { status })
      load(tab)
    } catch (err) {
      alert(err.response?.data?.error || 'Action failed')
    }
  }

  const tabs = isAlumni
    ? [{ id: 'pending', label: 'Pending' }, { id: 'all', label: 'All requests' }]
    : [{ id: 'all', label: 'All requests' }, { id: 'connections', label: 'Active connections' }]

  return (
    <div>
      <div className="page-header">
        <h1>{isAlumni ? 'Mentorship requests' : 'My connections'}</h1>
        <p>{isAlumni ? 'Review and respond to incoming requests.' : 'Track your sent requests and active mentorships.'}</p>
      </div>

      <div className="tab-bar">
        {tabs.map(t => (
          <button key={t.id} className={`tab-btn${tab===t.id?' active':''}`} onClick={() => setTab(t.id)}>
            {t.label}
          </button>
        ))}
      </div>

      {loading
        ? <div className="loading-screen"><div className="spinner"/></div>
        : data.length === 0
          ? <div className="empty-state card" style={{ padding: '60px 20px' }}>
              <h3>Nothing here yet</h3>
              <p>{isAlumni ? 'No requests to show.' : 'You have not sent any requests yet.'}</p>
            </div>
          : <div>
              {data.map(c => (
                <ConnectionRow key={c.id} conn={c} isAlumni={isAlumni} onAction={handleAction} />
              ))}
            </div>
      }
    </div>
  )
}
