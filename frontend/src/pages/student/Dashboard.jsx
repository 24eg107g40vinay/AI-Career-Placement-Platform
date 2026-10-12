import { useEffect, useState } from 'react';
import { ArrowRight, BriefcaseBusiness, GraduationCap, Sparkles } from 'lucide-react';
import { Link } from 'react-router-dom';
import { getApplications, getRecommendations, getRequestError, getResumes, getStudentProfile } from '../../services/studentService';

function profileCompletion(profile, resumes) {
  const completed = [
    Boolean(profile.name),
    Boolean(profile.email),
    Boolean(profile.education),
    Boolean(profile.college),
    Boolean(profile.graduationYear),
    Boolean(profile.location),
    Boolean(profile.skills?.length),
    Boolean(resumes.length),
    profile.experienceYears != null,
  ].filter(Boolean).length;
  return Math.round((completed / 9) * 100);
}

export function Dashboard() {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    let active = true;
    Promise.all([getStudentProfile(), getApplications(), getRecommendations(), getResumes()])
      .then(([profile, applications, recommendations, resumes]) => {
        if (active) setData({ profile, applications, recommendations, resumes });
      })
      .catch((requestError) => {
        if (active) setError(getRequestError(requestError, 'Could not load your dashboard. Please try again.'));
      })
      .finally(() => {
        if (active) setLoading(false);
      });
    return () => {
      active = false;
    };
  }, []);

  if (loading) {
    return <section className="student-page"><p className="page-message" role="status">Loading your career dashboard…</p></section>;
  }

  if (error) {
    return <section className="student-page"><p className="form-error" role="alert">{error}</p></section>;
  }

  const { profile, applications, recommendations, resumes } = data;
  const completion = profileCompletion(profile, resumes);
  const shortlisted = applications.filter((application) => application.status === 'SHORTLISTED').length;
  const interviews = applications.filter((application) => application.status === 'INTERVIEW').length;
  const recentApplications = [...applications].sort((first, second) => (
    new Date(second.appliedAt).getTime() - new Date(first.appliedAt).getTime()
  )).slice(0, 5);

  return (
    <section className="student-page">
      <header className="student-page-header">
        <div>
          <p className="eyebrow">YOUR CAREER WORKSPACE</p>
          <h1>Welcome, {profile.name.split(' ')[0]}.</h1>
          <p>Track applications, strengthen your profile, and find your next opportunity.</p>
        </div>
        <Link className="primary-button" to="/jobs">Find jobs <ArrowRight size={15} /></Link>
      </header>

      <div className="student-dashboard-metrics">
        <article className="student-dashboard-metric">
          <span>Profile completion</span>
          <strong>{completion}%</strong>
          <div className="dashboard-progress" role="progressbar" aria-label="Profile completion" aria-valuenow={completion} aria-valuemin="0" aria-valuemax="100">
            <i style={{ width: `${completion}%` }} />
          </div>
          <Link to="/student/profile">Complete your profile</Link>
        </article>
        <article className="student-dashboard-metric"><span>Applications</span><strong>{applications.length}</strong><Link to="/student/applications">View applications</Link></article>
        <article className="student-dashboard-metric"><span>Shortlisted</span><strong>{shortlisted}</strong><Link to="/student/applications">Track progress</Link></article>
        <article className="student-dashboard-metric"><span>Interviews</span><strong>{interviews}</strong><Link to="/student/applications">View status</Link></article>
      </div>

      <div className="student-dashboard-columns">
        <section className="student-dashboard-panel">
          <header><div><p className="eyebrow">BASED ON YOUR SKILLS</p><h2>Recommended jobs</h2></div><Link to="/student/recommended">All matches <ArrowRight size={14} /></Link></header>
          {recommendations.length === 0 ? (
            <p className="empty-state">No open jobs are available yet. Check back soon.</p>
          ) : (
            <div className="dashboard-job-list">
              {recommendations.slice(0, 3).map(({ job, matchPercentage, matchedSkills, missingSkills }) => (
                <article className="dashboard-job" key={job.id}>
                  <span className="dashboard-job-icon"><BriefcaseBusiness size={17} /></span>
                  <div className="dashboard-job-content">
                    <h3>{job.title}</h3>
                    <p>{job.company || 'Company not listed'} · {job.location}</p>
                    <small>{matchPercentage}% skill match · {matchedSkills.length} matched · {missingSkills.length} to learn</small>
                  </div>
                  <Link to={`/jobs/${job.id}`} aria-label={`View ${job.title}`}>View <ArrowRight size={14} /></Link>
                </article>
              ))}
            </div>
          )}
        </section>

        <section className="student-dashboard-panel">
          <header><div><p className="eyebrow">YOUR SKILLS</p><h2>Profile strengths</h2></div><Link to="/student/skills">Manage <ArrowRight size={14} /></Link></header>
          {profile.skills.length === 0 ? (
            <p className="empty-state">Add skills to your profile to get relevant job matches. <Link to="/student/skills">Add skills</Link></p>
          ) : (
            <div className="dashboard-skill-list">{profile.skills.map((skill) => <span key={skill}>{skill}</span>)}</div>
          )}
          <div className="dashboard-assistant-link">
            <Sparkles size={17} />
            <span>Need a learning plan or interview practice?</span>
            <Link to="/student/assistant">Ask the assistant</Link>
          </div>
        </section>
      </div>

      <section className="student-dashboard-panel student-dashboard-applications">
        <header><div><p className="eyebrow">APPLICATION ACTIVITY</p><h2>Recent applications</h2></div><Link to="/student/applications">All applications <ArrowRight size={14} /></Link></header>
        {recentApplications.length === 0 ? (
          <p className="empty-state">You have not applied to any jobs yet. <Link to="/jobs">Explore open roles</Link></p>
        ) : (
          <div className="dashboard-application-list">
            {recentApplications.map((application) => (
              <article key={application.id}>
                <div className="dashboard-application-icon"><GraduationCap size={16} /></div>
                <div><strong>{application.jobTitle}</strong><span>{application.company || 'Company not listed'}</span></div>
                <span className={`status-badge status-${application.status.toLowerCase()}`}>{application.status.replace('_', ' ')}</span>
                <time dateTime={application.appliedAt}>{new Date(application.appliedAt).toLocaleDateString()}</time>
              </article>
            ))}
          </div>
        )}
      </section>
    </section>
  );
}
