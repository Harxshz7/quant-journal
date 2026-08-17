import React, { useEffect, useState } from 'react';
import { getLessons, createLesson, updateLesson, deleteLesson } from '../api/lessons';
import NavBar from '../components/NavBar';

export default function Lessons() {
  const [lessons, setLessons] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const [showForm, setShowForm] = useState(false);
  const [title, setTitle] = useState('');
  const [content, setContent] = useState('');
  const [tags, setTags] = useState('');
  const [submitting, setSubmitting] = useState(false);

  const [filterTag, setFilterTag] = useState('');
  const [editingId, setEditingId] = useState(null);
  const [editTitle, setEditTitle] = useState('');
  const [editContent, setEditContent] = useState('');
  const [editTags, setEditTags] = useState('');

  const fetchLessons = async () => {
    try {
      setLoading(true);
      const data = await getLessons(filterTag || undefined);
      setLessons(Array.isArray(data) ? data : []);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to load lessons');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { fetchLessons(); }, [filterTag]);

  const handleCreate = async (e) => {
    e.preventDefault();
    if (!title.trim() || !content.trim()) return;
    try {
      setSubmitting(true);
      const payload = { title: title.trim(), content: content.trim() };
      if (tags.trim()) payload.tags = tags.split(',').map(t => t.trim()).filter(Boolean);
      await createLesson(payload);
      setTitle('');
      setContent('');
      setTags('');
      setShowForm(false);
      await fetchLessons();
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to create lesson');
    } finally {
      setSubmitting(false);
    }
  };

  const handleUpdate = async (id) => {
    if (!editTitle.trim() || !editContent.trim()) return;
    try {
      const payload = { title: editTitle.trim(), content: editContent.trim() };
      if (editTags.trim()) payload.tags = editTags.split(',').map(t => t.trim()).filter(Boolean);
      else payload.tags = [];
      await updateLesson(id, payload);
      setEditingId(null);
      await fetchLessons();
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to update lesson');
    }
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Delete this lesson?')) return;
    try {
      await deleteLesson(id);
      await fetchLessons();
    } catch (err) {
      alert(err.response?.data?.message || 'Failed to delete lesson');
    }
  };

  const allTags = [...new Set(lessons.flatMap(l => l.tags || []))].sort();

  return (
    <div className="container">
      <NavBar active="lessons">
        <button className="btn btn-primary btn-sm" onClick={() => setShowForm(true)}>
          + New Lesson
        </button>
      </NavBar>

      <header className="header">
        <div>
          <h1>Lessons Learned</h1>
          <p className="muted section-subtitle">
            Capture insights, patterns, and recurring mistakes
          </p>
        </div>
      </header>

      {allTags.length > 0 && (
        <div className="tag-filter-row">
          <button
            className={`btn btn-sm ${!filterTag ? 'is-selected' : ''}`}
            onClick={() => setFilterTag('')}
          >
            All
          </button>
          {allTags.map(tag => (
            <button
              key={tag}
              className={`btn btn-sm ${filterTag === tag ? 'is-selected' : ''}`}
              onClick={() => setFilterTag(filterTag === tag ? '' : tag)}
            >
              {tag}
            </button>
          ))}
        </div>
      )}

      {error && <div className="error-banner">{error}</div>}

      {loading ? (
        <p className="loading">Loading lessons...</p>
      ) : lessons.length === 0 ? (
        <div className="empty-state">
          <p>No lessons recorded yet.</p>
          <button className="btn btn-secondary" onClick={() => setShowForm(true)}>
            Record your first lesson
          </button>
        </div>
      ) : (
        <div className="trades-flex-col">
          {lessons.map((lesson) => (
            <div
              key={lesson.id}
              className="section-card"
            >
              {editingId === lesson.id ? (
                <>
                  <input
                    type="text"
                    value={editTitle}
                    onChange={(e) => setEditTitle(e.target.value)}
                    className="lesson-edit-input"
                  />
                  <textarea
                    value={editContent}
                    onChange={(e) => setEditContent(e.target.value)}
                    rows={4}
                    className="lesson-edit-textarea"
                  />
                  <input
                    type="text"
                    value={editTags}
                    onChange={(e) => setEditTags(e.target.value)}
                    placeholder="tags, comma, separated"
                    className="lesson-edit-tags"
                  />
                  <div className="btn-icon-row">
                    <button className="btn btn-primary btn-sm" onClick={() => handleUpdate(lesson.id)}>Save</button>
                    <button className="btn btn-secondary btn-sm" onClick={() => setEditingId(null)}>Cancel</button>
                  </div>
                </>
              ) : (
                <>
                  <div className="lesson-header-row">
                    <h3 className="lesson-title">{lesson.title}</h3>
                    <div className="lesson-actions">
                      <button
                        className="btn btn-sm btn-secondary"
                        onClick={() => {
                          setEditingId(lesson.id);
                          setEditTitle(lesson.title);
                          setEditContent(lesson.content);
                          setEditTags((lesson.tags || []).join(', '));
                        }}
                      >
                        Edit
                      </button>
                      <button
                        className="btn btn-sm btn-secondary btn-danger-text"
                        onClick={() => handleDelete(lesson.id)}
                      >
                        Delete
                      </button>
                    </div>
                  </div>
                  <p className="lesson-content">
                    {lesson.content}
                  </p>
                  {lesson.tags && lesson.tags.length > 0 && (
                    <div className="lesson-tags">
                      {lesson.tags.map(tag => (
                        <span
                          key={tag}
                          className="tag-chip"
                          onClick={() => setFilterTag(filterTag === tag ? '' : tag)}
                        >
                          {tag}
                        </span>
                      ))}
                    </div>
                  )}
                </>
              )}
            </div>
          ))}
        </div>
      )}

      {showForm && (
        <div className="modal-backdrop" onClick={() => setShowForm(false)}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <h2>New Lesson</h2>
            <form onSubmit={handleCreate}>
              <div className="form-group">
                <label htmlFor="lesson-title">Title</label>
                <input
                  id="lesson-title"
                  type="text"
                  placeholder="e.g. Always wait for confirmation candle"
                  value={title}
                  onChange={(e) => setTitle(e.target.value)}
                  required
                />
              </div>
              <div className="form-group">
                <label htmlFor="lesson-content">What you learned</label>
                <textarea
                  id="lesson-content"
                  rows="5"
                  placeholder="Describe the lesson, when it applies, and how to avoid the mistake..."
                  value={content}
                  onChange={(e) => setContent(e.target.value)}
                  required
                />
              </div>
              <div className="form-group">
                <label htmlFor="lesson-tags">Tags (comma-separated)</label>
                <input
                  id="lesson-tags"
                  type="text"
                  placeholder="e.g. entry, psychology, risk"
                  value={tags}
                  onChange={(e) => setTags(e.target.value)}
                />
              </div>
              <div className="modal-actions">
                <button type="button" className="btn btn-secondary" onClick={() => setShowForm(false)}>
                  Cancel
                </button>
                <button type="submit" className="btn btn-primary" disabled={submitting}>
                  {submitting ? 'Saving...' : 'Save Lesson'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
