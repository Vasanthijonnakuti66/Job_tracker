export default function LoadingState({ label = 'Loading your data…' }) {
  return <div className="loading-state" role="status"><span className="spinner" />{label}</div>;
}
