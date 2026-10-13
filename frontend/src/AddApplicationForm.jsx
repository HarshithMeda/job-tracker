import { useState } from 'react'
import { createApplication } from './api'
import { STATUSES } from './constants'

function today() {
  return new Date().toLocaleDateString('en-CA')
}

function AddApplicationForm({ onCreated, onSessionExpired }) {
  const [companyName, setCompanyName] = useState('')
  const [jobTitle, setJobTitle] = useState('')
  const [status, setStatus] = useState('APPLIED')
  const [appliedDate, setAppliedDate] = useState(today())
  const [jobUrl, setJobUrl] = useState('')
  const [notes, setNotes] = useState('')
  const [error, setError] = useState('')
  const [saving, setSaving] = useState(false)

  async function handleSubmit(event) {
    event.preventDefault()
    setError('')
    setSaving(true)
    try {
      await createApplication({
        companyName: companyName.trim(),
        jobTitle: jobTitle.trim(),
        status,
        appliedDate,
        jobUrl: jobUrl.trim() || null,
        notes: notes.trim() || null,
      })
      setCompanyName('')
      setJobTitle('')
      setStatus('APPLIED')
      setAppliedDate(today())
      setJobUrl('')
      setNotes('')
      onCreated()
    } catch (err) {
      if (err.status === 401) {
        onSessionExpired()
      } else {
        setError(err.message)
      }
    } finally {
      setSaving(false)
    }
  }

  return (
    <form className="add-form" onSubmit={handleSubmit}>
      <div className="field">
        <label htmlFor="companyName">Company</label>
        <input
          id="companyName"
          value={companyName}
          onChange={(event) => setCompanyName(event.target.value)}
          required
        />
      </div>

      <div className="field">
        <label htmlFor="jobTitle">Job title</label>
        <input
          id="jobTitle"
          value={jobTitle}
          onChange={(event) => setJobTitle(event.target.value)}
          required
        />
      </div>

      <div className="field">
        <label htmlFor="status">Status</label>
        <select id="status" value={status} onChange={(event) => setStatus(event.target.value)}>
          {STATUSES.map((s) => (
            <option key={s} value={s}>{s}</option>
          ))}
        </select>
      </div>

      <div className="field">
        <label htmlFor="appliedDate">Applied on</label>
        <input
          id="appliedDate"
          type="date"
          value={appliedDate}
          onChange={(event) => setAppliedDate(event.target.value)}
          required
        />
      </div>

      <div className="field full">
        <label htmlFor="jobUrl">Job link (optional)</label>
        <input
          id="jobUrl"
          type="url"
          placeholder="https://..."
          value={jobUrl}
          onChange={(event) => setJobUrl(event.target.value)}
        />
      </div>

      <div className="field full">
        <label htmlFor="notes">Notes (optional)</label>
        <textarea
          id="notes"
          rows={2}
          maxLength={2000}
          value={notes}
          onChange={(event) => setNotes(event.target.value)}
        />
      </div>

      {error && <p className="error full">{error}</p>}

      <button type="submit" disabled={saving}>
        {saving ? 'Saving...' : 'Add application'}
      </button>
    </form>
  )
}

export default AddApplicationForm
