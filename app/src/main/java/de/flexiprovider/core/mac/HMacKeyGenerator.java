package de.flexiprovider.core.mac;

import java.security.spec.AlgorithmParameterSpec;

import de.flexiprovider.api.Registry;
import de.flexiprovider.api.SecureRandom;
import de.flexiprovider.api.keys.SecretKey;
import de.flexiprovider.api.keys.SecretKeyGenerator;

public class HMacKeyGenerator extends SecretKeyGenerator {

    public static class SHA1 extends HMacKeyGenerator {
        public SHA1() {
            super(64);
        }
    }


    // the key size in bits
    private int keySize;

    // the source of randomness
    private SecureRandom random;

    // flag indicating whether the key generator has been initialized
    private boolean initialized;


    protected HMacKeyGenerator(int keySize) {
        this.keySize = keySize;
    }

    public void init(AlgorithmParameterSpec params, SecureRandom random) {
        init(random);
    }

    public void init(int keySize, SecureRandom random) {
        init(random);
    }

    public void init(SecureRandom random) {
        this.random = random != null ? random : Registry.getSecureRandom();
        initialized = true;
    }

    public SecretKey generateKey() {
        if (!initialized) {
            init(Registry.getSecureRandom());
        }

        byte[] keyBytes = new byte[keySize >> 3];
        random.nextBytes(keyBytes);

        return new HMacKey(keyBytes);
    }

}
