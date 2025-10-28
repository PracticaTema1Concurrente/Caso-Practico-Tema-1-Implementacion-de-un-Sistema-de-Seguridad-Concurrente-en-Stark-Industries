// --- Config API ---
const API = {
  readings: '/api/sensors/readings?size=100',
  devices: '/api/devices',
  start: (id) => `/api/devices/${id}/start`,
  stop: (id) => `/api/devices/${id}/stop`,
};

// --- Helpers DOM/estado ---
const el = (id) => document.getElementById(id);
const rows = el('rows');
const devicesRows = el('devicesRows');
const toast = el('toast');

let autoTimer = null;
let allReadings = [];
let filtered = [];
let page = 1;
let pageSize = 10;

// 🆕 Lista local de sensores (solo en memoria durante la ejecución)
let localDevices = [];

// --- UI helpers ---
function showToast(msg) {
  toast.textContent = msg;
  toast.classList.add('show');
  setTimeout(() => toast.classList.remove('show'), 2400);
}

function authHeader() {
  const pair = el('auth') ? el('auth').value.trim() : '';
  if (!pair) return {};
  try {
    return { Authorization: 'Basic ' + btoa(pair) };
  } catch {
    return {};
  }
}

function fmtTs(iso) {
  try {
    return new Date(iso).toLocaleString();
  } catch {
    return iso;
  }
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

// 🆕 Genera el siguiente ID según el tipo seleccionado
function nextSensorIdForType(type) {
  const prefix = type === 'TEMP' ? 'T-' : type === 'HUM' ? 'H-' : 'M-';
  const count = localDevices.filter((d) => d.type === type).length;
  return `${prefix}${count + 1}`;
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
  // 🪵 LOG 1: justo cuando llega la respuesta del backend
  console.log('[LOG 1] Devices desde API:', data);

  localDevices = Array.isArray(data) ? data : [];
  renderDevices(localDevices);
  el('d_sensorId').value = nextSensorIdForType(el('d_type').value);
}

function deviceActionButton(d) {
  // 🪵 LOG 3: cuando se crea el botón y se asigna el data-id
  console.log(`[LOG 3] Creando botón para device id=${d.id}`);
  return d.active
    ? `<button class="btn-sm" data-id="${d.id}" data-action="stop">Stop</button>`
    : `<button class="btn-sm primary" data-id="${d.id}" data-action="start">Start</button>`;
}

function renderDevices(list) {
  if (!list || list.length === 0) {
    devicesRows.innerHTML =
      '<tr><td colspan="7" class="muted">Sin dispositivos.</td></tr>';
    return;
  }
  devicesRows.innerHTML = list
    .map((d) => {
      // 🪵 LOG 2: cuando se genera la fila y el botón
      console.log(`[LOG 2] Pintando fila -> id=${d.id}, sensorId=${d.sensorId}`);
      return `
        <tr data-device-id="${d.id ?? ''}">
          <td>${d.id ?? ''}</td>
          <td>${d.sensorId}</td>
          <td>${d.type}</td>
          <td>${d.unit ?? ''}</td>
          <td>${d.active ? 'Sí' : 'No'}</td>
          <td>${d.periodMs ?? ''}</td>
          <td class="actions-col">
            ${deviceActionButton(d)}
          </td>
        </tr>`;
    })
    .join('');
}

// 🆕 Delegación de eventos: usar SIEMPRE el id de la fila como fuente de verdad
devicesRows.addEventListener('click', (e) => {
  const btn = e.target.closest('button[data-action]');
  if (!btn) return;

  const action = btn.getAttribute('data-action');
  const idFromBtn = btn.getAttribute('data-id');                 // id del botón
  const tr = btn.closest('tr');
  const idFromRow = tr?.getAttribute('data-device-id') || '';    // autoridad
  const idFromCell = tr?.querySelector('td')?.textContent?.trim() || ''; // id visible

  // 🪵 LOG 4: comparativa completa
  console.log(`[LOG 4] CLICK ${action.toUpperCase()} -> btn.data-id=${idFromBtn} | tr.data-device-id=${idFromRow} | firstCell=${idFromCell}`);

  if (idFromBtn !== idFromRow || idFromRow !== idFromCell) {
    console.warn('[WARN] Inconsistencia de IDs en la fila:', {
      idFromBtn, idFromRow, idFromCell, html: tr?.outerHTML
    });
  }

  // Usamos SIEMPRE el id de la fila
  const id = idFromRow;

  if (action === 'start') startDevice(id);
  else stopDevice(id);
});

async function createDevice() {
  const sensorId = el('d_sensorId').value.trim();
  const type = el('d_type').value;
  const periodMs = parseInt(el('d_period').value || '0', 10);

  if (!sensorId || !type) {
    showToast('Completa sensorId y type');
    return;
  }
  if (Number.isNaN(periodMs) || periodMs < 2500 || periodMs > 5000) {
    showToast('periodMs debe estar entre 2500 y 5000');
    return;
  }

  const res = await fetch(API.devices, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...authHeader() },
    body: JSON.stringify({ sensorId, type, periodMs }),
  });

  if (!res.ok) {
    showToast('Error creando dispositivo (' + res.status + ')');
    return;
  }

  const created = await res.json();
  localDevices.push(created);
  renderDevices(localDevices);
  showToast('Dispositivo creado');

  // 🆕 Actualiza autocompletado para el siguiente sensor
  el('d_sensorId').value = nextSensorIdForType(type);
  el('d_period').value = '2500'; // valor mínimo por defecto
}

async function startDevice(id) {
  console.log(`[API] POST ${API.start(id)} (deviceId=${id})`);
  const res = await fetch(API.start(id), {
    method: 'POST',
    headers: { ...authHeader() },
  });
  if (!res.ok) {
    showToast('No se pudo iniciar (' + res.status + ')');
    return;
  }
  showToast(`Sensor ${id} iniciado`);
  await loadDevices();
}

async function stopDevice(id) {
  console.log(`[API] POST ${API.stop(id)} (deviceId=${id})`);
  const res = await fetch(API.stop(id), {
    method: 'POST',
    headers: { ...authHeader() },
  });
  if (!res.ok) {
    showToast('No se pudo detener (' + res.status + ')');
    return;
  }
  showToast(`Sensor ${id} detenido`);
  await loadDevices();
}

// --- READINGS ---
async function loadReadings() {
  const res = await fetch(API.readings, {
    headers: { Accept: 'application/json', ...authHeader() },
  });
  if (!res.ok) {
    showToast('Error cargando lecturas (' + res.status + ')');
    return;
  }
  const data = await res.json();
  allReadings = Array.isArray(data) ? data : [];
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
  if (filtered.length === 0) {
    rows.innerHTML = '<tr><td colspan="7" class="muted">Sin lecturas.</td></tr>';
    el('pageInfo').textContent = 'Página 1/1';
    return;
  }

  const total = filtered.length;
  const pSize = pageSize;
  const totalPages = Math.max(1, Math.ceil(total / pSize));
  if (page > totalPages) page = totalPages;
  const start = (page - 1) * pSize;
  const slice = filtered.slice(start, start + pSize);

  rows.innerHTML = slice
    .map((r) => {
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
          <td>${
            typeof value === 'number' && value.toFixed
              ? value.toFixed(2)
              : value ?? ''
          }</td>
          <td>${status}</td>
          <td>${unit}</td>
          <td>${fmtTs(r.createdAt || r.timestamp)}</td>
        </tr>`;
    })
    .join('');

  el('pageInfo').textContent = `Página ${page}/${totalPages}`;
}

// --- Paginación ---
el('prevPage').addEventListener('click', () => {
  if (page > 1) {
    page--;
    renderPage();
  }
});
el('nextPage').addEventListener('click', () => {
  const totalPages = Math.max(1, Math.ceil(filtered.length / pageSize));
  if (page < totalPages) {
    page++;
    renderPage();
  }
});
el('pageSize').addEventListener('change', (e) => {
  pageSize = parseInt(e.target.value, 10) || 10;
  page = 1;
  renderPage();
});

// --- Filtros / acciones ---
el('filterBtn').addEventListener('click', applyFilters);
el('resetBtn').addEventListener('click', () => {
  el('fSensor').value = '';
  el('fType').value = '';
  applyFilters();
});
el('refreshBtn').addEventListener('click', () => {
  loadReadings();
});

el('auto').addEventListener('change', (e) => {
  if (e.target.checked) {
    autoTimer = setInterval(loadReadings, 5000);
  } else {
    clearInterval(autoTimer);
  }
});

// 🆕 Autocompletar el campo sensorId al cambiar el tipo
el('d_type').addEventListener('change', () => {
  const type = el('d_type').value;
  el('d_sensorId').value = nextSensorIdForType(type);
});

// --- Dispositivos actions ---
el('createDeviceBtn').addEventListener('click', createDevice);
el('reloadDevicesBtn').addEventListener('click', loadDevices);

// --- Inicialización ---
(async function init() {
  el('d_period').value = '2500'; // 🆕 valor por defecto mínimo
  el('d_sensorId').value = nextSensorIdForType(el('d_type').value);
  await loadDevices();
  await loadReadings();
})();
