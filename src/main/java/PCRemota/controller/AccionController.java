package PCRemota.controller;

import PCRemota.model.Equipo;
import PCRemota.service.*;
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

    @PostMapping("/{id}/conectar")
    public ResponseEntity<Map<String, String>> conectar(@PathVariable Long id,
                                                        @AuthenticationPrincipal UserDetails user) {
        Equipo equipo = equipoService.obtener(id);
        String resultado = rdpService.conectar(equipo, user.getUsername());
        return respuesta(resultado);
    }

    @PostMapping("/{id}/compartido")
    public ResponseEntity<Map<String, String>> compartido(@PathVariable Long id,
                                                          @RequestParam(required = false) String recurso,
                                                          @AuthenticationPrincipal UserDetails user) {
        Equipo equipo = equipoService.obtener(id);
        String resultado = compartidoService.abrirCompartido(equipo, recurso, user.getUsername());
        return respuesta(resultado);
    }

    @GetMapping("/{id}/ping")
    public ResponseEntity<Map<String, String>> ping(@PathVariable Long id) {
        Equipo equipo = equipoService.obtener(id);
        boolean online = redService.ping(equipo.getIp());
        return ResponseEntity.ok(Map.of("online", String.valueOf(online)));
    }

    @GetMapping("/{id}/password")
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
