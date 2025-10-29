    // Mensajes por querystring (?ok, ?error, ?exists, etc.)
    (function () {
      const params = new URLSearchParams(window.location.search);
      const msg = document.getElementById('msg');
      if (!msg) return;

      if (params.has('ok')) {
        msg.textContent = '✅ Cuenta creada correctamente. Revisa tu correo para activar la cuenta.';
        msg.classList.add('msg--ok');
      } else if (params.has('exists')) {
        msg.textContent = '⚠️ Ese usuario o correo ya existe.';
        msg.classList.add('msg--error');
      } else if (params.has('error')) {
        msg.textContent = '❌ No se pudo completar el registro. Inténtalo de nuevo.';
        msg.classList.add('msg--error');
      }
    })();

    // Mostrar/ocultar contraseñas
    (function () {
      const toggle1 = document.querySelector('.toggle');
      const toggle2 = document.querySelector('.toggle-2');
      const pwd1 = document.getElementById('password');
      const pwd2 = document.getElementById('password2');

      function toggle(btn, input) {
        if (!btn || !input) return;
        btn.addEventListener('click', () => {
          const isPwd = input.type === 'password';
          input.type = isPwd ? 'text' : 'password';
          btn.setAttribute('aria-pressed', isPwd ? 'true' : 'false');
        });
      }

      toggle(toggle1, pwd1);
      toggle(toggle2, pwd2);
    })();

    // Medidor de fuerza de contraseña (muy simple, client-side orientativo)
    (function () {
      const pwd = document.getElementById('password');
      const bar = document.getElementById('strengthBar');

      if (!pwd || !bar) return;

      function score(s) {
        let n = 0;
        if (!s) return 0;
        if (s.length >= 8) n++;
        if (/[A-Z]/.test(s)) n++;
        if (/[a-z]/.test(s)) n++;
        if (/[0-9]/.test(s)) n++;
        if (/[^A-Za-z0-9]/.test(s)) n++;
        return Math.min(n, 5);
      }

      pwd.addEventListener('input', () => {
        const s = score(pwd.value);
        const pct = (s / 5) * 100;
        bar.style.width = pct + '%';
        bar.className = 'meter__bar'; // reset classes
        if (s <= 2) bar.classList.add('is-weak');
        else if (s === 3) bar.classList.add('is-mid');
        else bar.classList.add('is-strong');
      });
    })();

    // Coincidencia de contraseñas + habilitar submit
    (function () {
      const form = document.getElementById('registerForm');
      const pwd1 = document.getElementById('password');
      const pwd2 = document.getElementById('password2');
      const matchMsg = document.getElementById('matchMsg');
      const submitBtn = document.getElementById('submitBtn');
      const terms = document.getElementById('terms');

      function validate() {
        const same = pwd1.value && pwd1.value === pwd2.value;
        matchMsg.textContent = same ? '✔ Las contraseñas coinciden' : (pwd2.value ? '✖ Las contraseñas no coinciden' : '');
        matchMsg.classList.toggle('ok', same);
        const formOk = form.checkValidity() && same && terms.checked;
        submitBtn.disabled = !formOk;
      }

      ['input', 'change'].forEach(ev => {
        pwd1.addEventListener(ev, validate);
        pwd2.addEventListener(ev, validate);
        terms.addEventListener(ev, validate);
        form.addEventListener(ev, validate);
      });

      form.addEventListener('submit', (e) => {
        if (submitBtn.disabled) {
          e.preventDefault();
          validate();
        }
      });
    })();

    // Año dinámico
    document.getElementById('year').textContent = new Date().getFullYear();