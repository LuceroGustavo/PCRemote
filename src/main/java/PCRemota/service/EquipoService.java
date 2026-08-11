package PCRemota.service;

import PCRemota.model.Equipo;
import PCRemota.repository.EquipoRepository;
import PCRemota.security.CryptoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class EquipoService {

    private final EquipoRepository equipoRepository;
    private final CryptoService cryptoService;
    private final RedService redService;

    public EquipoService(EquipoRepository equipoRepository, CryptoService cryptoService, RedService redService) {
        this.equipoRepository = equipoRepository;
        this.cryptoService = cryptoService;
        this.redService = redService;
    }

    public List<Equipo> listar() {
        return equipoRepository.findAll();
    }

    public Equipo obtener(Long id) {
        return equipoRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Equipo no encontrado"));
    }

    public Equipo guardar(Equipo equipo, String passPlano) {
        if (passPlano != null && !passPlano.isBlank()) {
            equipo.setPassCifrada(cryptoService.encriptar(passPlano));
        }
        return equipoRepository.save(equipo);
    }

    @Transactional
    public Equipo actualizar(Long id, Equipo equipo, String passPlano) {
        Equipo existente = obtener(id);
        existente.setNombre(equipo.getNombre());
        existente.setIp(equipo.getIp());
        existente.setPuertoRdp(equipo.getPuertoRdp());
        existente.setUsuario(equipo.getUsuario());
        existente.setSo(equipo.getSo());
        existente.setUbicacion(equipo.getUbicacion());
        existente.setNotas(equipo.getNotas());
        existente.setRecursoCompartido(equipo.getRecursoCompartido());
        if (passPlano != null && !passPlano.isBlank()) {
            existente.setPassCifrada(cryptoService.encriptar(passPlano));
        }
        return equipoRepository.save(existente);
    }

    public void eliminar(Long id) {
        equipoRepository.deleteById(id);
    }

    /**
     * Refresca el estado online/offline de todos los equipos (ping + puerto RDP).
     */
    public List<Equipo> refrescarEstado() {
        List<Equipo> equipos = equipoRepository.findAll();
        Map<String, Boolean> estado = redService.pingParalelo(
                equipos.stream().map(Equipo::getIp).toList());
        for (Equipo e : equipos) {
            e.setOnline(estado.getOrDefault(e.getIp(), false));
        }
        return equipos;
    }

    /**
     * Devuelve id -> online de todos los equipos, con pings en paralelo.
     */
    public Map<Long, Boolean> estadoTodos() {
        List<Equipo> equipos = equipoRepository.findAll();
        Map<String, Boolean> estado = redService.pingParalelo(
                equipos.stream().map(Equipo::getIp).toList());
        Map<Long, Boolean> resultado = new java.util.HashMap<>();
        for (Equipo e : equipos) {
            resultado.put(e.getId(), estado.getOrDefault(e.getIp(), false));
        }
        return resultado;
    }

    public String revelarPassword(Long id, String usuarioActual, AuditoriaService auditoriaService) {
        Equipo e = obtener(id);
        String pass = cryptoService.desencriptar(e.getPassCifrada());
        auditoriaService.registrar(usuarioActual, "VER_PASSWORD", id,
                "Reveló la contraseña de " + e.getNombre());
        return pass;
    }
}
