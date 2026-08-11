package PCRemota.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Corta la petición de login si la IP está bloqueada por intentos fallidos.
 */
@Component
public class LoginBloqueoFilter extends OncePerRequestFilter {

    private final LoginIntentosService loginIntentosService;

    public LoginBloqueoFilter(LoginIntentosService loginIntentosService) {
        this.loginIntentosService = loginIntentosService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if ("POST".equalsIgnoreCase(request.getMethod())
                && request.getRequestURI().endsWith("/login")
                && loginIntentosService.estaBloqueado(clienteIp(request))) {
            response.sendRedirect("/login?bloqueado");
            return;
        }
        filterChain.doFilter(request, response);
    }

    private String clienteIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
