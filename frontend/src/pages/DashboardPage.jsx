import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { getAnalytics, getDashboard } from '../api/jobTrackerApi.js';
import LoadingState from '../components/LoadingState.jsx';
import PageHeader from '../components/PageHeader.jsx';
import StatusBadge from '../components/StatusBadge.jsx';
import { useAuth } from '../context/AuthContext.jsx';
import { getErrorMessage } from '../utils/errors.js';
import { formatDate, formatMonth, formatTime, formatStatus, STATUSES } from '../utils/format.js';

const STATUS_COLORS = {
  APPLIED: '#8c98ab',
  ASSESSMENT: '#e3a23b',
  INTERVIEW: '#5e7ee8',
  OFFER: '#40a77a',
  REJECTED: '#dd6b6b',
};

function MonthlyChart({ rows }) {
  const width = 660;
  const height = 220;
  const left = 34;
  const right = 12;
  const top = 14;
  const bottom = 38;
  const chartHeight = height - top - bottom;
  const chartWidth = width - left - right;
  const max = Math.max(...rows.map((row) => row.applicationCount), 1);
  const step = chartWidth / Math.max(rows.length, 1);
  const barWidth = Math.min(34, step * 0.48);
  const guides = [0, 0.5, 1];

  if (!rows.length) return <div className="chart-empty">Your monthly activity will appear here.</div>;

  return (
    <div className="chart-wrap">
      <svg className="monthly-chart" viewBox={`0 0 ${width} ${height}`} role="img" aria-label="Applications submitted each month">
        {guides.map((fraction) => {
          const y = top + chartHeight * (1 - fraction);
          return <g key={fraction}><line x1={left} y1={y} x2={width - right} y2={y} className="chart-guide" /><text x={left - 10} y={y + 4} textAnchor="end" className="chart-axis-label">{Math.round(max * fraction)}</text></g>;
        })}
        {rows.map((row, index) => {
          const x = left + index * step + (step - barWidth) / 2;
          const barHeight = Math.max((row.applicationCount / max) * chartHeight, row.applicationCount ? 3 : 0);
          const y = top + chartHeight - barHeight;
          return <g key={row.month}>
            <rect x={x} y={y} width={barWidth} height={barHeight} rx="6" className="chart-bar"><title>{`${formatMonth(row.month)}: ${row.applicationCount} applications`}</title></rect>
            <text x={x + barWidth / 2} y={height - 12} textAnchor="middle" className="chart-axis-label">{formatMonth(row.month)}</text>
          </g>;
        })}
      </svg>
    </div>
  );
}

function StatusSummary({ counts }) {
  const total = Object.values(counts || {}).reduce((sum, count) => sum + count, 0);
  return (
    <div className="status-summary">
      <div className="status-total"><strong>{total}</strong><span>applications</span></div>
      <div className="status-summary-list">
        {STATUSES.map((status) => {
          const value = counts?.[status] || 0;
          const width = total ? (value / total) * 100 : 0;
          return <div className="status-summary-row" key={status}><div className="status-summary-label"><span style={{ backgroundColor: STATUS_COLORS[status] }} /><span>{formatStatus(status)}</span><strong>{value}</strong></div><div className="status-track"><span style={{ width: `${width}%`, backgroundColor: STATUS_COLORS[status] }} /></div></div>;
        })}
      </div>
    </div>
  );
}

export default function DashboardPage() {
  const { user } = useAuth();
  const [dashboard, setDashboard] = useState(null);
  const [analytics, setAnalytics] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    let active = true;
    Promise.all([getDashboard(), getAnalytics()])
      .then(([dashboardData, analyticsData]) => {
        if (active) {
          setDashboard(dashboardData);
          setAnalytics(analyticsData);
        }
      })
      .catch((requestError) => {
        if (active) setError(getErrorMessage(requestError));
      })
      .finally(() => {
        if (active) setLoading(false);
      });
    return () => { active = false; };
  }, []);

  if (loading) return <div className="content-page"><LoadingState label="Gathering your search insights…" /></div>;

  const today = new Intl.DateTimeFormat(undefined, { weekday: 'long', month: 'long', day: 'numeric' }).format(new Date());
  const conversionSteps = analytics?.conversionRates || [];
  const monthlyRows = analytics?.applicationsPerMonth || [];
  const chartRows = monthlyRows.slice(-8);

  return (
    <div className="content-page dashboard-page">
      <PageHeader eyebrow={today.toUpperCase()} title={`Good to see you, ${user?.fullName?.split(' ')[0] || 'there'}.`} description="A clear view of where your job search is today." actionLabel="Add application" actionTo="/applications/new" />
      {error && <div className="form-error form-error-block" role="alert">{error}</div>}
      {!error && dashboard && (
        <>
          <section className="stat-grid" aria-label="Job search summary">
            <article className="stat-card card"><div className="stat-top"><span className="stat-icon stat-icon-indigo">↗</span><span className="stat-trend">All time</span></div><strong className="stat-value">{dashboard.totalApplications}</strong><span className="stat-label">Total applications</span><span className="stat-foot">Every opportunity you’ve saved</span></article>
            <article className="stat-card card"><div className="stat-top"><span className="stat-icon stat-icon-green">✳</span><span className="stat-trend">Across your search</span></div><strong className="stat-value">{dashboard.offerRatePercent}%</strong><span className="stat-label">Offer rate</span><span className="stat-foot">Applications that reached offer</span></article>
            <article className="stat-card card"><div className="stat-top"><span className="stat-icon stat-icon-amber">◎</span><span className="stat-trend">Across your search</span></div><strong className="stat-value">{dashboard.responseRatePercent}%</strong><span className="stat-label">Response rate</span><span className="stat-foot">Applications that moved beyond applied</span></article>
            <article className="stat-card stat-card-dark"><div className="stat-top"><span className="stat-icon stat-icon-white">◷</span><span className="stat-trend stat-trend-white">This week</span></div><strong className="stat-value">{dashboard.upcomingInterviews.length}</strong><span className="stat-label">Upcoming interviews</span><span className="stat-foot">{formatDate(dashboard.weekStart)} – {formatDate(dashboard.weekEnd)}</span></article>
          </section>

          <section className="dashboard-grid">
            <article className="card panel-card activity-card">
              <div className="panel-heading"><div><p className="eyebrow">YOUR MOMENTUM</p><h2>Application activity</h2></div><span className="panel-badge">Last {chartRows.length || 0} months</span></div>
              <MonthlyChart rows={chartRows} />
            </article>
            <article className="card panel-card status-card">
              <div className="panel-heading"><div><p className="eyebrow">AT A GLANCE</p><h2>Pipeline stages</h2></div><Link className="text-link" to="/kanban">View board ↗</Link></div>
              <StatusSummary counts={dashboard.applicationsByStatus} />
            </article>
          </section>

          <section className="dashboard-grid dashboard-grid-lower">
            <article className="card panel-card conversion-card">
              <div className="panel-heading"><div><p className="eyebrow">THE JOURNEY</p><h2>Stage conversions</h2></div><span className="panel-badge">Reached next stage</span></div>
              {conversionSteps.length ? <div className="conversion-list">{conversionSteps.map((step) => <div className="conversion-row" key={`${step.fromStage}-${step.toStage}`}><div className="conversion-label"><StatusBadge status={step.fromStage} /><span className="conversion-arrow">→</span><StatusBadge status={step.toStage} /></div><div className="conversion-metric"><strong>{step.conversionRatePercent}%</strong><span>{step.applicationsConverted} of {step.applicationsReachedFromStage}</span></div><div className="conversion-track"><span style={{ width: `${Math.min(Number(step.conversionRatePercent), 100)}%` }} /></div></div>)}</div> : <div className="chart-empty">Conversion insights will appear as you update your stages.</div>}
            </article>
            <article className="card panel-card interview-panel">
              <div className="panel-heading"><div><p className="eyebrow">MAKE IT COUNT</p><h2>Coming up</h2></div><span className="panel-badge">{dashboard.upcomingInterviews.length} this week</span></div>
              {dashboard.upcomingInterviews.length ? <div className="interview-list">{dashboard.upcomingInterviews.map((interview) => <div className="interview-row" key={interview.id}><div className="interview-date-box"><strong>{new Date(`${interview.interviewDate}T00:00:00`).getDate()}</strong><span>{new Intl.DateTimeFormat(undefined, { month: 'short' }).format(new Date(`${interview.interviewDate}T00:00:00`))}</span></div><div className="interview-detail"><strong>{formatStatus(interview.type)} interview</strong><span>Application #{interview.applicationId}</span><small>{formatTime(interview.interviewTime)}{interview.location ? ` · ${interview.location}` : ''}</small></div><StatusBadge status="INTERVIEW" /></div>)}</div> : <div className="empty-interviews"><span>◷</span><strong>A little space to prepare.</strong><p>No interviews scheduled for this week.</p></div>}
            </article>
          </section>

          <section className="card panel-card stage-duration-card">
            <div className="panel-heading"><div><p className="eyebrow">TIME WELL SPENT</p><h2>Average days in each stage</h2></div><span className="panel-badge">Current stages run through today</span></div>
            {analytics?.averageDaysPerStage?.length ? <div className="duration-grid">{analytics.averageDaysPerStage.map((stage) => <div className="duration-item" key={stage.status}><StatusBadge status={stage.status} /><strong>{stage.averageDays}</strong><span>days on average</span></div>)}</div> : <div className="chart-empty">Stage timing will appear as applications build history.</div>}
          </section>
        </>
      )}
    </div>
  );
}
