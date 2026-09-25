import { useEffect, useMemo, useState } from 'react';
import { api, money, todayISO, formatDate } from './api.js';

function GradeBadge({ value }) {
  return <span className={`grade grade-${value}`}>{value}</span>;
}

function GradesTab({ subjects, report, reload }) {
  const [subjectId, setSubjectId] = useState('');
  const [value, setValue] = useState(5);
  const [date, setDate] = useState(todayISO());
  const [error, setError] = useState('');

  useEffect(() => {
    if (!subjectId && subjects.length) setSubjectId(String(subjects[0].id));
  }, [subjects, subjectId]);

  async function submit(e) {
    e.preventDefault();
    setError('');
    try {
      await api.createGrade({ subjectId: Number(subjectId), value: Number(value), gradeDate: date });
      reload();
    } catch (err) {
      setError(err.message);
    }
  }

  return (
    <div className="grid">
      <section className="card">
        <h2>Добавить оценку</h2>
        <form onSubmit={submit} className="form">
          <label>
            Предмет
            <select value={subjectId} onChange={(e) => setSubjectId(e.target.value)} required>
              {subjects.map((s) => (
                <option key={s.id} value={s.id}>
                  {s.name} {s.core ? '(основной)' : ''}
                </option>
              ))}
            </select>
          </label>
          <label>
            Оценка
            <select value={value} onChange={(e) => setValue(Number(e.target.value))}>
              {[5, 4, 3, 2].map((v) => (
                <option key={v} value={v}>{v}</option>
              ))}
            </select>
          </label>
          <label>
            Дата
            <input type="date" value={date} onChange={(e) => setDate(e.target.value)} required />
          </label>
          <button type="submit" disabled={!subjects.length}>Сохранить</button>
          {error && <p className="error">{error}</p>}
        </form>
      </section>

      <section className="card">
        <h2>Оценки за неделю</h2>
        <p className="muted">
          {formatDate(report.weekStart)} — {formatDate(report.weekEnd)}
        </p>
        {report.subjects.length === 0 && <p className="muted">За эту неделю оценок нет.</p>}
        {report.subjects.map((s) => (
          <div key={s.subjectId} className="subject-block">
            <div className="subject-head">
              <span>
                {s.subjectName}{' '}
                <span className="muted">× {Number(s.coefficient).toFixed(2)}</span>
                {s.core && <span className="tag">основной</span>}
              </span>
              <strong className={s.amount < 0 ? 'neg' : 'pos'}>{money(s.amount)}</strong>
            </div>
            <table>
              <thead>
                <tr>
                  <th>Дата</th>
                  <th>Оценка</th>
                  <th>Начисление</th>
                  <th />
                </tr>
              </thead>
              <tbody>
                {s.grades.map((g) => (
                  <tr key={g.id}>
                    <td>{formatDate(g.gradeDate)}</td>
                    <td><GradeBadge value={g.value} /></td>
                    <td className={g.amount < 0 ? 'neg' : 'pos'}>{money(g.amount)}</td>
                    <td>
                      <button className="link danger" onClick={() => api.deleteGrade(g.id).then(reload)}>
                        удалить
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        ))}
      </section>
    </div>
  );
}

function SubjectsTab({ subjects, reload }) {
  const [name, setName] = useState('');
  const [core, setCore] = useState(false);
  const [error, setError] = useState('');

  async function submit(e) {
    e.preventDefault();
    setError('');
    try {
      await api.createSubject({ name, core });
      setName('');
      setCore(false);
      reload();
    } catch (err) {
      setError(err.message);
    }
  }

  async function toggleCore(s) {
    await api.updateSubject(s.id, { name: s.name, core: !s.core });
    reload();
  }

  return (
    <section className="card">
      <h2>Предметы</h2>
      <form onSubmit={submit} className="form inline">
        <input placeholder="Название предмета" value={name} onChange={(e) => setName(e.target.value)} required />
        <label className="checkbox">
          <input type="checkbox" checked={core} onChange={(e) => setCore(e.target.checked)} />
          Основной (коэффициент 1.0)
        </label>
        <button type="submit">Добавить</button>
        {error && <p className="error">{error}</p>}
      </form>
      <table>
        <thead>
          <tr>
            <th>Предмет</th>
            <th>Тип</th>
            <th />
          </tr>
        </thead>
        <tbody>
          {subjects.map((s) => (
            <tr key={s.id}>
              <td>{s.name}</td>
              <td>{s.core ? 'основной' : 'обычный'}</td>
              <td className="actions">
                <button className="link" onClick={() => toggleCore(s)}>
                  сделать {s.core ? 'обычным' : 'основным'}
                </button>
                <button className="link danger" onClick={() => api.deleteSubject(s.id).then(reload)}>
                  удалить
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </section>
  );
}

function SettingsTab({ settings, reload }) {
  const [form, setForm] = useState(settings);
  const [saved, setSaved] = useState(false);

  useEffect(() => setForm(settings), [settings]);

  function change(field) {
    return (e) => setForm({ ...form, [field]: e.target.value });
  }

  async function submit(e) {
    e.preventDefault();
    await api.saveSettings({
      fiveReward: form.fiveReward,
      fourReward: form.fourReward,
      threePenalty: form.threePenalty,
      twoPenalty: form.twoPenalty,
      coreCoefficient: form.coreCoefficient,
      otherCoefficient: form.otherCoefficient,
    });
    setSaved(true);
    setTimeout(() => setSaved(false), 2000);
    reload();
  }

  return (
    <section className="card">
      <h2>Настройки поощрения</h2>
      <form onSubmit={submit} className="form">
        <div className="row">
          <label>5 — награда (₽)<input value={form.fiveReward} onChange={change('fiveReward')} /></label>
          <label>4 — награда (₽)<input value={form.fourReward} onChange={change('fourReward')} /></label>
        </div>
        <div className="row">
          <label>3 — штраф (₽)<input value={form.threePenalty} onChange={change('threePenalty')} /></label>
          <label>2 — штраф (₽)<input value={form.twoPenalty} onChange={change('twoPenalty')} /></label>
        </div>
        <div className="row">
          <label>Коэф. основных предметов<input value={form.coreCoefficient} onChange={change('coreCoefficient')} /></label>
          <label>Коэф. остальных предметов<input value={form.otherCoefficient} onChange={change('otherCoefficient')} /></label>
        </div>
        <button type="submit">Сохранить настройки</button>
        {saved && <span className="ok">Сохранено</span>}
      </form>
      <p className="muted note">
        Правило исправления: двойка (−{form.twoPenalty}₽) и две пятёрки (+{form.fiveReward}₽ +{form.fiveReward}₽)
        по одному предмету за неделю дают ровно {money(Number(form.twoPenalty) + 2 * Number(form.fiveReward))}.
      </p>
    </section>
  );
}

export default function App() {
  const [tab, setTab] = useState('grades');
  const [subjects, setSubjects] = useState([]);
  const [report, setReport] = useState(null);
  const [settings, setSettings] = useState(null);
  const [error, setError] = useState('');

  async function reload() {
    try {
      const [s, r, cfg] = await Promise.all([api.subjects(), api.weekReport(), api.settings()]);
      setSubjects(s);
      setReport(r);
      setSettings(cfg);
      setError('');
    } catch (err) {
      setError(err.message);
    }
  }

  useEffect(() => {
    reload();
  }, []);

  const totalClass = report && report.total < 0 ? 'neg' : 'pos';

  return (
    <div className="app">
      <header>
        <div>
          <h1>Поощрение школьника</h1>
          <p className="muted">Пятибалльная система · недельный расчёт</p>
        </div>
        {report && (
          <div className="total">
            <span className="muted">Итого за неделю</span>
            <strong className={totalClass}>{money(report.total)}</strong>
          </div>
        )}
      </header>

      {error && <p className="error box">{error}</p>}

      <nav className="tabs">
        <button className={tab === 'grades' ? 'active' : ''} onClick={() => setTab('grades')}>Оценки</button>
        <button className={tab === 'subjects' ? 'active' : ''} onClick={() => setTab('subjects')}>Предметы</button>
        <button className={tab === 'settings' ? 'active' : ''} onClick={() => setTab('settings')}>Настройки</button>
      </nav>

      {tab === 'grades' && report && (
        <GradesTab subjects={subjects} report={report} reload={reload} />
      )}
      {tab === 'subjects' && <SubjectsTab subjects={subjects} reload={reload} />}
      {tab === 'settings' && settings && <SettingsTab settings={settings} reload={reload} />}
    </div>
  );
}