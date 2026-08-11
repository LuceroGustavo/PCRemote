package PCRemota.config;

import PCRemota.model.Equipo;
import PCRemota.model.Usuario;
import PCRemota.repository.EquipoRepository;
import PCRemota.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataSeeder {

    @Bean
    public CommandLineRunner seed(UsuarioRepository usuarioRepository,
                                  EquipoRepository equipoRepository,
                                  PasswordEncoder passwordEncoder) {
        return args -> {
            if (usuarioRepository.count() == 0) {
                Usuario admin = new Usuario();
                admin.setUsername("admin");
                admin.setPasswordHash(passwordEncoder.encode("admin"));
                admin.setRol("ADMIN");
                admin.setActivo(true);
                usuarioRepository.save(admin);
                System.out.println("[PCRemota] Usuario por defecto creado -> usuario: admin / contraseña: admin");
            }

            if (equipoRepository.count() == 0) {
                equipoRepository.save(ejemplo("Servidor-01", "192.168.1.10", "Administrator", "Windows Server 2022", "Servidor", "Servidor principal de archivos"));
                equipoRepository.save(ejemplo("PC-Oficina-01", "192.168.1.20", "Administrator", "Windows 11 Pro", "Oficina", "PC de administración"));
                System.out.println("[PCRemota] Equipos de ejemplo creados");
            }
        };
    }

    private Equipo ejemplo(String nombre, String ip, String usuario, String so, String ubicacion, String notas) {
        Equipo e = new Equipo();
        e.setNombre(nombre);
        e.setIp(ip);
        e.setUsuario(usuario);
        e.setSo(so);
        e.setUbicacion(ubicacion);
        e.setNotas(notas);
        e.setRecursoCompartido("c$");
        e.setPuertoRdp(3389);
        e.setPassCifrada(null);
        return e;
    }
}
