import { useEffect, useState } from 'react';
import { getRequestError } from '../../services/studentService';
import { getCompanyProfile, saveCompanyProfile } from '../../services/recruiterService';

const blank = { name: '', description: '', website: '', location: '' };

export function CompanyProfile() {
  const [company, setCompany] = useState(blank);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [notice, setNotice] = useState('');

  useEffect(() => {
    getCompanyProfile().then((data) => data && setCompany(data))
      .catch((requestError) => setError(getRequestError(requestError, 'Could not load company profile.')))
      .finally(() => setLoading(false));
  }, []);

  async function save(event) {
    event.preventDefault();
    setError('');
    setNotice('');
    setSaving(true);
    try {
      setCompany(await saveCompanyProfile(company));
      setNotice('Company profile saved.');
    } catch (requestError) {
      setError(getRequestError(requestError, 'Could not save the company profile.'));
    } finally {
      setSaving(false);
    }
  }

  return <section className="student-page">
    <header className="student-page-header"><div><p className="eyebrow">COMPANY PROFILE</p><h1>Introduce your organization.</h1><p>Share details candidates need to know about your company.</p></div></header>
    {loading ? <p className="page-message" role="status">Loading company profile…</p> :
      <form className="student-form-card" onSubmit={save}><h2>Company details</h2>
        <div className="student-form-grid">
          <label>Company name<input required maxLength="160" value={company.name} onChange={(e) => setCompany({ ...company, name: e.target.value })} /></label>
          <label>Website<input type="url" maxLength="255" placeholder="https://" value={company.website || ''} onChange={(e) => setCompany({ ...company, website: e.target.value })} /></label>
          <label>Location<input maxLength="160" value={company.location || ''} onChange={(e) => setCompany({ ...company, location: e.target.value })} /></label>
          <label className="form-full-width">Description<textarea rows="5" maxLength="3000" value={company.description || ''} onChange={(e) => setCompany({ ...company, description: e.target.value })} /></label>
        </div>
        {error && <p className="form-error" role="alert">{error}</p>}
        {notice && <p className="form-success" role="status">{notice}</p>}
        <button className="primary-button" disabled={saving}>{saving ? 'Saving…' : 'Save company profile'}</button>
      </form>}
  </section>;
}
