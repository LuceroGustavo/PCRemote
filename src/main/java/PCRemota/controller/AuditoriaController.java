package PCRemota.controller;

import PCRemota.repository.AuditoriaRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuditoriaController {

    private final AuditoriaRepository auditoriaRepository;

    public AuditoriaController(AuditoriaRepository auditoriaRepository) {
        this.auditoriaRepository = auditoriaRepository;
    }

    @GetMapping("/auditoria")
    public String ver(Model model) {
        model.addAttribute("registros", auditoriaRepository.findTop50ByOrderByFechaDesc());
        return "auditoria";
    }
}
