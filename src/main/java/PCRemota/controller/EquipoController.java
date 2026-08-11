package PCRemota.controller;

import PCRemota.model.Equipo;
import PCRemota.service.EquipoService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import java.util.List;

@Controller
public class EquipoController {

    private final EquipoService equipoService;

    public EquipoController(EquipoService equipoService) {
        this.equipoService = equipoService;
    }

    @GetMapping("/")
    public String dashboard(Model model, @AuthenticationPrincipal UserDetails user) {
        List<Equipo> equipos = equipoService.refrescarEstado();
        model.addAttribute("equipos", equipos);
        model.addAttribute("usuario", user.getUsername());
        return "dashboard";
    }

    @GetMapping("/equipos/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("equipo", new Equipo());
        return "form-equipo";
    }

    @PostMapping("/equipos/guardar")
    public String guardar(@Valid @ModelAttribute("equipo") Equipo equipo,
                          BindingResult result,
                          @RequestParam(value = "password", required = false) String password,
                          RedirectAttributes ra) {
        if (result.hasErrors()) {
            return "form-equipo";
        }
        equipoService.guardar(equipo, password);
        ra.addFlashAttribute("mensaje", "Equipo guardado correctamente");
        return "redirect:/";
    }

    @GetMapping("/equipos/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        model.addAttribute("equipo", equipoService.obtener(id));
        return "form-equipo";
    }

    @PostMapping("/equipos/{id}/actualizar")
    public String actualizar(@PathVariable Long id,
                             @Valid @ModelAttribute("equipo") Equipo equipo,
                             BindingResult result,
                             @RequestParam(value = "password", required = false) String password,
                             RedirectAttributes ra) {
        if (result.hasErrors()) {
            return "form-equipo";
        }
        equipoService.actualizar(id, equipo, password);
        ra.addFlashAttribute("mensaje", "Equipo actualizado correctamente");
        return "redirect:/";
    }

    @PostMapping("/equipos/{id}/eliminar")
    public String eliminar(@PathVariable Long id, RedirectAttributes ra) {
        equipoService.eliminar(id);
        ra.addFlashAttribute("mensaje", "Equipo eliminado");
        return "redirect:/";
    }
}
