import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { connectionAPI, profileAPI, notificationAPI } from '../api'

export default function Dashboard() {
  const { user } = useAuth()
  const navigate  = useNavigate()
  const isAlumni  = user?.role === 'ALUMNI'
  const [stats, setStats] = useState({ connections: 0, requests: 0, pending: 0, unread: 0 })
  const [recent, setRecent] = useState([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    const load = async () => {
      try {
        const [connR, notifR] = await Promise.all([
          isAlumni ? connectionAPI.getIncoming() : connectionAPI.getMyRequests(),
          notificationAPI.getCount(),
        ])
        const connections = connR.data
        const accepted = connections.filter(c => c.status === 'ACCEPTED').length
        const pending  = connections.filter(c => c.status === 'PENDING').length
        setStats({
          connections: accepted,
          requests:    connections.length,
          pending,
          unread:      notifR.data.count,
        })
        setRecent(connections.slice(0, 4))
      } catch {}
      setLoading(false)
    }
    load()
  }, [isAlumni])

  const greeting = () => {
    const h = new Date().getHours()
    if (h < 12) return 'Good morning'
    if (h < 17) return 'Good afternoon'
    return 'Good evening'
  }

  const firstName = user?.email?.split('@')[0] || 'there'

  return (
    <div>
      <div className="page-header">
        <h1>{greeting()}, {firstName}.</h1>
        <p>{isAlumni ? 'Manage your mentorship requests and connections.' : 'Find and connect with alumni mentors.'}</p>
      </div>

      <div className="stats-grid">
        <div className="stat-card">
          <div className="stat-number stat-accent">{stats.connections}</div>
          <div className="stat-label">Active connections</div>
        </div>
        <div className="stat-card">
          <div className="stat-number">{stats.pending}</div>
          <div className="stat-label">{isAlumni ? 'Pending requests' : 'Requests sent'}</div>
        </div>
        <div className="stat-card">
          <div className="stat-number">{stats.unread}</div>
          <div className="stat-label">Unread notifications</div>
        </div>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 20 }}>
        {/* Recent activity */}
        <div className="card" style={{ padding: '22px 24px' }}>
          <h3 style={{ fontSize: 16, marginBottom: 16, color: 'var(--ink2)' }}>Recent activity</h3>
          {loading
            ? <div className="loading-screen"><div className="spinner"/></div>
            : recent.length === 0
              ? <div className="empty-state" style={{ padding: '30px 0' }}><p>No activity yet.</p></div>
              : <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
                  {recent.map(c => (
                    <div key={c.id} style={{ display: 'flex', alignItems: 'center', gap: 12, padding: '10px 0', borderBottom: '1px solid var(--border)' }}>
                      <div style={{ width: 36, height: 36, borderRadius: '50%', background: 'var(--cream2)', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: 13, fontWeight: 600, color: 'var(--accent)', flexShrink: 0 }}>
                        {isAlumni ? 'ST' : 'AL'}
                      </div>
                      <div style={{ flex: 1 }}>
                        <div style={{ fontSize: 13, fontWeight: 500 }}>
                          {isAlumni ? `Student #${c.studentId}` : `Alumni #${c.alumniId}`}
                        </div>
                        <div style={{ fontSize: 12, color: 'var(--ink3)', marginTop: 2 }}>
                          {new Date(c.requestedAt).toLocaleDateString()}
                        </div>
                      </div>
                      <span className={`badge badge-${c.status.toLowerCase()}`}>{c.status}</span>
                    </div>
                  ))}
                </div>
          }
        </div>

        {/* Quick actions */}
        <div className="card" style={{ padding: '22px 24px' }}>
          <h3 style={{ fontSize: 16, marginBottom: 16, color: 'var(--ink2)' }}>Quick actions</h3>
          <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
            {!isAlumni && (
              <button className="btn btn-primary" style={{ justifyContent: 'flex-start' }} onClick={() => navigate('/alumni')}>
                🔍 Browse alumni
              </button>
            )}
            <button className="btn btn-secondary" style={{ justifyContent: 'flex-start' }} onClick={() => navigate('/connections')}>
              🔗 {isAlumni ? 'View incoming requests' : 'View my connections'}
            </button>
            <button className="btn btn-secondary" style={{ justifyContent: 'flex-start' }} onClick={() => navigate('/profile')}>
              ✏️ Update my profile
            </button>
            <button className="btn btn-secondary" style={{ justifyContent: 'flex-start' }} onClick={() => navigate('/notifications')}>
              🔔 Check notifications {stats.unread > 0 && `(${stats.unread})`}
            </button>
          </div>
        </div>
      </div>
    </div>
  )
}
