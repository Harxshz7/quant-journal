import React, { useEffect, useRef, useState } from 'react';
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

  // Track object URLs in a ref so cleanup can revoke them exactly once, on unmount.
  // (Revoking in a state-keyed effect would kill URLs that are still on screen.)
  const urlCacheRef = useRef({});
  const inFlightRef = useRef({});

  /**
   * Load screenshot blob and create object URL for display.
   * Only runs when the screenshot list changes; never revokes on re-render.
   */
  useEffect(() => {
    let cancelled = false;

    screenshots.forEach((screenshot) => {
      if (urlCacheRef.current[screenshot.id] || inFlightRef.current[screenshot.id]) {
        return;
      }

      inFlightRef.current[screenshot.id] = true;
      setLoading((prev) => ({ ...prev, [screenshot.id]: true }));

      getScreenshotBlob(screenshot.id)
        .then((blob) => {
          if (cancelled) return;
          const url = URL.createObjectURL(blob);
          urlCacheRef.current[screenshot.id] = url;
          setThumbnails((prev) => ({ ...prev, [screenshot.id]: url }));
        })
        .catch((error) => {
          console.error('Failed to load screenshot:', error);
          if (!cancelled) {
            setThumbnails((prev) => ({ ...prev, [screenshot.id]: null }));
          }
        })
        .finally(() => {
          inFlightRef.current[screenshot.id] = false;
          if (!cancelled) {
            setLoading((prev) => ({ ...prev, [screenshot.id]: false }));
          }
        });
    });

    return () => {
      cancelled = true;
      // Let a re-run (e.g. StrictMode's double-invoke or a new screenshots list)
      // start fresh fetches instead of being blocked by the in-flight guard.
      inFlightRef.current = {};
    };
  }, [screenshots]);

  // Revoke all object URLs exactly once, when the gallery unmounts.
  useEffect(() => {
    return () => {
      Object.values(urlCacheRef.current).forEach((url) => {
        if (url) {
          URL.revokeObjectURL(url);
        }
      });
      urlCacheRef.current = {};
    };
  }, []);

  const handleDelete = async (screenshotId) => {
    if (!window.confirm('Delete this screenshot?')) {
      return;
    }

    setDeletingId(screenshotId);

    try {
      await deleteScreenshot(screenshotId);

      // Revoke object URL and remove from ref + state
      const url = urlCacheRef.current[screenshotId];
      if (url) {
        URL.revokeObjectURL(url);
        delete urlCacheRef.current[screenshotId];
      }
      setThumbnails((prev) => {
        const newState = { ...prev };
        delete newState[screenshotId];
        return newState;
      });

      onDeleteSuccess?.(screenshotId);
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
