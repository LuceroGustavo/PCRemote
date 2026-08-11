package PCRemota.security;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.platform.win32.WinBase;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIOptions;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Lee credenciales guardadas en el Administrador de Credenciales de Windows
 * (las que guarda cmdkey /add, el RDP con "recordar", etc.).
 * Solo las de tipo genérico exponen la contraseña (DPAPI de la misma sesión).
 */
@Service
public class CredencialesWindowsService {

    public static final int CRED_TYPE_GENERIC = 1;

    private interface CredApi extends StdCallLibrary {
        CredApi INSTANCE = Native.load("Advapi32", CredApi.class, W32APIOptions.UNICODE_OPTIONS);

        boolean CredEnumerateW(String filter, int flags, IntByReference count, PointerByReference credentials);

        boolean CredReadW(String target, int type, int flags, PointerByReference credential);

        void CredFree(Pointer buffer);
    }

    @Structure.FieldOrder({"Flags", "Type", "TargetName", "Comment", "LastWritten",
            "CredentialBlobSize", "CredentialBlob", "Persist", "AttributeCount",
            "Attributes", "TargetAlias", "UserName"})
    public static class CREDENTIALW extends Structure {
        public int Flags;
        public int Type;
        public Pointer TargetName;
        public Pointer Comment;
        public WinBase.FILETIME LastWritten;
        public int CredentialBlobSize;
        public Pointer CredentialBlob;
        public int Persist;
        public int AttributeCount;
        public Pointer Attributes;
        public Pointer TargetAlias;
        public Pointer UserName;

        public CREDENTIALW() {
        }

        public CREDENTIALW(Pointer p) {
            super(p);
            read();
        }
    }

    public static class CredencialWindows {
        public final String target;
        public final String usuario;
        public final int tipo;
        public final String password;

        public CredencialWindows(String target, String usuario, int tipo, String password) {
            this.target = target;
            this.usuario = usuario;
            this.tipo = tipo;
            this.password = password;
        }

        /**
         * Destino limpio (sin prefijos "LegacyGeneric:target=" ni "TERMSRV/") para mostrar y nombrar.
         */
        public String destinoLimpio() {
            return limpiarTarget(target);
        }

        public String nombreTipo() {
            return switch (tipo) {
                case 1 -> "Genérica";
                case 2 -> "Contraseña de dominio";
                case 3 -> "Certificado de dominio";
                case 4 -> "Contraseña visible de dominio";
                default -> "Tipo " + tipo;
            };
        }

        public boolean tienePassword() {
            return password != null && !password.isBlank();
        }
    }

    /**
     * Enumerar todas las credenciales del Administrador de Credenciales.
     */
    public List<CredencialWindows> listar() {
        List<CredencialWindows> resultado = new ArrayList<>();
        IntByReference count = new IntByReference();
        PointerByReference creds = new PointerByReference();
        if (!CredApi.INSTANCE.CredEnumerateW(null, 0, count, creds)) {
            return resultado;
        }
        try {
            int n = count.getValue();
            if (n > 0) {
                Pointer[] punteros = creds.getValue().getPointerArray(0, n);
                for (Pointer p : punteros) {
                    CREDENTIALW c = new CREDENTIALW(p);
                    String target = leer(c.TargetName);
                    String usuario = leer(c.UserName);
                    String password = leerBlob(c.CredentialBlob, c.CredentialBlobSize);
                    if (target != null) {
                        resultado.add(new CredencialWindows(target, usuario, c.Type, password));
                    }
                }
            }
        } finally {
            CredApi.INSTANCE.CredFree(creds.getValue());
        }
        return resultado;
    }

    /**
     * Leer una credencial puntual por su target (para importar del lado del servidor).
     */
    public CredencialWindows leer(String target) {
        PointerByReference cred = new PointerByReference();
        if (!CredApi.INSTANCE.CredReadW(target, CRED_TYPE_GENERIC, 0, cred)) {
            return null;
        }
        try {
            CREDENTIALW c = new CREDENTIALW(cred.getValue());
            return new CredencialWindows(leer(c.TargetName),
                    leer(c.UserName), c.Type,
                    leerBlob(c.CredentialBlob, c.CredentialBlobSize));
        } finally {
            CredApi.INSTANCE.CredFree(cred.getValue());
        }
    }

    /**
     * Quita prefijos del target ("legacygeneric:", "TERMSRV/") y sufijos de puerto.
     */
    public static String limpiarTarget(String target) {
        if (target == null) {
            return null;
        }
        String t = target;
        if (t.toLowerCase().startsWith("legacygeneric:")) {
            t = t.substring("legacygeneric:".length());
        }
        if (t.toLowerCase().startsWith("termsrv/")) {
            t = t.substring("termsrv/".length());
        }
        if (t.matches(".*:\\d+")) {
            t = t.substring(0, t.lastIndexOf(':'));
        }
        return t.trim().isEmpty() ? null : t.trim();
    }

    private String leer(Pointer p) {
        if (p == null) {
            return null;
        }
        String s = p.getWideString(0);
        return (s == null || s.isBlank()) ? null : s.trim();
    }

    private String leerBlob(Pointer blob, int size) {
        if (blob == null || size <= 0) {
            return null;
        }
        byte[] data = blob.getByteArray(0, size);
        String s = new String(data, StandardCharsets.UTF_16LE);
        while (s.endsWith("\u0000")) {
            s = s.substring(0, s.length() - 1);
        }
        return s.isBlank() ? null : s;
    }
}
