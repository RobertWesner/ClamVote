package io.wesner.robert.cb1060.clamvote.internal;

import io.wesner.robert.cb1060.clamvote.ClamVote;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import org.jspecify.annotations.NullMarked;

import javax.crypto.Cipher;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@RequiredArgsConstructor
@NullMarked
public class CVKeys {
    public final PublicKey publicKey;
    public final PrivateKey privateKey;

    @SneakyThrows({IOException.class, NoSuchAlgorithmException.class, InvalidKeySpecException.class})
    public static CVKeys load() {
        if (ClamVote.plugin == null) throw new NullPointerException("Cannot load RSA keys on unloaded plugin.");

        val dir = new File(ClamVote.plugin.getDataFolder(), "rsa");
        if (!dir.exists() && !dir.mkdirs()) throw new IOException("Failed to create RSA directory.");

        val publicFile = new File(dir, "public.key");
        val privateFile = new File(dir, "private.key");

        if (!publicFile.exists() || !privateFile.exists()) {
            val generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            val pair = generator.generateKeyPair();

            Files.write(publicFile.toPath(), Base64.getEncoder().encode(pair.getPublic().getEncoded()));
            Files.write(privateFile.toPath(), Base64.getEncoder().encode(pair.getPrivate().getEncoded()));

            return new CVKeys(pair.getPublic(), pair.getPrivate());
        }

        val factory = KeyFactory.getInstance("RSA");
        val b64 = Base64.getDecoder();

        return new CVKeys(
            factory.generatePublic(
                new X509EncodedKeySpec(
                    b64.decode(Files.readAllBytes(publicFile.toPath()))
                )
            ),
            factory.generatePrivate(
                new PKCS8EncodedKeySpec(
                    b64.decode(Files.readAllBytes(privateFile.toPath()))
                )
            )
        );
    }

    public byte[] decrypt(byte[] encrypted) throws IOException {
        try {
            val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(
                Cipher.DECRYPT_MODE,
                privateKey
            );

            return cipher.doFinal(encrypted);
        } catch (GeneralSecurityException exception) {
            throw new IOException("Failed to decrypt Votifier packet.", exception);
        }
    }
}
