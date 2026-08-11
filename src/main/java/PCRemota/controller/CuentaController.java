package PCRemota.controller;

import PCRemota.model.Usuario;
import PCRemota.repository.UsuarioRepository;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class CuentaController {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public CuentaController(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/cuenta")
    public String formulario() {
        return "cuenta";
    }

    @PostMapping("/cuenta/cambiar")
    public String cambiar(@RequestParam String actual,
                          @RequestParam String nueva,
                          @RequestParam String confirmacion,
                          @AuthenticationPrincipal UserDetails user,
                          Model model,
                          RedirectAttributes ra) {
        Usuario usuario = usuarioRepository.findByUsername(user.getUsername()).orElse(null);
        if (usuario == null) {
            return "redirect:/login";
        }
        if (!passwordEncoder.matches(actual, usuario.getPasswordHash())) {
            model.addAttribute("error", "La contraseña actual es incorrecta");
            return "cuenta";
        }
        if (!nueva.equals(confirmacion)) {
            model.addAttribute("error", "La nueva contraseña y su confirmación no coinciden");
            return "cuenta";
        }
        if (nueva.length() < 4) {
            model.addAttribute("error", "La nueva contraseña debe tener al menos 4 caracteres");
            return "cuenta";
        }
        usuario.setPasswordHash(passwordEncoder.encode(nueva));
        usuarioRepository.save(usuario);
        ra.addFlashAttribute("mensaje", "Contraseña cambiada correctamente");
        return "redirect:/";
    }
}
