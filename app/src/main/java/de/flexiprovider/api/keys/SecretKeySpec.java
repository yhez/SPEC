package de.flexiprovider.api.keys;

public class SecretKeySpec extends javax.crypto.spec.SecretKeySpec implements
        java.security.spec.KeySpec {

    public SecretKeySpec(byte[] key, String algorithm) {
        super(key, algorithm);
    }

}
