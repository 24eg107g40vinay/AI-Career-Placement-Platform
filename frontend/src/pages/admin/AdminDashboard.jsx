import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { ArrowRight, BriefcaseBusiness, Building2, FileCheck2, Users } from 'lucide-react';
import { api } from '../../services/api';
import { getRequestError } from '../../services/studentService';

const measures = [
  ['students', 'Students', Users],
  ['recruiters', 'Recruiters', Users],
  ['companies', 'Companies', Building2],
  ['jobs', 'Open jobs', BriefcaseBusiness],
  ['applications', 'Applications', FileCheck2],
];

const adminTools = [
  ['students', 'Manage students', 'Search student accounts and change access status.', Users],
  ['recruiters', 'Manage recruiters', 'Review recruiter accounts and change access status.', Users],
  ['companies', 'Review companies', 'Browse company profiles registered on the platform.', Building2],
  ['jobs', 'Moderate jobs', 'Review job listings and close inappropriate or outdated posts.', BriefcaseBusiness],
  ['applications', 'Manage applications', 'Review applications and update their hiring status.', FileCheck2],
];

export function AdminDashboard() {
  const [data, setData] = useState(null);
  const [error, setError] = useState('');

  useEffect(() => {
    api.get('/admin/dashboard').then(({ data: result }) => setData(result))
      .catch((requestError) => setError(getRequestError(requestError, 'Could not load administrator analytics.')));
  }, []);

  return <section className="student-page">
    <header className="student-page-header"><div><p className="eyebrow">PLATFORM OVERVIEW</p><h1>CareerAI administration.</h1><p>Review platform activity and manage access responsibly.</p></div></header>
    {error && <p className="form-error" role="alert">{error}</p>}
    {!data && !error && <p className="page-message" role="status">Loading platform analytics…</p>}
    {data && <div className="admin-metric-grid">{measures.map(([key, label, Icon]) => (
      <article className="admin-metric-card" key={key}><span><Icon size={18} /></span><strong>{data[key]}</strong><small>{label}</small></article>
    ))}</div>}
    <section className="student-form-card recruiter-welcome"><h2>Administration tools</h2><div className="recruiter-tool-grid">
      {adminTools.map(([section, title, description, Icon]) => (
        <Link className="recruiter-tool-link" to={`/admin/${section}`} key={section}>
          <span className="recruiter-tool-icon"><Icon size={18} /></span>
          <span><strong>{title}</strong><small>{description}</small></span>
          <ArrowRight size={17} />
        </Link>
      ))}
    </div></section>
  </section>;
}
