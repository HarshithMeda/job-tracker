const TOKEN_KEY = 'jobtracker_token'

export function getToken() {
  return localStorage.getItem(TOKEN_KEY)
}

export function saveToken(token) {
  localStorage.setItem(TOKEN_KEY, token)
}

export function clearToken() {
  localStorage.removeItem(TOKEN_KEY)
}

function buildMessage(data, status) {
  if (data && data.details) {
    return Object.values(data.details).join(', ')
  }
  if (data && data.message) {
    return data.message
  }
  return 'Request failed (status ' + status + ')'
}

async function request(path, options = {}) {
  const headers = { 'Content-Type': 'application/json' }
  const token = getToken()
  if (token && !path.startsWith('/api/auth/')) {
    headers.Authorization = 'Bearer ' + token
  }

  const response = await fetch(path, { ...options, headers })

  if (response.status === 204) {
    return null
  }

  let data = null
  try {
    data = await response.json()
  } catch {
    data = null
  }

  if (!response.ok) {
    const error = new Error(buildMessage(data, response.status))
    error.status = response.status
    throw error
  }
  return data
}

export function register(email, password) {
  return request('/api/auth/register', {
    method: 'POST',
    body: JSON.stringify({ email, password }),
  })
}

export async function login(email, password) {
  const data = await request('/api/auth/login', {
    method: 'POST',
    body: JSON.stringify({ email, password }),
  })
  saveToken(data.accessToken)
  return data
}

export function getMe() {
  return request('/api/me')
}

export function listApplications({ status, company, page, size }) {
  const params = new URLSearchParams()
  if (status) {
    params.set('status', status)
  }
  if (company) {
    params.set('company', company)
  }
  params.set('page', page)
  params.set('size', size)
  return request('/api/applications?' + params.toString())
}

export function createApplication(application) {
  return request('/api/applications', {
    method: 'POST',
    body: JSON.stringify(application),
  })
}

export function updateStatus(id, status) {
  return request('/api/applications/' + id + '/status', {
    method: 'PATCH',
    body: JSON.stringify({ status }),
  })
}

export function deleteApplication(id) {
  return request('/api/applications/' + id, { method: 'DELETE' })
}
