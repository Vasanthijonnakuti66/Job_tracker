import { formatStatus } from '../utils/format.js';

export default function StatusBadge({ status }) {
  return <span className={`status-badge status-${status.toLowerCase()}`}><span className="status-dot" />{formatStatus(status)}</span>;
}
