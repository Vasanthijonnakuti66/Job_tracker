export const STATUSES = ['APPLIED', 'ASSESSMENT', 'INTERVIEW', 'OFFER', 'REJECTED'];

export function formatStatus(status) {
  return status.charAt(0) + status.slice(1).toLowerCase();
}

export function formatDate(value) {
  if (!value) return '—';
  return new Intl.DateTimeFormat(undefined, { dateStyle: 'medium' }).format(new Date(`${value}T00:00:00`));
}

export function formatMonth(value) {
  return new Intl.DateTimeFormat(undefined, { month: 'short', year: '2-digit' })
    .format(new Date(`${value}T00:00:00`));
}

export function formatTime(value) {
  if (!value) return 'Time not set';
  const [hours, minutes] = value.split(':');
  return new Intl.DateTimeFormat(undefined, { hour: 'numeric', minute: '2-digit' })
    .format(new Date(2000, 0, 1, Number(hours), Number(minutes)));
}
