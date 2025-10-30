// --- Config API ---
const API = {
  readings: '/api/sensors/readings?size=100',
  devices: '/api/devices',
  start: (id) => `/api/devices/${id}/start`,
  stop: (id) => `/api/devices/${id}/stop`,
  del:  (id) => `/api/devices/${id}`, // eliminar uno

  // 🆕 Alertas peligrosas (solo DANGER guardadas en backend)
  alertsRecent: (size = 50) => `/api/alerts/recent?size=${encodeURIComponent(size)}`
};

// --- Helpers DOM/estado ---
const el = (id) => document.getElementById(id);
const rows = el('rows');
const devicesRows = el('devicesRows');
const dangerRows = el('dangerRows'); // puede no existir si aún no añadiste la tabla
const toast = el('toast');

let autoTimer = null;
let autoDangerTimer = null; // 🆕 auto refresh alertas

let allReadings = [];
let filtered = [];
let page = 1;
let pageSize = 10;
let localDevices = [];
let isAdmin = false;

// 🆕 Detección de nuevas lecturas (alert bloqueante con confirmación)
let lastSeenReadingId = null;
let firstReadingsInit = true;

// --- UI helpers ---
function showToast(msg) {
  if (!toast) return;
  toast.textContent = msg;
  toast.classList.add('show');
  setTimeout(() => toast.classList.remove('show'), 2400);
}

function authHeader() {
  const pair = el('auth') ? el('auth').value.trim() : '';
  if (!pair) return {};
  try { return { Authorization: 'Basic ' + btoa(pair) }; } catch { return {}; }
}

function getCookie(name){
  const m = document.cookie.match('(^|;)\\s*' + name + '\\s*=\\s*([^;]+)');
  return m ? m.pop() : '';
}

function fmtTs(iso) {
  try { return new Date(iso).toLocaleString(); } catch { return iso; }
}

function statusPillByType(type, value) {
  if (type === 'TEMP') {
    if (value >= 60) return '<span class="pill crit">Crítica</span>';
    if (value >= 40) return '<span class="pill warn">Alta</span>';
    return '<span class="pill ok">Normal</span>';
  }
  if (type === 'HUM') {
    if (value >= 80) return '<span class="pill crit">Muy alta</span>';
    if (value >= 60) return '<span class="pill warn">Alta</span>';
    return '<span class="pill ok">Ok</span>';
  }
  if (type === 'MOTION') {
    return value && Number(value) > 0
      ? '<span class="pill warn">Movimiento</span>'
      : '<span class="pill ok">Sin movimiento</span>';
  }
  return '<span class="pill ok">–</span>';
}

// Genera el siguiente ID según el tipo seleccionado
function nextSensorIdForType(type) {
  const prefix = type === 'TEMP' ? 'T-' : type === 'HUM' ? 'H-' : 'M-';
  const count = localDevices.filter((d) => d.type === type).length;
  return `${prefix}${count + 1}`;
}

// Para POST con CSRF activo
function commonPostOpts(headers={}) {
  return {
    method: 'POST',
    credentials: 'same-origin',
    headers: {
      'X-XSRF-TOKEN': getCookie('XSRF-TOKEN'),
      ...headers
    }
  };
}

async function fetchMe() {
  try {
    const res = await fetch('/auth/me', { credentials: 'same-origin' });
    if (!res.ok) return { ok:false, roles:[] };
    return await res.json();
  } catch { return { ok:false, roles:[] }; }
}

// Bloquear/desbloquear la UI de “Dispositivos” según rol
function setAdminPanelEnabled(enabled) {
  const card = document.querySelector('section.card'); // 1ª card = Dispositivos
  if (card) card.classList.toggle('locked', !enabled);

  const lock = document.getElementById('adminLock');
  if (lock) lock.style.display = enabled ? 'none' : 'inline';

  // Deshabilitar controles visibles (reload puede quedar activo)
  ['createDeviceBtn','reloadDevicesBtn','deleteAllDevicesBtn','d_sensorId','d_type','d_period']
    .forEach(id => {
      const c = el(id);
      if (!c) return;
      const shouldDisable = !enabled && id !== 'reloadDevicesBtn';
      c.disabled = shouldDisable;
      c.setAttribute('aria-disabled', String(shouldDisable));
      if (shouldDisable) c.title = 'Requiere rol ADMIN'; else c.removeAttribute('title');
    });
}

// --- DEVICES ---
async function loadDevices() {
  const res = await fetch(API.devices, {
    headers: { Accept: 'application/json', ...authHeader() },
  });
  if (!res.ok) {
    showToast('Error cargando dispositivos (' + res.status + ')');
    return;
  }
  const data = await res.json();
  localDevices = Array.isArray(data) ? data : [];
  renderDevices(localDevices);
  if (el('d_type')) el('d_sensorId').value = nextSensorIdForType(el('d_type').value);
}

function deviceActionButton(d) {
  const disabledAttr = isAdmin ? '' : 'disabled title="Requiere rol ADMIN"';
  const startStopBtn = d.active
    ? `<button class="btn-sm" ${disabledAttr} data-id="${d.id}" data-action="stop">Stop</button>`
    : `<button class="btn-sm primary" ${disabledAttr} data-id="${d.id}" data-action="start">Start</button>`;
  const deleteBtn = `<button class="btn-sm" ${disabledAttr} data-id="${d.id}" data-action="delete" style="margin-left:6px">Eliminar</button>`;
  return `${startStopBtn} ${deleteBtn}`;
}

function renderDevices(list) {
  if (!list || list.length === 0) {
    if (devicesRows) devicesRows.innerHTML = '<tr><td colspan="7" class="muted">Sin dispositivos.</td></tr>';
    return;
  }
  const sorted = [...list].sort((a, b) => (a.id ?? 0) - (b.id ?? 0));
  if (devicesRows) {
    devicesRows.innerHTML = sorted.map((d) => `
      <tr data-device-id="${d.id ?? ''}">
        <td>${d.id ?? ''}</td>
        <td>${d.sensorId}</td>
        <td>${d.type}</td>
        <td>${d.unit ?? ''}</td>
        <td>${d.active ? 'Sí' : 'No'}</td>
        <td>${d.periodMs ?? ''}</td>
        <td class="actions-col">${deviceActionButton(d)}</td>
      </tr>`).join('');
  }
}

// Delegación de eventos
devicesRows?.addEventListener('click', (e) => {
  const btn = e.target.closest('button[data-action]');
  if (!btn) return;

  if (!isAdmin) { showToast('🔒 No tienes permiso (requiere rol ADMIN)'); return; }

  const action = btn.getAttribute('data-action');
  const tr = btn.closest('tr');
  const id = tr?.getAttribute('data-device-id') || '';

  if (action === 'start') startDevice(id);
  else if (action === 'stop') stopDevice(id);
  else if (action === 'delete') deleteDevice(id);
});

async function createDevice() {
  if (!isAdmin) { showToast('🔒 No tienes permiso (ADMIN)'); return; }

  const sensorId = el('d_sensorId').value.trim();
  const type = el('d_type').value;
  const periodMs = parseInt(el('d_period').value || '0', 10);

  if (!sensorId || !type) { showToast('Completa sensorId y type'); return; }
  if (Number.isNaN(periodMs) || periodMs < 2500 || periodMs > 5000) {
    showToast('periodMs debe estar entre 2500 y 5000'); return;
  }

  const res = await fetch(API.devices, {
    ...commonPostOpts({ 'Content-Type': 'application/json', ...authHeader() }),
    body: JSON.stringify({ sensorId, type, periodMs }),
  });
  if (!res.ok) { showToast('Error creando dispositivo (' + res.status + ')'); return; }

  const created = await res.json();
  localDevices.push(created);
  renderDevices(localDevices);
  showToast('Dispositivo creado');
  el('d_sensorId').value = nextSensorIdForType(type);
  el('d_period').value = '2500';
}

async function startDevice(id) {
  if (!isAdmin) { showToast('🔒 No tienes permiso (ADMIN)'); return; }
  const res = await fetch(API.start(id), { ...commonPostOpts({ ...authHeader() }) });
  if (!res.ok) { showToast('No se pudo iniciar (' + res.status + ')'); return; }
  showToast(`Sensor ${id} iniciado`);
  await loadDevices();
}

async function stopDevice(id) {
  if (!isAdmin) { showToast('🔒 No tienes permiso (ADMIN)'); return; }
  const res = await fetch(API.stop(id), { ...commonPostOpts({ ...authHeader() }) });
  if (!res.ok) { showToast('No se pudo detener (' + res.status + ')'); return; }
  showToast(`Sensor ${id} detenido`);
  await loadDevices();
}

async function deleteDevice(id) {
  if (!isAdmin) { showToast('🔒 No tienes permiso (ADMIN)'); return; }
  if (!id) return;
  if (!confirm(`¿Eliminar el sensor ${id}? Esta acción no se puede deshacer.`)) return;

  const res = await fetch(API.del(id), {
    method: 'DELETE',
    credentials: 'same-origin',
    headers: { 'X-XSRF-TOKEN': getCookie('XSRF-TOKEN'), ...authHeader() },
  });

  if (res.status === 204 || res.ok) {
    showToast(`Sensor ${id} eliminado`);
    localDevices = localDevices.filter((d) => String(d.id) !== String(id));
    renderDevices(localDevices);
    await loadDevices();
  } else if (res.status === 404) {
    showToast('No existe ese sensor (404)');
  } else {
    showToast('Error eliminando (' + res.status + ')');
  }
}

async function deleteAllDevices() {
  if (!isAdmin) { showToast('🔒 No tienes permiso (ADMIN)'); return; }
  if (!confirm('¿Eliminar TODOS los sensores? Se detendrán tareas y se borrarán de la BBDD.')) return;

  const res = await fetch(API.devices, {
    method: 'DELETE',
    credentials: 'same-origin',
    headers: { 'X-XSRF-TOKEN': getCookie('XSRF-TOKEN'), ...authHeader() },
  });
  if (!res.ok) { showToast('Error al borrar todos (' + res.status + ')'); return; }

  const text = await res.text();
  showToast(text || 'Sensores eliminados');
  localDevices = [];
  renderDevices(localDevices);
  await loadDevices();
}

// --- READINGS ---
async function loadReadings() {
  const res = await fetch(API.readings, {
    headers: { Accept: 'application/json', ...authHeader() },
  });
  if (!res.ok) { showToast('Error cargando lecturas (' + res.status + ')'); return; }

  const data = await res.json();

  // 🆕 Detectar nuevas lecturas y preguntar Revisar/ Ignorar
  const raw = Array.isArray(data) ? data : [];
  const currentMaxId = raw.reduce((max, r) => {
    const idNum = Number(r?.id ?? 0);
    return Number.isFinite(idNum) && idNum > max ? idNum : max;
  }, 0);

  if (firstReadingsInit) {
    lastSeenReadingId = currentMaxId;
    firstReadingsInit = false;
  } else {
    const newOnes = raw.filter(r => Number(r?.id ?? 0) > (lastSeenReadingId ?? 0));
    if (newOnes.length > 0) {
      const first = newOnes[0] || {};
      const device = first.device || {};
      const sensorId = first.sensorId || device.sensorId || '?';
      const type = (device.type || first.type || '').toUpperCase();
      const val = first.value;

      const msg = `🔔 ${newOnes.length} lectura(s) nueva(s)\n\nPrimera:\n• Sensor: ${sensorId}\n• Tipo: ${type}\n• Valor: ${val}\n\n¿Deseas revisarla ahora?`;
      const revisar = confirm(msg);
      if (revisar) {
        const q = new URLSearchParams({
          type,
          value: String(val ?? ''),
          sensorId: String(sensorId ?? '')
        }).toString();
        window.location.href = `revision.html?${q}`;
      }
      lastSeenReadingId = currentMaxId;
    }
  }

  allReadings = raw;
  applyFilters();
}

function applyFilters() {
  const sensorQ = el('fSensor').value.trim().toLowerCase();
  const typeQ = el('fType').value;

  filtered = allReadings.filter((r) => {
    const device = r.device || {};
    const sensorId = (r.sensorId || device.sensorId || '').toLowerCase();
    const type = (device.type || r.type || '').toUpperCase();
    const okSensor = !sensorQ || sensorId.includes(sensorQ);
    const okType = !typeQ || type === typeQ;
    return okSensor && okType;
  });

  page = 1;
  renderPage();
}

function renderPage() {
  if (!rows) return;

  if (filtered.length === 0) {
    rows.innerHTML = '<tr><td colspan="7" class="muted">Sin lecturas.</td></tr>';
    const pageInfo = el('pageInfo');
    if (pageInfo) pageInfo.textContent = 'Página 1/1';
    return;
  }

  const total = filtered.length;
  const totalPages = Math.max(1, Math.ceil(total / pageSize));
  if (page > totalPages) page = totalPages;
  const start = (page - 1) * pageSize;
  const slice = filtered.slice(start, start + pageSize);

  rows.innerHTML = slice.map((r) => {
    const device = r.device || {};
    const type = (device.type || r.type || '').toUpperCase();
    const unit = r.unit || device.unit || '';
    const value = r.value;
    const status = statusPillByType(type, value);
    const sensorId = r.sensorId || device.sensorId || '';
    return `
      <tr>
        <td>${r.id ?? ''}</td>
        <td>${sensorId}</td>
        <td>${type}</td>
        <td>${typeof value === 'number' && value.toFixed ? value.toFixed(2) : value ?? ''}</td>
        <td>${status}</td>
        <td>${unit}</td>
        <td>${fmtTs(r.createdAt || r.timestamp)}</td>
      </tr>`;
  }).join('');

  const pageInfo = el('pageInfo');
  if (pageInfo) pageInfo.textContent = `Página ${page}/${totalPages}`;
}

// --- Alertas peligrosas (solo render si existe la tabla) ---
async function loadDangerAlerts() {
  if (!dangerRows) return; // si no hay tabla, no hacemos nada
  try {
    const res = await fetch(API.alertsRecent(50), { headers: { Accept: 'application/json' } });
    if (!res.ok) throw new Error('HTTP ' + res.status);

    const data = await res.json();
    renderDangerAlerts(Array.isArray(data) ? data : []);
  } catch (e) {
    console.warn(e);
    dangerRows.innerHTML = '<tr><td colspan="8" class="muted">Error cargando alertas.</td></tr>';
  }
}

function renderDangerAlerts(list) {
  if (!dangerRows) return;

  if (!list || list.length === 0) {
    dangerRows.innerHTML = '<tr><td colspan="8" class="muted">Sin alertas peligrosas registradas…</td></tr>';
    return;
  }
  const rowsHtml = list.map(a => {
    const id = a.id ?? '';
    const sensorId = a.sensorId ?? '';
    const type = (a.type ?? '').toUpperCase();
    const value = (typeof a.value === 'number' && a.value.toFixed) ? a.value.toFixed(2) : (a.value ?? '');
    const unit = a.unit ?? '';
    const by = a.decidedBy ?? '';
    const date = fmtTs(a.createdAt);
    const notes = (a.notes ?? '').slice(0, 160);
    return `
      <tr>
        <td>${id}</td>
        <td>${sensorId}</td>
        <td>${type}</td>
        <td>${value}</td>
        <td>${unit}</td>
        <td>${by}</td>
        <td>${date}</td>
        <td title="${a.notes ?? ''}">${notes}</td>
      </tr>`;
  }).join('');
  dangerRows.innerHTML = rowsHtml;
}

// --- Paginación ---
el('prevPage')?.addEventListener('click', () => { if (page > 1) { page--; renderPage(); } });
el('nextPage')?.addEventListener('click', () => {
  const totalPages = Math.max(1, Math.ceil(filtered.length / pageSize));
  if (page < totalPages) { page++; renderPage(); }
});
el('pageSize')?.addEventListener('change', (e) => {
  pageSize = parseInt(e.target.value, 10) || 10;
  page = 1;
  renderPage();
});

// --- Filtros / acciones ---
el('filterBtn')?.addEventListener('click', applyFilters);
el('resetBtn')?.addEventListener('click', () => { el('fSensor').value = ''; el('fType').value = ''; applyFilters(); });
el('refreshBtn')?.addEventListener('click', () => { loadReadings(); });

const autoCb = el('auto');
if (autoCb) {
  autoCb.checked = true;                 // que aparezca marcada
  clearInterval(autoTimer);
  autoTimer = setInterval(loadReadings, 5000);  // <-- cambia 5000 si quieres otro intervalo
}

// Autocompletar sensorId al cambiar el tipo
el('d_type')?.addEventListener('change', () => {
  const type = el('d_type').value;
  el('d_sensorId').value = nextSensorIdForType(type);
});

// Dispositivos actions
el('createDeviceBtn')?.addEventListener('click', createDevice);
el('reloadDevicesBtn')?.addEventListener('click', loadDevices);
el('deleteAllDevicesBtn')?.addEventListener('click', deleteAllDevices);

// 🆕 Botones/auto para alertas peligrosas (si existen)
el('refreshDangerBtn')?.addEventListener('click', loadDangerAlerts);
const autoDangerCb = el('autoDanger');
if (autoDangerCb) {
  autoDangerCb.checked = true;                 // que aparezca marcada
  clearInterval(autoDangerTimer);
  autoDangerTimer = setInterval(loadDangerAlerts, 10000); // <-- cambia 10000 si quieres otro intervalo
}

// --- Inicialización ---
(async function init() {
  if (el('d_period')) el('d_period').value = '2500';
  if (el('d_sensorId') && el('d_type')) el('d_sensorId').value = nextSensorIdForType(el('d_type').value);

  // 1) Saber quién soy y roles
  const me = await fetchMe();
  const roles = (me && me.ok && Array.isArray(me.roles)) ? me.roles : [];
  isAdmin = roles.includes('ROLE_ADMIN');

  // 2) Bloquear/Desbloquear panel de Dispositivos
  setAdminPanelEnabled(isAdmin);

  // 2.1) Mostrar/ocultar sección Usuarios según rol
  const usersSection = document.getElementById('usersSection');
  if (usersSection) usersSection.style.display = isAdmin ? 'block' : 'none';

  // 2.2) Exponer estado de auth y disparar evento global (para users.js)
  window.__IS_ADMIN__ = isAdmin;
  document.dispatchEvent(new CustomEvent('auth-ready', { detail: { isAdmin } }));

  // 3) Cargar datos principales
  await loadDevices();
  await loadReadings();

  // 4) 🆕 Cargar (si existe tabla) las alertas peligrosas
  await loadDangerAlerts();
})();
