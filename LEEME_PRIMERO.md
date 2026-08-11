# 🖥️ PCRemota — Panel de Acceso Remoto

Aplicación web en **Spring Boot** para administradores de sistemas. Centraliza el inventario de
equipos de la red (IP, usuario, contraseña cifrada, SO, notas) y permite desde el navegador:

- **Conectar** por Escritorio Remoto (RDP nativo de Windows) con un clic y sin tipear credenciales.
- **Abrir recursos compartidos** (ej. `\\IP\c$`) en el Explorador de Windows.
- **Ver estado** online/offline de cada equipo (ping).
- **Revelar contraseñas** de forma controlada y auditada.

---

## Tecnologías

| Capa | Tecnología |
|---|---|
| Backend | Java 21, Spring Boot 4.1.0, Maven |
| Seguridad | Spring Security 7 (BCrypt, CSRF, sesiones) |
| Persistencia | H2 embebida (archivo `data/pcremota.mv.db`) + Spring Data JPA |
| Vistas | Thymeleaf + Bootstrap 5 (CDN) |
| Cifrado | AES-256-GCM con clave derivada por equipo (HKDF-SHA256) |
| Clave maestra | DPAPI de Windows (`CryptProtectData` vía JNA) |
| Nativos | `mstsc.exe`, `cmdkey.exe`, `net.exe`, `explorer.exe` |

---

## Estructura del proyecto

```
src/main/java/PCRemota/
  config/         SecurityConfig, DataSeeder
  controller/     AuthController, EquipoController, AccionController
  model/          Equipo, Auditoria, Usuario
  repository/     EquipoRepository, AuditoriaRepository, UsuarioRepository
  security/       CryptoService, MasterKeyService (DPAPI)
  service/        EquipoService, RdpService, CompartidoService, RedService, AuditoriaService
src/main/resources/
  application.properties
  templates/      login.html, dashboard.html, form-equipo.html
  static/         css/estilos.css, js/app.js
```

---

## Seguridad (cómo está implementada)

1. **Clave maestra (MasterKeyService)** — Se genera aleatoria (32 bytes) en el primer arranque y se
   protege con **DPAPI de Windows**. Solo la cuenta de Windows que la creó puede descifrarla.
2. **Cifrado por equipo (CryptoService)** — Cada contraseña se cifra con **AES-256-GCM** usando una
   clave derivada con **HKDF-SHA256** (clave maestra + sal aleatoria única por registro).
   Formato guardado: `sal:iv:cifrado` (base64).
3. **Login propio** — Usuarios en tabla `usuarios` con hash **BCrypt**. Roles disponibles: `ADMIN`.
4. **HTTPS** — Para entornos reales se debe habilitar TLS (ver sección Pendientes).
5. **Auditoría** — Cada acceso a RDP, recurso compartido o revelación de contraseña queda registrado
   en la tabla `auditoria` (usuario, fecha, acción, equipo, detalle).
6. **CSRF** — Activado; las peticiones `fetch` del frontend envían el token desde un `<meta>`.

---

## Cómo ejecutar

Prerequisitos: JDK 21 y Maven (o usar `mvnw`).

```bash
./mvnw spring-boot:run
```

La app queda en: `http://localhost:8443`

Credencial inicial (¡cambiarla ya!):

```
usuario:   admin
contraseña: admin
```

La base de datos y la clave maestra se crean en `data/` la primera vez que arranca.
Los equipos de ejemplo cargados se eliminan/editan desde el panel.

---

## Características por tarjeta

| Botón | Comportamiento |
|---|---|
| **Conectar** | Verifica puerto RDP → `cmdkey` guarda la credencial → genera `.rdp` temporal → lanza `mstsc.exe` |
| **Archivos** | `net use P: \\IP\c$` con credenciales → abre `explorer.exe P:\` |
| **Ver pass** | Confirma en pantalla → revela/copia contraseña → registra auditoría |
| Editar / Eliminar | CRUD completo |
| Estado | `ping` ICMP de 3 s → badge EN LÍNEA / OFFLINE |

---

## Notas operativas

- `mstsc.exe` abre la ventana nativa de Escritorio Remoto (no dentro del navegador).
- El disco se monta en la letra `P:` (sobrescribe cualquier uso previo de esa letra).
- El `.rdp` temporal se genera en `data/rdp/` y los credenciales `cmdkey` quedan en el almacén
  de credenciales de Windows para la sesión.
- La H2 Console está disponible en `/h2-console` para administrar la base (mismo usuario `sa`).
