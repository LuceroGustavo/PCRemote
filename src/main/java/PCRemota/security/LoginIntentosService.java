package PCRemota.security;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Control de intentos fallidos de login por IP (en memoria).
 * Después de N fallos, la IP queda bloqueada por un tiempo.
 */
@Service
public class LoginIntentosService {

    private static final int MAX_INTENTOS = 5;
    private static final int BLOQUEO_MINUTOS = 15;

    private final Map<String, Intentos> intentos = new ConcurrentHashMap<>();

    public void registrarFallo(String ip) {
        Intentos i = intentos.computeIfAbsent(ip, k -> new Intentos());
        i.fallos++;
        i.ultimoFallo = LocalDateTime.now();
    }

    public void registrarExito(String ip) {
        intentos.remove(ip);
    }

    public boolean estaBloqueado(String ip) {
        Intentos i = intentos.get(ip);
        if (i == null || i.fallos < MAX_INTENTOS) {
            return false;
        }
        if (i.bloqueadoHasta() == null || LocalDateTime.now().isAfter(i.bloqueadoHasta())) {
            intentos.remove(ip);
            return false;
        }
        return true;
    }

    public int minutosRestantes(String ip) {
        Intentos i = intentos.get(ip);
        if (i == null || i.bloqueadoHasta() == null) {
            return 0;
        }
        long minutos = java.time.Duration.between(LocalDateTime.now(), i.bloqueadoHasta()).toMinutes();
        return (int) Math.max(1, minutos);
    }

    private static class Intentos {
        int fallos;
        LocalDateTime ultimoFallo;

        LocalDateTime bloqueadoHasta() {
            if (fallos < MAX_INTENTOS) {
                return null;
            }
            return ultimoFallo.plusMinutes(BLOQUEO_MINUTOS);
        }
    }
}
