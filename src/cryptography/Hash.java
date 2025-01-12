package cryptography;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Hash {
    public static String sha256(String input) {
        byte[] inputBytes = input.getBytes(StandardCharsets.UTF_8);
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(inputBytes);
            return new BigInteger(1, hash).toString(16).toLowerCase();
        } catch (NoSuchAlgorithmException e) {
            return "Error";
        }
    }

    public static byte[] sha384(byte[] input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-384");
            md.update(input);
            return md.digest();
        } catch (NoSuchAlgorithmException e) {
            return new byte[0];
        }
    }
}
