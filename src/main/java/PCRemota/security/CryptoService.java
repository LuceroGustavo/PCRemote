package PCRemota.security;

import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Cifrado de credenciales con AES-256-GCM.
 * Cada equipo usa una clave derivada (HKDF-SHA256) de la clave maestra
 * con una sal aleatoria única: formato "sal:iv:cifrado".
 */
@Service
public class CryptoService {

    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int IV_SIZE = 12;
    private static final int SALT_SIZE = 16;

    private final MasterKeyService masterKeyService;

    public CryptoService(MasterKeyService masterKeyService) {
        this.masterKeyService = masterKeyService;
    }

    public String encriptar(String textoPlano) {
        try {
            byte[] salt = randomBytes(SALT_SIZE);
            byte[] iv = randomBytes(IV_SIZE);
            SecretKeySpec key = derivarClave(salt);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(128, iv));
            byte[] cifrado = cipher.doFinal(textoPlano.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(salt) + ":"
                    + Base64.getEncoder().encodeToString(iv) + ":"
                    + Base64.getEncoder().encodeToString(cifrado);
        } catch (Exception e) {
            throw new IllegalStateException("Error al cifrar", e);
        }
    }

    public String desencriptar(String almacenado) {
        try {
            String[] partes = almacenado.split(":");
            if (partes.length != 3) {
                throw new IllegalArgumentException("Formato de credencial inválido");
            }
            byte[] salt = Base64.getDecoder().decode(partes[0]);
            byte[] iv = Base64.getDecoder().decode(partes[1]);
            byte[] cifrado = Base64.getDecoder().decode(partes[2]);
            SecretKeySpec key = derivarClave(salt);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(128, iv));
            return new String(cipher.doFinal(cifrado), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException("Error al descifrar", e);
        }
    }

    private byte[] randomBytes(int size) {
        byte[] b = new byte[size];
        new SecureRandom().nextBytes(b);
        return b;
    }

    private SecretKeySpec derivarClave(byte[] salt) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(masterKeyService.getMasterKey(), "HmacSHA256"));
        byte[] prk = mac.doFinal(salt);
        mac.init(new SecretKeySpec(prk, "HmacSHA256"));
        byte[] info = "pcremota-equipo".getBytes(StandardCharsets.UTF_8);
        byte[] okm = new byte[info.length + 1];
        System.arraycopy(info, 0, okm, 0, info.length);
        okm[info.length] = 1;
        byte[] derivada = mac.doFinal(okm);
        return new SecretKeySpec(derivada, "AES");
    }
}
