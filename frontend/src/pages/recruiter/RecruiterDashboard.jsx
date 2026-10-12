import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { ArrowRight, BriefcaseBusiness, Building2, CalendarDays, ClipboardList, Plus, UserCheck, Users } from 'lucide-react';
import { getRequestError } from '../../services/studentService';
import { getRecruiterDashboard } from '../../services/recruiterService';

const metrics = [
  ['activeJobs', 'Active jobs', BriefcaseBusiness],
  ['totalApplicants', 'Total applicants', Users],
  ['shortlisted', 'Shortlisted', UserCheck],
  ['interviews', 'Interviews', CalendarDays],
];

export function RecruiterDashboard() {
  const [data, setData] = useState(null);
  const [error, setError] = useState('');

  useEffect(() => {
    getRecruiterDashboard().then(setData)
      .catch((requestError) => setError(getRequestError(requestError, 'Could not load recruiter dashboard.')));
  }, []);

  return (
    <section className="student-page">
      <header className="student-page-header"><div><p className="eyebrow">RECRUITER WORKSPACE</p>
        <h1>Build your next great team.</h1><p>Manage roles and discover candidates who fit.</p></div>
        <Link className="primary-button" to="/recruiter/jobs">Manage jobs</Link></header>
      {error && <p className="form-error" role="alert">{error}</p>}
      {!data && !error && <p className="page-message" role="status">Loading recruiter overview…</p>}
      {data && <div className="admin-metric-grid">{metrics.map(([key, label, Icon]) => (
        <article className="admin-metric-card" key={key}><span><Icon size={18} /></span><strong>{data[key]}</strong><small>{label}</small></article>
      ))}</div>}
      <section className="student-form-card recruiter-welcome">
        <h2>Recruiter tools</h2>
        <div className="recruiter-tool-grid">
          <Link className="recruiter-tool-link" to="/recruiter/jobs#publish-job">
            <span className="recruiter-tool-icon"><Plus size={18} /></span>
            <span><strong>Publish a job</strong><small>Create a new opportunity or edit an existing listing.</small></span>
            <ArrowRight size={17} />
          </Link>
          <Link className="recruiter-tool-link" to="/recruiter/jobs#my-jobs">
            <span className="recruiter-tool-icon"><ClipboardList size={18} /></span>
            <span><strong>Review applicants</strong><small>Choose a job to review candidates and update application status.</small></span>
            <ArrowRight size={17} />
          </Link>
          <Link className="recruiter-tool-link" to="/recruiter/company">
            <span className="recruiter-tool-icon"><Building2 size={18} /></span>
            <span><strong>Company profile</strong><small>Keep your organization details up to date.</small></span>
            <ArrowRight size={17} />
          </Link>
        </div>
      </section>
    </section>
  );
}
