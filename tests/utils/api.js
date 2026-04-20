const API_BASE = 'http://localhost:7777/api/v1'

async function request(api, method, path, data = null, token = null) {
  const headers = { 'Content-Type': 'application/json' }
  if (token) headers['Authorization'] = `Bearer ${token}`
  const opts = { method, headers }
  if (data && method !== 'GET') opts.data = data
  if (data && method === 'GET') opts.params = data
  const res = await api.post(`${API_BASE}${path}`, { ...opts, data: method === 'GET' ? undefined : data })
  return res
}

async function login(api, username, password) {
  const res = await api.post(`${API_BASE}/user/login`, { data: { username, password } })
  const json = res.json ? await res.json() : res
  return json
}

module.exports = { API_BASE, request, login }