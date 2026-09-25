const BASE = '/api';

async function request(path, options = {}) {
  const res = await fetch(BASE + path, {
    headers: { 'Content-Type': 'application/json' },
    ...options,
  });
  if (!res.ok) {
    const text = await res.text().catch(() => '');
    throw new Error(text || `Ошибка запроса: ${res.status}`);
  }
  if (res.status === 204) return null;
  return res.json();
}

export const api = {
  subjects: () => request('/subjects'),
  createSubject: (data) => request('/subjects', { method: 'POST', body: JSON.stringify(data) }),
  updateSubject: (id, data) => request(`/subjects/${id}`, { method: 'PUT', body: JSON.stringify(data) }),
  deleteSubject: (id) => request(`/subjects/${id}`, { method: 'DELETE' }),

  grades: () => request('/grades'),
  createGrade: (data) => request('/grades', { method: 'POST', body: JSON.stringify(data) }),
  updateGrade: (id, data) => request(`/grades/${id}`, { method: 'PUT', body: JSON.stringify(data) }),
  deleteGrade: (id) => request(`/grades/${id}`, { method: 'DELETE' }),

  weekReport: (date) => request(`/report/week${date ? `?date=${date}` : ''}`),
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