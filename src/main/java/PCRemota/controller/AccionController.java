package PCRemota.controller;

import PCRemota.model.Equipo;
import PCRemota.service.*;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
@RequestMapping("/api/equipos")
public class AccionController {

    private final EquipoService equipoService;
    private final RdpService rdpService;
    private final CompartidoService compartidoService;
    private final AuditoriaService auditoriaService;
    private final RedService redService;

    public AccionController(EquipoService equipoService, RdpService rdpService,
                            CompartidoService compartidoService, AuditoriaService auditoriaService,
                            RedService redService) {
        this.equipoService = equipoService;
        this.rdpService = rdpService;
        this.compartidoService = compartidoService;
        this.auditoriaService = auditoriaService;
        this.redService = redService;
    }

    @PostMapping(value = "/conectar-nuevo", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, String>> conectarNuevo(@RequestParam String ip,
                                                             @RequestParam(defaultValue = "3389") int puerto,
                                                             @AuthenticationPrincipal UserDetails user) {
        String ipLimpia = ip == null ? "" : ip.trim();
        if (!ipLimpia.matches("\\d{1,3}(\\.\\d{1,3}){3}")) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", "Dirección IP inválida"));
        }
        if (puerto < 1 || puerto > 65535) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", "Puerto inválido"));
        }

        // Si ya existe la tarjeta, conectar a la existente (sin duplicar)
        Equipo existente = equipoService.listar().stream()
                .filter(e -> e.getIp().equals(ipLimpia))
                .findFirst().orElse(null);

        if (existente != null) {
            String resultado = existente.getPassCifrada() != null && existente.getUsuario() != null
                    ? rdpService.conectar(existente, user.getUsername())
                    : rdpService.conectarNuevo(existente, user.getUsername());
            Map<String, String> body = resultado.startsWith("ok")
                    ? Map.of("mensaje", resultado.substring(3).trim(), "creado", "false")
                    : Map.of("mensaje", resultado.substring(6).trim());
            return resultado.startsWith("ok")
                    ? ResponseEntity.ok(body)
                    : ResponseEntity.badRequest().body(body);
        }

        // Equipo nuevo: solo se crea la tarjeta si responde RDP
        Equipo nuevo = new Equipo();
        nuevo.setNombre(ipLimpia);
        nuevo.setIp(ipLimpia);
        nuevo.setPuertoRdp(puerto);
        String resultado = rdpService.conectarNuevo(nuevo, user.getUsername());
        if (resultado.startsWith("ok")) {
            Equipo guardado = equipoService.guardar(nuevo, null);
            auditoriaService.registrar(user.getUsername(), "ALTA_AUTOMATICA", guardado.getId(),
                    "Tarjeta creada tras conectarse a " + ipLimpia);
            return ResponseEntity.ok(Map.of(
                    "mensaje", "Conexión iniciada y tarjeta creada para " + ipLimpia,
                    "creado", "true"));
        }
        return ResponseEntity.badRequest().body(Map.of("mensaje", resultado.substring(6).trim()));
    }

    @PostMapping(value = "/{id}/conectar", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, String>> conectar(@PathVariable Long id,
                                                        @AuthenticationPrincipal UserDetails user) {
        Equipo equipo = equipoService.obtener(id);
        String resultado = rdpService.conectar(equipo, user.getUsername());
        return respuesta(resultado);
    }

    @PostMapping(value = "/{id}/compartido", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, String>> compartido(@PathVariable Long id,
                                                          @RequestParam(required = false) String recurso,
                                                          @AuthenticationPrincipal UserDetails user) {
        Equipo equipo = equipoService.obtener(id);
        String resultado = compartidoService.abrirCompartido(equipo, recurso, user.getUsername());
        return respuesta(resultado);
    }

    @GetMapping(value = "/{id}/ping", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, String>> ping(@PathVariable Long id) {
        Equipo equipo = equipoService.obtener(id);
        boolean online = redService.ping(equipo.getIp());
        return ResponseEntity.ok(Map.of("online", String.valueOf(online)));
    }

    @GetMapping(value = "/estado", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<Long, Boolean>> estado() {
        return ResponseEntity.ok(equipoService.estadoTodos());
    }

    @GetMapping(value = "/{id}/password", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, String>> password(@PathVariable Long id,
                                                        @AuthenticationPrincipal UserDetails user) {
        String pass = equipoService.revelarPassword(id, user.getUsername(), auditoriaService);
        return ResponseEntity.ok(Map.of("password", pass));
    }

    private ResponseEntity<Map<String, String>> respuesta(String resultado) {
        if (resultado.startsWith("ok")) {
            return ResponseEntity.ok(Map.of("mensaje", resultado.substring(3).trim()));
        }
        return ResponseEntity.badRequest().body(Map.of("mensaje", resultado.substring(6).trim()));
    }
}
