import { useCallback, useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { deleteApplication, getApplications } from '../api/jobTrackerApi.js';
import LoadingState from '../components/LoadingState.jsx';
import PageHeader from '../components/PageHeader.jsx';
import StatusBadge from '../components/StatusBadge.jsx';
import { getErrorMessage } from '../utils/errors.js';
import { formatDate, STATUSES } from '../utils/format.js';

const initialFilters = { company: '', role: '', status: '', fromDate: '', toDate: '' };

export default function ApplicationsPage() {
  const [filters, setFilters] = useState(initialFilters);
  const [activeFilters, setActiveFilters] = useState(initialFilters);
  const [sortBy, setSortBy] = useState('dateApplied');
  const [sortDir, setSortDir] = useState('desc');
  const [page, setPage] = useState(0);
  const [result, setResult] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [deletingId, setDeletingId] = useState(null);

  const loadApplications = useCallback(async () => {
    setLoading(true);
    setError('');
    try {
      const data = await getApplications({
        ...activeFilters,
        status: activeFilters.status || undefined,
        company: activeFilters.company || undefined,
        role: activeFilters.role || undefined,
        fromDate: activeFilters.fromDate || undefined,
        toDate: activeFilters.toDate || undefined,
        page,
        size: 10,
        sortBy,
        sortDir,
      });
      setResult(data);
    } catch (requestError) {
      setError(getErrorMessage(requestError));
    } finally {
      setLoading(false);
    }
  }, [activeFilters, page, sortBy, sortDir]);

  useEffect(() => {
    loadApplications();
  }, [loadApplications]);

  function applyFilters(event) {
    event.preventDefault();
    setPage(0);
    setActiveFilters(filters);
  }

  function clearFilters() {
    setFilters(initialFilters);
    setActiveFilters(initialFilters);
    setPage(0);
  }

  async function removeApplication(application) {
    if (!window.confirm(`Delete the application to ${application.companyName}? This also deletes its interview and status history.`)) return;
    setDeletingId(application.id);
    setError('');
    try {
      await deleteApplication(application.id);
      if (result?.content.length === 1 && page > 0) {
        setPage((current) => current - 1);
      } else {
        await loadApplications();
      }
    } catch (requestError) {
      setError(getErrorMessage(requestError));
    } finally {
      setDeletingId(null);
    }
  }

  function updateFilter(event) {
    setFilters((current) => ({ ...current, [event.target.name]: event.target.value }));
  }

  return (
    <div className="content-page">
      <PageHeader eyebrow="YOUR SEARCH" title="Applications" description="Every opportunity, organized in one place." actionLabel="Add application" actionTo="/applications/new" />
      {error && <div className="form-error form-error-block" role="alert">{error}</div>}
      <form className="filter-bar card" onSubmit={applyFilters}>
        <label className="search-field"><span className="field-icon">⌕</span><input aria-label="Company" name="company" value={filters.company} onChange={updateFilter} placeholder="Search companies" /></label>
        <label className="search-field"><span className="field-icon">⌕</span><input aria-label="Role" name="role" value={filters.role} onChange={updateFilter} placeholder="Search roles" /></label>
        <label className="filter-select"><span className="sr-only">Status</span><select name="status" value={filters.status} onChange={updateFilter}><option value="">All stages</option>{STATUSES.map((status) => <option key={status} value={status}>{status.charAt(0) + status.slice(1).toLowerCase()}</option>)}</select></label>
        <label className="filter-date"><span className="sr-only">Applied from</span><input aria-label="Applied from" type="date" name="fromDate" value={filters.fromDate} onChange={updateFilter} /></label>
        <label className="filter-date"><span className="sr-only">Applied to</span><input aria-label="Applied to" type="date" name="toDate" value={filters.toDate} onChange={updateFilter} /></label>
        <button className="button button-dark" type="submit">Filter</button>
        <button className="button button-quiet clear-button" type="button" onClick={clearFilters}>Clear</button>
      </form>

      <section className="table-card card" aria-label="Job applications">
        <div className="table-toolbar">
          <div><h2>All applications</h2><span className="muted">{result?.totalElements ?? '—'} opportunities</span></div>
          <label className="sort-control">Sort by
            <select value={sortBy} onChange={(event) => { setSortBy(event.target.value); setPage(0); }}>
              <option value="dateApplied">Date applied</option><option value="companyName">Company</option><option value="role">Role</option><option value="status">Status</option>
            </select>
            <button type="button" className="sort-direction" aria-label={`Sort ${sortDir === 'asc' ? 'descending' : 'ascending'}`} onClick={() => setSortDir((direction) => direction === 'asc' ? 'desc' : 'asc')}>{sortDir === 'asc' ? '↑' : '↓'}</button>
          </label>
        </div>
        {loading ? <LoadingState label="Loading applications…" /> : result?.content.length ? (
          <>
            <div className="table-scroll">
              <table>
                <thead><tr><th>Company</th><th>Role</th><th>Applied</th><th>Status</th><th>Salary range</th><th><span className="sr-only">Actions</span></th></tr></thead>
                <tbody>
                  {result.content.map((application) => (
                    <tr key={application.id}>
                      <td><div className="company-cell"><span className="company-avatar">{application.companyName.charAt(0).toUpperCase()}</span><div><strong>{application.companyName}</strong>{application.location && <small>{application.location}</small>}</div></div></td>
                      <td className="role-cell">{application.role}</td>
                      <td className="date-cell">{formatDate(application.dateApplied)}</td>
                      <td><StatusBadge status={application.status} /></td>
                      <td className="salary-cell">{application.salaryMin || application.salaryMax ? `${application.salaryMin?.toLocaleString() || '—'} – ${application.salaryMax?.toLocaleString() || '—'}` : '—'}</td>
                      <td><div className="row-actions"><Link className="icon-button" title="Edit application" aria-label={`Edit ${application.companyName}`} to={`/applications/${application.id}/edit`}>↗</Link><button className="icon-button danger-hover" title="Delete application" aria-label={`Delete ${application.companyName}`} disabled={deletingId === application.id} onClick={() => removeApplication(application)}>×</button></div></td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
            <footer className="pagination">
              <span>Showing {result.page * result.size + 1}–{Math.min((result.page + 1) * result.size, result.totalElements)} of {result.totalElements}</span>
              <div><button className="button button-quiet page-button" disabled={result.page === 0} onClick={() => setPage((current) => current - 1)}>← Previous</button><span className="page-number">{result.totalPages ? result.page + 1 : 0} <span>of</span> {result.totalPages}</span><button className="button button-quiet page-button" disabled={result.last} onClick={() => setPage((current) => current + 1)}>Next →</button></div>
            </footer>
          </>
        ) : (
          <div className="empty-state"><span className="empty-icon">✳</span><h3>{result?.totalElements ? 'No matches this time' : 'Start with one application'}</h3><p>{result?.totalElements ? 'Try adjusting your filters to find what you’re looking for.' : 'Add an opportunity and keep every next step in view.'}</p>{result?.totalElements ? <button className="button button-quiet" onClick={clearFilters}>Clear filters</button> : <Link className="button button-primary" to="/applications/new">Add your first application <span aria-hidden="true">→</span></Link>}</div>
        )}
      </section>
    </div>
  );
}
