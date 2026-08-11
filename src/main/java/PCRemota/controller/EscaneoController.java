package PCRemota.controller;

import PCRemota.model.Equipo;
import PCRemota.service.AuditoriaService;
import PCRemota.service.EquipoService;
import PCRemota.service.RedService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class EscaneoController {

    private final RedService redService;
    private final EquipoService equipoService;
    private final AuditoriaService auditoriaService;

    public EscaneoController(RedService redService, EquipoService equipoService,
                             AuditoriaService auditoriaService) {
        this.redService = redService;
        this.equipoService = equipoService;
        this.auditoriaService = auditoriaService;
    }

    @GetMapping("/escanear")
    public String formulario(Model model, @RequestParam(required = false) String rango) {
        model.addAttribute("rango", rango == null ? "192.168.1.1-254" : rango);
        return "escanear";
    }

    @PostMapping("/escanear")
    public String escanear(@RequestParam String rango,
                           @AuthenticationPrincipal UserDetails user,
                           Model model) {
        try {
            List<String> encontrados = redService.escanearRed(rango);
            model.addAttribute("encontrados", encontrados);
            model.addAttribute("rango", rango);
            model.addAttribute("total", encontrados.size());
            auditoriaService.registrar(user.getUsername(), "ESCANEO", null,
                    "Escaneo de red " + rango + " -> " + encontrados.size() + " equipos encontrados");
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("rango", rango);
        }
        return "escanear";
    }

    @PostMapping("/escanear/importar")
    public String importar(@RequestParam(value = "ips", required = false) List<String> ips,
                           @AuthenticationPrincipal UserDetails user,
                           RedirectAttributes ra) {
        if (ips == null || ips.isEmpty()) {
            ra.addFlashAttribute("mensaje", "No se seleccionó ningún equipo");
            return "redirect:/escanear";
        }
        int nuevos = 0;
        for (String ip : ips) {
            boolean existe = equipoService.listar().stream()
                    .anyMatch(e -> e.getIp().equals(ip));
            if (!existe) {
                Equipo equipo = new Equipo();
                equipo.setNombre(ip);
                equipo.setIp(ip);
                equipo.setPuertoRdp(3389);
                equipoService.guardar(equipo, null);
                nuevos++;
            }
        }
        auditoriaService.registrar(user.getUsername(), "IMPORTAR", null,
                "Se importaron " + nuevos + " equipos del escaneo");
        ra.addFlashAttribute("mensaje", "Se importaron " + nuevos + " equipos nuevos");
        return "redirect:/";
    }
}
