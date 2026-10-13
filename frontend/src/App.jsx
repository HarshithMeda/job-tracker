import { useState } from 'react'

function App() {
  const [result, setResult] = useState('Not checked yet')

  async function checkBackend() {
    try {
      const response = await fetch('/api/applications')
      setResult('Backend answered with status ' + response.status)
    } catch (error) {
      setResult('Could not reach the backend. Is it running?')
    }
  }

  return (
    <div style={{ padding: 24, fontFamily: 'sans-serif' }}>
      <h1>Job Application Tracker</h1>
      <button onClick={checkBackend}>Check backend</button>
      <p>{result}</p>
    </div>
  )
}

export default App
