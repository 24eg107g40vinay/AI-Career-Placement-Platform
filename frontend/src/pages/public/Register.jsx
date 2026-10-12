import { useState } from 'react';
import { Link, Navigate, useNavigate } from 'react-router-dom';
import { BriefcaseBusiness, Eye, EyeOff, ShieldCheck, UserRound } from 'lucide-react';
import { pathForRole } from '../../components/ProtectedRoute';
import { useAuth } from '../../context/AuthContext';
import { getApiErrorMessage } from '../../services/authService';

const accountTypes = [
  { role: 'STUDENT', label: 'Student', description: 'Find jobs and manage applications.', Icon: UserRound },
  { role: 'RECRUITER', label: 'Recruiter', description: 'Publish jobs and review candidates.', Icon: BriefcaseBusiness },
  { role: 'ADMIN', label: 'Admin', description: 'Manage and oversee the platform.', Icon: ShieldCheck },
];

export function Register() {
  const { user, register } = useAuth();
  const navigate = useNavigate();
  const [form, setForm] = useState({ name: '', email: '', password: '', role: 'STUDENT' });
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [showPassword, setShowPassword] = useState(false);

  if (user) return <Navigate to={pathForRole(user.role)} replace />;

  async function handleSubmit(event) {
    event.preventDefault();
    setError('');
    setSubmitting(true);
    try {
      const session = await register(form);
      navigate(pathForRole(session.role), { replace: true });
    } catch (requestError) {
      setError(getApiErrorMessage(requestError, 'Account creation failed. Please try again.'));
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <main className="auth-page">
      <section className="auth-card" aria-labelledby="register-heading">
        <Link className="public-brand" to="/">Northstar</Link>
        <p className="eyebrow">START YOUR NEXT CHAPTER</p>
        <h1 id="register-heading">Create your account</h1>
        <p className="auth-intro">Choose your workspace first. You’ll be taken to the matching dashboard after registration.</p>
        <form onSubmit={handleSubmit}>
          <fieldset className="account-type-picker">
            <legend>Choose your workspace</legend>
            <div className="account-type-options">
              {accountTypes.map(({ role, label, description, Icon }) => (
                <button key={role} type="button" className={`account-type-option ${form.role === role ? 'selected' : ''}`}
                  aria-pressed={form.role === role} onClick={() => setForm({ ...form, role })}>
                  <Icon size={18} aria-hidden="true" />
                  <strong>{label}</strong>
                  <small>{description}</small>
                </button>
              ))}
            </div>
          </fieldset>
          {form.role === 'ADMIN' && <p className="account-type-notice" role="status">
            Admin workspaces are created by the platform owner, not through public sign-up. If you already have an admin account, <Link to="/login">sign in here</Link>.
          </p>}
          <label htmlFor="register-name">Full name</label>
          <input id="register-name" name="name" type="text" autoComplete="name" maxLength={120} required
            value={form.name} onChange={(event) => setForm({ ...form, name: event.target.value })} />
          <label htmlFor="register-email">Email address</label>
          <input id="register-email" name="email" type="email" autoComplete="email" maxLength={190} required
            value={form.email} onChange={(event) => setForm({ ...form, email: event.target.value })} />
          <label htmlFor="register-password">Password</label>
          <div className="password-field">
            <input id="register-password" name="password" type={showPassword ? 'text' : 'password'} autoComplete="new-password"
              minLength={8} maxLength={72} required value={form.password}
              onChange={(event) => setForm({ ...form, password: event.target.value })} />
            <button className="password-toggle" type="button" aria-label={showPassword ? 'Hide password' : 'Show password'}
              aria-pressed={showPassword} onClick={() => setShowPassword(!showPassword)}>
              {showPassword ? <EyeOff size={18} aria-hidden="true" /> : <Eye size={18} aria-hidden="true" />}
            </button>
          </div>
          {error && <p className="form-error" role="alert">{error}</p>}
          <button className="primary-button auth-submit" type="submit" disabled={submitting || form.role === 'ADMIN'}>
            {submitting ? 'Creating account…' : form.role === 'ADMIN' ? 'Admin account required' : `Create ${form.role.toLowerCase()} account`}
          </button>
        </form>
        <p className="auth-switch">Already registered? <Link to="/login">Sign in</Link></p>
      </section>
    </main>
  );
}
