package PCRemota.controller;

import PCRemota.model.Equipo;
import PCRemota.security.CredencialesWindowsService;
import PCRemota.security.CredencialesWindowsService.CredencialWindows;
import PCRemota.service.AuditoriaService;
import PCRemota.service.EquipoService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class CredencialesController {

    private final CredencialesWindowsService credencialesWindowsService;
    private final EquipoService equipoService;
    private final AuditoriaService auditoriaService;

    public CredencialesController(CredencialesWindowsService credencialesWindowsService,
                                  EquipoService equipoService, AuditoriaService auditoriaService) {
        this.credencialesWindowsService = credencialesWindowsService;
        this.equipoService = equipoService;
        this.auditoriaService = auditoriaService;
    }

    @GetMapping("/credenciales")
    public String listar(Model model, @AuthenticationPrincipal UserDetails user) {
        List<CredencialWindows> credenciales = credencialesWindowsService.listar();
        model.addAttribute("credenciales", credenciales);
        model.addAttribute("equipos", equipoService.listar());
        model.addAttribute("usuario", user.getUsername());
        return "credenciales";
    }

    @PostMapping("/credenciales/importar")
    public String importar(@RequestParam String target,
                           @RequestParam(required = false) Long equipoId,
                           @AuthenticationPrincipal UserDetails user,
                           RedirectAttributes ra) {
        CredencialWindows credencial = credencialesWindowsService.leer(target);
        if (credencial == null || !credencial.tienePassword()) {
            ra.addFlashAttribute("mensaje", "No se pudo leer la credencial (¿sin contraseña o tipo de dominio?)");
            return "redirect:/credenciales";
        }

        if (equipoId != null) {
            Equipo equipo = equipoService.obtener(equipoId);
            equipo.setUsuario(credencial.usuario);
            equipoService.guardar(equipo, credencial.password);
            String limpio = credencial.destinoLimpio();
            if (limpio != null) {
                equipo.setNombre(limpio);
                equipoService.guardar(equipo, null);
            }
            auditoriaService.registrar(user.getUsername(), "IMPORTAR_CREDENCIAL", equipoId,
                    "Credencial adjuntada al equipo " + equipo.getNombre());
            ra.addFlashAttribute("mensaje", "Credencial adjuntada al equipo " + equipo.getNombre());
        } else {
            String destino = credencial.destinoLimpio();
            boolean existe = equipoService.listar().stream()
                    .anyMatch(e -> e.getIp().equalsIgnoreCase(destino) || e.getNombre().equalsIgnoreCase(destino));
            if (existe) {
                ra.addFlashAttribute("mensaje", "Ya existe un equipo con ese destino: " + destino);
                return "redirect:/credenciales";
            }
            Equipo nuevo = new Equipo();
            nuevo.setNombre(destino);
            nuevo.setIp(destino);
            nuevo.setPuertoRdp(3389);
            nuevo.setUsuario(credencial.usuario);
            nuevo.setSo("Desconocido");
            nuevo.setNotas("Importado desde el Administrador de Credenciales de Windows");
            equipoService.guardar(nuevo, credencial.password);
            auditoriaService.registrar(user.getUsername(), "IMPORTAR_CREDENCIAL", nuevo.getId(),
                    "Equipo creado desde credencial de Windows (" + destino + ")");
            ra.addFlashAttribute("mensaje", "Equipo creado desde la credencial " + destino);
        }
        return "redirect:/";
    }
}
