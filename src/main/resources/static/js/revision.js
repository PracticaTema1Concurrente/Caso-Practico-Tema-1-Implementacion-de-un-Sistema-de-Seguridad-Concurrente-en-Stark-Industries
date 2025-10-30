// revision.js — Stark Industries (versión final)

// Utilidades
function $(sel){ return document.querySelector(sel); }
function getCookie(name){
  const m=document.cookie.match('(^|;)\\s*'+name+'\\s*=\\s*([^;]+)');
  return m?m.pop():'';
}
function qp(name,def=null){
  const p=new URLSearchParams(location.search);
  return p.get(name)??def;
}

// Solo guardamos si es PELIGROSA
async function enviarDecisionPeligrosa(notes=''){
  const payload={
    readingId:qp('readingId')?Number(qp('readingId')):null,
    sensorId:qp('sensorId')||null,
    type:qp('type')||null,
    value:qp('value')?Number(qp('value')):null,
    unit:qp('unit')||null,
    decision:'DANGER',
    notes
  };
  const res=await fetch('/api/alerts/decision',{
    method:'POST',
    credentials:'same-origin',
    headers:{
      'Content-Type':'application/json',
      'X-XSRF-TOKEN':getCookie('XSRF-TOKEN')
    },
    body:JSON.stringify(payload)
  });
  if(!res.ok) throw new Error('HTTP '+res.status);
  return res.json();
}

// ====== Botones ======
const btnSeguro=$('#dec-safe');
const btnPeligro=$('#dec-danger');

// SEGURO → botón verde + redirección
btnSeguro?.addEventListener('click',()=>{
  btnSeguro.classList.add('safe-active');
  btnSeguro.disabled=true;
  btnPeligro.disabled=true;
  [...document.querySelectorAll('button,input,select,textarea')].forEach(el=>{
    if(el!==btnSeguro) el.disabled=true;
  });
  setTimeout(()=>window.location.href='index.html',1000);
});

// PELIGROSO → modo rojo + modal
const overlay=$('#dangerOverlay');
const modal=$('#dangerModal');
const btnDangerConfirm=$('#dangerConfirm');
const btnDangerCancel=$('#dangerCancel');

// Abre modal de peligro
btnPeligro?.addEventListener('click',()=>{
  document.body.classList.add('danger-state');
  modal.classList.add('open');
  overlay.setAttribute('aria-hidden','false');
  if(navigator.vibrate) navigator.vibrate([60,40,60]);
  // Enfocar el primer botón del modal para accesibilidad
  btnDangerConfirm?.focus();
});

// Función para restaurar "modo normal"
function cerrarModalYRestaurar(){
  modal?.classList.remove('open');
  overlay?.setAttribute('aria-hidden','true');
  document.body.classList.remove('danger-state');
  // Reenfocar el botón "Marcar como Peligroso" para continuidad
  btnPeligro?.focus();
}

// Modal: Cancelar = volver a modo normal
btnDangerCancel?.addEventListener('click', cerrarModalYRestaurar);

// Extra: cerrar con ESC
document.addEventListener('keydown',(e)=>{
  if(e.key==='Escape' && modal?.classList.contains('open')){
    e.preventDefault();
    cerrarModalYRestaurar();
  }
});

// Modal: Confirmar = registrar DANGER y volver al panel
btnDangerConfirm?.addEventListener('click',async()=>{
  try{
    btnDangerConfirm.disabled=true;
    btnDangerCancel.disabled=true;
    const notes=(document.getElementById('dangerNotes')?.value||'').trim();
    await enviarDecisionPeligrosa(notes);
  }catch(e){
    alert('No se pudo registrar la alerta ('+(e?.message||'Error')+')');
    btnDangerConfirm.disabled=false;
    btnDangerCancel.disabled=false;
    return;
  }
  window.location.href='index.html';
});

// ====== Infografías ======
const params=new URLSearchParams(window.location.search);
const type=(params.get('type')||'').toUpperCase();
const sensorId=params.get('sensorId')||'¿?';
const valueRaw=params.get('value');
const value=valueRaw!==null?Number(valueRaw):null;

const meta=$('#meta');
if(meta) meta.textContent=`Sensor: ${sensorId} · Tipo: ${type||'—'} · Valor recibido: ${valueRaw??'—'}`;
const metaB=$('#meta-badges');
if(metaB) metaB.innerHTML=`
  <span class="badge">ID: <strong>${sensorId}</strong></span>
  <span class="badge">Tipo: <strong>${type}</strong></span>
  <span class="badge">Valor: <strong>${valueRaw??'—'}</strong></span>`;

function placeCursor(bar,fraction){
  const x=Math.max(0,Math.min(1,fraction))*bar.clientWidth;
  const cur=document.createElement('div');
  cur.className='cursor';
  cur.style.left=`${x-9}px`;
  bar.appendChild(cur);
}
function addTicks(bar,count=5){
  for(let i=0;i<=count;i++){
    const t=document.createElement('div');
    t.className='tick';
    t.style.left=`${(i/count)*100}%`;
    bar.appendChild(t);
  }
}
function renderTemp(){
  const sec=$('#sec-temp'); sec.style.display='block';
  $('#v-temp').textContent=(value??'—')+' °C';
  const bar=$('#bar-temp'); addTicks(bar,6);
  const min=-10,max=100; const frac=value==null?0:(value-min)/(max-min);
  placeCursor(bar,frac);
}
function renderHum(){
  const sec=$('#sec-hum'); sec.style.display='block';
  $('#v-hum').textContent=(value??'—')+' %';
  const bar=$('#bar-hum'); addTicks(bar,5);
  const min=0,max=100; const frac=value==null?0:(value-min)/(max-min);
  placeCursor(bar,frac);
}
function renderMotion(){
  const sec=$('#sec-motion'); sec.style.display='block';
  const val=(value??0)?1:0;
  $('#v-motion').textContent=String(val);
  const bar=$('#bar-motion'); addTicks(bar,1); placeCursor(bar,val);
}

switch(type){
  case 'TEMP': renderTemp(); break;
  case 'HUM': renderHum(); break;
  case 'MOTION': renderMotion(); break;
  default:
    alert('Tipo de sensor no reconocido. Volviendo al panel.');
    window.location.href='index.html';
}