package PCRemota.service;

import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

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
     * Ping en paralelo a varias IPs. Devuelve ip -> online.
     */
    public Map<String, Boolean> pingParalelo(List<String> ips) {
        ExecutorService pool = Executors.newFixedThreadPool(Math.min(64, Math.max(4, ips.size())));
        try {
            List<Callable<Map.Entry<String, Boolean>>> tareas = ips.stream()
                    .map(ip -> (Callable<Map.Entry<String, Boolean>>) () -> Map.entry(ip, ping(ip)))
                    .toList();
            return pool.invokeAll(tareas).stream()
                    .map(futuro -> {
                        try {
                            return futuro.get();
                        } catch (Exception e) {
                            return Map.entry("", false);
                        }
                    })
                    .filter(entrada -> !entrada.getKey().isEmpty())
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        } catch (Exception e) {
            return ips.stream().collect(Collectors.toMap(Function.identity(), ip -> false));
        } finally {
            pool.shutdownNow();
        }
    }

    /**
     * Genera la lista de IPs de un rango. Acepta "192.168.1.10-254" o "192.168.1.10-192.168.1.254".
     */
    public List<String> generarRango(String rango) {
        String[] partes = rango.trim().split("-");
        if (partes.length != 2) {
            throw new IllegalArgumentException("Rango inválido. Formato: 192.168.1.10-254");
        }
        String inicio = partes[0].trim();
        String fin = partes[1].trim();
        String[] octetosInicio = inicio.split("\\.");
        String[] octetosFin = fin.contains(".") ? fin.split("\\.") : null;

        int desde = Integer.parseInt(octetosInicio[3]);
        int hasta;
        String base;
        if (octetosFin == null) {
            hasta = Integer.parseInt(fin);
            base = inicio.substring(0, inicio.lastIndexOf('.'));
        } else {
            hasta = Integer.parseInt(octetosFin[3]);
            base = inicio.substring(0, inicio.lastIndexOf('.'));
            if (!fin.substring(0, fin.lastIndexOf('.')).equals(base)) {
                throw new IllegalArgumentException("El rango debe estar dentro de la misma red");
            }
        }
        if (desde < 1 || hasta > 254 || desde > hasta) {
            throw new IllegalArgumentException("Rango inválido: debe estar entre 1 y 254");
        }
        java.util.List<String> ips = new java.util.ArrayList<>();
        for (int i = desde; i <= hasta; i++) {
            ips.add(base + "." + i);
        }
        return ips;
    }

    /**
     * Escanea un rango de IPs y devuelve las que responden (en paralelo).
     */
    public List<String> escanearRed(String rango) {
        List<String> ips = generarRango(rango);
        Map<String, Boolean> estado = pingParalelo(ips);
        return estado.entrySet().stream()
                .filter(Map.Entry::getValue)
                .map(Map.Entry::getKey)
                .sorted()
                .toList();
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
