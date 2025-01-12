package cryptography;

import com.auth0.jwt.algorithms.Algorithm;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class ECDSA {
    public static boolean verify(String message, String encodedSignature, String publicKey) {
        try {
            PublicKey key = KeyFactory.getInstance("EC")
                    .generatePublic(new X509EncodedKeySpec(Base64.getDecoder().decode(publicKey)));

            Signature ecdsa = Signature.getInstance("SHA256withECDSA");
            ecdsa.initVerify(key);
            ecdsa.update(message.getBytes(StandardCharsets.UTF_8));
            byte[] signature = new BigInteger(encodedSignature, 16).toByteArray();
            return ecdsa.verify(signature);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException | InvalidKeyException | SignatureException e) {
            return false;
        }
    }
}
