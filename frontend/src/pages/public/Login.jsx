import { useState } from 'react';
import { Link, Navigate, useLocation, useNavigate } from 'react-router-dom';
import { Eye, EyeOff } from 'lucide-react';
import { useAuth } from '../../context/AuthContext';
import { getApiErrorMessage } from '../../services/authService';
import { pathForRole } from '../../components/ProtectedRoute';

export function Login() {
  const { user, login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [form, setForm] = useState({ email: '', password: '' });
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [showPassword, setShowPassword] = useState(false);

  if (user) return <Navigate to={pathForRole(user.role)} replace />;

  async function handleSubmit(event) {
    event.preventDefault();
    setError('');
    setSubmitting(true);
    try {
      const session = await login(form);
      navigate(location.state?.from?.pathname || pathForRole(session.role), { replace: true });
    } catch (requestError) {
      setError(getApiErrorMessage(requestError, 'Sign-in failed. Check your details and try again.'));
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <main className="auth-page">
      <section className="auth-card" aria-labelledby="login-heading">
        <Link className="public-brand" to="/">Northstar</Link>
        <p className="eyebrow">WELCOME BACK</p>
        <h1 id="login-heading">Sign in to your account</h1>
        <p className="auth-intro">Continue building toward your next opportunity.</p>
        <form onSubmit={handleSubmit}>
          <label htmlFor="login-email">Email address</label>
          <input id="login-email" name="email" type="email" autoComplete="email" required
            value={form.email} onChange={(event) => setForm({ ...form, email: event.target.value })} />
          <label htmlFor="login-password">Password</label>
          <div className="password-field">
            <input id="login-password" name="password" type={showPassword ? 'text' : 'password'} autoComplete="current-password" required
              value={form.password} onChange={(event) => setForm({ ...form, password: event.target.value })} />
            <button className="password-toggle" type="button" aria-label={showPassword ? 'Hide password' : 'Show password'}
              aria-pressed={showPassword} onClick={() => setShowPassword(!showPassword)}>
              {showPassword ? <EyeOff size={18} aria-hidden="true" /> : <Eye size={18} aria-hidden="true" />}
            </button>
          </div>
          {error && <p className="form-error" role="alert">{error}</p>}
          <button className="primary-button auth-submit" type="submit" disabled={submitting}>
            {submitting ? 'Signing in…' : 'Sign in'}
          </button>
        </form>
        <p className="auth-switch">New to Northstar? <Link to="/register">Create an account</Link></p>
      </section>
    </main>
  );
}
