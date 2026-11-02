import { useState } from 'react';
import { Link, Navigate, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext.jsx';
import { getErrorMessage } from '../utils/errors.js';

export default function LoginPage() {
  const { signIn, token, loading, notice } = useAuth();
  const navigate = useNavigate();
  const [values, setValues] = useState({ email: '', password: '' });
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  if (!loading && token) return <Navigate to="/dashboard" replace />;

  function update(event) {
    setValues((current) => ({ ...current, [event.target.name]: event.target.value }));
  }

  async function submit(event) {
    event.preventDefault();
    setSubmitting(true);
    setError('');
    try {
      await signIn(values);
      navigate('/dashboard', { replace: true });
    } catch (requestError) {
      setError(getErrorMessage(requestError));
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <main className="auth-page">
      <section className="auth-card">
        <Link className="brand auth-brand" to="/login"><span className="brand-mark">J</span><span>job<span className="brand-light">tracker</span></span></Link>
        <p className="eyebrow">WELCOME BACK</p>
        <h1>Your next move starts here.</h1>
        <p className="page-description">Sign in to pick up where you left off.</p>
        <form className="form-stack auth-form" onSubmit={submit}>
          <label>Email address<input name="email" type="email" autoComplete="email" value={values.email} onChange={update} required /></label>
          <label>Password<input name="password" type="password" autoComplete="current-password" value={values.password} onChange={update} required /></label>
          {error && <div className="form-error" role="alert">{error}</div>}
          <button className="button button-primary button-wide" type="submit" disabled={submitting}>{submitting ? 'Signing in…' : 'Sign in'}<span aria-hidden="true">→</span></button>
        </form>
        {notice && <div className="session-notice" role="status">{notice}</div>}
        <p className="auth-switch">New to Job Tracker? <Link to="/register">Create an account</Link></p>
      </section>
      <aside className="auth-aside">
        <div className="aside-glow" />
        <div className="aside-content">
          <span className="aside-kicker">A little more focus.</span>
          <h2>Make your job search feel like progress.</h2>
          <p>Keep every application, interview, and next step in one calm, clear place.</p>
          <div className="aside-stat"><span className="aside-stat-icon">↗</span><span><strong>One step at a time</strong><small>See your search move forward</small></span></div>
        </div>
        <span className="aside-decoration aside-decoration-one" />
        <span className="aside-decoration aside-decoration-two" />
      </aside>
    </main>
  );
}
