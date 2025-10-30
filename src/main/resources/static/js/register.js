
(function safeEnableSubmit(){
  const btn = document.getElementById('submitBtn');
  const terms = document.getElementById('terms');
  const p1 = document.getElementById('password');
  const p2 = document.getElementById('password2');
  const matchMsg = document.getElementById('matchMsg');
  if(!btn || !terms || !p1 || !p2) return;

  function evaluate() {
    const okTerms = terms.checked;
    const okPwd = p1.value.length >= 8 && p1.value === p2.value;
    matchMsg.textContent = (p1.value && p2.value && p1.value !== p2.value) ? "Las contraseñas no coinciden" : "";
    btn.disabled = !(okTerms && okPwd);
  }
  ['input','change','keyup'].forEach(ev=>{
    terms.addEventListener(ev, evaluate);
    p1.addEventListener(ev, evaluate);
    p2.addEventListener(ev, evaluate);
  });
  evaluate();
})();
