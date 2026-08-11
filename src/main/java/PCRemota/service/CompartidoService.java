package PCRemota.service;

import PCRemota.model.Equipo;
import PCRemota.security.CryptoService;
import org.springframework.stereotype.Service;

/**
 * Monta y abre recursos compartidos de Windows (\\IP\c$, etc.)
 * con las credenciales guardadas, sin controlar el equipo.
 */
@Service
public class CompartidoService {

    private final CryptoService cryptoService;
    private final AuditoriaService auditoriaService;
    private final RedService redService;

    public CompartidoService(CryptoService cryptoService, AuditoriaService auditoriaService, RedService redService) {
        this.cryptoService = cryptoService;
        this.auditoriaService = auditoriaService;
        this.redService = redService;
    }

    /**
     * Conecta al recurso compartido y abre el Explorador de Windows.
     * Si no se especifica recurso, usa la letra C: (\\IP\c$).
     */
    public String abrirCompartido(Equipo equipo, String recurso, String usuarioActual) {
        if (equipo.getUsuario() == null || equipo.getPassCifrada() == null) {
            return "error: el equipo no tiene credenciales guardadas";
        }

        String ruta = "\\\\" + equipo.getIp() + "\\" + (recurso == null || recurso.isBlank() ? "c$" : recurso);
        String pass = cryptoService.desencriptar(equipo.getPassCifrada());

        // Eliminar unidad mapeada previa si existía
        redService.ejecutarComando("net.exe", "use", "P: /delete /y");

        String resultado = redService.ejecutarComando("net.exe", "use", "P:",
                ruta, "/user:" + equipo.getUsuario(), pass);

        if (resultado.toLowerCase().contains("no se pudo") || resultado.contains("error")
                || resultado.contains("System error")) {
            return "error: " + resultado;
        }

        try {
            new ProcessBuilder("explorer.exe", "P:\\").start();
        } catch (Exception e) {
            return "error: no se pudo abrir el Explorador: " + e.getMessage();
        }

        equipo.setUltimoAcceso(java.time.LocalDateTime.now());
        auditoriaService.registrar(usuarioActual, "COMPARTIDO", equipo.getId(), "Acceso a " + ruta);
        return "ok: compartido " + ruta + " montado en P:";
    }
}
