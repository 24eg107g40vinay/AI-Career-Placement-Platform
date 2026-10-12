import { useEffect, useState } from 'react';
import { ArrowUpRight, BriefcaseBusiness, MapPin, Search } from 'lucide-react';
import { Link, useParams, useSearchParams } from 'react-router-dom';
import { api } from '../../services/api';
import { getApiErrorMessage } from '../../services/authService';
import { useAuth } from '../../context/AuthContext';
import { applyForJob, getApplications, getRequestError } from '../../services/studentService';

const employmentTypes = ['FULL_TIME', 'PART_TIME', 'CONTRACT', 'INTERNSHIP', 'TEMPORARY'];
const pageSize = 12;

function displayType(value) {
  return value.toLowerCase().replace('_', ' ').replace(/\b\w/g, (letter) => letter.toUpperCase());
}

function displaySalary(value) {
  return new Intl.NumberFormat('en-IN', {
    style: 'currency',
    currency: 'INR',
    maximumFractionDigits: 0,
  }).format(value);
}

export function Jobs() {
  const { user } = useAuth();
  const [searchParams, setSearchParams] = useSearchParams();
  const [jobs, setJobs] = useState([]);
  const [filters, setFilters] = useState({
    keyword: searchParams.get('keyword') || '',
    location: searchParams.get('location') || '',
    company: searchParams.get('company') || '',
    skill: searchParams.get('skill') || '',
    employmentType: searchParams.get('employmentType') || '',
    minExperience: searchParams.get('minExperience') || '',
    maxExperience: searchParams.get('maxExperience') || '',
    minSalary: searchParams.get('minSalary') || '',
    maxSalary: searchParams.get('maxSalary') || '',
    postedWithinDays: searchParams.get('postedWithinDays') || '',
  });
  const [activeFilters, setActiveFilters] = useState(filters);
  const [sortBy, setSortBy] = useState(searchParams.get('sort') || 'RELEVANCE');
  const [page, setPage] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [appliedJobIds, setAppliedJobIds] = useState(() => new Set());
  const [checkingApplications, setCheckingApplications] = useState(false);
  const [applicationStatusError, setApplicationStatusError] = useState('');
  const [applyingJobId, setApplyingJobId] = useState(null);
  const [applyErrors, setApplyErrors] = useState({});

  useEffect(() => {
    let active = true;
    setLoading(true);
    setError('');
    api.get('/jobs/search', {
      params: {
        ...Object.fromEntries(Object.entries(activeFilters).filter(([, value]) => value !== '')),
        page,
        size: pageSize,
        sort: sortBy,
      },
    })
      .then(({ data }) => {
        if (active) {
          setJobs(data.content);
          setTotalElements(data.totalElements);
          setTotalPages(data.totalPages);
        }
      })
      .catch((requestError) => {
        if (active) {
          setJobs([]);
          setTotalElements(0);
          setTotalPages(0);
          setError(getApiErrorMessage(requestError, 'Could not load jobs. Please try again.'));
        }
      })
      .finally(() => {
        if (active) setLoading(false);
      });
    return () => {
      active = false;
    };
  }, [activeFilters, page, sortBy]);

  useEffect(() => {
    if (user?.role !== 'STUDENT') {
      setAppliedJobIds(new Set());
      setApplicationStatusError('');
      return undefined;
    }

    let active = true;
    setCheckingApplications(true);
    setApplicationStatusError('');
    getApplications()
      .then((applications) => {
        if (active) setAppliedJobIds(new Set(applications.map((application) => Number(application.jobId))));
      })
      .catch((requestError) => {
        if (active) setApplicationStatusError(getRequestError(requestError, 'Could not check your existing applications.'));
      })
      .finally(() => {
        if (active) setCheckingApplications(false);
      });
    return () => {
      active = false;
    };
  }, [user]);

  function handleSubmit(event) {
    event.preventDefault();
    const nextParams = {
      ...Object.fromEntries(Object.entries(filters).filter(([, value]) => value !== '')),
      sort: sortBy,
    };
    setSearchParams(nextParams);
    setActiveFilters({ ...filters });
    setPage(0);
  }

  function updateFilter(event) {
    setFilters((current) => ({ ...current, [event.target.name]: event.target.value }));
  }

  function clearFilters() {
    const clearedFilters = {
      keyword: '',
      location: '',
      company: '',
      skill: '',
      employmentType: '',
      minExperience: '',
      maxExperience: '',
      minSalary: '',
      maxSalary: '',
      postedWithinDays: '',
    };
    setFilters(clearedFilters);
    setActiveFilters(clearedFilters);
    setSortBy('RELEVANCE');
    setPage(0);
    setSearchParams({});
  }

  function updateSort(event) {
    const nextSort = event.target.value;
    setSortBy(nextSort);
    setPage(0);
    setSearchParams({
      ...Object.fromEntries(Object.entries(activeFilters).filter(([, value]) => value !== '')),
      sort: nextSort,
    });
  }

  async function applyFromCard(jobId) {
    setApplyingJobId(jobId);
    setApplyErrors((current) => ({ ...current, [jobId]: '' }));
    try {
      await applyForJob(jobId);
      setAppliedJobIds((current) => new Set(current).add(Number(jobId)));
    } catch (requestError) {
      if (requestError.response?.status === 409) {
        setAppliedJobIds((current) => new Set(current).add(Number(jobId)));
      } else {
        setApplyErrors((current) => ({
          ...current,
          [jobId]: getRequestError(requestError, 'Could not submit your application.'),
        }));
      }
    } finally {
      setApplyingJobId(null);
    }
  }

  return (
    <main className="jobs-page">
      <header className="public-nav">
        <Link className="public-brand" to="/">Northstar</Link>
        <nav aria-label="Main navigation">
          <Link to="/">Home</Link>
          <Link className="public-nav-cta" to="/register">Get started</Link>
        </nav>
      </header>
      <section className="jobs-heading">
        <p className="eyebrow">OPPORTUNITIES</p>
        <h1>Find work that fits your next step.</h1>
        <p>Explore roles and narrow your search by what matters to you.</p>
      </section>
      <form className="job-filters" onSubmit={handleSubmit}>
        <label>
          <span>Role or keyword</span>
          <input name="keyword" value={filters.keyword} onChange={updateFilter} placeholder="e.g. Java developer" />
        </label>
        <label>
          <span>Location</span>
          <input name="location" value={filters.location} onChange={updateFilter} placeholder="City or remote" />
        </label>
        <label>
          <span>Company</span>
          <input name="company" value={filters.company} onChange={updateFilter} placeholder="Company name" />
        </label>
        <label>
          <span>Skill</span>
          <input name="skill" value={filters.skill} onChange={updateFilter} placeholder="e.g. React" />
        </label>
        <label>
          <span>Employment</span>
          <select name="employmentType" value={filters.employmentType} onChange={updateFilter}>
            <option value="">All types</option>
            {employmentTypes.map((type) => <option key={type} value={type}>{displayType(type)}</option>)}
          </select>
        </label>
        <label>
          <span>Minimum experience</span>
          <select name="minExperience" value={filters.minExperience} onChange={updateFilter}>
            <option value="">Any</option>
            {[0, 1, 2, 3, 5, 8, 10].map((years) => <option key={years} value={years}>{years}+ years</option>)}
          </select>
        </label>
        <label>
          <span>Maximum experience</span>
          <select name="maxExperience" value={filters.maxExperience} onChange={updateFilter}>
            <option value="">Any</option>
            {[1, 2, 3, 5, 8, 10].map((years) => <option key={years} value={years}>Up to {years} years</option>)}
          </select>
        </label>
        <label>
          <span>Minimum salary (INR)</span>
          <input name="minSalary" type="number" min="0" value={filters.minSalary} onChange={updateFilter} placeholder="No minimum" />
        </label>
        <label>
          <span>Maximum salary (INR)</span>
          <input name="maxSalary" type="number" min="0" value={filters.maxSalary} onChange={updateFilter} placeholder="No maximum" />
        </label>
        <label>
          <span>Posted</span>
          <select name="postedWithinDays" value={filters.postedWithinDays} onChange={updateFilter}>
            <option value="">Any time</option>
            <option value="1">Past 24 hours</option>
            <option value="7">Past week</option>
            <option value="30">Past month</option>
          </select>
        </label>
        <div className="job-filter-actions">
          <button className="primary-button job-search-button" type="submit"><Search size={16} /> Apply filters</button>
          <button className="secondary-button" type="button" onClick={clearFilters}>Clear filters</button>
        </div>
      </form>
      <section className="job-results" aria-live="polite">
        <div className="job-results-heading">
          <div>
            <h2>Open opportunities</h2>
            {!loading && !error && <span>{totalElements} {totalElements === 1 ? 'job' : 'jobs'} found</span>}
          </div>
          <label className="job-sort">
            <span>Sort by</span>
            <select value={sortBy} onChange={updateSort}>
              <option value="RELEVANCE">Relevance</option>
              <option value="NEWEST">Newest</option>
              <option value="SALARY_HIGH">Salary: high to low</option>
              <option value="SALARY_LOW">Salary: low to high</option>
            </select>
          </label>
        </div>
        {loading && <p className="jobs-message" role="status">Loading opportunities…</p>}
        {error && <p className="jobs-message jobs-error" role="alert">{error}</p>}
        {applicationStatusError && <p className="jobs-message jobs-error" role="alert">{applicationStatusError} The server will still prevent duplicate applications.</p>}
        {!loading && !error && jobs.length === 0 && (
          <div className="jobs-message">
            <p>No jobs match your current search.</p>
            <button className="secondary-button" type="button" onClick={clearFilters}>Clear filters</button>
          </div>
        )}
        {!loading && !error && jobs.length > 0 && (
          <div className="job-card-grid">
            {jobs.map((job) => (
              <article className="job-card" key={job.id}>
                <div className="job-card-icon"><BriefcaseBusiness size={19} aria-hidden="true" /></div>
                <span className="job-type">{displayType(job.employmentType)}</span>
                <h3>{job.title}</h3>
                <p className="job-company">{job.company || 'Company profile not provided'}</p>
                <p className="job-location"><MapPin size={14} aria-hidden="true" />{job.location}</p>
                <p className="job-card-facts">
                  {job.salary != null && <span>{displaySalary(job.salary)}</span>}
                  <span>{job.experience} {job.experience === 1 ? 'year' : 'years'} experience</span>
                </p>
                <div className="job-tags">
                  {job.skills.slice(0, 4).map((skill) => <span key={skill}>{skill}</span>)}
                  {job.skills.length > 4 && <span>+{job.skills.length - 4}</span>}
                </div>
                <p className="job-posted">Posted {new Date(job.createdAt).toLocaleDateString()}</p>
                <div className="job-card-actions">
                  <Link className="job-card-link" to={`/jobs/${job.id}`}>View details <ArrowUpRight size={15} /></Link>
                  {user?.role === 'STUDENT' ? appliedJobIds.has(Number(job.id)) ? (
                    <span className="job-applied-label" role="status">Applied</span>
                  ) : (
                    <button className="secondary-button job-card-apply" type="button"
                      disabled={checkingApplications || applyingJobId === job.id}
                      onClick={() => applyFromCard(job.id)}>
                      {applyingJobId === job.id ? 'Applying…' : 'Apply'}
                    </button>
                  ) : user ? (
                    <span className="job-applied-label">Student account required</span>
                  ) : (
                    <Link className="secondary-button job-card-apply" to="/login">Sign in to apply</Link>
                  )}
                </div>
                {applyErrors[job.id] && <p className="form-error" role="alert">{applyErrors[job.id]}</p>}
              </article>
            ))}
          </div>
        )}
        {!loading && !error && totalPages > 1 && (
          <nav className="job-pagination" aria-label="Job search pages">
            <button className="secondary-button" type="button" disabled={page === 0} onClick={() => setPage((current) => current - 1)}>
              Previous
            </button>
            <span>Page {page + 1} of {totalPages}</span>
            <button className="secondary-button" type="button" disabled={page + 1 >= totalPages} onClick={() => setPage((current) => current + 1)}>
              Next
            </button>
          </nav>
        )}
      </section>
    </main>
  );
}

export function JobDetails() {
  const { id } = useParams();
  const { user } = useAuth();
  const [applied, setApplied] = useState(false);
  const [coverMessage, setCoverMessage] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [applicationError, setApplicationError] = useState('');
  const [applicationNotice, setApplicationNotice] = useState('');
  const [job, setJob] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    let active = true;
    setLoading(true);
    setError('');
    api.get(`/jobs/${id}`)
      .then(({ data }) => {
        if (active) setJob(data);
      })
      .catch((requestError) => {
        if (active) setError(getApiErrorMessage(requestError, 'Could not load this job.'));
      })
      .finally(() => {
        if (active) setLoading(false);
      });
    return () => {
      active = false;
    };
  }, [id]);

  useEffect(() => {
    if (user?.role !== 'STUDENT') return undefined;
    let active = true;
    getApplications().then((applications) => {
      if (active) setApplied(applications.some((application) => String(application.jobId) === String(id)));
    }).catch((requestError) => {
      if (active) setApplicationError(getRequestError(requestError, 'Could not check your application status.'));
    });
    return () => { active = false; };
  }, [id, user]);

  async function submitApplication(event) {
    event.preventDefault();
    setSubmitting(true);
    setApplicationError('');
    setApplicationNotice('');
    try {
      await applyForJob(id, coverMessage);
      setApplied(true);
      setApplicationNotice('Application submitted successfully.');
    } catch (requestError) {
      setApplicationError(getRequestError(requestError, 'Could not submit your application.'));
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <main className="jobs-page">
      <header className="public-nav">
        <Link className="public-brand" to="/">Northstar</Link>
        <nav aria-label="Main navigation">
          <Link to="/jobs">All jobs</Link>
          <Link className="public-nav-cta" to="/register">Get started</Link>
        </nav>
      </header>
      <section className="job-detail">
        {loading && <p className="jobs-message" role="status">Loading job details…</p>}
        {error && <p className="jobs-message jobs-error" role="alert">{error}</p>}
        {!loading && !error && job && (
          <>
            <Link className="job-back-link" to="/jobs">← Back to jobs</Link>
            <span className="job-type">{displayType(job.employmentType)}</span>
            <h1>{job.title}</h1>
            <p className="job-detail-company">{job.company || 'Company profile not provided'} · {job.location}</p>
            <div className="job-detail-facts">
              <span>{job.experience} years experience</span>
              {job.salary != null && <span>{new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR', maximumFractionDigits: 0 }).format(job.salary)}</span>}
              {job.deadline && <span>Apply by {job.deadline}</span>}
            </div>
            <h2>About the role</h2>
            <p className="job-detail-description">{job.description}</p>
            <h2>Skills</h2>
            {job.skills.length ? (
              <div className="job-tags">{job.skills.map((skill) => <span key={skill}>{skill}</span>)}</div>
            ) : <p className="job-detail-company">No required skills listed.</p>}
            <div className="job-apply-panel">
              {user?.role === 'STUDENT' ? applied ? (
                <p className="form-success" role="status">Application submitted. <Link to="/student/applications">Track it in My Applications.</Link></p>
              ) : (
                <form onSubmit={submitApplication}>
                  <label htmlFor="cover-message">Optional message to the recruiter</label>
                  <textarea id="cover-message" maxLength="3000" rows="3" value={coverMessage}
                    onChange={(event) => setCoverMessage(event.target.value)} placeholder="Briefly introduce yourself." />
                  {applicationError && <p className="form-error" role="alert">{applicationError}</p>}
                  {applicationNotice && <p className="form-success" role="status">{applicationNotice}</p>}
                  <button className="primary-button" type="submit" disabled={submitting}>{submitting ? 'Submitting…' : 'Apply now'}</button>
                </form>
              ) : user ? <p>Sign in with a student account to apply for this role.</p> : <Link className="primary-button" to="/login">Sign in to apply</Link>}
            </div>
          </>
        )}
      </section>
    </main>
  );
}
