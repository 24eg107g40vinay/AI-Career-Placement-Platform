import { useEffect, useRef, useState } from 'react';
import { Navigate, useParams } from 'react-router-dom';
import { api } from '../../services/api';
import { getRequestError } from '../../services/studentService';

const sections = {
  students: { title: 'Students', endpoint: '/admin/students', userRows: true },
  recruiters: { title: 'Recruiters', endpoint: '/admin/recruiters', userRows: true },
  companies: { title: 'Companies', endpoint: '/admin/companies' },
  jobs: { title: 'Jobs', endpoint: '/admin/jobs' },
  applications: { title: 'Applications', endpoint: '/admin/applications', applicationRows: true },
};

const applicationStatuses = ['APPLIED', 'SHORTLISTED', 'INTERVIEW', 'SELECTED', 'REJECTED'];

export function AdminManagement() {
  const { section = 'students' } = useParams();
  const config = sections[section] || sections.students;
  const isKnownSection = Boolean(sections[section]);
  const [query, setQuery] = useState('');
  const [rows, setRows] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [notice, setNotice] = useState('');
  const [busyUserIds, setBusyUserIds] = useState([]);
  const [busyApplicationId, setBusyApplicationId] = useState(null);
  const loadRequestId = useRef(0);

  async function load() {
    const requestId = ++loadRequestId.current;
    setLoading(true);
    setError('');
    try {
      const { data } = await api.get(config.endpoint, {
        params: (config.userRows || section === 'companies') && query ? { search: query } : undefined,
      });
      if (requestId === loadRequestId.current) setRows(data);
    } catch (requestError) {
      if (requestId === loadRequestId.current) {
        setError(getRequestError(requestError, `Could not load ${config.title.toLowerCase()}.`));
      }
    } finally {
      if (requestId === loadRequestId.current) setLoading(false);
    }
  }

  useEffect(() => {
    if (isKnownSection) load();
  }, [section, query, isKnownSection]);

  async function toggleUser(user) {
    const enabled = !user.enabled;
    if (!window.confirm(`${enabled ? 'Reactivate' : 'Deactivate'} ${user.name}'s account?`)) return;
    setBusyUserIds((ids) => [...ids, user.id]);
    setError('');
    setNotice('');
    try {
      const { data: updatedUser } = await api.patch(`/admin/users/${user.id}`, { enabled });
      setRows((currentRows) => currentRows.map((row) => row.id === updatedUser.id ? updatedUser : row));
      setNotice(`Account ${updatedUser.enabled ? 'reactivated' : 'deactivated'}.`);
      await load();
    } catch (requestError) {
      setError(getRequestError(requestError, 'Could not change account status.'));
    } finally {
      setBusyUserIds((ids) => ids.filter((id) => id !== user.id));
    }
  }

  async function closeJob(job) {
    if (!window.confirm(`Close "${job.title}"? This removes it from public job search.`)) return;
    try {
      await api.delete(`/admin/jobs/${job.id}`);
      setNotice('Job closed.');
      await load();
    } catch (requestError) {
      setError(getRequestError(requestError, 'Could not close the job.'));
    }
  }

  async function changeApplicationStatus(applicationId, status) {
    setBusyApplicationId(applicationId);
    setError('');
    setNotice('');
    try {
      await api.put(`/applications/${applicationId}/status`, { status });
      setNotice('Application status updated.');
      await load();
    } catch (requestError) {
      setError(getRequestError(requestError, 'Could not update this application.'));
    } finally {
      setBusyApplicationId(null);
    }
  }

  if (!isKnownSection) return <Navigate to="/admin/dashboard" replace />;

  return <section className="student-page">
    <header className="student-page-header"><div><p className="eyebrow">ADMINISTRATION</p><h1>Manage {config.title.toLowerCase()}.</h1><p>Search the directory and take careful, reversible actions.</p></div></header>
    {(config.userRows || section === 'companies') && <label className="admin-search">Search {config.title.toLowerCase()}<input value={query} onChange={(event) => setQuery(event.target.value)} placeholder="Type a name, email, or company" /></label>}
    {error && <p className="form-error" role="alert">{error}</p>}
    {notice && <p className="form-success" role="status">{notice}</p>}
    <section className="student-form-card">
      {loading ? <p className="page-message" role="status">Loading {config.title.toLowerCase()}…</p> :
        rows.length === 0 ? <p className="empty-state">No records found.</p> :
          <div className="admin-table-wrap"><table className="admin-table"><thead><tr>
            {config.userRows ? <><th>Name</th><th>Email</th><th>Role</th><th>Joined</th><th>Status</th><th>Actions</th></> :
              config.applicationRows ? <><th>Job</th><th>Company</th><th>Student</th><th>Applied</th><th>Status</th><th>Update status</th></> :
              section === 'companies' ? <><th>Company</th><th>Location</th><th>Website</th></> : <><th>Job</th><th>Company</th><th>Location</th><th>Type</th><th>Deadline</th><th>Actions</th></>}
          </tr></thead><tbody>{rows.map((row) => <tr key={row.id}>
            {config.userRows ? <><td><strong>{row.name}</strong></td><td>{row.email}</td><td>{row.role}</td><td>{new Date(row.createdAt).toLocaleDateString()}</td><td>{row.enabled ? 'Active' : 'Inactive'}</td>
              <td><button type="button" disabled={busyUserIds.includes(row.id)} onClick={() => toggleUser(row)}>{busyUserIds.includes(row.id) ? 'Updating…' : row.enabled ? 'Deactivate' : 'Reactivate'}</button></td></> :
              config.applicationRows ? <><td><strong>{row.jobTitle}</strong></td><td>{row.company || '—'}</td>
                <td>{row.studentName}<small className="admin-secondary-cell">{row.studentEmail}</small></td>
                <td>{new Date(row.appliedAt).toLocaleDateString()}</td><td>{row.status}</td>
                <td><select aria-label={`Update ${row.studentName}'s application for ${row.jobTitle}`}
                  value={row.status} disabled={busyApplicationId === row.id}
                  onChange={(event) => changeApplicationStatus(row.id, event.target.value)}>
                  {applicationStatuses.map((status) => <option key={status} value={status}>{status.replace('_', ' ')}</option>)}
                </select></td></> :
              section === 'companies' ? <><td><strong>{row.name}</strong></td><td>{row.location || '—'}</td><td>{row.website || '—'}</td></> :
                <><td><strong>{row.title}</strong></td><td>{row.company || '—'}</td><td>{row.location}</td><td>{row.employmentType}</td><td>{row.deadline || '—'}</td>
                  <td>{row.active ? <button className="danger-text" type="button" onClick={() => closeJob(row)}>Close job</button> : <span>Closed</span>}</td></>}
          </tr>)}</tbody></table></div>}
    </section>
  </section>;
}
