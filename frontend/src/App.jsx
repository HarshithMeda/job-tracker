import { useEffect, useState } from 'react'
import { clearToken, getMe, getToken } from './api'
import LoginPage from './LoginPage'

function App() {
  const [loggedIn, setLoggedIn] = useState(Boolean(getToken()))
  const [email, setEmail] = useState('')

  useEffect(() => {
    if (!loggedIn) {
      return
    }
    getMe()
      .then((data) => setEmail(data.email))
      .catch((err) => {
        if (err.status === 401) {
          clearToken()
          setLoggedIn(false)
        }
      })
  }, [loggedIn])

  function handleLogout() {
    clearToken()
    setEmail('')
    setLoggedIn(false)
  }

  if (!loggedIn) {
    return <LoginPage onLoggedIn={() => setLoggedIn(true)} />
  }

  return (
    <div className="card">
      <h1>Job Application Tracker</h1>
      <p>Welcome{email ? ', ' + email : ''}.</p>
      <p className="hint">Your applications will appear here in Part C.</p>
      <button type="button" onClick={handleLogout}>Log out</button>
    </div>
  )
}

export default App
