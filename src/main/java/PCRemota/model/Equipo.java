package PCRemota.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Entity
@Table(name = "equipos")
public class Equipo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    @Column(nullable = false)
    private String nombre;

    @NotBlank(message = "La IP es obligatoria")
    @Column(nullable = false)
    private String ip;

    @Column(nullable = false)
    private int puertoRdp = 3389;

    private String usuario;

    @Column(length = 512)
    private String passCifrada;

    private String so;

    private String ubicacion;

    @Column(length = 1024)
    private String notas;

    @Column(name = "recurso_compartido")
    private String recursoCompartido;

    @Column(name = "ultimo_acceso")
    private LocalDateTime ultimoAcceso;

    private boolean online;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getIp() { return ip; }
    public void setIp(String ip) { this.ip = ip; }

    public int getPuertoRdp() { return puertoRdp; }
    public void setPuertoRdp(int puertoRdp) { this.puertoRdp = puertoRdp; }

    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }

    public String getPassCifrada() { return passCifrada; }
    public void setPassCifrada(String passCifrada) { this.passCifrada = passCifrada; }

    public String getSo() { return so; }
    public void setSo(String so) { this.so = so; }

    public String getUbicacion() { return ubicacion; }
    public void setUbicacion(String ubicacion) { this.ubicacion = ubicacion; }

    public String getNotas() { return notas; }
    public void setNotas(String notas) { this.notas = notas; }

    public String getRecursoCompartido() { return recursoCompartido; }
    public void setRecursoCompartido(String recursoCompartido) { this.recursoCompartido = recursoCompartido; }

    public LocalDateTime getUltimoAcceso() { return ultimoAcceso; }
    public void setUltimoAcceso(LocalDateTime ultimoAcceso) { this.ultimoAcceso = ultimoAcceso; }

    public boolean isOnline() { return online; }
    public void setOnline(boolean online) { this.online = online; }

    /** Tipo derivado del SO: "SERVER" o "WORKSTATION". */
    @jakarta.persistence.Transient
    public String getTipo() {
        if (so != null && (so.toLowerCase().contains("server") || so.toLowerCase().contains("ubuntu")
                || so.toLowerCase().contains("linux") || so.toLowerCase().contains("rhel")
                || so.toLowerCase().contains("core"))) {
            return "SERVER";
        }
        return "WORKSTATION";
    }
}
