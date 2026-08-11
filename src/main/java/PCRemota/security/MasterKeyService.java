package PCRemota.security;

import com.sun.jna.platform.win32.Crypt32Util;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;

/**
 * Clave maestra protegida con DPAPI de Windows (CryptProtectData).
 * Solo se puede descifrar con la cuenta de Windows que la generó.
 */
@Service
public class MasterKeyService {

    private static final String KEY_FILE = "data/masterkey.bin";
    private static final int KEY_SIZE = 32;

    private final byte[] masterKey;

    public MasterKeyService() {
        this.masterKey = loadOrCreate();
    }

    private byte[] loadOrCreate() {
        try {
            Path path = Paths.get(KEY_FILE).toAbsolutePath();
            if (Files.exists(path)) {
                byte[] protectedData = Files.readAllBytes(path);
                return Crypt32Util.cryptUnprotectData(protectedData);
            }
            byte[] key = new byte[KEY_SIZE];
            new SecureRandom().nextBytes(key);
            byte[] protectedData = Crypt32Util.cryptProtectData(key);
            if (path.getParent() != null) {
                Files.createDirectories(path.getParent());
            }
            Files.write(path, protectedData);
            return key;
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo inicializar la clave maestra (DPAPI)", e);
        }
    }

    public byte[] getMasterKey() {
        return masterKey;
    }
}
