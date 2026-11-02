import { useEffect, useRef, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import {
  createApplication,
  downloadAttachment,
  getApplication,
  getAttachments,
  updateApplication,
  uploadAttachment,
} from '../api/jobTrackerApi.js';
import LoadingState from '../components/LoadingState.jsx';
import { getErrorMessage } from '../utils/errors.js';

function localToday() {
  const date = new Date();
  date.setMinutes(date.getMinutes() - date.getTimezoneOffset());
  return date.toISOString().slice(0, 10);
}

const emptyApplication = {
  companyName: '',
  role: '',
  location: '',
  jobLink: '',
  salaryMin: '',
  salaryMax: '',
  notes: '',
  dateApplied: localToday(),
};

export default function ApplicationFormPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const editing = Boolean(id);
  const [values, setValues] = useState(emptyApplication);
  const [loading, setLoading] = useState(editing);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');
  const [attachments, setAttachments] = useState([]);
  const [attachmentType, setAttachmentType] = useState('RESUME');
  const [selectedFile, setSelectedFile] = useState(null);
  const [uploading, setUploading] = useState(false);
  const fileInputRef = useRef(null);

  useEffect(() => {
    if (!editing) return undefined;
    let active = true;
    Promise.all([getApplication(id), getAttachments(id)])
      .then(([application, savedAttachments]) => {
        if (active) {
          setValues({
            companyName: application.companyName,
            role: application.role,
            location: application.location || '',
            jobLink: application.jobLink || '',
            salaryMin: application.salaryMin ?? '',
            salaryMax: application.salaryMax ?? '',
            notes: application.notes || '',
            dateApplied: application.dateApplied,
          });
          setAttachments(savedAttachments);
        }
      })
      .catch((requestError) => {
        if (active) setError(requestError.message || getErrorMessage(requestError));
      })
      .finally(() => {
        if (active) setLoading(false);
      });
    return () => { active = false; };
  }, [editing, id]);

  function update(event) {
    setValues((current) => ({ ...current, [event.target.name]: event.target.value }));
  }

  async function submit(event) {
    event.preventDefault();
    setSubmitting(true);
    setError('');
    const request = {
      ...values,
      salaryMin: values.salaryMin === '' ? null : Number(values.salaryMin),
      salaryMax: values.salaryMax === '' ? null : Number(values.salaryMax),
      location: values.location || null,
      jobLink: values.jobLink || null,
      notes: values.notes || null,
    };
    try {
      if (editing) await updateApplication(id, request);
      else await createApplication(request);
      navigate('/applications', { replace: true });
    } catch (requestError) {
      setError(getErrorMessage(requestError));
    } finally {
      setSubmitting(false);
    }
  }

  async function submitAttachment() {
    if (!selectedFile) {
      setError('Choose a PDF before uploading.');
      return;
    }
    if (!selectedFile.name.toLowerCase().endsWith('.pdf')) {
      setError('Only PDF files are allowed.');
      return;
    }
    if (selectedFile.size > 10 * 1024 * 1024) {
      setError('PDF files must be 10 MB or smaller.');
      return;
    }

    setUploading(true);
    setError('');
    try {
      const saved = await uploadAttachment(id, selectedFile, attachmentType);
      setAttachments((current) => [saved, ...current]);
      setSelectedFile(null);
      fileInputRef.current.value = '';
    } catch (requestError) {
      setError(getErrorMessage(requestError));
    } finally {
      setUploading(false);
    }
  }

  async function downloadSavedAttachment(attachment) {
    setError('');
    try {
      const response = await downloadAttachment(id, attachment.id);
      const url = URL.createObjectURL(response.data);
      const link = document.createElement('a');
      link.href = url;
      link.download = attachment.fileName;
      document.body.appendChild(link);
      link.click();
      link.remove();
      window.setTimeout(() => URL.revokeObjectURL(url), 1000);
    } catch (requestError) {
      setError(getErrorMessage(requestError));
    }
  }

  if (loading) return <LoadingState label="Loading application…" />;

  return (
    <div className="content-page form-page">
      <Link to="/applications" className="back-link">← Back to applications</Link>
      <header className="page-header form-page-header">
        <div><p className="eyebrow">APPLICATION DETAILS</p><h1>{editing ? 'Edit application' : 'Add an application'}</h1><p className="page-description">Keep the important details together. You can update its stage anytime.</p></div>
      </header>
      {error && <div className="form-error form-error-block" role="alert">{error}</div>}
      <form className="application-form card" onSubmit={submit}>
        <div className="form-section-title"><span className="step-number">01</span><div><h2>The opportunity</h2><p>Where are you hoping to work?</p></div></div>
        <div className="form-grid">
          <label>Company name <span className="required">*</span><input name="companyName" value={values.companyName} onChange={update} maxLength="255" required placeholder="e.g. Northstar Labs" /></label>
          <label>Role title <span className="required">*</span><input name="role" value={values.role} onChange={update} maxLength="255" required placeholder="e.g. Software Engineer" /></label>
          <label>Location<input name="location" value={values.location} onChange={update} maxLength="255" placeholder="City, state, or remote" /></label>
          <label>Job posting link<input name="jobLink" type="url" value={values.jobLink} onChange={update} maxLength="500" placeholder="https://" /></label>
        </div>
        <div className="form-section-title form-section-spaced"><span className="step-number">02</span><div><h2>A few more details</h2><p>Optional information to help you stay prepared.</p></div></div>
        <div className="form-grid">
          <label>Minimum salary<input name="salaryMin" type="number" min="1" value={values.salaryMin} onChange={update} placeholder="e.g. 80000" /></label>
          <label>Maximum salary<input name="salaryMax" type="number" min="1" value={values.salaryMax} onChange={update} placeholder="e.g. 110000" /></label>
          <label>Date applied <span className="required">*</span><input name="dateApplied" type="date" value={values.dateApplied} onChange={update} required /></label>
          <label className="form-span-two">Notes<textarea name="notes" value={values.notes} onChange={update} rows="4" placeholder="Add a referral, a follow-up reminder, or anything you want to remember." /></label>
        </div>
        <div className="form-section-title form-section-spaced"><span className="step-number">03</span><div><h2>Documents</h2><p>Keep a resume or job description PDF with this application.</p></div></div>
        {editing ? (
          <div className="attachment-section">
            <div className="attachment-upload">
              <label>Document type<select value={attachmentType} onChange={(event) => setAttachmentType(event.target.value)}><option value="RESUME">Resume</option><option value="JOB_DESCRIPTION">Job description</option></select></label>
              <label className="attachment-file-input">PDF file<input ref={fileInputRef} type="file" accept="application/pdf,.pdf" onChange={(event) => setSelectedFile(event.target.files?.[0] || null)} /></label>
              <button className="button button-dark" type="button" onClick={submitAttachment} disabled={uploading || !selectedFile}>{uploading ? 'Uploading…' : 'Upload PDF'}<span aria-hidden="true">↑</span></button>
            </div>
            <p className="field-hint">PDF only · Up to 10 MB</p>
            {attachments.length ? <ul className="attachment-list">{attachments.map((attachment) => <li key={attachment.id}><span className="attachment-file-icon">PDF</span><span className="attachment-meta"><strong>{attachment.fileName}</strong><small>{attachment.fileType === 'RESUME' ? 'Resume' : 'Job description'} · {Math.max(1, Math.round(attachment.fileSize / 1024))} KB</small></span><button className="attachment-download" type="button" onClick={() => downloadSavedAttachment(attachment)} aria-label={`Download ${attachment.fileName}`}>↓</button></li>)}</ul> : <p className="attachment-empty">No documents attached yet.</p>}
          </div>
        ) : <p className="attachment-create-hint">Save this application first, then open it to upload its PDFs.</p>}
        <div className="form-actions"><Link className="button button-quiet" to="/applications">Cancel</Link><button className="button button-primary" type="submit" disabled={submitting}>{submitting ? 'Saving…' : editing ? 'Save changes' : 'Save application'}<span aria-hidden="true">→</span></button></div>
      </form>
    </div>
  );
}
