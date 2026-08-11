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
});
