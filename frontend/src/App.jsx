import { useCallback, useEffect, useState } from 'react'
import { clearToken, getMe, getToken } from './api'
import LoginPage from './LoginPage'
import HomePage from './HomePage'

function App() {
  const [loggedIn, setLoggedIn] = useState(Boolean(getToken()))
  const [email, setEmail] = useState('')

  const handleLogout = useCallback(() => {
    clearToken()
    setEmail('')
    setLoggedIn(false)
  }, [])

  useEffect(() => {
    if (!loggedIn) {
      return
    }
    getMe()
      .then((data) => setEmail(data.email))
      .catch((err) => {
        if (err.status === 401) {
          handleLogout()
        }
      })
  }, [loggedIn, handleLogout])

  if (!loggedIn) {
    return <LoginPage onLoggedIn={() => setLoggedIn(true)} />
  }

  return <HomePage email={email} onLogout={handleLogout} />
}

export default App
