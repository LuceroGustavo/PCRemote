package PCRemota.service;

import PCRemota.model.Equipo;
import PCRemota.security.CryptoService;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * Lanza el Escritorio Remoto de Windows (mstsc) con las credenciales
 * guardadas mediante cmdkey y un archivo .rdp temporal.
 */
@Service
public class RdpService {

    private static final String RDP_TEMP = "data/rdp";

    private final CryptoService cryptoService;
    private final AuditoriaService auditoriaService;
    private final RedService redService;

    public RdpService(CryptoService cryptoService, AuditoriaService auditoriaService, RedService redService) {
        this.cryptoService = cryptoService;
        this.auditoriaService = auditoriaService;
        this.redService = redService;
    }

    /**
     * Lanza la sesión RDP. Devuelve un mensaje de estado.
     */
    public String conectar(Equipo equipo, String usuarioActual) {
        if (equipo.getUsuario() == null || equipo.getPassCifrada() == null) {
            return "error: el equipo no tiene credenciales guardadas";
        }
        if (!redService.puertoAbierto(equipo.getIp(), equipo.getPuertoRdp())) {
            return "error: el puerto RDP (" + equipo.getPuertoRdp() + ") de " + equipo.getIp() + " no responde";
        }

        String pass = cryptoService.desencriptar(equipo.getPassCifrada());

        try {
            // Guardar credencial en el almacén de Windows (por sesión)
            redService.ejecutarComando("cmdkey.exe", "/generic:" + equipo.getIp(),
                    "/user:" + equipo.getUsuario(), "/pass:" + pass);

            // Generar archivo .rdp
            Path dir = Paths.get(RDP_TEMP).toAbsolutePath();
            Files.createDirectories(dir);
            Path rdpFile = dir.resolve("equipo-" + equipo.getId() + "-" + UUID.randomUUID() + ".rdp");
            Files.write(rdpFile, contenidoRdp(equipo).getBytes(StandardCharsets.UTF_8));

            new ProcessBuilder("mstsc.exe", rdpFile.toString()).start();

            equipo.setUltimoAcceso(java.time.LocalDateTime.now());
            auditoriaService.registrar(usuarioActual, "RDP", equipo.getId(), "Conexión RDP a " + equipo.getIp());
            return "ok: sesión RDP lanzada";
        } catch (IOException e) {
            return "error: no se pudo lanzar la sesión: " + e.getMessage();
        }
    }

    private String contenidoRdp(Equipo equipo) {
        StringBuilder sb = new StringBuilder();
        sb.append("screen mode id:i:2\n");
        sb.append("use multimon:i:0\n");
        sb.append("desktopwidth:i:1920\n");
        sb.append("desktopheight:i:1080\n");
        sb.append("session bpp:i:32\n");
        sb.append("winposstr:s:0,1,0,0,1920,1080\n");
        sb.append("full address:s:").append(equipo.getIp()).append(":").append(equipo.getPuertoRdp()).append("\n");
        sb.append("prompt for credentials:i:0\n");
        sb.append("username:s:").append(equipo.getUsuario()).append("\n");
        sb.append("authentication level:i:0\n");
        sb.append("redirectclipboard:i:1\n");
        sb.append("redirectprinters:i:1\n");
        sb.append("redirectsmartcards:i:1\n");
        sb.append("allow font smoothing:i:1\n");
        return sb.toString();
    }
}
