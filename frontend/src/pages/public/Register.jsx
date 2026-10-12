import { useState } from 'react';
import { Link, Navigate, useNavigate } from 'react-router-dom';
import { Eye, EyeOff } from 'lucide-react';
import { pathForRole } from '../../components/ProtectedRoute';
import { useAuth } from '../../context/AuthContext';
import { getApiErrorMessage } from '../../services/authService';

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
        <p className="auth-intro">Create a student account to explore opportunities and manage your applications.</p>
        <form onSubmit={handleSubmit}>
          <p className="account-type-notice">
            Recruiter and admin accounts are created by the platform administrator. If you already have an authorized account, <Link to="/login">sign in here</Link>.
          </p>
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
          <button className="primary-button auth-submit" type="submit" disabled={submitting}>
            {submitting ? 'Creating account…' : 'Create student account'}
          </button>
        </form>
        <p className="auth-switch">Already registered? <Link to="/login">Sign in</Link></p>
      </section>
    </main>
  );
}
