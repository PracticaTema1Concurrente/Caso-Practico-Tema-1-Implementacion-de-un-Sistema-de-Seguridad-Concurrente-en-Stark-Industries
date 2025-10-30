function validateRegisterForm(ev){
  console.log('SUBMIT');

  // --- Capturas ---
  const form    = document.getElementById('registerForm');
  const fullNm  = document.getElementById('fullname');
  const user    = document.getElementById('username');
  const email   = document.getElementById('email');
  const pass1   = document.getElementById('password');
  const pass2   = document.getElementById('password2');
  const terms   = document.getElementById('terms');
  const bar     = document.getElementById('strengthBar');
  const matchMsg= document.getElementById('matchMsg');

  // --- Helpers ---
  const emailRe   = /^[^\s@]+@[^\s@]+\.[^\s@]{2,}$/i;
  const hasUpper  = s => /[A-Z]/.test(s);
  const hasLower  = s => /[a-z]/.test(s);
  const hasDigit  = s => /\d/.test(s);
  const hasSymbol = s => /[^A-Za-z0-9]/.test(s);

  const setFieldError = (input, message) => {
    input.setCustomValidity(message || '');
    input.classList.toggle('is-invalid', Boolean(message));
  };

  const scorePassword = (pwd) => {
    let score = 0;
    if (pwd.length >= 8) score++;
    if (hasUpper(pwd))   score++;
    if (hasLower(pwd))   score++;
    if (hasDigit(pwd))   score++;
    if (hasSymbol(pwd))  score++;
    return score;
  };

  const updateStrengthUI = () => {
    if (!bar) return;
    const s = scorePassword(pass1.value);
    const perc = [0,20,40,60,80,100][s];
    bar.style.width = perc + '%';
    bar.setAttribute('aria-valuenow', String(perc));
  };

  // --- Validaciones por campo ---
  const validateFullName = () => {
    const v = fullNm.value.trim();
    if (v.length < 3) { setFieldError(fullNm, 'Introduce tu nombre y apellidos (mín. 3 caracteres)'); return false; }
    setFieldError(fullNm, ''); return true;
  };

  const validateUsername = () => {
    const v = user.value.trim();
    if (v.length < 3) { setFieldError(user, 'Usuario demasiado corto (mín. 3)'); return false; }
    setFieldError(user, ''); return true;
  };

  const validateEmail = () => {
    const v = email.value.trim();
    if (!emailRe.test(v)) { setFieldError(email, 'Email no válido'); return false; }
    setFieldError(email, ''); return true;
  };

  const validatePassword = () => {
    const v = pass1.value;
    updateStrengthUI();
    if (v.length < 8) { setFieldError(pass1, 'La contraseña debe tener al menos 8 caracteres'); return false; }
    const kinds = [hasUpper(v), hasLower(v), hasDigit(v), hasSymbol(v)].filter(Boolean).length;
    if (kinds < 2) { setFieldError(pass1, 'Usa al menos 2 tipos: mayúsculas, minúsculas, dígitos o símbolos'); return false; }
    setFieldError(pass1, ''); return true;
  };

  const validatePassword2 = () => {
    if (pass1.value !== pass2.value) {
      setFieldError(pass2, 'Las contraseñas no coinciden');
      if (matchMsg) matchMsg.textContent = 'Las contraseñas no coinciden';
      return false;
    }
    setFieldError(pass2, '');
    if (matchMsg) matchMsg.textContent = '';
    return true;
  };

  const validateTerms = () => {
    if (!terms.checked) { terms.setCustomValidity('Debes aceptar los términos'); return false; }
    terms.setCustomValidity(''); return true;
  };

  // --- Ejecutar todas ---
  const checks = [
    validateFullName(),
    validateUsername(),
    validateEmail(),
    validatePassword(),
    validatePassword2(),
    validateTerms()
  ];
  const ok = checks.every(Boolean);

  // --- Log de depuración ---
  console.group('Validación de formulario');
  console.log('FullName válido:', checks[0]);
  console.log('Username válido:', checks[1]);
  console.log('Email válido:', checks[2]);
  console.log('Password válido:', checks[3]);
  console.log('Confirmación válida:', checks[4]);
  console.log('Términos aceptados:', checks[5]);
  console.log('Resultado global:', ok ? '✅ Todo válido' : '❌ Hay errores');
  console.groupEnd();

    if (!ok) {
      ev?.preventDefault();
      form.reportValidity?.();
      const firstInvalid = [fullNm, user, email, pass1, pass2, terms].find(el => !el.checkValidity());
      firstInvalid?.focus();
      return false;
    }

    // ✅ Si todo es válido: enviar por fetch y mostrar feedback
    const msg = document.getElementById('msg');
    const csrf = document.getElementById('csrfField')?.value || '';

    const body = new URLSearchParams({
      fullName: fullNm.value.trim(),
      username: user.value.trim(),
      email: email.value.trim(),
      password: pass1.value
    });
    if (csrf) body.append('_csrf', csrf);

    fetch('/auth/register', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8',
        // En Spring con CookieCsrfTokenRepository, también puedes mandar el header:
        'X-XSRF-TOKEN': csrf
      },
      body
    })
    .then(async res => {
      const data = await res.json().catch(()=> ({}));
      if (res.ok && data.ok) {
        // Éxito: pinta feedback y redirige suave
        msg.textContent = data.message || 'Registrado correctamente';
        msg.classList.remove('error');
        msg.classList.add('success');
        // Redirige al login después de un pequeño delay
        setTimeout(() => { window.location.href = '/login.html?registered=1'; }, 900);
      } else {
        // Error: muestra mensaje y marca el campo implicado
        const errorText = data.error || 'No se pudo registrar';
        msg.textContent = errorText;
        msg.classList.remove('success');
        msg.classList.add('error');

        if (data.field === 'email') {
          setFieldError(email, errorText);
          email.focus();
        } else if (data.field === 'username') {
          setFieldError(user, errorText);
          user.focus();
        } else {
          // error genérico
          setFieldError(email, ''); setFieldError(user, '');
        }
        form.reportValidity?.();
      }
    })
    .catch(err => {
      msg.textContent = 'Error de red. Inténtalo de nuevo.';
      msg.classList.remove('success');
      msg.classList.add('error');
      console.error(err);
    });

    return true;
}
