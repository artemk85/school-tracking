import { useCallback, useEffect, useMemo, useState } from 'react';
import { api, money, todayISO, formatDate, getToken, setToken } from './api.js';

function GradeBadge({ value }) {
  return <span className={`grade grade-${value}`}>{value}</span>;
}

function AuthScreen({ onAuthenticated }) {
  const [mode, setMode] = useState('login');
  const [form, setForm] = useState({ username: '', password: '', displayName: '' });
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);

  function change(field) {
    return (e) => setForm({ ...form, [field]: e.target.value });
  }

  async function submit(e) {
    e.preventDefault();
    setError('');
    setBusy(true);
    try {
      const payload =
        mode === 'register'
          ? { username: form.username, password: form.password, displayName: form.displayName }
          : { username: form.username, password: form.password };
      const res = mode === 'register' ? await api.register(payload) : await api.login(payload);
      setToken(res.token);
      onAuthenticated(res.user);
    } catch (err) {
      setError(err.message);
    } finally {
      setBusy(false);
    }
  }

  return (
    <div className="auth-wrap">
      <div className="card auth-card">
        <h1>Поощрение школьника</h1>
        <p className="muted">Войдите, чтобы вводить оценки и следить за статистикой.</p>

        <div className="auth-tabs">
          <button className={mode === 'login' ? 'active' : ''} onClick={() => setMode('login')}>Вход</button>
          <button className={mode === 'register' ? 'active' : ''} onClick={() => setMode('register')}>
            Регистрация родителя
          </button>
        </div>

        <form onSubmit={submit} className="form">
          <label>
            Логин
            <input value={form.username} onChange={change('username')} required minLength={3} autoComplete="username" />
          </label>
          <label>
            Пароль
            <input
              type="password"
              value={form.password}
              onChange={change('password')}
              required
              minLength={6}
              autoComplete={mode === 'register' ? 'new-password' : 'current-password'}
            />
          </label>
          {mode === 'register' && (
            <label>
              Имя (необязательно)
              <input value={form.displayName} onChange={change('displayName')} autoComplete="name" />
            </label>
          )}
          <button type="submit" disabled={busy}>{busy ? 'Подождите…' : mode === 'register' ? 'Создать аккаунт' : 'Войти'}</button>
          {error && <p className="error">{error}</p>}
        </form>

        {mode === 'login' && (
          <p className="muted note">
            Аккаунт ребёнка создаёт родитель в разделе «Дети».
          </p>
        )}
      </div>
    </div>
  );
}

function GradesTab({ subjects, report, reload, childId, childName }) {
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
      await api.createGrade({
        subjectId: Number(subjectId),
        value: Number(value),
        gradeDate: date,
        childId,
      });
      reload();
    } catch (err) {
      setError(err.message);
    }
  }

  return (
    <div className="grid">
      <section className="card">
        <h2>Добавить оценку{childName ? ` — ${childName}` : ''}</h2>
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

  async function remove(s) {
    try {
      await api.deleteSubject(s.id);
      reload();
    } catch (err) {
      setError(err.message);
    }
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
                <button className="link danger" onClick={() => remove(s)}>
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

function StatsTab({ stats, childName }) {
  if (!stats || stats.weeks.length === 0) {
    return (
      <section className="card">
        <h2>Статистика за все периоды{childName ? ` — ${childName}` : ''}</h2>
        <p className="muted">Пока нет данных.</p>
      </section>
    );
  }

  return (
    <section className="card">
      <h2>Статистика за все периоды{childName ? ` — ${childName}` : ''}</h2>
      <table>
        <thead>
          <tr>
            <th>Неделя</th>
            <th>Оценок</th>
            <th>Итого</th>
          </tr>
        </thead>
        <tbody>
          {stats.weeks.map((w) => (
            <tr key={w.weekStart}>
              <td>{formatDate(w.weekStart)} — {formatDate(w.weekEnd)}</td>
              <td>{w.gradeCount}</td>
              <td className={w.total < 0 ? 'neg' : 'pos'}>{money(w.total)}</td>
            </tr>
          ))}
        </tbody>
        <tfoot>
          <tr className="total-row">
            <td><strong>Итого ({stats.totalGrades} оценок)</strong></td>
            <td></td>
            <td className={stats.grandTotal < 0 ? 'neg' : 'pos'}>
              <strong>{money(stats.grandTotal)}</strong>
            </td>
          </tr>
        </tfoot>
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

function ProfileTab({ user }) {
  const [form, setForm] = useState({ currentPassword: '', newPassword: '', confirm: '' });
  const [error, setError] = useState('');
  const [saved, setSaved] = useState(false);
  const [busy, setBusy] = useState(false);

  function change(field) {
    return (e) => setForm({ ...form, [field]: e.target.value });
  }

  async function submit(e) {
    e.preventDefault();
    setError('');
    setSaved(false);
    if (form.newPassword !== form.confirm) {
      setError('Новый пароль и подтверждение не совпадают');
      return;
    }
    setBusy(true);
    try {
      await api.changePassword({ currentPassword: form.currentPassword, newPassword: form.newPassword });
      setForm({ currentPassword: '', newPassword: '', confirm: '' });
      setSaved(true);
      setTimeout(() => setSaved(false), 3000);
    } catch (err) {
      setError(err.message);
    } finally {
      setBusy(false);
    }
  }

  return (
    <section className="card">
      <h2>Профиль</h2>
      <p className="muted">
        Вы вошли как <strong>{user.displayName || user.username}</strong> ({user.role === 'PARENT' ? 'родитель' : 'ученик'}).
      </p>
      <h3>Смена пароля</h3>
      <form onSubmit={submit} className="form">
        <label>
          Текущий пароль
          <input
            type="password"
            value={form.currentPassword}
            onChange={change('currentPassword')}
            required
            autoComplete="current-password"
          />
        </label>
        <label>
          Новый пароль
          <input
            type="password"
            value={form.newPassword}
            onChange={change('newPassword')}
            required
            minLength={6}
            autoComplete="new-password"
          />
        </label>
        <label>
          Подтверждение нового пароля
          <input
            type="password"
            value={form.confirm}
            onChange={change('confirm')}
            required
            minLength={6}
            autoComplete="new-password"
          />
        </label>
        <button type="submit" disabled={busy}>{busy ? 'Сохраняем…' : 'Сменить пароль'}</button>
        {saved && <span className="ok">Пароль изменён</span>}
        {error && <p className="error">{error}</p>}
      </form>
    </section>
  );
}

function ChildrenTab({ children, reload }) {
  const [form, setForm] = useState({ username: '', password: '', displayName: '' });
  const [error, setError] = useState('');
  const [created, setCreated] = useState('');

  function change(field) {
    return (e) => setForm({ ...form, [field]: e.target.value });
  }

  async function submit(e) {
    e.preventDefault();
    setError('');
    setCreated('');
    try {
      const child = await api.createChild(form);
      setCreated(`Аккаунт «${child.username}» создан. Передайте ребёнку логин и пароль.`);
      setForm({ username: '', password: '', displayName: '' });
      reload();
    } catch (err) {
      setError(err.message);
    }
  }

  async function remove(child) {
    if (!confirm(`Удалить ребёнка «${child.displayName || child.username}» вместе с его оценками?`)) return;
    try {
      await api.deleteChild(child.id);
      reload();
    } catch (err) {
      setError(err.message);
    }
  }

  return (
    <section className="card">
      <h2>Дети</h2>
      <p className="muted">Создайте учётную запись — ребёнок войдёт под ней и сможет вводить свои оценки.</p>
      <form onSubmit={submit} className="form inline">
        <input placeholder="Логин ребёнка" value={form.username} onChange={change('username')} required minLength={3} />
        <input
          type="password"
          placeholder="Пароль"
          value={form.password}
          onChange={change('password')}
          required
          minLength={6}
        />
        <input placeholder="Имя (необязательно)" value={form.displayName} onChange={change('displayName')} />
        <button type="submit">Создать аккаунт</button>
      </form>
      {error && <p className="error">{error}</p>}
      {created && <p className="ok">{created}</p>}
      <table>
        <thead>
          <tr>
            <th>Логин</th>
            <th>Имя</th>
            <th />
          </tr>
        </thead>
        <tbody>
          {children.length === 0 && (
            <tr>
              <td colSpan={3} className="muted">Пока нет добавленных детей.</td>
            </tr>
          )}
          {children.map((c) => (
            <tr key={c.id}>
              <td>{c.username}</td>
              <td>{c.displayName}</td>
              <td className="actions">
                <button className="link danger" onClick={() => remove(c)}>удалить</button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </section>
  );
}

export default function App() {
  const [user, setUser] = useState(null);
  const [booting, setBooting] = useState(true);
  const [tab, setTab] = useState('grades');
  const [children, setChildren] = useState([]);
  const [selectedChildId, setSelectedChildId] = useState(null);
  const [subjects, setSubjects] = useState([]);
  const [report, setReport] = useState(null);
  const [settings, setSettings] = useState(null);
  const [stats, setStats] = useState(null);
  const [error, setError] = useState('');
  const [weekOffset, setWeekOffset] = useState(0);

  const isParent = user?.role === 'PARENT';
  const activeChildId = isParent ? selectedChildId : user?.id;

  const weekDate = useMemo(() => {
    const d = new Date();
    d.setDate(d.getDate() + weekOffset * 7);
    return d.toISOString().slice(0, 10);
  }, [weekOffset]);

  const reloadChildren = useCallback(async () => {
    if (!isParent) return [];
    const list = await api.children();
    setChildren(list);
    setSelectedChildId((current) => {
      if (current && list.some((c) => c.id === current)) return current;
      return list.length ? list[0].id : null;
    });
    return list;
  }, [isParent]);

  const reloadData = useCallback(async (childId) => {
    if (!childId) {
      setSubjects([]);
      setReport(null);
      setStats(null);
      return;
    }
    const [s, r, cfg, st] = await Promise.all([
      api.subjects(),
      api.weekReport(weekDate, childId),
      api.settings(),
      api.allStats(childId),
    ]);
    setSubjects(s);
    setReport(r);
    setSettings(cfg);
    setStats(st);
  }, [weekDate]);

  useEffect(() => {
    (async () => {
      if (getToken()) {
        try {
          const me = await api.me();
          setUser(me);
        } catch {
          setToken(null);
        }
      }
      setBooting(false);
    })();
  }, []);

  useEffect(() => {
    if (!user) return;
    (async () => {
      try {
        setError('');
        if (isParent) await reloadChildren();
        await reloadData(isParent ? selectedChildId : user.id);
      } catch (err) {
        setError(err.message);
      }
    })();
  }, [user, isParent, selectedChildId, reloadChildren, reloadData]);

  async function reload() {
    try {
      setError('');
      if (isParent) await reloadChildren();
      await reloadData(activeChildId);
    } catch (err) {
      setError(err.message);
    }
  }

  function logout() {
    setToken(null);
    setUser(null);
    setChildren([]);
    setSelectedChildId(null);
    setSubjects([]);
    setReport(null);
    setStats(null);
    setSettings(null);
  }

  if (booting) {
    return <div className="auth-wrap"><p className="muted">Загрузка…</p></div>;
  }

  if (!user) {
    return <AuthScreen onAuthenticated={setUser} />;
  }

  const totalClass = report && report.total < 0 ? 'neg' : 'pos';
  const noChild = isParent && !activeChildId;
  const activeChild = isParent ? children.find((c) => c.id === activeChildId) : null;
  const activeChildName = isParent
    ? activeChild
      ? activeChild.displayName || activeChild.username
      : null
    : user.displayName || user.username;

  return (
    <div className="app">
      <header>
        <div>
          <h1>Поощрение школьника</h1>
          <p className="muted">
            {user.displayName || user.username} · {isParent ? 'родитель' : 'ученик'}
          </p>
        </div>
        <div className="header-right">
          {isParent && children.length > 0 && (
            <label className="child-select">
              Ребёнок
              <select value={selectedChildId ?? ''} onChange={(e) => setSelectedChildId(Number(e.target.value))}>
                {children.map((c) => (
                  <option key={c.id} value={c.id}>{c.displayName || c.username}</option>
                ))}
              </select>
            </label>
          )}
          {report && !noChild && (
            <div className="total">
              <span className="muted">Итого за неделю</span>
              <strong className={totalClass}>{money(report.total)}</strong>
              <div className="week-nav">
                <button className="link" onClick={() => setWeekOffset((w) => w - 1)}>&larr; Пред.</button>
                {weekOffset !== 0 && (
                  <button className="link" onClick={() => setWeekOffset(0)}>Текущая</button>
                )}
                <button className="link" onClick={() => setWeekOffset((w) => Math.min(w + 1, 0))}>След. &rarr;</button>
              </div>
            </div>
          )}
          <button className="link logout" onClick={logout}>Выйти</button>
        </div>
      </header>

      {error && <p className="error box">{error}</p>}

      <nav className="tabs">
        <button className={tab === 'grades' ? 'active' : ''} onClick={() => setTab('grades')}>Оценки</button>
        <button className={tab === 'stats' ? 'active' : ''} onClick={() => setTab('stats')}>Статистика</button>
        {isParent && (
          <button className={tab === 'children' ? 'active' : ''} onClick={() => setTab('children')}>Дети</button>
        )}
        {isParent && (
          <button className={tab === 'subjects' ? 'active' : ''} onClick={() => setTab('subjects')}>Предметы</button>
        )}
        {isParent && (
          <button className={tab === 'settings' ? 'active' : ''} onClick={() => setTab('settings')}>Настройки</button>
        )}
        <button className={tab === 'profile' ? 'active' : ''} onClick={() => setTab('profile')}>Профиль</button>
      </nav>

      {tab === 'grades' && noChild && (
        <section className="card">
          <p className="muted">Сначала создайте аккаунт ребёнка в разделе «Дети».</p>
        </section>
      )}
      {tab === 'grades' && report && !noChild && (
        <GradesTab
          subjects={subjects}
          report={report}
          reload={reload}
          childId={isParent ? selectedChildId : undefined}
          childName={activeChildName}
        />
      )}
      {tab === 'stats' && <StatsTab stats={stats} childName={activeChildName} />}
      {tab === 'children' && isParent && <ChildrenTab children={children} reload={reload} />}
      {tab === 'subjects' && isParent && <SubjectsTab subjects={subjects} reload={reload} />}
      {tab === 'settings' && isParent && settings && <SettingsTab settings={settings} reload={reload} />}
      {tab === 'profile' && <ProfileTab user={user} />}
    </div>
  );
}