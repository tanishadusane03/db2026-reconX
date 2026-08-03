const BASE = '/api';
function authHeaders() { const token = typeof sessionStorage !== 'undefined' ? sessionStorage.getItem('reconx-token') : null; return token ? { Authorization: `Bearer ${token}` } : {}; }
async function request(method, path, body) {
  const response = await fetch(`${BASE}${path}`, { method, headers: { 'Content-Type': 'application/json', ...authHeaders() }, body: body != null ? JSON.stringify(body) : undefined });
  if (!response.ok) { let detail = response.statusText; try { detail = await response.text() || detail; } catch { /* retain status text */ } throw new Error(`HTTP ${response.status}: ${detail}`); }
  if (response.status === 204 || !(response.headers.get('content-type') || '').includes('application/json')) return null;
  return response.json();
}
export const api = {
  login: (email, password) => request('POST', '/auth/login', { email, password }),
  listTrades: (params = '') => request('GET', `/v1/trades${params ? `?${params}` : ''}`),
  createTrade: (requestBody) => request('POST', '/v1/trades', requestBody),
  updateStatus: (id, status) => request('PATCH', `/v1/trades/${id}/status`, { status }),
  deleteTrade: (id) => request('DELETE', `/v1/trades/${id}`),
  runRecon: (requestBody) => request('POST', '/v1/recon/run', requestBody),
  reconResults: (jobId) => request('GET', `/v1/recon/jobs/${jobId}/results`),
  audit: (tradeRef) => request('GET', `/v1/audit/trades/${tradeRef}`),
};
