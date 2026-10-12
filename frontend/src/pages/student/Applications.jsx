import { useEffect, useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import { getApplications, getRequestError } from '../../services/studentService';

const statuses = ['ALL', 'APPLIED', 'SHORTLISTED', 'INTERVIEW', 'SELECTED', 'REJECTED'];

export function Applications() {
  const [applications, setApplications] = useState([]);
  const [status, setStatus] = useState('ALL');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    getApplications().then(setApplications)
      .catch((requestError) => setError(getRequestError(requestError, 'Could not load your applications.')))
      .finally(() => setLoading(false));
  }, []);

  const visible = useMemo(
    () => status === 'ALL' ? applications : applications.filter((application) => application.status === status),
    [applications, status],
  );

  return (
    <section className="student-page">
      <header className="student-page-header"><div><p className="eyebrow">APPLICATION TRACKER</p><h1>Keep every opportunity in view.</h1>
        <p>Follow the progress of roles you have applied to.</p></div><div className="completion-card"><strong>{applications.length}</strong><span>applications</span></div></header>
      <div className="status-tabs" role="tablist" aria-label="Filter applications">
        {statuses.map((item) => <button type="button" role="tab" aria-selected={status === item} className={status === item ? 'selected' : ''}
          key={item} onClick={() => setStatus(item)}>{item === 'ALL' ? 'All' : item.toLowerCase()}</button>)}
      </div>
      {error && <p className="form-error" role="alert">{error}</p>}
      {loading && <p className="page-message" role="status">Loading applications…</p>}
      {!loading && !error && visible.length === 0 && <p className="empty-state">No {status === 'ALL' ? '' : `${status.toLowerCase()} `}applications here yet. <Link to="/jobs">Explore open roles</Link>.</p>}
      {!loading && visible.length > 0 && <div className="application-list">
        {visible.map((application) => <article className="application-card" key={application.id}>
          <div><p className="eyebrow">{application.company || 'Company'}</p><h2>{application.jobTitle}</h2>
            <p>Applied {new Date(application.appliedAt).toLocaleDateString()}</p></div>
          <span className={`status-badge status-${application.status.toLowerCase()}`}>{application.status.toLowerCase()}</span>
          <Link className="text-link" to={`/jobs/${application.jobId}`}>View role</Link>
        </article>)}
      </div>}
    </section>
  );
}
