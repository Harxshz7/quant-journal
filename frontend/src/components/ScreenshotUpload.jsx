import React, { useRef, useState } from 'react';
import { uploadScreenshot } from '../api/journal';
import '../styles/ScreenshotUpload.css';

/**
 * ScreenshotUpload Component
 * Handles file upload and display of screenshots for a trade
 * @param {string} tradeId - The trade ID to upload screenshots for
 * @param {function} onUploadSuccess - Callback when upload succeeds
 * @param {function} onUploadError - Callback when upload fails
 */
export default function ScreenshotUpload({ tradeId, onUploadSuccess, onUploadError }) {
  const fileInputRef = useRef(null);
  const [isUploading, setIsUploading] = useState(false);
  const [error, setError] = useState(null);

  const MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

  const handleFileSelect = async (e) => {
    const file = e.target.files?.[0];
    if (!file) return;

    // Client-side 10MB check
    if (file.size > MAX_FILE_SIZE) {
      const errorMsg = 'File size exceeds 10MB limit';
      setError(errorMsg);
      onUploadError?.(errorMsg);
      return;
    }

    // Validate it's an image
    if (!file.type.startsWith('image/')) {
      const errorMsg = 'File must be an image';
      setError(errorMsg);
      onUploadError?.(errorMsg);
      return;
    }

    setError(null);
    setIsUploading(true);

    try {
      const screenshot = await uploadScreenshot(tradeId, file, (progressEvent) => {
        // Optional: Handle progress updates here
        const progress = Math.round(
          (progressEvent.loaded / progressEvent.total) * 100
        );
        console.log(`Upload progress: ${progress}%`);
      });

      onUploadSuccess?.(screenshot);
      setIsUploading(false);

      // Reset file input
      if (fileInputRef.current) {
        fileInputRef.current.value = '';
      }
    } catch (err) {
      const errorMsg = err.response?.data?.message || err.message || 'Upload failed';
      setError(errorMsg);
      onUploadError?.(errorMsg);
      setIsUploading(false);
    }
  };

  const handleClick = () => {
    fileInputRef.current?.click();
  };

  return (
    <div className="screenshot-upload">
      <button
        className="btn btn-neumorphic upload-btn"
        onClick={handleClick}
        disabled={isUploading}
        type="button"
        title="Upload a screenshot (JPEG, PNG, or WebP, max 10MB)"
      >
        {isUploading ? '⏳ Uploading...' : '📸 Add Screenshot'}
      </button>

      <input
        ref={fileInputRef}
        type="file"
        accept="image/*"
        onChange={handleFileSelect}
        className="file-input-hidden"
        disabled={isUploading}
      />

      {error && <div className="error-message">{error}</div>}
    </div>
  );
}
