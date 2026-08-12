document.addEventListener('DOMContentLoaded', function () {

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
        toast.className = 'toast show align-items-center text-bg-' + (tipo || 'success') + ' border-0';
        toast.innerHTML = '<div class="d-flex"><div class="toast-body">' + mensaje + '</div>' +
            '<button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast"></button></div>';
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
            btn.innerHTML = '<span class="spinner-border spinner-border-sm"></span>';
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
            btnRefrescar.innerHTML = '<span class="spinner-border spinner-border-sm"></span> Actualizando...';
            fetch('/api/equipos/estado', { credentials: 'same-origin' })
                .then(function (r) { return r.json(); })
                .then(function (estados) {
                    Object.keys(estados).forEach(function (id) {
                        const badge = document.getElementById('estado-' + id);
                        if (!badge) return;
                        const online = estados[id] === true;
                        badge.className = 'badge estado-badge ' + (online ? 'bg-success' : 'bg-danger');
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
            btnConfirmar.innerHTML = '<span class="spinner-border spinner-border-sm"></span> Conectando...';
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
