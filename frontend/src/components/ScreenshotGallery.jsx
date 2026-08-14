import React, { useEffect, useState } from 'react';
import { getScreenshotBlob, deleteScreenshot } from '../api/journal';
import '../styles/ScreenshotGallery.css';

/**
 * ScreenshotGallery Component
 * Displays thumbnails of trade screenshots with delete functionality
 * @param {Array<TradeScreenshotDTO>} screenshots - List of screenshot objects
 * @param {function} onDeleteSuccess - Callback when delete succeeds
 * @param {function} onDeleteError - Callback when delete fails
 */
export default function ScreenshotGallery({ screenshots = [], onDeleteSuccess, onDeleteError }) {
  const [thumbnails, setThumbnails] = useState({});
  const [loading, setLoading] = useState({});
  const [deletingId, setDeletingId] = useState(null);

  /**
   * Load screenshot blob and create object URL for display
   */
  useEffect(() => {
    screenshots.forEach((screenshot) => {
      if (!thumbnails[screenshot.id] && !loading[screenshot.id]) {
        loadThumbnail(screenshot.id);
      }
    });

    return () => {
      // Cleanup object URLs on unmount
      Object.values(thumbnails).forEach((url) => {
        if (url) {
          URL.revokeObjectURL(url);
        }
      });
    };
  }, [screenshots, thumbnails, loading]);

  const loadThumbnail = async (screenshotId) => {
    setLoading((prev) => ({ ...prev, [screenshotId]: true }));

    try {
      const blob = await getScreenshotBlob(screenshotId);
      const url = URL.createObjectURL(blob);
      setThumbnails((prev) => ({ ...prev, [screenshotId]: url }));
    } catch (error) {
      console.error('Failed to load screenshot:', error);
      setThumbnails((prev) => ({ ...prev, [screenshotId]: null }));
    } finally {
      setLoading((prev) => ({ ...prev, [screenshotId]: false }));
    }
  };

  const handleDelete = async (screenshotId) => {
    if (!window.confirm('Delete this screenshot?')) {
      return;
    }

    setDeletingId(screenshotId);

    try {
      await deleteScreenshot(screenshotId);
      
      // Revoke object URL
      if (thumbnails[screenshotId]) {
        URL.revokeObjectURL(thumbnails[screenshotId]);
      }
      
      // Remove from state
      setThumbnails((prev) => {
        const newState = { ...prev };
        delete newState[screenshotId];
        return newState;
      });

      onDeleteSuccess?.();
    } catch (error) {
      const errorMsg = error.response?.data?.message || error.message || 'Delete failed';
      console.error('Failed to delete screenshot:', error);
      onDeleteError?.(errorMsg);
    } finally {
      setDeletingId(null);
    }
  };

  if (screenshots.length === 0) {
    return null;
  }

  return (
    <div className="screenshot-gallery">
      <div className="gallery-grid">
        {screenshots.map((screenshot) => (
          <div key={screenshot.id} className="gallery-item">
            <div className="thumbnail-container">
              {loading[screenshot.id] ? (
                <div className="thumbnail-loading">⏳ Loading...</div>
              ) : thumbnails[screenshot.id] ? (
                <img
                  src={thumbnails[screenshot.id]}
                  alt={screenshot.originalFileName}
                  className="thumbnail-image"
                  title={screenshot.originalFileName}
                />
              ) : (
                <div className="thumbnail-error">❌ Failed to load</div>
              )}

              <button
                className="delete-btn"
                onClick={() => handleDelete(screenshot.id)}
                disabled={deletingId === screenshot.id}
                type="button"
                title="Delete screenshot"
              >
                {deletingId === screenshot.id ? '⏳' : '✕'}
              </button>
            </div>

            <div className="thumbnail-info">
              <div className="thumbnail-name" title={screenshot.originalFileName}>
                {screenshot.originalFileName}
              </div>
              <div className="thumbnail-size">
                {(screenshot.fileSizeBytes / 1024).toFixed(1)} KB
              </div>
              <div className="thumbnail-date">
                {new Date(screenshot.uploadedAt).toLocaleDateString()}
              </div>
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}
