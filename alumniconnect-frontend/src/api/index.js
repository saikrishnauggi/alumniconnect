import axios from 'axios'

const api = axios.create({ 
  baseURL: 'http://localhost:8080/api'
})

// Attach JWT to every request automatically
api.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

// Auto-logout on 401
api.interceptors.response.use(
  res => res,
  err => {
    if (err.response?.status === 401) {
      localStorage.clear()
      window.location.href = '/login'
    }
    return Promise.reject(err)
  }
)

// ── Auth ─────────────────────────────────
export const authAPI = {
  register: (data)   => api.post('/auth/register', data),
  login:    (data)   => api.post('/auth/login', data),
}

// ── Profiles ─────────────────────────────
export const profileAPI = {
  createProfile:  (data)    => api.post('/profiles', data),
  updateProfile:  (data)    => api.put('/profiles/me', data),
  getMyProfile:   ()        => api.get('/profiles/me'),
  getById:        (id)      => api.get(`/profiles/${id}`),
  getAllAlumni:    ()        => api.get('/profiles/alumni'),
  searchAlumni:   (keyword) => api.get(`/profiles/alumni/search?keyword=${keyword}`),
}

// ── Connections ───────────────────────────
export const connectionAPI = {
  sendRequest:      (data) => api.post('/connections/request', data),
  updateStatus:     (id, data) => api.put(`/connections/${id}/status`, data),
  getMyRequests:    ()     => api.get('/connections/my-requests'),
  getMyConnections: ()     => api.get('/connections/my-connections'),
  getIncoming:      ()     => api.get('/connections/incoming'),
  getPending:       ()     => api.get('/connections/incoming/pending'),
}

// ── Notifications ─────────────────────────
export const notificationAPI = {
  getAll:       ()   => api.get('/notifications'),
  getUnread:    ()   => api.get('/notifications/unread'),
  getCount:     ()   => api.get('/notifications/unread/count'),
  markRead:     (id) => api.put(`/notifications/${id}/read`),
  markAllRead:  ()   => api.put('/notifications/read-all'),
}

export default api


/* // src/services/api.js
import axios from "axios";

const API = axios.create({
  baseURL: "http://localhost:8080", // 🔥 ALWAYS gateway
});

// Attach token automatically
API.interceptors.request.use((req) => {
  const token = localStorage.getItem("token");
  if (token) {
    req.headers.Authorization = `Bearer ${token}`;
  }
  return req;
});

export default API; */