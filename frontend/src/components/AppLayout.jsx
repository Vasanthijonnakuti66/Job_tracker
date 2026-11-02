import { NavLink, Outlet, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext.jsx';

function Icon({ name }) {
  const paths = {
    overview: <><rect x="3" y="3" width="7" height="7" rx="1" /><rect x="14" y="3" width="7" height="7" rx="1" /><rect x="3" y="14" width="7" height="7" rx="1" /><rect x="14" y="14" width="7" height="7" rx="1" /></>,
    applications: <><rect x="4" y="3" width="16" height="18" rx="2" /><path d="M8 8h8M8 12h8M8 16h5" /></>,
    board: <><rect x="3" y="4" width="5" height="16" rx="1" /><rect x="10" y="4" width="5" height="10" rx="1" /><rect x="17" y="4" width="5" height="13" rx="1" /></>,
  };
  return <svg aria-hidden="true" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.7" strokeLinecap="round" strokeLinejoin="round">{paths[name]}</svg>;
}

export default function AppLayout() {
  const { user, signOut } = useAuth();
  const navigate = useNavigate();

  async function handleSignOut() {
    try {
      await signOut();
    } catch {
      // The context keeps a notice for the login screen after local sign-out.
    } finally {
      navigate('/login', { replace: true });
    }
  }

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <NavLink className="brand" to="/dashboard" aria-label="Job Tracker home">
          <span className="brand-mark">J</span>
          <span>job<span className="brand-light">tracker</span></span>
        </NavLink>
        <div className="nav-caption">WORKSPACE</div>
        <nav className="primary-nav" aria-label="Main navigation">
          <NavLink to="/dashboard"><Icon name="overview" />Overview</NavLink>
          <NavLink to="/applications"><Icon name="applications" />Applications</NavLink>
          <NavLink to="/kanban"><Icon name="board" />Pipeline board</NavLink>
        </nav>
        <div className="sidebar-bottom">
          <div className="user-chip">
            <div className="avatar">{user?.fullName?.charAt(0)?.toUpperCase() || 'U'}</div>
            <div className="user-meta"><strong>{user?.fullName || 'Job seeker'}</strong><span>{user?.email}</span></div>
          </div>
          <button type="button" className="sign-out" onClick={handleSignOut}>Sign out <span aria-hidden="true">↗</span></button>
        </div>
      </aside>
      <main className="main-area">
        <Outlet />
      </main>
    </div>
  );
}
