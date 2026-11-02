import { useState } from 'react';
import { Link, Navigate, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext.jsx';
import { getErrorMessage } from '../utils/errors.js';

export default function RegisterPage() {
  const { signUp, token, loading } = useAuth();
  const navigate = useNavigate();
  const [values, setValues] = useState({ fullName: '', email: '', password: '' });
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
      await signUp(values);
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
        <p className="eyebrow">GET STARTED</p>
        <h1>Give your search a home.</h1>
        <p className="page-description">Create your account and keep every opportunity organized.</p>
        <form className="form-stack auth-form" onSubmit={submit}>
          <label>Full name<input name="fullName" type="text" autoComplete="name" value={values.fullName} onChange={update} maxLength="255" required /></label>
          <label>Email address<input name="email" type="email" autoComplete="email" value={values.email} onChange={update} required /></label>
          <label>Password<input name="password" type="password" autoComplete="new-password" value={values.password} onChange={update} minLength="8" maxLength="72" required /><span className="field-hint">Use at least 8 characters.</span></label>
          {error && <div className="form-error" role="alert">{error}</div>}
          <button className="button button-primary button-wide" type="submit" disabled={submitting}>{submitting ? 'Creating account…' : 'Create account'}<span aria-hidden="true">→</span></button>
        </form>
        <p className="auth-switch">Already have an account? <Link to="/login">Sign in</Link></p>
      </section>
      <aside className="auth-aside">
        <div className="aside-glow" />
        <div className="aside-content">
          <span className="aside-kicker">Your search, in good hands.</span>
          <h2>Less juggling. More momentum.</h2>
          <p>See what’s moving, what’s next, and where to put your energy.</p>
          <div className="aside-stat"><span className="aside-stat-icon">✳</span><span><strong>Clarity for your next chapter</strong><small>One workspace, all your opportunities</small></span></div>
        </div>
        <span className="aside-decoration aside-decoration-one" />
        <span className="aside-decoration aside-decoration-two" />
      </aside>
    </main>
  );
}
