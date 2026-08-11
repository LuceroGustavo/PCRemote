package PCRemota.service;

import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

@Service
public class RedService {

    /**
     * Ping ICMP al equipo con timeout corto.
     */
    public boolean ping(String ip) {
        try {
            return InetAddress.getByName(ip).isReachable(3000);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Comprueba si el puerto RDP responde (conexión TCP).
     */
    public boolean puertoAbierto(String ip, int puerto) {
        try (Socket socket = new Socket()) {
            socket.connect(new java.net.InetSocketAddress(ip, puerto), 3000);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Ejecuta un comando de Windows y devuelve su salida.
     */
    public String ejecutarComando(String... comando) {
        try {
            ProcessBuilder pb = new ProcessBuilder(comando);
            pb.redirectErrorStream(true);
            Process proceso = pb.start();
            boolean terminado = proceso.waitFor(10, TimeUnit.SECONDS);
            StringBuilder out = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(proceso.getInputStream()))) {
                String linea;
                while ((linea = reader.readLine()) != null) {
                    out.append(linea).append(System.lineSeparator());
                }
            }
            if (!terminado) {
                proceso.destroy();
            }
            return out.toString().trim();
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
}
