import { useEffect, useState } from 'react';
import { Download, FileText, Trash2, Upload } from 'lucide-react';
import { api } from '../../services/api';
import { deleteResume, getRequestError, getResumes, uploadResume } from '../../services/studentService';

export function Resume() {
  const [resumes, setResumes] = useState([]);
  const [file, setFile] = useState(null);
  const [loading, setLoading] = useState(true);
  const [uploading, setUploading] = useState(false);
  const [progress, setProgress] = useState(0);
  const [error, setError] = useState('');
  const [notice, setNotice] = useState('');

  async function refresh() {
    const data = await getResumes();
    setResumes(data);
  }

  useEffect(() => {
    refresh().catch((requestError) => setError(getRequestError(requestError, 'Could not load your resumes.')))
      .finally(() => setLoading(false));
  }, []);

  async function handleUpload(event) {
    event.preventDefault();
    if (!file) {
      setError('Choose a PDF file first.');
      return;
    }
    if (file.type !== 'application/pdf' || !file.name.toLowerCase().endsWith('.pdf')) {
      setError('Only PDF resumes are allowed.');
      return;
    }
    if (file.size > 5 * 1024 * 1024) {
      setError('Resume must be 5 MB or smaller.');
      return;
    }
    setError('');
    setNotice('');
    setUploading(true);
    setProgress(0);
    try {
      await uploadResume(file, (eventProgress) => {
        if (eventProgress.total) setProgress(Math.round((eventProgress.loaded * 100) / eventProgress.total));
      });
      setFile(null);
      event.target.reset();
      await refresh();
      setNotice('Your resume has been uploaded securely.');
    } catch (requestError) {
      setError(getRequestError(requestError, 'Could not upload your resume.'));
    } finally {
      setUploading(false);
    }
  }

  async function remove(id) {
    setError('');
    setNotice('');
    try {
      await deleteResume(id);
      await refresh();
      setNotice('Resume deleted.');
    } catch (requestError) {
      setError(getRequestError(requestError, 'Could not delete this resume.'));
    }
  }

  return (
    <section className="student-page">
      <header className="student-page-header"><div><p className="eyebrow">RESUME MANAGEMENT</p><h1>Keep your experience ready.</h1>
        <p>Your resume is stored privately and only available to your account.</p></div></header>
      <form className="resume-upload-card" onSubmit={handleUpload}>
        <span className="public-feature-icon"><Upload size={20} /></span>
        <h2>Upload a resume</h2><p>PDF only · Up to 5 MB</p>
        <label className="resume-file-label">Choose PDF<input type="file" accept="application/pdf,.pdf"
          onChange={(event) => { setFile(event.target.files?.[0] || null); setError(''); }} /></label>
        {file && <span className="selected-file">{file.name}</span>}
        <button className="primary-button" type="submit" disabled={uploading || !file}>{uploading ? `Uploading ${progress}%…` : 'Upload resume'}</button>
        {uploading && <progress max="100" value={progress} aria-label="Resume upload progress" />}
      </form>
      {error && <p className="form-error" role="alert">{error}</p>}
      {notice && <p className="form-success" role="status">{notice}</p>}
      <section className="student-form-card resume-list-card">
        <h2>Your resumes</h2>
        {loading ? <p className="page-message" role="status">Loading resumes…</p> :
          resumes.length === 0 ? <p className="empty-state">No resume uploaded yet. Upload a PDF to use it when applying for a job.</p> :
            resumes.map((resume) => <article className="resume-row" key={resume.id}>
              <span className="resume-file-icon"><FileText size={18} /></span>
              <div><strong>{resume.fileName}</strong><small>{(resume.fileSizeBytes / 1024 / 1024).toFixed(2)} MB · Uploaded {new Date(resume.uploadedAt).toLocaleDateString()}</small></div>
              <a className="icon-action" href={`${api.defaults.baseURL}/students/resumes/${resume.id}/download`}
                onClick={async (event) => {
                  event.preventDefault();
                  try {
                    const response = await api.get(`/students/resumes/${resume.id}/download`, { responseType: 'blob' });
                    const url = URL.createObjectURL(response.data);
                    const anchor = document.createElement('a');
                    anchor.href = url;
                    anchor.download = resume.fileName;
                    anchor.click();
                    URL.revokeObjectURL(url);
                  } catch (requestError) {
                    setError(getRequestError(requestError, 'Could not download this resume.'));
                  }
                }} aria-label={`Download ${resume.fileName}`}><Download size={17} /></a>
              <button type="button" className="icon-action danger-action" onClick={() => remove(resume.id)} aria-label={`Delete ${resume.fileName}`}><Trash2 size={17} /></button>
            </article>)}
      </section>
    </section>
  );
}
