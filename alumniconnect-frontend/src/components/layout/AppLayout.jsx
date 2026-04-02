import { NavLink, Outlet, useNavigate } from 'react-router-dom'
import { useAuth } from '../../context/AuthContext'
import { useEffect, useState } from 'react'
import { notificationAPI } from '../../api'

const IconDashboard  = () => <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8"><rect x="3" y="3" width="7" height="7" rx="1"/><rect x="14" y="3" width="7" height="7" rx="1"/><rect x="3" y="14" width="7" height="7" rx="1"/><rect x="14" y="14" width="7" height="7" rx="1"/></svg>
const IconUsers      = () => <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg>
const IconLink       = () => <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8"><path d="M10 13a5 5 0 0 0 7.54.54l3-3a5 5 0 0 0-7.07-7.07l-1.72 1.71"/><path d="M14 11a5 5 0 0 0-7.54-.54l-3 3a5 5 0 0 0 7.07 7.07l1.71-1.71"/></svg>
const IconBell       = () => <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8"><path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"/><path d="M13.73 21a2 2 0 0 1-3.46 0"/></svg>
const IconUser       = () => <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8"><path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/></svg>
const IconLogout     = () => <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" width="16" height="16"><path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/><polyline points="16 17 21 12 16 7"/><line x1="21" y1="12" x2="9" y2="12"/></svg>

export default function AppLayout() {
  const { user, logout } = useAuth()
  const navigate = useNavigate()
  const [unread, setUnread] = useState(0)
  const isAlumni = user?.role === 'ALUMNI'

  useEffect(() => {
    notificationAPI.getCount()
      .then(r => setUnread(r.data.count))
      .catch(() => {})
    const interval = setInterval(() => {
      notificationAPI.getCount()
        .then(r => setUnread(r.data.count))
        .catch(() => {})
    }, 30000)
    return () => clearInterval(interval)
  }, [])

  const initials = user?.email?.slice(0, 2).toUpperCase() || 'AC'

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <div className="sidebar-logo">
          <h2>Alumni Connect</h2>
          <span>Mentorship Platform</span>
        </div>

        <nav className="sidebar-nav">
          <NavLink to="/dashboard"     className={({isActive}) => `nav-item${isActive?' active':''}`}><IconDashboard/> Dashboard</NavLink>
          <NavLink to="/alumni"        className={({isActive}) => `nav-item${isActive?' active':''}`}><IconUsers/> Browse Alumni</NavLink>
          <NavLink to="/connections"   className={({isActive}) => `nav-item${isActive?' active':''}`}><IconLink/>
            {isAlumni ? 'Requests' : 'My Connections'}
          </NavLink>
          <NavLink to="/notifications" className={({isActive}) => `nav-item${isActive?' active':''}`}><IconBell/>
            Notifications
            {unread > 0 && <span className="nav-badge">{unread}</span>}
          </NavLink>
          <NavLink to="/profile"       className={({isActive}) => `nav-item${isActive?' active':''}`}><IconUser/> My Profile</NavLink>
        </nav>

        <div className="sidebar-user">
          <div className="sidebar-avatar">{initials}</div>
          <div className="sidebar-user-info">
            <div className="sidebar-user-name">{user?.email}</div>
            <div className="sidebar-user-role">{user?.role?.toLowerCase()}</div>
          </div>
          <button className="logout-btn" onClick={() => { logout(); navigate('/login') }} title="Logout">
            <IconLogout />
          </button>
        </div>
      </aside>

      <main className="main-content">
        <Outlet />
      </main>
    </div>
  )
}
