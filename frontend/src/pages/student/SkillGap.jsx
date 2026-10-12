import { useEffect, useState } from 'react';
import { api } from '../../services/api';
import { getRequestError, getSkillGap } from '../../services/studentService';

export function SkillGap() {
  const [jobs, setJobs] = useState([]);
  const [jobId, setJobId] = useState('');
  const [analysis, setAnalysis] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    api.get('/jobs').then(({ data }) => {
      setJobs(data);
      if (data.length) setJobId(String(data[0].id));
    }).catch((requestError) => setError(getRequestError(requestError, 'Could not load open jobs.')))
      .finally(() => setLoading(false));
  }, []);

  useEffect(() => {
    if (!jobId) {
      setAnalysis(null);
      return undefined;
    }
    let active = true;
    getSkillGap(jobId).then((result) => { if (active) setAnalysis(result); })
      .catch((requestError) => { if (active) setError(getRequestError(requestError, 'Could not analyze this role.')); });
    return () => { active = false; };
  }, [jobId]);

  return (
    <section className="student-page">
      <header className="student-page-header"><div><p className="eyebrow">SKILL GAP ANALYSIS</p><h1>See what to learn next.</h1>
        <p>Compare your skills with real job requirements and focus your preparation.</p></div></header>
      <label className="job-select-label">Choose a role<select value={jobId} onChange={(event) => { setJobId(event.target.value); setError(''); }}>
        <option value="">Select a job</option>{jobs.map((job) => <option key={job.id} value={job.id}>{job.title} · {job.company || job.location}</option>)}
      </select></label>
      {loading && <p className="page-message" role="status">Loading roles…</p>}
      {error && <p className="form-error" role="alert">{error}</p>}
      {!loading && jobs.length === 0 && <p className="empty-state">No roles available to analyze yet.</p>}
      {analysis && <article className="gap-card">
        <div className="gap-summary"><div><p className="eyebrow">ROLE ANALYSIS</p><h2>{analysis.jobTitle}</h2></div><strong>{analysis.matchPercentage}%<small>match</small></strong></div>
        <div className="gap-meter"><i style={{ width: `${analysis.matchPercentage}%` }} /></div>
        <div className="gap-columns">
          <div><h3>Skills you have</h3>{analysis.matchedSkills.length ? analysis.matchedSkills.map((skill) => <p className="gap-skill matched" key={skill}>✓ {skill}</p>) : <p className="empty-state">Add profile skills to see matches.</p>}</div>
          <div><h3>Recommended learning areas</h3>{analysis.missingSkills.length ? analysis.missingSkills.map((skill) => <p className="gap-skill missing" key={skill}>＋ {skill}</p>) : <p className="empty-state">You match all listed skills.</p>}</div>
        </div>
      </article>}
    </section>
  );
}
