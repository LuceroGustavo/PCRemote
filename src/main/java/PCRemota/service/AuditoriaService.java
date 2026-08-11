package PCRemota.service;

import PCRemota.model.Auditoria;
import PCRemota.repository.AuditoriaRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AuditoriaService {

    private final AuditoriaRepository auditoriaRepository;

    public AuditoriaService(AuditoriaRepository auditoriaRepository) {
        this.auditoriaRepository = auditoriaRepository;
    }

    public void registrar(String usuario, String accion, Long equipoId, String detalle) {
        Auditoria a = new Auditoria();
        a.setFecha(LocalDateTime.now());
        a.setUsuario(usuario == null ? "desconocido" : usuario);
        a.setAccion(accion);
        a.setEquipoId(equipoId);
        a.setDetalle(detalle);
        auditoriaRepository.save(a);
    }
}
