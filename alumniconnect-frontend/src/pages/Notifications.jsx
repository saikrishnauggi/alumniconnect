import { useEffect, useState } from 'react'
import { notificationAPI } from '../api'

function timeAgo(dateStr) {
  const diff = Date.now() - new Date(dateStr).getTime()
  const mins  = Math.floor(diff / 60000)
  const hours = Math.floor(diff / 3600000)
  const days  = Math.floor(diff / 86400000)
  if (mins < 1)   return 'just now'
  if (mins < 60)  return `${mins}m ago`
  if (hours < 24) return `${hours}h ago`
  return `${days}d ago`
}

const typeIcon = (type) => {
  if (type === 'NEW_REQUEST') return '📩'
  if (type === 'ACCEPTED')    return '✅'
  if (type === 'DECLINED')    return '❌'
  return '🔔'
}

export default function Notifications() {
  const [notifications, setNotifications] = useState([])
  const [loading, setLoading] = useState(true)

  const load = () => {
    notificationAPI.getAll()
      .then(r => setNotifications(r.data))
      .catch(() => {})
      .finally(() => setLoading(false))
  }

  useEffect(() => { load() }, [])

  const markRead = async (id) => {
    await notificationAPI.markRead(id).catch(() => {})
    setNotifications(prev => prev.map(n => n.id === id ? { ...n, isRead: true } : n))
  }

  const markAllRead = async () => {
    await notificationAPI.markAllRead().catch(() => {})
    setNotifications(prev => prev.map(n => ({ ...n, isRead: true })))
  }

  const unreadCount = notifications.filter(n => !n.isRead).length

  return (
    <div>
      <div className="page-header" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
        <div>
          <h1>Notifications</h1>
          <p>{unreadCount > 0 ? `${unreadCount} unread` : 'All caught up!'}</p>
        </div>
        {unreadCount > 0 && (
          <button className="btn btn-secondary btn-sm" onClick={markAllRead}>
            Mark all as read
          </button>
        )}
      </div>

      {loading
        ? <div className="loading-screen"><div className="spinner"/></div>
        : notifications.length === 0
          ? <div className="empty-state card" style={{ padding: '60px 20px' }}>
              <h3>No notifications</h3>
              <p>We'll notify you when something happens.</p>
            </div>
          : <div className="card notif-list">
              {notifications.map(n => (
                <div key={n.id}
                  className={`notif-item${!n.isRead ? ' unread' : ''}`}
                  onClick={() => !n.isRead && markRead(n.id)}>
                  <span style={{ fontSize: 18 }}>{typeIcon(n.type)}</span>
                  <div className="notif-text">
                    {n.message}
                  </div>
                  <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'flex-end', gap: 6, flexShrink: 0 }}>
                    <span className="notif-time">{timeAgo(n.createdAt)}</span>
                    {!n.isRead && <span className="notif-dot"/>}
                  </div>
                </div>
              ))}
            </div>
      }
    </div>
  )
}
