# 📋 Plan de Desarrollo — PCRemota

Documento de seguimiento del proyecto. Estado al día de hoy y pendientes para continuar
en una próxima sesión.

---

## Objetivo

App web (Spring Boot) que permite al administrador de sistemas:

- Ver tarjetas con los equipos de la red (IP fija, usuario, credenciales cifradas).
- Conectar por **RDP nativo** con un clic (sin tipear usuario/contraseña).
- Abrir **recursos compartidos** (`\\IP\c$`) con las credenciales guardadas.
- Auditoría completa de accesos y revelación de contraseñas.
- Cifrado fuerte de credenciales en reposo (AES-256-GCM + clave maestra con DPAPI de Windows).

---

## Estado de implementación

| # | Módulo | Estado |
|---|---|---|
| 1 | Proyecto Spring Boot 4.1.0 + Maven (compila) | ✅ Terminado |
| 2 | `application.properties` (H2 archivo, puerto 8443, JPA) | ✅ Terminado |
| 3 | Entidades + repositorios (Equipo, Auditoria, Usuario) | ✅ Terminado |
| 4 | Clave maestra con DPAPI (Windows) | ✅ Terminado |
| 5 | Cifrado AES-256-GCM con clave derivada por equipo (HKDF) | ✅ Terminado |
| 6 | Spring Security: login, BCrypt, CSRF, roles | ✅ Terminado |
| 7 | Servicios: RDP (mstsc+cmdkey), compartidos (net use), ping | ✅ Terminado |
| 8 | Controladores: Auth, CRUD equipos, acciones API | ✅ Terminado |
| 9 | Vistas: login, dashboard de tarjetas, formulario | ✅ Terminado |
| 10 | Seeder: usuario admin + equipos de ejemplo | ✅ Terminado |
| 11 | Tests de contexto / arranque | ✅ Terminado |
| 12 | Documentación (LEEME_PRIMERO.md + este plan) | ✅ Terminado |
| 13 | Estado en vivo con AJAX (refresh de pings) | ✅ Terminado |
| 14 | Escaneo de red por rango de IP + importación | ✅ Terminado |
| 15 | Vista de auditoría en el panel | ✅ Terminado |
| 16 | Cambio de contraseña del usuario | ✅ Terminado |
| 17 | Bloqueo por intentos fallidos de login | ✅ Terminado |
| 18 | Importación de credenciales del Administrador de Credenciales de Windows | ✅ Terminado |

---

## Bitácora de sesiones

### Sesión 1 — 10/08/2026
- Proyecto generado con Spring Initializr (Spring Boot 4.1.0, Java 21, Maven, jar).
- Implementados: entidades, repositorios, cifrado (AES-GCM + DPAPI), Spring Security,
  servicios RDP/compartidos/ping, controladores, vistas (login/dashboard/form), seeder.
- **Bug corregido**: las expresiones de los botones usaban `${...}` suelto; se reemplazó por
  sintaxis `@{/api/equipos/{id}/conectar(id=${equipo.id})}` en `dashboard.html`.
- La app quedó compilando, tests OK y corriendo en `http://localhost:8443` (admin/admin).
- ⚠️ **Sigue pendiente**: verificar manualmente la acción real de RDP y compartidos contra un
  equipo de la red (solo se probó el login y el render del dashboard).

### Sesión 2 — 11/08/2026
- **Estado en vivo**: botón "Refrescar estado" que repinga todos los equipos en paralelo
  (`/api/equipos/estado`) y actualiza los badges sin recargar la página.
- **Escaneo de red**: página `/escanear` que busca equipos en un rango de IPs
  (formato `192.168.1.1-254`) en paralelo, muestra resultados y permite **importarlos** a la grilla.
  Verificado contra la red local real (56 equipos detectados).
- **Vista de auditoría**: página `/auditoria` con tabla de últimos 50 registros
  (RDP, COMPARTIDO, VER_PASSWORD, ESCANEO, IMPORTAR).
- **Cambio de contraseña**: página `/cuenta` para cambiar la propia contraseña
  (pide la actual, valida confirmación y largo mínimo 4).
- **Bloqueo por intentos fallidos**: 5 fallos → IP bloqueada 15 min (filtro + handler).
  Verificado: el 6º intento redirige a `/login?bloqueado`.
- **Bug corregido**: `form-equipo.html` usaba `*{...}` sin `th:object="${equipo}"`
  (daba 500 en `/equipos/nuevo` y edición). Agregado.
- Cifrado verificado: crear equipo con password → `/api/equipos/{id}/password` devuelve
  el valor original (round-trip AES-GCM + HKDF OK).
- ⚠️ **Pendiente**: probar RDP y compartidos contra un equipo real (requiere IP con
  RDP habilitado y credenciales válidas).

### Sesión 3 — 11/08/2026
- **Importación de credenciales de Windows**: página `/credenciales` que enumera el
  Administrador de Credenciales del usuario (vía `CredEnumerate`/`CredRead` con JNA).
  Permite importar cada credencial como equipo nuevo o adjuntarla a uno existente.
  - Solo las de tipo **Genérica** exponen la contraseña (DPAPI de la misma sesión); las de
    **dominio** no (diseño del SO).
  - Los targets crudos llevan prefijo `LegacyGeneric:target=` / `TERMSRV/`; se limpian para
    mostrar/nombrar y se usa el crudo al releer con `CredReadW`.
  - Verificado end-to-end con `cmdkey /generic`: la password real se importó, se cifró y se
    reveló correctamente; auditoría registra `IMPORTAR_CREDENCIAL`.
  - En la máquina real se detectaron credenciales legibles (ej. TERMSRV/10.17.252.32 y
    10.78.12.246, tipo genérico).

### Sesión 4 — (próxima)
- Retomar desde "Pendientes / próximos pasos".

---

## Pendientes / próximos pasos

### 🚨 Críticos
- [ ] **Cambiar la contraseña por defecto de `admin`** al primer uso (ya se puede hacer desde
      `/cuenta`; falta forzar el cambio en el primer login).
- [ ] Habilitar **HTTPS con certificado autogenerado** en el primer arranque (o usar proxy
      TLS). Actualmente va por HTTP.

### 🔒 Mejoras de seguridad
- [ ] Roles más finos: `ADMIN` (todo) y `OPERADOR` (conectar/compartidos sin ver passwords) +
      pantalla de gestión de usuarios.
- [ ] Vista de auditoría: ya implementada; falta **exportación CSV** y filtros por fecha/acción.
- [ ] Rotación/renovación de la clave maestra con recifrado de todas las credenciales.
- [ ] Importar/exportar base de datos como respaldo (H2 script o backup).

### ⚙️ Funcionales
- [ ] Soporte de **etiquetas/tags** y filtros/búsqueda por nombre, IP o ubicación.
- [ ] Guardado de la **última sesión** y favoritos.
- [ ] Verificar el funcionamiento real de RDP/compartidos con un equipo de prueba en la red
      (principal pendiente: falta un equipo real con RDP habilitado).

### 🧪 Opciones a futuro (evaluar con el usuario)
- [ ] **Apache Guacamole**: control remoto 100% dentro del navegador (tipo AnyDesk).
- [ ] **RustDesk self-hosted**: alternativa tipo AnyDesk real con agente en cada equipo.
- [ ] Despliegue en un servidor central para que varios administradores accedan.

---

## Decisiones tomadas

1. **RDP nativo** como método de control remoto (default): simple, sin instalar nada en los
   equipos, abre la ventana nativa de Windows.
2. **Usuarios locales** por equipo (no dominio AD).
3. La app corre en **la PC del administrador** (por eso DPAPI es válido como protección de la
   clave maestra).
4. **H2 embebida** con cifrado de credenciales en reposo; migrable a MySQL/PostgreSQL.

---

## Dudas que quedaron abiertas

- ¿Se quiere el control remoto **dentro** del navegador (Guacamole) o alcanza con RDP nativo?
- ¿Puerto fijo para la app o se deja el 8443?
- ¿Hace falta respaldo automático de la base?

---

## Cómo verificar que todo funciona

```bash
./mvnw test          # compila + levanta el contexto
./mvnw spring-boot:run
# abrir http://localhost:8443  →  admin / admin
```

Flujo de prueba manual:
1. Login con `admin`.
2. Crear/editar un equipo con IP real y credenciales.
3. Botón **Conectar** → debe abrirse mstsc.
4. Botón **Archivos** → se monta `P:` y abre el Explorador.
5. **Ver pass** → confirma y copia la contraseña (queda en auditoría).

### Nota de arranque (al retomar mañana)
- Si la app sigue corriendo de la sesión anterior, cerrarla para evitar el 500 del template
  cacheado:
  ```powershell
  Get-NetTCPConnection -LocalPort 8443 -State Listen | ForEach-Object { Stop-Process -Id $_.OwningProcess -Force }
  ./mvnw spring-boot:run
  ```
- La base `data/` ya existe (equipos de ejemplo + admin). Para regenerarla desde cero, borrar
  la carpeta `data/` antes de arrancar.
