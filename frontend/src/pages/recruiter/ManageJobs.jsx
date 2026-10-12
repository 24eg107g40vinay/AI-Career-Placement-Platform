import { useEffect, useState } from 'react';
import { Link, useLocation } from 'react-router-dom';
import { getRequestError } from '../../services/studentService';
import { deactivateJob, getApplicants, getMyJobs, saveJob, updateApplicantStatus } from '../../services/recruiterService';

const initialJob = { title: '', description: '', location: '', employmentType: 'FULL_TIME', salary: '', experience: '0', deadline: '', skills: '' };
const applicationStatuses = ['SHORTLISTED', 'INTERVIEW', 'SELECTED', 'REJECTED'];

export function ManageJobs() {
  const location = useLocation();
  const [jobs, setJobs] = useState([]);
  const [selectedJob, setSelectedJob] = useState(null);
  const [applicants, setApplicants] = useState([]);
  const [form, setForm] = useState(initialJob);
  const [editingId, setEditingId] = useState(null);
  const [loading, setLoading] = useState(true);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState('');
  const [notice, setNotice] = useState('');

  async function refreshJobs() {
    setJobs(await getMyJobs());
  }

  useEffect(() => {
    refreshJobs().catch((requestError) => setError(getRequestError(requestError, 'Could not load your jobs.')))
      .finally(() => setLoading(false));
  }, []);

  useEffect(() => {
    if (!location.hash) return undefined;
    const frame = requestAnimationFrame(() => {
      document.getElementById(location.hash.slice(1))?.scrollIntoView({ behavior: 'smooth', block: 'start' });
    });
    return () => cancelAnimationFrame(frame);
  }, [location.hash]);

  async function submitJob(event) {
    event.preventDefault();
    setBusy(true);
    setError('');
    setNotice('');
    try {
      await saveJob({
        ...form,
        salary: form.salary ? Number(form.salary) : null,
        experience: Number(form.experience),
        deadline: form.deadline || null,
        skills: form.skills.split(',').map((skill) => skill.trim()).filter(Boolean),
      }, editingId);
      setForm(initialJob);
      setEditingId(null);
      await refreshJobs();
      setNotice('Job saved and published.');
    } catch (requestError) {
      setError(getRequestError(requestError, 'Could not save this job.'));
    } finally {
      setBusy(false);
    }
  }

  function editJob(job) {
    setEditingId(job.id);
    setForm({
      title: job.title,
      description: job.description,
      location: job.location,
      employmentType: job.employmentType,
      salary: job.salary ?? '',
      experience: String(job.experience),
      deadline: job.deadline || '',
      skills: job.skills.join(', '),
    });
  }

  async function removeJob(job) {
    if (!window.confirm(`Close "${job.title}"? Candidates will no longer see it.`)) return;
    setError('');
    try {
      await deactivateJob(job.id);
      await refreshJobs();
      setNotice('Job closed.');
    } catch (requestError) {
      setError(getRequestError(requestError, 'Could not close this job.'));
    }
  }

  async function showApplicants(job) {
    setSelectedJob(job);
    setError('');
    try {
      setApplicants(await getApplicants(job.id));
    } catch (requestError) {
      setApplicants([]);
      setError(getRequestError(requestError, 'Could not load applicants.'));
    }
  }

  async function changeStatus(applicationId, status) {
    setError('');
    try {
      await updateApplicantStatus(applicationId, status);
      if (selectedJob) await showApplicants(selectedJob);
      setNotice('Application status updated.');
    } catch (requestError) {
      setError(getRequestError(requestError, 'Could not update this application.'));
    }
  }

  return <section className="student-page">
    <header className="student-page-header"><div><p className="eyebrow">JOB MANAGEMENT</p><h1>Your open opportunities.</h1><p>Create roles, update details, and review applicants.</p></div></header>
    {error && <p className="form-error" role="alert">{error}</p>}
    {notice && <p className="form-success" role="status">{notice}</p>}
    <form id="publish-job" className="student-form-card recruiter-job-form" onSubmit={submitJob}>
      <h2>{editingId ? 'Edit job' : 'Publish a job'}</h2>
      <div className="student-form-grid">
        <label>Job title<input required maxLength="180" value={form.title} onChange={(e) => setForm({ ...form, title: e.target.value })} /></label>
        <label>Location<input required maxLength="160" value={form.location} onChange={(e) => setForm({ ...form, location: e.target.value })} /></label>
        <label>Employment type<select value={form.employmentType} onChange={(e) => setForm({ ...form, employmentType: e.target.value })}>
          {['FULL_TIME', 'PART_TIME', 'CONTRACT', 'INTERNSHIP', 'TEMPORARY'].map((value) => <option key={value} value={value}>{value.replace('_', ' ')}</option>)}
        </select></label>
        <label>Salary (optional)<input type="number" min="0" step="1" value={form.salary} onChange={(e) => setForm({ ...form, salary: e.target.value })} /></label>
        <label>Experience (years)<input required type="number" min="0" value={form.experience} onChange={(e) => setForm({ ...form, experience: e.target.value })} /></label>
        <label>Application deadline<input type="date" value={form.deadline} onChange={(e) => setForm({ ...form, deadline: e.target.value })} /></label>
        <label className="form-full-width">Description<textarea required rows="5" maxLength="10000" value={form.description} onChange={(e) => setForm({ ...form, description: e.target.value })} /></label>
        <label className="form-full-width">Required skills (comma separated)<input maxLength="1000" value={form.skills} onChange={(e) => setForm({ ...form, skills: e.target.value })} placeholder="Java, Spring Boot, MySQL" /></label>
      </div>
      <button className="primary-button" disabled={busy}>{busy ? 'Saving…' : editingId ? 'Update job' : 'Publish job'}</button>
      {editingId && <button className="quiet-button" type="button" onClick={() => { setEditingId(null); setForm(initialJob); }}>Cancel edit</button>}
    </form>
    <section id="my-jobs" className="student-form-card recruiter-jobs-list"><h2>My jobs and applicants</h2>
      {loading ? <p className="page-message" role="status">Loading your jobs…</p> : jobs.length === 0 ?
        <p className="empty-state">You have not published any jobs yet.</p> :
        <div className="admin-table-wrap"><table className="admin-table"><thead><tr><th>Role</th><th>Location</th><th>Skills</th><th>Deadline</th><th>Actions</th></tr></thead>
          <tbody>{jobs.map((job) => <tr key={job.id}><td><strong>{job.title}</strong></td><td>{job.location}</td><td>{job.skills.join(', ') || '—'}</td><td>{job.deadline || '—'}</td>
            <td className="table-actions"><button type="button" onClick={() => editJob(job)}>Edit</button><button type="button" onClick={() => showApplicants(job)}>Applicants</button><button type="button" className="danger-text" onClick={() => removeJob(job)}>Close</button></td>
          </tr>)}</tbody></table></div>}
    </section>
    {selectedJob && <section className="student-form-card"><div className="section-title-row"><h2>Applicants · {selectedJob.title}</h2><button className="quiet-button" onClick={() => setSelectedJob(null)}>Close</button></div>
      {applicants.length === 0 ? <p className="empty-state">No applications received yet.</p> : applicants.map((applicant) => <article className="applicant-row" key={applicant.id}>
        <div><strong>{applicant.studentName}</strong><small>{applicant.studentEmail} · {applicant.studentEducation || 'Education not added'}</small><small>{applicant.studentSkills.join(', ') || 'No skills listed'}</small></div>
        <span className={`status-badge status-${applicant.status.toLowerCase()}`}>{applicant.status.toLowerCase()}</span>
        <div className="applicant-actions">{applicationStatuses.map((status) => <button type="button" key={status} onClick={() => changeStatus(applicant.id, status)}>{status}</button>)}</div>
      </article>)}
    </section>}
    <p className="student-page-footnote"><Link to="/recruiter/company">Manage your company profile →</Link></p>
  </section>;
}
