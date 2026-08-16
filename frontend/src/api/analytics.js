import client from './client';

function dateParams(fromDate, toDate) {
  const params = {};
  if (fromDate) params.fromDate = fromDate;
  if (toDate) params.toDate = toDate;
  return params;
}

export async function getEquityCurve(fromDate, toDate) {
  const response = await client.get('/analytics/equity-curve', { params: dateParams(fromDate, toDate) });
  return response.data;
}

export async function getDrawdown(fromDate, toDate) {
  const response = await client.get('/analytics/drawdown', { params: dateParams(fromDate, toDate) });
  return response.data;
}

export async function getByStrategy(fromDate, toDate) {
  const response = await client.get('/analytics/by-strategy', { params: dateParams(fromDate, toDate) });
  return response.data;
}

export async function getByTicker(fromDate, toDate) {
  const response = await client.get('/analytics/by-ticker', { params: dateParams(fromDate, toDate) });
  return response.data;
}

export async function getByDayOfWeek(fromDate, toDate) {
  const response = await client.get('/analytics/by-day-of-week', { params: dateParams(fromDate, toDate) });
  return response.data;
}

export async function getByHour(fromDate, toDate) {
  const response = await client.get('/analytics/by-hour', { params: dateParams(fromDate, toDate) });
  return response.data;
}

export async function getMonthly(fromDate, toDate) {
  const response = await client.get('/analytics/monthly', { params: dateParams(fromDate, toDate) });
  return response.data;
}

export async function getWeekly(fromDate, toDate) {
  const response = await client.get('/analytics/weekly', { params: dateParams(fromDate, toDate) });
  return response.data;
}
