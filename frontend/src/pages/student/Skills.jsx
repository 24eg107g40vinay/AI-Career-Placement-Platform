import { useEffect, useState } from 'react';
import { X } from 'lucide-react';
import { addStudentSkill, getRequestError, getStudentSkills, removeStudentSkill } from '../../services/studentService';

export function Skills() {
  const [skills, setSkills] = useState([]);
  const [name, setName] = useState('');
  const [loading, setLoading] = useState(true);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    getStudentSkills()
      .then(setSkills)
      .catch((requestError) => setError(getRequestError(requestError, 'Could not load your skills.')))
      .finally(() => setLoading(false));
  }, []);

  async function add(event) {
    event.preventDefault();
    if (!name.trim()) return;
    setBusy(true);
    setError('');
    try {
      setSkills(await addStudentSkill(name.trim()));
      setName('');
    } catch (requestError) {
      setError(getRequestError(requestError, 'Could not add this skill.'));
    } finally {
      setBusy(false);
    }
  }

  async function remove(id) {
    setBusy(true);
    setError('');
    try {
      setSkills(await removeStudentSkill(id));
    } catch (requestError) {
      setError(getRequestError(requestError, 'Could not remove this skill.'));
    } finally {
      setBusy(false);
    }
  }

  return (
    <section className="student-page">
      <header className="student-page-header"><div><p className="eyebrow">SKILLS</p><h1>Your strengths, clearly shown.</h1>
        <p>Add skills you can confidently demonstrate. They power your job matches.</p></div></header>
      <form className="skill-add-form" onSubmit={add}>
        <label htmlFor="new-skill">Add a skill</label>
        <div><input id="new-skill" maxLength="100" value={name} onChange={(event) => setName(event.target.value)} placeholder="e.g. Spring Boot" />
          <button className="primary-button" disabled={busy || !name.trim()} type="submit">{busy ? 'Saving…' : 'Add skill'}</button></div>
      </form>
      {error && <p className="form-error" role="alert">{error}</p>}
      <section className="student-form-card">
        <h2>My skills <span className="subtle-count">{skills.length}</span></h2>
        {loading ? <p className="page-message" role="status">Loading skills…</p> :
          skills.length === 0 ? <p className="empty-state">No skills added yet. Add a skill above to start getting matched with relevant roles.</p> :
            <div className="skill-chip-list">{skills.map((skill) => (
              <span className="skill-chip" key={skill.id}>{skill.name}
                <button type="button" disabled={busy} aria-label={`Remove ${skill.name}`} onClick={() => remove(skill.id)}><X size={14} /></button>
              </span>
            ))}</div>}
      </section>
    </section>
  );
}
