import { useEffect, useMemo, useState } from 'react';
import { ArcElement, Chart as ChartJS, Legend, Tooltip } from 'chart.js';
import { Doughnut } from 'react-chartjs-2';
import { Link, NavLink, Navigate, Route, Routes, useNavigate } from 'react-router-dom';
import { dashboardStats as defaultDashboardStats, featuredSurvey as defaultFeaturedSurvey, historyItems as defaultHistoryItems, participationChart as defaultParticipationChart, resultsDataset as defaultResultsDataset, surveyQuestion as defaultSurveyQuestion } from './data/mockData';
import { submitAdminSurvey, submitLogin, submitLogout, submitRegistration, submitSurvey, submitUserSave, submitVoteCode, submitVote, fetchDashboardStats, fetchParticipationChart, fetchCurrentSurvey, fetchSurveyHistory, fetchSurveyQuestion, fetchResultsDataset, fetchResidentPadron, updateResidentHousing, blockResident, fetchSurveyVotes, fetchParticipationSummary, updateSurveyStatus, exportSurveyResults } from './lib/api';

ChartJS.register(ArcElement, Tooltip, Legend);

const dashboardColors = ['#2f6fed', '#0f766e'];
const resultColors = ['#2f6fed', '#0f766e', '#d97706', '#475569'];

function Topbar({ compact = false }) {
  const navigate = useNavigate();

  const handleLogout = async () => {
    try {
      await submitLogout();
    } finally {
      localStorage.removeItem('residentCode');
      localStorage.removeItem('opcionSeleccionada');
      navigate('/');
    }
  };

  return (
    <header className={compact ? 'topbar topbar--compact' : 'topbar'}>
      <div className="topbar__brand">
        <div className="brand-mark">A</div>
        <div>
          <div className="brand-name">APPSEMBLY</div>
          <div className="brand-tag">Encuestas para conjuntos cerrados</div>
        </div>
      </div>

      {!compact ? (
        <nav className="topbar__nav">
          {[
            ['/inicio', 'Inicio'],
            ['/adminpanel', 'Panel admin'],
            ['/admin/dashboard', 'Dashboard'],
            ['/pregunta', 'Pregunta'],
            ['/resultados', 'Resultados'],
            ['/historial', 'Historial']
          ].map(([to, label]) => (
            <NavLink key={to} to={to} className={({ isActive }) => `topbar__link${isActive ? ' is-active' : ''}`}>
              {label}
            </NavLink>
          ))}
        </nav>
      ) : (
        <div className="topbar__compact-copy">Acceso seguro y administración centralizada</div>
      )}

      <div className="topbar__actions">
        {!compact ? (
          <button type="button" className="button button--ghost" onClick={handleLogout}>
            Cerrar sesión
          </button>
        ) : (
          <Link to="/register" className="button button--ghost button--link">
            Crear cuenta
          </Link>
        )}
      </div>
    </header>
  );
}

function PageHero({ eyebrow, title, subtitle, rightNote }) {
  return (
    <section className="page-hero page-hero--split">
      <div>
        <p className="eyebrow">{eyebrow}</p>
        <h1>{title}</h1>
        <p>{subtitle}</p>
      </div>
      {rightNote ? (
        <div className="hero-note">
          <strong>{rightNote.title}</strong>
          <span>{rightNote.body}</span>
        </div>
      ) : null}
    </section>
  );
}

function MetricCard({ label, value, detail }) {
  return (
    <article className="metric-card">
      <span>{label}</span>
      <strong>{value}</strong>
      <small>{detail}</small>
    </article>
  );
}

function Shell({ compact = false, children }) {
  return (
    <div className="app-scene">
      <Topbar compact={compact} />
      {children}
    </div>
  );
}

function downloadTextFile(fileName, text, mimeType = 'text/plain') {
  const blob = new Blob([text], { type: mimeType });
  const url = URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href = url;
  link.download = fileName;
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
  URL.revokeObjectURL(url);
}

function LoginPage() {
  const navigate = useNavigate();
  const [form, setForm] = useState({ email: '', password: '' });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleChange = (event) => {
    const { name, value } = event.target;
    setForm((current) => ({ ...current, [name]: value }));
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    setLoading(true);
    setError('');

    try {
      const result = await submitLogin(form.email, form.password);
      if (result.success) {
        navigate(result.redirectTo || '/inicio');
        return;
      }

      setError(result.message || 'No fue posible iniciar sesión. Revisa tus credenciales.');
    } catch {
      setError('Error de red al intentar iniciar sesión.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Shell compact>
      <main className="page-shell auth-layout">
        <section className="auth-copy card card--soft card--hero">
          <p className="eyebrow">Conjuntos cerrados</p>
          <h1>Decisiones de comunidad con una interfaz más clara.</h1>
          <p>
            Centraliza encuestas, votaciones y seguimiento de resultados con una experiencia consistente para vecinos y administradores.
          </p>
          <div className="auth-pills">
            <span>Seguridad por sesión</span>
            <span>Gráficas integradas</span>
            <span>Flujo unificado</span>
          </div>
        </section>

        <section className="card auth-card">
          <div className="card__header">
            <p className="eyebrow">Acceso</p>
            <h2>Inicia sesión</h2>
            <p>Accede al panel y a las votaciones activas de tu conjunto.</p>
          </div>

          <form className="stack" onSubmit={handleSubmit}>
            {error ? <div className="alert-box alert-box--danger">{error}</div> : null}
            <label className="field">
              <span>Correo electrónico</span>
              <input type="email" name="email" value={form.email} onChange={handleChange} placeholder="nombre@correo.com" required autoComplete="username" />
            </label>
            <label className="field">
              <span>Contraseña</span>
              <input type="password" name="password" value={form.password} onChange={handleChange} placeholder="Ingresa tu contraseña" required minLength={8} autoComplete="current-password" />
            </label>
            <button className="button button--primary" type="submit" disabled={loading}>
              {loading ? 'Ingresando...' : 'Ingresar'}
            </button>
          </form>

          <div className="card__footer center-text">
            <p>
              ¿No tienes cuenta? <Link to="/register">Regístrate aquí</Link>
            </p>
          </div>
        </section>
      </main>
    </Shell>
  );
}

function RegisterPage() {
  const navigate = useNavigate();
  const [form, setForm] = useState({ firstName: '', lastName: '', email: '', password: '', passwordConfirm: '', role: 'USER' });
  const [message, setMessage] = useState({ type: '', text: '' });
  const [loading, setLoading] = useState(false);

  const handleChange = (event) => {
    const { name, value } = event.target;
    setForm((current) => ({ ...current, [name]: value }));
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    setLoading(true);
    setMessage({ type: '', text: '' });

    if (form.password !== form.passwordConfirm) {
      setMessage({ type: 'error', text: 'Las contraseñas deben coincidir.' });
      setLoading(false);
      return;
    }

    try {
      const result = await submitRegistration(form);
      if (result.success) {
        setMessage({ type: 'success', text: 'Usuario registrado correctamente.' });
        setTimeout(() => navigate('/'), 1100);
        return;
      }

      setMessage({ type: 'error', text: result.message || 'No fue posible registrar la cuenta.' });
    } catch {
      setMessage({ type: 'error', text: 'Error de red al intentar registrar la cuenta.' });
    } finally {
      setLoading(false);
    }
  };

  return (
    <Shell compact>
      <main className="page-shell auth-layout auth-layout--wide">
        <section className="card auth-card auth-card--side">
          <div className="card__header">
            <p className="eyebrow">Registro</p>
            <h2>Crear una cuenta</h2>
            <p>Alta de usuario con validaciones visuales y una interfaz más limpia.</p>
          </div>

          <form className="stack" onSubmit={handleSubmit}>
            {message.text ? <div className={message.type === 'success' ? 'alert-box alert-box--success' : 'alert-box alert-box--danger'}>{message.text}</div> : null}
            <div className="grid grid--2">
              <label className="field">
                <span>Nombre</span>
                <input name="firstName" value={form.firstName} onChange={handleChange} required />
              </label>
              <label className="field">
                <span>Apellido</span>
                <input name="lastName" value={form.lastName} onChange={handleChange} required />
              </label>
            </div>
            <label className="field">
              <span>Correo electrónico</span>
              <input type="email" name="email" value={form.email} onChange={handleChange} required />
            </label>
            <div className="grid grid--2">
              <label className="field">
                <span>Contraseña</span>
                <input type="password" name="password" value={form.password} onChange={handleChange} minLength={8} required />
              </label>
              <label className="field">
                <span>Confirmar contraseña</span>
                <input type="password" name="passwordConfirm" value={form.passwordConfirm} onChange={handleChange} minLength={8} required />
              </label>
            </div>
            <label className="field">
              <span>Tipo de cuenta</span>
              <select name="role" value={form.role} onChange={handleChange} required>
                <option value="USER">Usuario estándar</option>
                <option value="ADMIN">Administrador</option>
              </select>
            </label>
            <button className="button button--primary" type="submit" disabled={loading}>
              {loading ? 'Registrando...' : 'Registrarse'}
            </button>
          </form>

          <div className="card__footer center-text">
            <p>
              ¿Ya tienes una cuenta? <Link to="/">Inicia sesión aquí</Link>
            </p>
          </div>
        </section>

        <section className="card card--soft card--hero auth-copy">
          <p className="eyebrow">Membresía</p>
          <h1>Acceso ordenado para vecinos, administración y votaciones.</h1>
          <p>
            Conserva el flujo de usuario, pero con una jerarquía visual más clara, campos más amplios y una lectura más amable en escritorio y móvil.
          </p>
          <div className="auth-pills">
            <span>Campos obligatorios</span>
            <span>Acción clara</span>
            <span>Estilo consistente</span>
          </div>
        </section>
      </main>
    </Shell>
  );
}

function InicioPage() {
  const navigate = useNavigate();
  const [feedback, setFeedback] = useState({ type: '', text: '' });
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (event) => {
    event.preventDefault();
    setLoading(true);
    setFeedback({ type: '', text: '' });
    const formData = new FormData(event.currentTarget);
    const codigo = String(formData.get('codigo') || '').trim();

    const result = await submitVoteCode(codigo);
    if (result.success) {
      localStorage.setItem('residentCode', result.residentCode || codigo);
      navigate(result.redirectTo || '/pregunta');
      setLoading(false);
      return;
    }

    setFeedback({ type: 'error', text: result.message || 'No fue posible validar el código.' });
    setLoading(false);
  };

  return (
    <Shell>
      <main className="page-shell">
        <PageHero
          eyebrow="Encuestas activas"
          title="Ingresa el código y entra directo a tu votación."
          subtitle="La experiencia de acceso ahora está más limpia, con una jerarquía visual más clara y una superficie enfocada para la acción principal."
          rightNote={{ title: 'Acceso rápido', body: 'Usa el código entregado por administración y continúa al cuestionario activo.' }}
        />

        <section className="grid grid--2">
          <article className="card form-card">
            <h2>Ingresar a encuesta</h2>
            <p className="panel__subtitle">La pantalla está centrada en una sola acción para reducir fricción.</p>
            <form className="stack" onSubmit={handleSubmit}>
              {feedback.text ? <div className={feedback.type === 'error' ? 'alert-box alert-box--danger' : 'alert-box alert-box--success'}>{feedback.text}</div> : null}
              <label className="field">
                <span>Código de encuesta</span>
                <input type="text" name="codigo" placeholder="Ingrese su código aquí" required />
              </label>
              <button className="button button--primary" type="submit" disabled={loading}>{loading ? 'Validando...' : 'Ingresar'}</button>
            </form>
          </article>

          <div className="stack">
            <article className="card stat-spotlight">
              <p className="eyebrow">Participación</p>
              <strong>92%</strong>
              <span>Promedio de respuesta en votaciones activas.</span>
            </article>
            <article className="card stat-spotlight stat-spotlight--alt">
              <p className="eyebrow">Soporte</p>
              <strong>Contacto</strong>
              <span>contacto@soporte.com · 01800012345</span>
            </article>
          </div>
        </section>
      </main>
    </Shell>
  );
}

function AdminPanelPage() {
  return (
    <Shell>
      <main className="page-shell">
        <PageHero
          eyebrow="Administración"
          title="Panel con atajos y control de usuarios."
          subtitle="La pantalla se reorganizó para que las acciones clave queden visibles sin saturar la vista."
          rightNote={{ title: 'Lectura rápida', body: 'Más aire, menos ruido visual y bloques de acción más fáciles de escanear.' }}
        />

        <section className="grid grid--3">
          <article className="card action-card action-card--primary">
            <p className="eyebrow">Encuestas</p>
            <h3>Crear votación</h3>
            <p>Define pregunta, opciones y vencimiento desde una superficie más limpia.</p>
            <Link className="button button--light" to="/admin/dashboard">Abrir dashboard</Link>
          </article>
          <article className="card action-card">
            <p className="eyebrow">Usuarios</p>
            <h3>Gestionar residentes</h3>
            <p>Altas, bajas y permisos con el panel consolidado y validado.</p>
            <Link className="button button--light" to="/user/save">Crear usuario</Link>
          </article>
          <article className="card action-card">
            <p className="eyebrow">Resultados</p>
            <h3>Leer participación</h3>
            <p>Gráficas, historiales y resumen visual para lectura rápida.</p>
            <Link className="button button--light" to="/admin/management">Gestión avanzada</Link>
          </article>
        </section>
      </main>
    </Shell>
  );
}

function AdminSurveyPage() {
  const navigate = useNavigate();
  const [survey, setSurvey] = useState({
    title: '',
    question: '',
    responses: ['', '', '', ''],
    expirationDate: '',
    audienceMode: 'ALL',
    audienceBlocks: '',
    audienceTowers: '',
    votePrivacy: 'ANONYMOUS',
    status: 'OPEN',
    actor: 'admin'
  });
  const [message, setMessage] = useState({ type: '', text: '' });
  const [loading, setLoading] = useState(false);

  const updateField = (event) => {
    const { name, value } = event.target;
    setSurvey((current) => ({ ...current, [name]: value }));
  };

  const updateResponse = (index, value) => {
    setSurvey((current) => {
      const responses = [...current.responses];
      responses[index] = value;
      return { ...current, responses };
    });
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    setLoading(true);
    setMessage({ type: '', text: '' });

    const validResponses = survey.responses.map((entry) => entry.trim()).filter(Boolean);
    if (validResponses.length < 2) {
      setMessage({ type: 'error', text: 'Debes agregar al menos dos respuestas distintas.' });
      setLoading(false);
      return;
    }

    const result = await submitAdminSurvey({
      title: survey.title,
      question: survey.question,
      expirationDate: survey.expirationDate,
      respuestas: validResponses,
      audienceMode: survey.audienceMode,
      audienceBlocks: survey.audienceBlocks,
      audienceTowers: survey.audienceTowers,
      votePrivacy: survey.votePrivacy,
      status: survey.status,
      actor: survey.actor
    });

    if (result.success) {
      setMessage({ type: 'success', text: 'Encuesta creada correctamente.' });
      setTimeout(() => navigate('/adminpanel'), 900);
      setLoading(false);
      return;
    }

    setMessage({ type: 'error', text: result.message || 'No fue posible guardar la encuesta.' });
    setLoading(false);
  };

  return (
    <Shell>
      <main className="page-shell">
        <PageHero
          eyebrow="Admin / encuesta"
          title="Vista dedicada para crear votaciones desde admin."
          subtitle="Mantiene la lógica del proyecto original, pero con una superficie React más clara y editable."
          rightNote={{ title: 'Compatibilidad', body: 'Envía a /admin/survey/save para conservar el flujo del backend.' }}
        />

        <section className="dashboard-layout">
          <article className="card form-card form-card--accent">
            <div className="card__header">
              <p className="eyebrow">Nueva encuesta</p>
              <h2>Crear votación</h2>
              <p>Formulario portado desde la vista antigua de administración.</p>
            </div>

            <form className="stack" onSubmit={handleSubmit}>
              {message.text ? <div className={message.type === 'success' ? 'alert-box alert-box--success' : 'alert-box alert-box--danger'}>{message.text}</div> : null}
              <label className="field">
                <span>Título</span>
                <input name="title" value={survey.title} onChange={updateField} required />
              </label>
              <label className="field">
                <span>Pregunta</span>
                <input name="question" value={survey.question} onChange={updateField} required />
              </label>
              <div className="stack stack--tight">
                <span className="field__label">Respuestas</span>
                {survey.responses.map((responseText, index) => (
                  <label className="field" key={index}>
                    <input
                      value={responseText}
                      onChange={(event) => updateResponse(index, event.target.value)}
                      placeholder={`Respuesta ${index + 1}`}
                      required
                    />
                  </label>
                ))}
              </div>
              <label className="field">
                <span>Fecha de expiración</span>
                <input type="date" name="expirationDate" value={survey.expirationDate} onChange={updateField} required />
              </label>
              <div className="grid grid--2">
                <label className="field">
                  <span>Alcance</span>
                  <select name="audienceMode" value={survey.audienceMode} onChange={updateField}>
                    <option value="ALL">Todo el conjunto</option>
                    <option value="BLOCK">Solo bloque</option>
                    <option value="TOWER">Solo torre</option>
                    <option value="BLOCK_AND_TOWER">Bloque o torre</option>
                  </select>
                </label>
                <label className="field">
                  <span>Privacidad</span>
                  <select name="votePrivacy" value={survey.votePrivacy} onChange={updateField}>
                    <option value="ANONYMOUS">Anónima</option>
                    <option value="PUBLIC">Pública</option>
                  </select>
                </label>
              </div>
              <div className="grid grid--2">
                <label className="field">
                  <span>Bloques permitidos</span>
                  <input name="audienceBlocks" value={survey.audienceBlocks} onChange={updateField} placeholder="Bloque A, Bloque B" />
                </label>
                <label className="field">
                  <span>Torres permitidas</span>
                  <input name="audienceTowers" value={survey.audienceTowers} onChange={updateField} placeholder="Torre 1, Torre 2" />
                </label>
              </div>
              <div className="grid grid--2">
                <label className="field">
                  <span>Estado inicial</span>
                  <select name="status" value={survey.status} onChange={updateField}>
                    <option value="OPEN">Activa</option>
                    <option value="DRAFT">Borrador</option>
                    <option value="CLOSED">Cerrada</option>
                    <option value="ARCHIVED">Archivada</option>
                  </select>
                </label>
                <label className="field">
                  <span>Actor</span>
                  <input name="actor" value={survey.actor} onChange={updateField} placeholder="admin" />
                </label>
              </div>
              <div className="button-row">
                <button className="button button--primary" type="submit" disabled={loading}>{loading ? 'Guardando...' : 'Guardar encuesta'}</button>
                <Link className="button button--ghost" to="/adminpanel">Volver</Link>
              </div>
            </form>
          </article>

          <aside className="stack">
            <article className="card stat-spotlight">
              <p className="eyebrow">Flujo admin</p>
              <strong>Directo</strong>
              <span>Esta vista replica el alta de encuesta del panel original.</span>
            </article>
            <article className="card stat-spotlight stat-spotlight--alt">
              <p className="eyebrow">Compatibilidad</p>
              <strong>React</strong>
              <span>Sirve como reemplazo de la antigua página /admin/prueba.</span>
            </article>
          </aside>
        </section>
      </main>
    </Shell>
  );
}

function AdminManagementPage() {
  const [padron, setPadron] = useState([]);
  const [surveyHistory, setSurveyHistory] = useState([]);
  const [selectedSurveyId, setSelectedSurveyId] = useState('');
  const [surveyVotes, setSurveyVotes] = useState([]);
  const [participation, setParticipation] = useState([]);
  const [dimension, setDimension] = useState('block');
  const [loading, setLoading] = useState(true);
  const [message, setMessage] = useState('');
  const [padronQuery, setPadronQuery] = useState('');
  const [surveyQuery, setSurveyQuery] = useState('');
  const [surveyStatusFilter, setSurveyStatusFilter] = useState('');
  const [housingForm, setHousingForm] = useState({ userId: '', blockName: '', towerName: '', unitNumber: '' });
  const [statusForm, setStatusForm] = useState({ surveyId: '', status: 'CLOSED', actor: 'admin' });

  useEffect(() => {
    const loadData = async () => {
      try {
        setLoading(true);
        const [padronData, historyData, participationData] = await Promise.all([
          fetchResidentPadron(padronQuery),
          fetchSurveyHistory(surveyQuery, surveyStatusFilter),
          fetchParticipationSummary(dimension)
        ]);
        setPadron(padronData || []);
        setSurveyHistory(historyData || []);
        setParticipation(participationData || []);
        if (historyData && historyData.length > 0 && !selectedSurveyId) {
          setSelectedSurveyId(String(historyData[0].surveyId || ''));
        }
      } catch (error) {
        console.error('Error loading admin management data:', error);
      } finally {
        setLoading(false);
      }
    };

    loadData();
  }, [dimension, padronQuery, surveyQuery, surveyStatusFilter, selectedSurveyId]);

  useEffect(() => {
    const loadVotes = async () => {
      if (!selectedSurveyId) {
        setSurveyVotes([]);
        return;
      }
      const votes = await fetchSurveyVotes(selectedSurveyId);
      setSurveyVotes(votes || []);
    };

    loadVotes();
  }, [selectedSurveyId]);

  const handleHousingSubmit = async (event) => {
    event.preventDefault();
    setMessage('');
    const result = await updateResidentHousing(housingForm.userId, housingForm.blockName, housingForm.towerName, housingForm.unitNumber);
    setMessage(result.success ? 'Vivienda actualizada correctamente.' : result.message || 'No fue posible actualizar la vivienda.');
  };

  const handleBlockToggle = async (userId, blocked) => {
    const result = await blockResident(userId, blocked);
    setMessage(result.success ? (blocked ? 'Residente bloqueado.' : 'Residente desbloqueado.') : result.message || 'No fue posible cambiar el bloqueo.');
    if (result.success) {
      setPadron((current) => current.map((entry) => String(entry.id) === String(userId) ? { ...entry, blocked } : entry));
    }
  };

  const handleExport = async () => {
    if (!selectedSurveyId) {
      setMessage('Selecciona una encuesta para exportar.');
      return;
    }

    const result = await exportSurveyResults(selectedSurveyId);
    if (result.success) {
      downloadTextFile(`survey-${selectedSurveyId}-results.csv`, result.csv, 'text/csv;charset=utf-8;');
      setMessage('Exportación generada correctamente.');
      return;
    }

    setMessage(result.message || 'No fue posible exportar los resultados.');
  };

  const handleStatusSubmit = async (event) => {
    event.preventDefault();
    const result = await updateSurveyStatus(statusForm.surveyId, statusForm.status, statusForm.actor);
    setMessage(result.success ? 'Estado de encuesta actualizado.' : result.message || 'No fue posible actualizar el estado.');
  };

  const handleSelectedSurveyChange = (event) => {
    const surveyId = event.target.value;
    setSelectedSurveyId(surveyId);
    setStatusForm((current) => ({ ...current, surveyId }));
  };

  const handlePadronSearchSubmit = (event) => {
    event.preventDefault();
    setPadronQuery((current) => current.trim());
  };

  const handleSurveySearchSubmit = (event) => {
    event.preventDefault();
    setSurveyQuery((current) => current.trim());
  };

  return (
    <Shell>
      <main className="page-shell">
        <PageHero
          eyebrow="Gestión avanzada"
          title="Padrón, alcance de encuesta y exportación en una sola vista."
          subtitle="Aquí se concentran los endpoints nuevos del backend para corregir viviendas, bloquear residentes, cambiar estados y extraer reportes."
          rightNote={{ title: 'Conectado', body: 'Consume /admin/padron, /admin/participation, /admin/surveys/{id} y /admin/surveys/{id}/export.' }}
        />

        {message ? <div className="alert-box alert-box--success" style={{ marginBottom: '1rem' }}>{message}</div> : null}

        <section className="dashboard-layout">
          <article className="card form-card form-card--accent">
            <div className="card__header">
              <p className="eyebrow">Padrón</p>
              <h2>Residentes y vivienda</h2>
              <p>Corrige bloque, torre y unidad desde el mismo panel.</p>
            </div>

            <form className="stack admin-toolbar" onSubmit={handlePadronSearchSubmit}>
              <label className="field">
                <span>Buscar residente</span>
                <input value={padronQuery} onChange={(event) => setPadronQuery(event.target.value)} placeholder="Nombre, correo, bloque o torre" />
              </label>
              <button className="button button--ghost" type="submit">Buscar padrón</button>
            </form>

            <form className="stack" onSubmit={handleHousingSubmit}>
              <div className="grid grid--2">
                <label className="field">
                  <span>ID usuario</span>
                  <input value={housingForm.userId} onChange={(event) => setHousingForm((current) => ({ ...current, userId: event.target.value }))} required />
                </label>
                <label className="field">
                  <span>Bloque</span>
                  <input value={housingForm.blockName} onChange={(event) => setHousingForm((current) => ({ ...current, blockName: event.target.value }))} />
                </label>
              </div>
              <div className="grid grid--2">
                <label className="field">
                  <span>Torre</span>
                  <input value={housingForm.towerName} onChange={(event) => setHousingForm((current) => ({ ...current, towerName: event.target.value }))} />
                </label>
                <label className="field">
                  <span>Unidad</span>
                  <input value={housingForm.unitNumber} onChange={(event) => setHousingForm((current) => ({ ...current, unitNumber: event.target.value }))} />
                </label>
              </div>
              <button className="button button--primary" type="submit">Actualizar vivienda</button>
            </form>
          </article>

          <article className="card form-card">
            <div className="card__header">
              <p className="eyebrow">Encuestas</p>
              <h2>Estado y exportación</h2>
              <p>Selecciona una votación para cerrar, archivar o exportar resultados.</p>
            </div>

            <form className="stack admin-toolbar" onSubmit={handleSurveySearchSubmit}>
              <div className="grid grid--2">
                <label className="field">
                  <span>Buscar encuesta</span>
                  <input value={surveyQuery} onChange={(event) => setSurveyQuery(event.target.value)} placeholder="Título o pregunta" />
                </label>
                <label className="field">
                  <span>Filtrar por estado</span>
                  <select value={surveyStatusFilter} onChange={(event) => setSurveyStatusFilter(event.target.value)}>
                    <option value="">Todos</option>
                    <option value="DRAFT">Borrador</option>
                    <option value="OPEN">Activa</option>
                    <option value="CLOSED">Cerrada</option>
                    <option value="ARCHIVED">Archivada</option>
                  </select>
                </label>
              </div>
              <button className="button button--ghost" type="submit">Aplicar filtros</button>
            </form>

            <div className="stack">
              <label className="field">
                <span>Encuesta</span>
                <select value={selectedSurveyId} onChange={handleSelectedSurveyChange}>
                  <option value="">Selecciona una encuesta</option>
                  {surveyHistory.map((item) => (
                    <option key={item.surveyId} value={item.surveyId}>{item.title} · {item.status}</option>
                  ))}
                </select>
              </label>

              <form className="stack" onSubmit={handleStatusSubmit}>
                <div className="grid grid--2">
                  <label className="field">
                    <span>Estado</span>
                    <select value={statusForm.status} onChange={(event) => setStatusForm((current) => ({ ...current, status: event.target.value }))}>
                      <option value="DRAFT">Borrador</option>
                      <option value="OPEN">Activa</option>
                      <option value="CLOSED">Cerrada</option>
                      <option value="ARCHIVED">Archivada</option>
                    </select>
                  </label>
                  <label className="field">
                    <span>Actor</span>
                    <input value={statusForm.actor} onChange={(event) => setStatusForm((current) => ({ ...current, actor: event.target.value }))} />
                  </label>
                </div>
                <button className="button button--primary" type="submit" disabled={!statusForm.surveyId}>Actualizar estado</button>
              </form>

              <button className="button button--ghost" type="button" onClick={handleExport} disabled={!selectedSurveyId}>Exportar CSV</button>
            </div>
          </article>

          <aside className="stack">
            <article className="card stat-spotlight">
              <p className="eyebrow">Participación</p>
              <strong>{participation.length}</strong>
              <span>Grupos listados según bloque, torre o unidad.</span>
            </article>
            <article className="card stat-spotlight stat-spotlight--alt">
              <p className="eyebrow">Encuesta seleccionada</p>
              <strong>{surveyVotes.length}</strong>
              <span>Votos visibles según privacidad pública o anónima.</span>
            </article>
          </aside>
        </section>

        <section className="dashboard-layout">
          <article className="card results-list">
            <div className="card__header">
              <p className="eyebrow">Padrón</p>
              <h2>Residentes registrados</h2>
            </div>
            <div className="history-list">
              {loading ? <p>Cargando padrón...</p> : padron.map((resident) => (
                <div key={resident.id} className="history-item">
                  <div>
                    <p className="eyebrow">{resident.blockName || 'Sin bloque'} · {resident.towerName || 'Sin torre'}</p>
                    <h3>{resident.firstName} {resident.lastName}</h3>
                    <p>Unidad {resident.unitNumber || 'N/A'} · Código {resident.personalCode || 'N/A'}</p>
                  </div>
                  <div className="history-item__meta">
                    <strong>{resident.blocked ? 'Bloqueado' : 'Activo'}</strong>
                    <button className="button button--ghost button--small" type="button" onClick={() => handleBlockToggle(resident.id, !resident.blocked)}>
                      {resident.blocked ? 'Desbloquear' : 'Bloquear'}
                    </button>
                  </div>
                </div>
              ))}
            </div>
          </article>

          <article className="card results-list">
            <div className="card__header">
              <p className="eyebrow">Votos</p>
              <h2>Detalle de la encuesta</h2>
            </div>
            <div className="history-list">
              {surveyVotes.length === 0 ? <p>Selecciona una encuesta para ver sus votos.</p> : surveyVotes.map((vote, index) => (
                <div key={`${vote.voterCode}-${index}`} className="history-item">
                  <div>
                    <p className="eyebrow">{vote.voterBlockName || 'Anónimo'} · {vote.voterTowerName || 'Anónimo'}</p>
                    <h3>{vote.voterFirstName ? `${vote.voterFirstName} ${vote.voterLastName || ''}` : vote.voterCode}</h3>
                    <p>{vote.selectedOption} · {vote.voterUnitNumber || 'Sin unidad'}</p>
                  </div>
                  <div className="history-item__meta">
                    <strong>{vote.createdAt ? String(vote.createdAt).slice(0, 10) : ''}</strong>
                  </div>
                </div>
              ))}
            </div>
          </article>
        </section>

        <section className="card results-list">
          <div className="card__header">
            <p className="eyebrow">Participación</p>
            <h2>Resumen por {dimension === 'tower' ? 'torre' : dimension === 'unit' ? 'unidad' : 'bloque'}</h2>
          </div>
          <div className="button-row">
            <button className="button button--ghost" type="button" onClick={() => setDimension('block')}>Bloques</button>
            <button className="button button--ghost" type="button" onClick={() => setDimension('tower')}>Torres</button>
            <button className="button button--ghost" type="button" onClick={() => setDimension('unit')}>Unidades</button>
          </div>
          <div className="results-bars">
            {participation.map((item) => (
              <div key={`${item.dimension}-${item.label}`} className="result-row">
                <div className="result-row__head">
                  <span>{item.label}</span>
                  <strong>{item.percentage}%</strong>
                </div>
                <div className="progress-bar">
                  <span style={{ width: `${Math.min(item.percentage, 100)}%` }} />
                </div>
                <small>{item.voters} votos / {item.residents} residentes</small>
              </div>
            ))}
          </div>
        </section>
      </main>
    </Shell>
  );
}

function UserSavePage() {
  const navigate = useNavigate();
  const [form, setForm] = useState({ email: '', password: '', role: 'USER' });
  const [message, setMessage] = useState('');

  const handleChange = (event) => {
    const { name, value } = event.target;
    setForm((current) => ({ ...current, [name]: value }));
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    setMessage('');

    const result = await submitUserSave(form);

    if (result.success) {
      setMessage('Usuario creado correctamente.');
      setTimeout(() => navigate('/adminpanel'), 900);
      return;
    }

    setMessage('No fue posible crear el usuario.');
  };

  return (
    <Shell>
      <main className="page-shell auth-layout auth-layout--wide">
        <section className="card auth-card">
          <div className="card__header">
            <p className="eyebrow">Usuarios</p>
            <h2>Alta de usuario</h2>
            <p>Reemplazo React para la vista /user/save del front anterior.</p>
          </div>

          <form className="stack" onSubmit={handleSubmit}>
            {message ? <div className="alert-box alert-box--success">{message}</div> : null}
            <label className="field">
              <span>Correo electrónico</span>
              <input type="email" name="email" value={form.email} onChange={handleChange} required />
            </label>
            <label className="field">
              <span>Contraseña</span>
              <input type="password" name="password" value={form.password} onChange={handleChange} minLength={8} required />
            </label>
            <label className="field">
              <span>Rol</span>
              <select name="role" value={form.role} onChange={handleChange} required>
                <option value="USER">Usuario</option>
                <option value="ADMIN">Administrador</option>
              </select>
            </label>
            <div className="button-row">
              <button className="button button--primary" type="submit">Crear usuario</button>
              <Link className="button button--ghost" to="/adminpanel">Volver</Link>
            </div>
          </form>
        </section>

        <section className="card card--soft card--hero auth-copy">
          <p className="eyebrow">Cuentas</p>
          <h1>Gestión de usuarios con la misma línea visual.</h1>
          <p>
            El alta quedó integrada en React para evitar volver a la vista Thymeleaf antigua y mantener todo dentro del mismo lenguaje visual.
          </p>
          <div className="auth-pills">
            <span>Campos requeridos</span>
            <span>Salida consistente</span>
            <span>Flujo admin unificado</span>
          </div>
        </section>
      </main>
    </Shell>
  );
}

function UserIndexPage() {
  return (
    <Shell>
      <main className="page-shell">
        <PageHero
          eyebrow="Usuario"
          title="Espacio central para acceso y seguimiento."
          subtitle="Una vista de entrada para residentes, con acceso a votaciones, historial y resultados sin salir del lenguaje visual React."
          rightNote={{ title: 'Navegación', body: 'Pensado como reemplazo de la antigua vista /user/index.' }}
        />

        <section className="grid grid--3">
          <article className="card action-card action-card--primary">
            <p className="eyebrow">Votar</p>
            <h3>Encuesta activa</h3>
            <p>Entra al flujo de votación y confirma tu elección.</p>
            <Link className="button button--light" to="/inicio">Ir al acceso</Link>
          </article>
          <article className="card action-card">
            <p className="eyebrow">Historial</p>
            <h3>Revisar actividad</h3>
            <p>Consulta votaciones pasadas y programadas.</p>
            <Link className="button button--light" to="/historial">Abrir historial</Link>
          </article>
          <article className="card action-card">
            <p className="eyebrow">Resultados</p>
            <h3>Ver resumen</h3>
            <p>Accede a las gráficas y porcentajes de participación.</p>
            <Link className="button button--light" to="/resultados">Abrir resultados</Link>
          </article>
        </section>
      </main>
    </Shell>
  );
}

function DashboardPage() {
  const navigate = useNavigate();
  const [dashboardStats, setDashboardStats] = useState(defaultDashboardStats);
  const [participationChart, setParticipationChart] = useState(defaultParticipationChart);
  const [survey, setSurvey] = useState({
    title: '',
    question: '',
    responses: ['', '', '', ''],
    expirationDate: ''
  });
  const [feedback, setFeedback] = useState({ type: '', text: '' });
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const loadData = async () => {
      try {
        setLoading(true);
        const [stats, chart] = await Promise.all([
          fetchDashboardStats(),
          fetchParticipationChart()
        ]);
        setDashboardStats(stats || defaultDashboardStats);
        setParticipationChart(chart || defaultParticipationChart);
      } catch (error) {
        console.error('Error loading dashboard data:', error);
        setDashboardStats(defaultDashboardStats);
        setParticipationChart(defaultParticipationChart);
      } finally {
        setLoading(false);
      }
    };
    loadData();
  }, []);

  const chartData = useMemo(() => ({
    labels: participationChart.labels,
    datasets: [
      {
        data: participationChart.values,
        backgroundColor: dashboardColors,
        borderWidth: 0,
        hoverOffset: 4
      }
    ]
  }), [participationChart]);

  const updateField = (event) => {
    const { name, value } = event.target;
    setSurvey((current) => ({ ...current, [name]: value }));
  };

  const updateResponse = (index, value) => {
    setSurvey((current) => {
      const responses = [...current.responses];
      responses[index] = value;
      return { ...current, responses };
    });
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    setFeedback({ type: '', text: '' });

    const validResponses = survey.responses.map((entry) => entry.trim()).filter(Boolean);
    if (validResponses.length < 2) {
      setFeedback({ type: 'error', text: 'Debes agregar al menos dos respuestas distintas.' });
      return;
    }

    const result = await submitSurvey({
      title: survey.title,
      question: survey.question,
      expirationDate: survey.expirationDate,
      respuestas: validResponses
    });

    if (result.success) {
      setFeedback({ type: 'success', text: 'Encuesta enviada correctamente.' });
      setSurvey({ title: '', question: '', responses: ['', '', '', ''], expirationDate: '' });
      setTimeout(() => navigate('/adminpanel'), 900);
      return;
    }

    setFeedback({ type: 'error', text: result.message || 'No fue posible guardar la encuesta.' });
  };

  return (
    <Shell>
      <main className="page-shell">
        <PageHero
          eyebrow="Dashboard"
          title="Panel administrativo con métricas, formulario y gráfico."
          subtitle="La interacción principal ahora vive en un layout de dos columnas con más aire y mejor jerarquía."
          rightNote={{ title: 'Participación', body: 'Las gráficas usan Chart.js dentro de React para mantener el componente visual del proyecto.' }}
        />

        <section className="grid grid--4">
          {dashboardStats.map((stat) => (
            <MetricCard key={stat.label} {...stat} />
          ))}
        </section>

        <section className="dashboard-layout">
          <section className="panel">
            <div className="panel__head">
              <p className="eyebrow">Participación</p>
              <h2>Distribución general</h2>
              <p className="panel__subtitle">Lectura rápida de activos vs pendientes.</p>
            </div>
            <div className="panel__body chart-wrap chart-wrap--doughnut">
              {!loading && <Doughnut data={chartData} options={{ responsive: true, maintainAspectRatio: false, cutout: '72%', plugins: { legend: { position: 'bottom' } } }} />}
              {loading && <div style={{ textAlign: 'center', padding: '40px' }}>Cargando datos...</div>}
            </div>
          </section>

          <article className="card form-card form-card--accent">
            <div className="card__header">
              <p className="eyebrow">Nueva encuesta</p>
              <h2>Crear votación</h2>
              <p>Formulario más ordenado para título, pregunta, respuestas y vencimiento.</p>
            </div>

            <form className="stack" onSubmit={handleSubmit}>
              {feedback.text ? <div className={feedback.type === 'success' ? 'alert-box alert-box--success' : 'alert-box alert-box--danger'}>{feedback.text}</div> : null}
              <label className="field">
                <span>Título</span>
                <input name="title" value={survey.title} onChange={updateField} required />
              </label>
              <label className="field">
                <span>Pregunta</span>
                <input name="question" value={survey.question} onChange={updateField} required />
              </label>
              <div className="stack stack--tight">
                <span className="field__label">Respuestas</span>
                {survey.responses.map((responseText, index) => (
                  <label className="field" key={index}>
                    <input
                      value={responseText}
                      onChange={(event) => updateResponse(index, event.target.value)}
                      placeholder={`Respuesta ${index + 1}`}
                      required
                    />
                  </label>
                ))}
              </div>
              <label className="field">
                <span>Fecha de expiración</span>
                <input type="date" name="expirationDate" value={survey.expirationDate} onChange={updateField} required />
              </label>
              <button className="button button--primary" type="submit">Guardar encuesta</button>
            </form>
          </article>
        </section>
      </main>
    </Shell>
  );
}

function PreguntaPage() {
  const navigate = useNavigate();
  const [selected, setSelected] = useState('');
  const [surveyData, setSurveyData] = useState(defaultSurveyQuestion);
  const [loading, setLoading] = useState(true);
  const [feedback, setFeedback] = useState('');
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    const loadSurvey = async () => {
      try {
        setLoading(true);
        const data = await fetchSurveyQuestion();
        setSurveyData(data || defaultSurveyQuestion);
      } catch (error) {
        console.error('Error loading survey question:', error);
        setSurveyData(defaultSurveyQuestion);
      } finally {
        setLoading(false);
      }
    };
    loadSurvey();
  }, []);

  const handleConfirm = async () => {
    if (!selected) {
      setFeedback('Selecciona una opción antes de confirmar.');
      return;
    }

    const residentCode = localStorage.getItem('residentCode');
    if (!residentCode) {
      setFeedback('No se encontró un código válido de la sesión. Vuelve a ingresar.');
      navigate('/inicio');
      return;
    }

    setSubmitting(true);
    setFeedback('');

    const result = await submitVote(residentCode, selected);
    if (result.success) {
      localStorage.setItem('opcionSeleccionada', selected);
      navigate('/home');
      setSubmitting(false);
      return;
    }

    setFeedback(result.message || 'No fue posible registrar el voto.');
    setSubmitting(false);
  };

  if (loading) {
    return (
      <Shell>
        <main className="page-shell" style={{ textAlign: 'center', padding: '40px' }}>
          <p>Cargando encuesta...</p>
        </main>
      </Shell>
    );
  }

  const options = surveyData.options && surveyData.options.length > 0
    ? surveyData.options.map(opt => opt.label || opt)
    : [];

  return (
    <Shell>
      <main className="page-shell">
        <PageHero
          eyebrow="Votación activa"
          title={surveyData.title}
          subtitle={surveyData.question}
          rightNote={{ title: 'Tu voto es privado', body: 'Selecciona una opción y confirma en una sola acción.' }}
        />

        <section className="survey-layout">
          <article className="card poll-card">
            <div className="stack stack--tight">
              {feedback ? <div className="alert-box alert-box--danger">{feedback}</div> : null}
              {options.map((option) => (
                <button
                  key={option}
                  type="button"
                  className={`poll-option${selected === option ? ' is-selected' : ''}`}
                  onClick={() => setSelected(option)}
                >
                  <span className="poll-option__dot" />
                  <span>{option}</span>
                </button>
              ))}
            </div>

            <button className="button button--primary" type="button" onClick={handleConfirm} disabled={!selected || submitting}>
              {submitting ? 'Registrando...' : 'Confirmar elección'}
            </button>
          </article>

          <aside className="stack">
            <article className="card stat-spotlight">
              <p className="eyebrow">Estado</p>
              <strong>En curso</strong>
              <span>La votación se mantiene abierta hasta el cierre programado.</span>
            </article>
            <article className="card stat-spotlight stat-spotlight--alt">
              <p className="eyebrow">Participación</p>
              <strong>73%</strong>
              <span>Promedio actual de respuestas registradas.</span>
            </article>
          </aside>
        </section>
      </main>
    </Shell>
  );
}

function ResultadosPage() {
  const [currentSurvey, setCurrentSurvey] = useState(defaultFeaturedSurvey);
  const [resultsData, setResultsData] = useState(defaultResultsDataset);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const loadResults = async () => {
      try {
        setLoading(true);
        const [survey, results] = await Promise.all([
          fetchCurrentSurvey(),
          fetchResultsDataset()
        ]);
        setCurrentSurvey(survey || defaultFeaturedSurvey);
        setResultsData(results || defaultResultsDataset);
      } catch (error) {
        console.error('Error loading results:', error);
        setCurrentSurvey(defaultFeaturedSurvey);
        setResultsData(defaultResultsDataset);
      } finally {
        setLoading(false);
      }
    };
    loadResults();
  }, []);

  const chartData = useMemo(() => ({
    labels: resultsData.labels,
    datasets: [
      {
        data: resultsData.values,
        backgroundColor: resultColors,
        borderWidth: 0,
        hoverOffset: 4
      }
    ]
  }), [resultsData]);

  return (
    <Shell>
      <main className="page-shell">
        <PageHero
          eyebrow="Resultados"
          title="Lectura visual de la votación con gráficas integradas."
          subtitle="La información ahora se distribuye mejor entre gráfico, barras de progreso y un resumen lateral más limpio."
          rightNote={{ title: currentSurvey.title, body: currentSurvey.question }}
        />

        <section className="results-layout">
          <section className="panel">
            <div className="panel__head">
              <p className="eyebrow">Votos</p>
              <h2>Distribución general</h2>
              <p className="panel__subtitle">Gráfica principal de participación.</p>
            </div>
            <div className="panel__body chart-wrap chart-wrap--doughnut">
              {!loading && <Doughnut data={chartData} options={{ responsive: true, maintainAspectRatio: false, cutout: '68%', plugins: { legend: { position: 'bottom' } } }} />}
              {loading && <div style={{ textAlign: 'center', padding: '40px' }}>Cargando datos...</div>}
            </div>
          </section>

          <article className="card results-list">
            <div className="card__header">
              <p className="eyebrow">Detalle</p>
              <h2>Opciones de votación</h2>
            </div>

            <div className="results-bars">
              {currentSurvey.options && currentSurvey.options.map((option) => (
                <div key={option.label} className="result-row">
                  <div className="result-row__head">
                    <span>{option.label}</span>
                    <strong>{option.percentage}%</strong>
                  </div>
                  <div className="progress-bar">
                    <span style={{ width: `${option.percentage * 1.5}%` }} />
                  </div>
                  <small>{option.votes} votos</small>
                </div>
              ))}
            </div>
          </article>

          <article className="card note-card">
            <p className="eyebrow">Notas</p>
            <h2>Observaciones de la encuesta</h2>
            <p>
              Este espacio conserva el concepto de notas, pero con un bloque visual más contenido, más aire y mejor contraste.
            </p>
            <p className="note-card__meta">Actualizado hace unos minutos · Conjunto cerrado</p>
          </article>
        </section>
      </main>
    </Shell>
  );
}

function HistorialPage() {
  const [historyData, setHistoryData] = useState(defaultHistoryItems);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const loadHistory = async () => {
      try {
        setLoading(true);
        const data = await fetchSurveyHistory();
        setHistoryData(data || defaultHistoryItems);
      } catch (error) {
        console.error('Error loading survey history:', error);
        setHistoryData(defaultHistoryItems);
      } finally {
        setLoading(false);
      }
    };
    loadHistory();
  }, []);

  return (
    <Shell>
      <main className="page-shell">
        <PageHero
          eyebrow="Historial"
          title="Seguimiento de votaciones pasadas y programadas."
          subtitle="Se reorganizó el contenido en tarjetas con estado, fecha y una lectura más limpia de cada votación."
          rightNote={{ title: 'Resumen', body: 'Vistas más compactas y más fáciles de escanear en escritorio y móvil.' }}
        />

        {loading ? (
          <div style={{ textAlign: 'center', padding: '40px' }}>
            <p>Cargando historial...</p>
          </div>
        ) : (
          <section className="history-layout">
            <article className="card history-list">
              {historyData.map((item) => (
                <div key={item.title} className="history-item">
                  <div>
                    <p className="eyebrow">{item.status}</p>
                    <h3>{item.title}</h3>
                    <p>{item.date}</p>
                  </div>
                  <div className="history-item__meta">
                    <strong>{item.votes}</strong>
                    <Link className="button button--ghost button--small" to="/resultados">
                      Ver detalle
                    </Link>
                  </div>
                </div>
              ))}
            </article>

            <aside className="stack">
              <article className="card stat-spotlight">
                <p className="eyebrow">Actividad</p>
                <strong>{historyData.length} votaciones</strong>
                <span>Listadas entre programadas, en curso y finalizadas.</span>
              </article>
              <article className="card stat-spotlight stat-spotlight--alt">
                <p className="eyebrow">Descargas</p>
                <strong>Actividad</strong>
                <span>Zona reservada para exportar reportes y respaldos.</span>
              </article>
            </aside>
          </section>
        )}
      </main>
    </Shell>
  );
}

function HomePage() {
  const [selection, setSelection] = useState('');

  useEffect(() => {
    setSelection(localStorage.getItem('opcionSeleccionada') || 'Todavía no se ha registrado un voto en esta sesión.');
  }, []);

  return (
    <Shell>
      <main className="page-shell page-shell--centered">
        <section className="card thankyou-card">
          <p className="eyebrow">Gracias por votar</p>
          <h1>Tu elección quedó registrada.</h1>
          <p className="thankyou-card__selection">Has votado por: {selection}</p>
          <div className="auth-pills thankyou-card__pills">
            <span>Flujo completado</span>
            <span>React + Spring Boot</span>
            <span>Navegación unificada</span>
          </div>
          <div className="button-row">
            <Link className="button button--primary" to="/resultados">
              Ver resultados
            </Link>
            <Link className="button button--ghost" to="/inicio">
              Volver al inicio
            </Link>
          </div>
        </section>
      </main>
    </Shell>
  );
}

export default function App() {
  return (
    <Routes>
      <Route path="/" element={<LoginPage />} />
      <Route path="/index" element={<LoginPage />} />
      <Route path="/login" element={<LoginPage />} />
      <Route path="/register" element={<RegisterPage />} />
      <Route path="/registration" element={<RegisterPage />} />
      <Route path="/inicio" element={<InicioPage />} />
      <Route path="/adminpanel" element={<AdminPanelPage />} />
      <Route path="/admin/index" element={<DashboardPage />} />
      <Route path="/admin/dashboard" element={<DashboardPage />} />
      <Route path="/admin/prueba" element={<AdminSurveyPage />} />
      <Route path="/admin/management" element={<AdminManagementPage />} />
      <Route path="/pregunta" element={<PreguntaPage />} />
      <Route path="/resultados" element={<ResultadosPage />} />
      <Route path="/historial" element={<HistorialPage />} />
      <Route path="/user/index" element={<UserIndexPage />} />
      <Route path="/user/save" element={<UserSavePage />} />
      <Route path="/home" element={<HomePage />} />
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}
