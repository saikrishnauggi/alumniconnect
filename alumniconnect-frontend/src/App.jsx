import { Routes, Route, Navigate } from 'react-router-dom'
import { AuthProvider, useAuth } from './context/AuthContext'
import Login      from './pages/Login'
import Register   from './pages/Register'
import Dashboard  from './pages/Dashboard'
import AlumniBrowse from './pages/AlumniBrowse'
import AlumniProfile from './pages/AlumniProfile'
import Connections from './pages/Connections'
import Notifications from './pages/Notifications'
import Profile    from './pages/Profile'
import AppLayout  from './components/layout/AppLayout'

function PrivateRoute({ children }) {
  const { user, loading } = useAuth()
  if (loading) return <div className="loading-screen"><div className="spinner" /></div>
  return user ? children : <Navigate to="/login" replace />
}

function PublicRoute({ children }) {
  const { user, loading } = useAuth()
  if (loading) return null
  return user ? <Navigate to="/dashboard" replace /> : children
}

export default function App() {
  return (
    <AuthProvider>
      <Routes>
        <Route path="/" element={<Navigate to="/login" replace />} />
        <Route path="/login"    element={<PublicRoute><Login /></PublicRoute>} />
        <Route path="/register" element={<PublicRoute><Register /></PublicRoute>} />

        <Route element={<PrivateRoute><AppLayout /></PrivateRoute>}>
          <Route path="/dashboard"     element={<Dashboard />} />
          <Route path="/alumni"        element={<AlumniBrowse />} />
          <Route path="/alumni/:id"    element={<AlumniProfile />} />
          <Route path="/connections"   element={<Connections />} />
          <Route path="/notifications" element={<Notifications />} />
          <Route path="/profile"       element={<Profile />} />
        </Route>
      </Routes>
    </AuthProvider>
  )
}
