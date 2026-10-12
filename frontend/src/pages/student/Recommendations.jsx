import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { getRecommendations, getRequestError } from '../../services/studentService';

export function Recommendations() {
  const [jobs, setJobs] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    getRecommendations().then(setJobs)
      .catch((requestError) => setError(getRequestError(requestError, 'Could not load recommendations.')))
      .finally(() => setLoading(false));
  }, []);

  return (
    <section className="student-page">
      <header className="student-page-header"><div><p className="eyebrow">PERSONALIZED MATCHES</p><h1>Roles that fit your skills.</h1>
        <p>Each match explains the skills you already have and what you could learn next.</p></div></header>
      {loading && <p className="page-message" role="status">Finding your best matches…</p>}
      {error && <p className="form-error" role="alert">{error}</p>}
      {!loading && !error && jobs.length === 0 && <p className="empty-state">No open roles are available right now.</p>}
      <div className="recommendation-list">{jobs.map(({ job, matchPercentage, matchedSkills, missingSkills }) => (
        <article className="recommendation-card" key={job.id}>
          <div className="recommendation-score"><strong>{matchPercentage}%</strong><span>skill match</span></div>
          <div className="recommendation-content"><p className="eyebrow">{job.company || 'Company'}</p><h2>{job.title}</h2>
            <p>{job.location} · {job.employmentType.toLowerCase().replace('_', ' ')}</p>
            <div className="match-explanation"><span>Matched: {matchedSkills.length ? matchedSkills.join(', ') : 'Add skills to improve this match'}</span>
              {missingSkills.length > 0 && <span>To build: {missingSkills.join(', ')}</span>}</div>
          </div>
          <Link className="primary-button" to={`/jobs/${job.id}`}>View job</Link>
        </article>
      ))}</div>
    </section>
  );
}
