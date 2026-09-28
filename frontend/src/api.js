const BASE = '/api';
const TOKEN_KEY = 'school-tracking-token';

export function getToken() {
  return localStorage.getItem(TOKEN_KEY);
}

export function setToken(token) {
  if (token) localStorage.setItem(TOKEN_KEY, token);
  else localStorage.removeItem(TOKEN_KEY);
}

async function request(path, options = {}) {
  const token = getToken();
  const headers = { 'Content-Type': 'application/json', ...(options.headers || {}) };
  if (token) headers.Authorization = `Bearer ${token}`;

  const res = await fetch(BASE + path, { ...options, headers });

  if (res.status === 401) {
    setToken(null);
  }
  if (!res.ok) {
    const text = await res.text().catch(() => '');
    let message = text;
    try {
      const parsed = JSON.parse(text);
      message = parsed.message || parsed.error || text;
    } catch {
      /* not json */
    }
    throw new Error(message || `Ошибка запроса: ${res.status}`);
  }
  if (res.status === 204) return null;
  return res.json();
}

function qs(params) {
  const entries = Object.entries(params).filter(([, v]) => v !== undefined && v !== null && v !== '');
  return entries.length ? `?${entries.map(([k, v]) => `${k}=${encodeURIComponent(v)}`).join('&')}` : '';
}

export const api = {
  register: (data) => request('/auth/register', { method: 'POST', body: JSON.stringify(data) }),
  login: (data) => request('/auth/login', { method: 'POST', body: JSON.stringify(data) }),
  me: () => request('/auth/me'),
  changePassword: (data) => request('/auth/password', { method: 'PUT', body: JSON.stringify(data) }),

  children: () => request('/children'),
  createChild: (data) => request('/children', { method: 'POST', body: JSON.stringify(data) }),
  deleteChild: (id) => request(`/children/${id}`, { method: 'DELETE' }),

  subjects: () => request('/subjects'),
  createSubject: (data) => request('/subjects', { method: 'POST', body: JSON.stringify(data) }),
  updateSubject: (id, data) => request(`/subjects/${id}`, { method: 'PUT', body: JSON.stringify(data) }),
  deleteSubject: (id) => request(`/subjects/${id}`, { method: 'DELETE' }),

  grades: (childId) => request(`/grades${qs({ childId })}`),
  createGrade: (data) => request('/grades', { method: 'POST', body: JSON.stringify(data) }),
  updateGrade: (id, data) => request(`/grades/${id}`, { method: 'PUT', body: JSON.stringify(data) }),
  deleteGrade: (id) => request(`/grades/${id}`, { method: 'DELETE' }),

  weekReport: (date, childId) => request(`/report/week${qs({ date, childId })}`),
  allStats: (childId) => request(`/stats/all${qs({ childId })}`),
  settings: () => request('/settings'),
  saveSettings: (data) => request('/settings', { method: 'PUT', body: JSON.stringify(data) }),
};

export function money(value) {
  const n = Number(value ?? 0);
  return n.toLocaleString('ru-RU', { style: 'currency', currency: 'RUB', minimumFractionDigits: 0 });
}

export function todayISO() {
  return new Date().toISOString().slice(0, 10);
}

export function formatDate(iso) {
  if (!iso) return '';
  const [y, m, d] = iso.split('-');
  return `${d}.${m}.${y}`;
}