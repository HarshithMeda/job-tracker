import { useEffect, useState } from 'react'
import { deleteApplication, listApplications, updateStatus } from './api'
import { STATUSES } from './constants'
import AddApplicationForm from './AddApplicationForm'

const PAGE_SIZE = 5

function safeUrl(value) {
  if (!value) {
    return null
  }
  try {
    const url = new URL(value)
    return url.protocol === 'http:' || url.protocol === 'https:' ? url.href : null
  } catch {
    return null
  }
}

function HomePage({ email, onLogout }) {
  const [data, setData] = useState(null)
  const [statusFilter, setStatusFilter] = useState('')
  const [searchText, setSearchText] = useState('')
  const [company, setCompany] = useState('')
  const [page, setPage] = useState(0)
  const [reloadCount, setReloadCount] = useState(0)
  const [error, setError] = useState('')

  useEffect(() => {
    let cancelled = false
    listApplications({ status: statusFilter, company, page, size: PAGE_SIZE })
      .then((result) => {
        if (!cancelled) {
          setData(result)
          setError('')
        }
      })
      .catch((err) => {
        if (cancelled) {
          return
        }
        if (err.status === 401) {
          onLogout()
        } else {
          setError(err.message)
        }
      })
    return () => {
      cancelled = true
    }
  }, [statusFilter, company, page, reloadCount, onLogout])

  function reload() {
    setReloadCount((count) => count + 1)
  }

  function handleActionError(err) {
    if (err.status === 401) {
      onLogout()
    } else {
      setError(err.message)
    }
  }

  function applySearch(event) {
    event.preventDefault()
    setPage(0)
    setCompany(searchText.trim())
  }

  function changeFilter(event) {
    setStatusFilter(event.target.value)
    setPage(0)
  }

  function handleCreated() {
    setStatusFilter('')
    setSearchText('')
    setCompany('')
    setPage(0)
    reload()
  }

  async function changeStatus(id, newStatus) {
    try {
      await updateStatus(id, newStatus)
      reload()
    } catch (err) {
      handleActionError(err)
    }
  }

  async function remove(id, companyName) {
    if (!window.confirm('Delete the application to ' + companyName + '?')) {
      return
    }
    try {
      await deleteApplication(id)
      if (data.content.length === 1 && page > 0) {
        setPage(page - 1)
      } else {
        reload()
      }
    } catch (err) {
      handleActionError(err)
    }
  }

  return (
    <div className="card wide">
      <div className="topbar">
        <div>
          <h1>Job Application Tracker</h1>
          <p className="hint">Logged in as {email}</p>
        </div>
        <button type="button" onClick={onLogout}>Log out</button>
      </div>

      <h3>Add an application</h3>
      <AddApplicationForm onCreated={handleCreated} onSessionExpired={onLogout} />

      <h3>Your applications</h3>
      <form className="filters" onSubmit={applySearch}>
        <input
          type="search"
          placeholder="Search company..."
          value={searchText}
          onChange={(event) => setSearchText(event.target.value)}
        />
        <button type="submit">Search</button>
        <select value={statusFilter} onChange={changeFilter}>
          <option value="">All statuses</option>
          {STATUSES.map((s) => (
            <option key={s} value={s}>{s}</option>
          ))}
        </select>
      </form>

      {error && <p className="error">{error}</p>}

      {data === null && <p className="hint">Loading...</p>}

      {data !== null && data.content.length === 0 && (
        <p className="empty">No applications found.</p>
      )}

      {data !== null && data.content.length > 0 && (
        <div className="table-wrap">
          <table>
            <thead>
              <tr>
                <th>Company</th>
                <th>Job title</th>
                <th>Applied</th>
                <th>Status</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              {data.content.map((app) => (
                <tr key={app.id}>
                  <td>
                    {safeUrl(app.jobUrl) ? (
                      <a href={safeUrl(app.jobUrl)} target="_blank" rel="noopener noreferrer">
                        {app.companyName}
                      </a>
                    ) : (
                      app.companyName
                    )}
                  </td>
                  <td>
                    {app.jobTitle}
                    {app.notes && <div className="hint">{app.notes}</div>}
                  </td>
                  <td>{app.appliedDate}</td>
                  <td>
                    <select
                      value={app.status}
                      onChange={(event) => changeStatus(app.id, event.target.value)}
                    >
                      {STATUSES.map((s) => (
                        <option key={s} value={s}>{s}</option>
                      ))}
                    </select>
                  </td>
                  <td>
                    <button
                      type="button"
                      className="danger"
                      onClick={() => remove(app.id, app.companyName)}
                    >
                      Delete
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {data !== null && (
        <div className="pager">
          <button type="button" disabled={page === 0} onClick={() => setPage(page - 1)}>
            Previous
          </button>
          <span>
            Page {page + 1} of {Math.max(data.totalPages, 1)} &middot; {data.totalElements} total
          </span>
          <button
            type="button"
            disabled={page + 1 >= data.totalPages}
            onClick={() => setPage(page + 1)}
          >
            Next
          </button>
        </div>
      )}
    </div>
  )
}

export default HomePage
