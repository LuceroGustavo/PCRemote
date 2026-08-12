document.addEventListener('DOMContentLoaded', function () {


    // Alternar modo claro/oscuro con persistencia local
    const themeButtons = [document.getElementById('themeToggle'), document.getElementById('themeToggleHeader')].filter(Boolean);
    const themeLabels = [document.getElementById('themeLabel'), document.getElementById('themeLabelHeader')].filter(Boolean);

    function setTheme(theme) {
        const isLight = theme === 'light';
        document.documentElement.dataset.theme = theme;
        localStorage.setItem('pc-theme', theme);
        themeButtons.forEach(function (button) {
            button.setAttribute('aria-pressed', String(isLight));
            button.setAttribute('aria-label', isLight ? 'Cambiar a modo oscuro' : 'Cambiar a modo claro');
            button.title = isLight ? 'Cambiar a modo oscuro' : 'Cambiar a modo claro';
        });
        themeLabels.forEach(function (label) {
            label.textContent = isLight ? 'Modo claro' : 'Modo oscuro';
        });
    }

    setTheme(document.documentElement.dataset.theme || 'dark');
    themeButtons.forEach(function (button) {
        button.addEventListener('click', function () {
            setTheme(document.documentElement.dataset.theme === 'light' ? 'dark' : 'light');
        });
    });

    // Confirmar eliminación de equipos

    // Si Material Symbols no carga, evitar textos crudos como TOP_ mostrando glifos simples.
    const iconFallbacks = {
        add: '+', dashboard: '▦', radar: '⌁', key: '⌘', security: '◆', settings: '⚙', logout: '↩',
        account_circle: '●', refresh: '↻', cast: '↗', dns: '▥', desktop_window: '▣',
        folder_open: '▤', edit: '✎', delete: '×', progress_activity: '↻'
    };
    document.querySelectorAll('.material-symbols-outlined').forEach(function (icon) {
        const name = icon.textContent.trim();
        icon.dataset.fallbackIcon = iconFallbacks[name] || '•';
        icon.setAttribute('aria-hidden', 'true');
    });
    if (document.fonts && document.fonts.check) {
        document.fonts.ready.then(function () {
            const materialReady = document.fonts.check('16px "Material Symbols Outlined"');
            document.body.classList.remove('icons-pending');
            document.body.classList.toggle('icons-fallback', !materialReady);
        });
    } else {
        document.body.classList.remove('icons-pending');
        document.body.classList.add('icons-fallback');
    }

    document.querySelectorAll('.btn-eliminar').forEach(function (btn) {
        btn.addEventListener('click', function (e) {
            if (!confirm('¿Eliminar ' + btn.dataset.nombre + '?')) {
                e.preventDefault();
                e.stopPropagation();
            }
        });
    });

    // Enviar CSRF en peticiones fetch
    const csrfToken = document.querySelector('meta[name="_csrf"]');
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]');

    function csrfHeaders() {
        const h = { 'Content-Type': 'application/x-www-form-urlencoded' };
        if (csrfToken && csrfHeader) {
            h[csrfHeader.content] = csrfToken.content;
        }
        return h;
    }

    function mostrarToast(mensaje, tipo) {
        const cont = document.getElementById('toastContainer');
        const toast = document.createElement('div');
        const estilo = tipo === 'danger'
            ? 'bg-[#450a0a] border-[#f43f5e]/40 text-[#fecaca]'
            : 'bg-[#064e35] border-[#10b981]/40 text-[#bbf7d0]';
        toast.className = 'mb-3 p-3 border rounded-DEFAULT font-label-technical text-xs shadow-lg ' + estilo;
        toast.innerHTML = mensaje;
        cont.appendChild(toast);
        setTimeout(function () { toast.remove(); }, 6000);
    }

    function post(url, params) {
        return fetch(url, {
            method: 'POST',
            headers: csrfHeaders(),
            credentials: 'same-origin',
            body: params || ''
        });
    }

    // Botones "Conectar" y "Archivos"
    document.querySelectorAll('.accion').forEach(function (btn) {
        btn.addEventListener('click', function () {
            const url = btn.dataset.accion;
            const original = btn.innerHTML;
            btn.disabled = true;
            btn.innerHTML = '<span class="material-symbols-outlined text-[16px] animate-spin">progress_activity</span>';
            post(url).then(function (r) {
                return r.json().then(function (data) { return { ok: r.ok, data: data }; });
            }).then(function (res) {
                mostrarToast(res.data.mensaje, res.ok ? 'success' : 'danger');
            }).catch(function () {
                mostrarToast('Error de comunicación con el servidor', 'danger');
            }).finally(function () {
                btn.disabled = false;
                btn.innerHTML = original;
            });
        });
    });

    // Ver contraseña (con confirmación)
    document.querySelectorAll('.ver-pass').forEach(function (btn) {
        btn.addEventListener('click', function () {
            if (!confirm('¿Revelar la contraseña de ' + btn.dataset.nombre + '? Esta acción queda registrada.')) {
                return;
            }
            fetch(btn.dataset.url, { credentials: 'same-origin' })
                .then(function (r) { return r.json(); })
                .then(function (data) {
                    navigator.clipboard.writeText(data.password).then(function () {
                        mostrarToast('Contraseña copiada al portapapeles');
                    }, function () {
                        mostrarToast('Contraseña: ' + data.password, 'info');
                    });
                })
                .catch(function () { mostrarToast('No se pudo obtener la contraseña', 'danger'); });
        });
    });

    // Refrescar estado online/offline sin recargar
    const btnRefrescar = document.getElementById('btnRefrescar');
    if (btnRefrescar) {
        btnRefrescar.addEventListener('click', function () {
            const original = btnRefrescar.innerHTML;
            btnRefrescar.disabled = true;
            btnRefrescar.innerHTML = '<span class="material-symbols-outlined text-[16px] animate-spin">progress_activity</span> Actualizando...';
            fetch('/api/equipos/estado', { credentials: 'same-origin' })
                .then(function (r) { return r.json(); })
                .then(function (estados) {
                    Object.keys(estados).forEach(function (id) {
                        const badge = document.getElementById('estado-' + id);
                        if (!badge) return;
                        const online = estados[id] === true;
                        badge.className = 'badge-status font-label-technical text-[10px] px-2 py-0.5 rounded-DEFAULT ' +
                            (online
                                ? 'border border-emerald-500 bg-emerald-900/40 text-emerald-400 shadow-led'
                                : 'border border-[#f43f5e]/50 bg-[#1e293b] text-[#f43f5e]');
                        badge.textContent = online ? 'EN LÍNEA' : 'OFFLINE';
                    });
                    mostrarToast('Estado actualizado');
                })
                .catch(function () { mostrarToast('No se pudo actualizar el estado', 'danger'); })
                .finally(function () {
                    btnRefrescar.disabled = false;
                    btnRefrescar.innerHTML = original;
                });
        });
    }

    // Conectar equipo nuevo (solo IP) -> si responde, crea la tarjeta sola
    const btnConectarNuevo = document.getElementById('btnConectarNuevo');
    const modalConectar = document.getElementById('modalConectarNuevo');
    const btnConfirmar = document.getElementById('btnConectarNuevoConfirmar');
    const inputIp = document.getElementById('nuevaIp');
    const inputPuerto = document.getElementById('nuevaIpPuerto');

    if (btnConectarNuevo && modalConectar) {
        btnConectarNuevo.addEventListener('click', function () {
            inputIp.value = '';
            inputPuerto.value = '3389';
            new bootstrap.Modal(modalConectar).show();
            setTimeout(function () { inputIp.focus(); }, 400);
        });
    }

    if (btnConfirmar) {
        btnConfirmar.addEventListener('click', function () {
            const ip = inputIp.value.trim();
            if (!ip) {
                mostrarToast('Ingresá una dirección IP', 'danger');
                return;
            }
            const puerto = inputPuerto.value || '3389';
            const original = btnConfirmar.innerHTML;
            btnConfirmar.disabled = true;
            btnConfirmar.innerHTML = '<span class="material-symbols-outlined text-[16px] animate-spin">progress_activity</span> Conectando...';
            post('/api/equipos/conectar-nuevo', 'ip=' + encodeURIComponent(ip) + '&puerto=' + encodeURIComponent(puerto))
                .then(function (r) {
                    return r.json().then(function (data) { return { ok: r.ok, data: data }; });
                })
                .then(function (res) {
                    mostrarToast(res.data.mensaje, res.ok ? 'success' : 'danger');
                    if (res.ok && res.data.creado === 'true') {
                        setTimeout(function () { window.location.reload(); }, 1200);
                    }
                    if (bootstrap.Modal.getInstance(modalConectar)) {
                        bootstrap.Modal.getInstance(modalConectar).hide();
                    }
                })
                .catch(function () { mostrarToast('Error de comunicación con el servidor', 'danger'); })
                .finally(function () {
                    btnConfirmar.disabled = false;
                    btnConfirmar.innerHTML = original;
                });
        });
        inputIp.addEventListener('keydown', function (e) {
            if (e.key === 'Enter') btnConfirmar.click();
        });
    }
});
