import { Link } from 'react-router-dom';

export default function PageHeader({ eyebrow, title, description, actionLabel, actionTo }) {
  return (
    <header className="page-header">
      <div>
        {eyebrow && <p className="eyebrow">{eyebrow}</p>}
        <h1>{title}</h1>
        {description && <p className="page-description">{description}</p>}
      </div>
      {actionLabel && <Link className="button button-primary" to={actionTo}>{actionLabel}<span aria-hidden="true">＋</span></Link>}
    </header>
  );
}
