import { useEffect, useMemo, useState } from 'react';
import { getRequestError, getStudentProfile, updateStudentProfile } from '../../services/studentService';

const emptyProfile = { phone: '', education: '', college: '', graduationYear: '', bio: '', location: '', experienceYears: '' };

export function Profile() {
  const [profile, setProfile] = useState(emptyProfile);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [notice, setNotice] = useState('');

  useEffect(() => {
    getStudentProfile()
      .then((data) => setProfile({
        phone: data.phone || '',
        education: data.education || '',
        college: data.college || '',
        graduationYear: data.graduationYear || '',
        bio: data.bio || '',
        location: data.location || '',
        experienceYears: data.experienceYears ?? '',
      }))
      .catch((requestError) => setError(getRequestError(requestError, 'Could not load your profile.')))
      .finally(() => setLoading(false));
  }, []);

  const completion = useMemo(() => {
    const filled = Object.values(profile).filter((value) => String(value).trim()).length;
    return Math.round((filled / Object.keys(emptyProfile).length) * 100);
  }, [profile]);

  async function save(event) {
    event.preventDefault();
    setError('');
    setNotice('');
    setSaving(true);
    try {
      const updated = await updateStudentProfile({
        ...profile,
        graduationYear: profile.graduationYear ? Number(profile.graduationYear) : null,
        experienceYears: profile.experienceYears === '' ? null : Number(profile.experienceYears),
      });
      setProfile({
        phone: updated.phone || '',
        education: updated.education || '',
        college: updated.college || '',
        graduationYear: updated.graduationYear || '',
        bio: updated.bio || '',
        location: updated.location || '',
        experienceYears: updated.experienceYears ?? '',
      });
      setNotice('Your profile has been saved.');
    } catch (requestError) {
      setError(getRequestError(requestError, 'Could not save your profile.'));
    } finally {
      setSaving(false);
    }
  }

  if (loading) return <p className="page-message" role="status">Loading your profile…</p>;

  return (
    <section className="student-page">
      <header className="student-page-header">
        <div><p className="eyebrow">YOUR PROFILE</p><h1>Tell your career story.</h1>
          <p>Keep your education and experience up to date to improve job matches.</p></div>
        <div className="completion-card"><strong>{completion}%</strong><span>profile completed</span></div>
      </header>
      <form className="student-form-card" onSubmit={save}>
        <h2>Personal information</h2>
        <div className="student-form-grid">
          <label>Phone<input type="tel" maxLength="32" value={profile.phone} onChange={(e) => setProfile({ ...profile, phone: e.target.value })} /></label>
          <label>Location<input maxLength="160" value={profile.location} onChange={(e) => setProfile({ ...profile, location: e.target.value })} placeholder="City, country or remote" /></label>
          <label>Education<input maxLength="160" value={profile.education} onChange={(e) => setProfile({ ...profile, education: e.target.value })} placeholder="e.g. B.Tech Computer Science" /></label>
          <label>College or institution<input maxLength="200" value={profile.college} onChange={(e) => setProfile({ ...profile, college: e.target.value })} /></label>
          <label>Graduation year<input type="number" min="1900" max="2200" value={profile.graduationYear} onChange={(e) => setProfile({ ...profile, graduationYear: e.target.value })} /></label>
          <label>Years of experience<input type="number" min="0" max="80" value={profile.experienceYears} onChange={(e) => setProfile({ ...profile, experienceYears: e.target.value })} /></label>
          <label className="form-full-width">About me<textarea rows="5" maxLength="3000" value={profile.bio} onChange={(e) => setProfile({ ...profile, bio: e.target.value })} placeholder="Share your focus, strengths, and the roles you are looking for." /></label>
        </div>
        {error && <p className="form-error" role="alert">{error}</p>}
        {notice && <p className="form-success" role="status">{notice}</p>}
        <button className="primary-button" type="submit" disabled={saving}>{saving ? 'Saving…' : 'Save profile'}</button>
      </form>
    </section>
  );
}
