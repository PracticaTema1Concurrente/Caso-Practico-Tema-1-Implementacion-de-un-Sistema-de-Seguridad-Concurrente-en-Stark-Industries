// js/users.js — listado de usuarios con paginación y búsqueda
// Reutiliza helpers globales definidos en main.js: showToast, fmtTs, authHeader, etc.

// --- Config API (local a este módulo) ---
const UAPI = {
  all: '/auth/all' // devuelve { ok, count, users: [...] }
};

// --- Helpers DOM/estado ---
const $ = (id) => document.getElementById(id);
const usersRows = $('usersRows');

let usersAll = [];
let usersFiltered = [];
let usersPage = 1;
let usersPageSize = 10;
let usersAutoTimer = null;

// --- Inicialización diferida: solo si ADMIN y existe la sección ---
function canMountUsers() {
  const sec = document.getElementById('usersSection');
  return !!sec && window.__IS_ADMIN__ === true;
}

async function mountUsersIfAdmin() {
  if (!canMountUsers()) return;
  // Evitar montajes dobles si se dispara el evento más de una vez
  if (window.__USERS_MOUNTED__) return;
  window.__USERS_MOUNTED__ = true;

  // Inicia auto-refresh si el checkbox existe
  const usersAutoCb = document.getElementById('usersAuto');
  if (usersAutoCb) {
    usersAutoCb.checked = true;
    clearInterval(usersAutoTimer);
    usersAutoTimer = setInterval(() => {
      if (usersAutoCb.checked) loadUsers();
    }, 15000);
  }

  await loadUsers();
}


// Campos típicos del dominio User (defensivo por si faltan)
function safe(v, fallback = '') {
  return (v === null || v === undefined) ? fallback : v;
}

function rolesToStr(roles) {
  if (!roles) return '';
  if (Array.isArray(roles)) return roles.join(', ');
  return String(roles);
}

function enabledToStr(enabled) {
  if (enabled === true) return 'Sí';
  if (enabled === false) return 'No';
  return String(enabled ?? '');
}

// Render de una página
function renderUsersPage() {
  if (!usersRows) return;

  if (usersFiltered.length === 0) {
    usersRows.innerHTML = '<tr><td colspan="7" class="muted">Sin usuarios.</td></tr>';
    const info = $('usersPageInfo');
    if (info) info.textContent = 'Página 1/1';
    return;
  }

  const total = usersFiltered.length;
  const totalPages = Math.max(1, Math.ceil(total / usersPageSize));
  if (usersPage > totalPages) usersPage = totalPages;

  const start = (usersPage - 1) * usersPageSize;
  const slice = usersFiltered.slice(start, start + usersPageSize);

  usersRows.innerHTML = slice.map(u => {
    const id = safe(u.id);
    const fullName = safe(u.fullName);
    const username = safe(u.username);
    const email = safe(u.email);
    const enabled = enabledToStr(u.enabled);
    const roles = rolesToStr(u.roles);
    const created = fmtTs(safe(u.createdAt, u.created || u.created_at || '')); // tolerante con nombres

    return `
      <tr>
        <td>${id}</td>
        <td>${fullName}</td>
        <td>${username}</td>
        <td>${email}</td>
        <td>${enabled}</td>
        <td>${roles}</td>
        <td>${created}</td>
      </tr>
    `;
  }).join('');

  const info = $('usersPageInfo');
  if (info) info.textContent = `Página ${usersPage}/${totalPages}`;
}

// Filtro por texto (nombre, usuario, email)
function applyUsersFilter() {
  const q = $('usersSearch')?.value?.trim().toLowerCase() ?? '';
  if (!q) {
    usersFiltered = usersAll.slice();
  } else {
    usersFiltered = usersAll.filter(u => {
      const fullName = String(safe(u.fullName)).toLowerCase();
      const username = String(safe(u.username)).toLowerCase();
      const email = String(safe(u.email)).toLowerCase();
      return fullName.includes(q) || username.includes(q) || email.includes(q);
    });
  }
  usersPage = 1;
  renderUsersPage();
}

// Carga desde /auth/all
async function loadUsers() {
  try {
    const res = await fetch(UAPI.all, {
      headers: { Accept: 'application/json', ...authHeader() },
      credentials: 'same-origin'
    });
    if (!res.ok) {
      showToast('Error cargando usuarios (' + res.status + ')');
      return;
    }
    const data = await res.json();
    const list = Array.isArray(data?.users) ? data.users : (Array.isArray(data) ? data : []);
    usersAll = list;
    applyUsersFilter();
  } catch (e) {
    console.warn(e);
    if (usersRows) usersRows.innerHTML = '<tr><td colspan="7" class="muted">Error cargando usuarios.</td></tr>';
  }
}

// --- Eventos de paginación ---
$('usersPrevPage')?.addEventListener('click', () => {
  if (usersPage > 1) { usersPage--; renderUsersPage(); }
});
$('usersNextPage')?.addEventListener('click', () => {
  const totalPages = Math.max(1, Math.ceil(usersFiltered.length / usersPageSize));
  if (usersPage < totalPages) { usersPage++; renderUsersPage(); }
});
$('usersPageSize')?.addEventListener('change', (e) => {
  usersPageSize = parseInt(e.target.value, 10) || 10;
  usersPage = 1;
  renderUsersPage();
});

// --- Filtros / acciones ---
$('usersFilterBtn')?.addEventListener('click', applyUsersFilter);
$('usersResetBtn')?.addEventListener('click', () => { const s=$('usersSearch'); if (s) s.value=''; applyUsersFilter(); });
$('usersRefreshBtn')?.addEventListener('click', () => { loadUsers(); });

// --- Inicialización diferida: solo si ADMIN y existe la sección ---
function canMountUsers() {
  const sec = document.getElementById('usersSection');
  return !!sec && window.__IS_ADMIN__ === true;
}

async function mountUsersIfAdmin() {
  if (!canMountUsers()) return;
  // Evitar montajes dobles
  if (window.__USERS_MOUNTED__) return;
  window.__USERS_MOUNTED__ = true;

  // Auto-refresh solo si la sección está visible y eres ADMIN
  const usersAutoCb = document.getElementById('usersAuto');
  if (usersAutoCb) {
    usersAutoCb.checked = true;
    clearInterval(usersAutoTimer);
    usersAutoTimer = setInterval(() => {
      if (usersAutoCb.checked) loadUsers();
    }, 15000);
  }

  await loadUsers();
}

// 1) Intento inmediato (por si main.js ya resolvió auth antes)
mountUsersIfAdmin();

// 2) O espera a que main.js emita el evento tras fetchMe()
document.addEventListener('auth-ready', () => {
  mountUsersIfAdmin();
});
