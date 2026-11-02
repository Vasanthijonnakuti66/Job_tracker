import { useCallback, useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { changeApplicationStatus, getAllApplications } from '../api/jobTrackerApi.js';
import LoadingState from '../components/LoadingState.jsx';
import PageHeader from '../components/PageHeader.jsx';
import StatusBadge from '../components/StatusBadge.jsx';
import { getErrorMessage } from '../utils/errors.js';
import { formatDate, formatStatus, STATUSES } from '../utils/format.js';

export default function KanbanPage() {
  const [applications, setApplications] = useState([]);
  const [loading, setLoading] = useState(true);
  const [movingId, setMovingId] = useState(null);
  const [draggedId, setDraggedId] = useState(null);
  const [error, setError] = useState('');

  const loadBoard = useCallback(async () => {
    setLoading(true);
    setError('');
    try {
      setApplications(await getAllApplications());
    } catch (requestError) {
      setError(getErrorMessage(requestError));
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => { loadBoard(); }, [loadBoard]);

  async function moveApplication(id, newStatus) {
    const application = applications.find((item) => item.id === id);
    if (!application || application.status === newStatus) return;
    setMovingId(id);
    setError('');
    try {
      const updated = await changeApplicationStatus(id, newStatus);
      setApplications((current) => current.map((item) => item.id === id ? updated : item));
    } catch (requestError) {
      setError(getErrorMessage(requestError));
    } finally {
      setMovingId(null);
      setDraggedId(null);
    }
  }

  function handleDrop(event, status) {
    event.preventDefault();
    const id = Number(event.dataTransfer.getData('text/plain'));
    if (Number.isFinite(id)) moveApplication(id, status);
  }

  return (
    <div className="content-page board-page">
      <PageHeader eyebrow="THE BIG PICTURE" title="Pipeline board" description="Drag an opportunity into its next stage to update your progress." actionLabel="Add application" actionTo="/applications/new" />
      {error && <div className="form-error form-error-block" role="alert">{error}</div>}
      {loading ? <LoadingState label="Loading your pipeline…" /> : applications.length === 0 ? (
        <div className="empty-state card"><span className="empty-icon">✳</span><h3>Your pipeline is ready</h3><p>Add your first opportunity and move it through each stage.</p><Link className="button button-primary" to="/applications/new">Add an application <span aria-hidden="true">→</span></Link></div>
      ) : (
        <div className="kanban-board">
          {STATUSES.map((status) => {
            const cards = applications.filter((application) => application.status === status);
            return (
              <section key={status} className={`kanban-column column-${status.toLowerCase()}`} onDragOver={(event) => event.preventDefault()} onDrop={(event) => handleDrop(event, status)} aria-label={`${formatStatus(status)} stage`}>
                <header className="kanban-column-header"><div><span className="column-indicator" /><h2>{formatStatus(status)}</h2><span className="column-count">{cards.length}</span></div><span className="column-menu" aria-hidden="true">···</span></header>
                <div className="kanban-cards">
                  {cards.map((application) => (
                    <article
                      key={application.id}
                      className={`kanban-card card ${draggedId === application.id ? 'is-dragging' : ''}`}
                      draggable={movingId !== application.id}
                      onDragStart={(event) => { setDraggedId(application.id); event.dataTransfer.setData('text/plain', String(application.id)); event.dataTransfer.effectAllowed = 'move'; }}
                      onDragEnd={() => setDraggedId(null)}
                    >
                      <div className="kanban-card-top"><span className="company-avatar">{application.companyName.charAt(0).toUpperCase()}</span><span className="card-date">{formatDate(application.dateApplied)}</span></div>
                      <h3>{application.companyName}</h3><p className="kanban-role">{application.role}</p>
                      {application.location && <p className="kanban-location">⌖ {application.location}</p>}
                      <div className="kanban-card-bottom"><StatusBadge status={application.status} /><label className="move-select"><span className="sr-only">Move {application.companyName} to another stage</span><select value={application.status} onChange={(event) => moveApplication(application.id, event.target.value)} disabled={movingId === application.id}>{STATUSES.filter((stage) => stage !== application.status).map((stage) => <option key={stage} value={stage}>Move to {formatStatus(stage)}</option>)}</select></label><Link className="card-edit-link" title="Edit application" to={`/applications/${application.id}/edit`}>↗</Link></div>
                      {movingId === application.id && <span className="moving-label">Updating…</span>}
                    </article>
                  ))}
                  {cards.length === 0 && <div className="column-empty">Drop an application here</div>}
                </div>
              </section>
            );
          })}
        </div>
      )}
    </div>
  );
}
