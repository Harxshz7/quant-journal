import client from './client';

export async function getJournalEntries() {
  const response = await client.get('/journal');
  return response.data;
}

export async function getJournalEntry(id) {
  const response = await client.get(`/journal/${id}`);
  return response.data;
}

export async function createJournalEntry(data) {
  const response = await client.post('/journal', data);
  return response.data;
}

export async function updateJournalEntry(id, data) {
  const response = await client.put(`/journal/${id}`, data);
  return response.data;
}

export async function createTrade(data) {
  const response = await client.post('/trades', data);
  return response.data;
}

export async function updateTrade(id, data) {
  const response = await client.put(`/trades/${id}`, data);
  return response.data;
}

export async function closeTrade(id, data) {
  const response = await client.put(`/trades/${id}/close`, data);
  return response.data;
}

export async function deleteTrade(id) {
  const response = await client.delete(`/trades/${id}`);
  return response.data;
}

export async function getTrades(filters = {}) {
  const params = {};
  const entries = Object.entries(filters);
  entries.forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== '') {
      params[key] = value;
    }
  });

  const response = await client.get('/trades', {
    params,
  });
  return response.data;
}

export async function getStatistics() {
  const response = await client.get('/statistics');
  return response.data;
}

/**
 * Upload a screenshot for a trade
 * @param {string} tradeId - The trade ID
 * @param {File} file - The image file to upload
 * @param {function} onUploadProgress - Optional callback for upload progress
 * @returns {Promise<TradeScreenshotDTO>}
 */
export async function uploadScreenshot(tradeId, file, onUploadProgress) {
  const formData = new FormData();
  formData.append('file', file);

  const response = await client.post(`/trades/${tradeId}/screenshots`, formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
    onUploadProgress,
  });
  return response.data;
}

/**
 * Get screenshot file as blob (raw image bytes)
 * @param {string} screenshotId - The screenshot ID
 * @returns {Promise<Blob>}
 */
export async function getScreenshotBlob(screenshotId) {
  const response = await client.get(`/screenshots/${screenshotId}/file`, {
    responseType: 'blob',
  });
  return response.data;
}

/**
 * Delete a screenshot
 * @param {string} screenshotId - The screenshot ID
 * @returns {Promise<void>}
 */
export async function deleteScreenshot(screenshotId) {
  await client.delete(`/screenshots/${screenshotId}`);
}

/**
 * Get all screenshots for a trade
 * @param {string} tradeId - The trade ID
 * @returns {Promise<TradeScreenshotDTO[]>}
 */
export async function getScreenshotsForTrade(tradeId) {
  const response = await client.get(`/trades/${tradeId}/screenshots`);
  return response.data;
}

/**
 * Import trades from a TradingView CSV export
 * @param {File} file - CSV file
 * @param {string} [journalEntryId] - Optional journal entry ID
 * @returns {Promise<ImportSummaryDTO>}
 */
export async function importTradingViewCsv(file, journalEntryId) {
  const formData = new FormData();
  formData.append('file', file);
  if (journalEntryId) {
    formData.append('journalEntryId', journalEntryId);
  }

  const response = await client.post('/trades/import/tradingview', formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  });
  return response.data;
}

export default client;
