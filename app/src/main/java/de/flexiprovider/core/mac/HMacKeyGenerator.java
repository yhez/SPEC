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

    public static class SHA224 extends HMacKeyGenerator {
        public SHA224() {
            super(64);
        }
    }

    public static class SHA256 extends HMacKeyGenerator {
        public SHA256() {
            super(64);
        }
    }

    public static class SHA384 extends HMacKeyGenerator {
        public SHA384() {
            super(128);
        }
    }

    public static class SHA512 extends HMacKeyGenerator {
        public SHA512() {
            super(128);
        }
    }

    public static class MD4 extends HMacKeyGenerator {
        public MD4() {
            super(64);
        }
    }

    public static class MD5 extends HMacKeyGenerator {
        public MD5() {
            super(64);
        }
    }

    public static class RIPEMD128 extends HMacKeyGenerator {
        public RIPEMD128() {
            super(64);
        }
    }

    public static class RIPEMD160 extends HMacKeyGenerator {
        public RIPEMD160() {
            super(64);
        }
    }

    public static class RIPEMD256 extends HMacKeyGenerator {
        public RIPEMD256() {
            super(64);
        }
    }

    public static class RIPEMD320 extends HMacKeyGenerator {
        public RIPEMD320() {
            super(64);
        }
    }

    public static class Tiger extends HMacKeyGenerator {
        public Tiger() {
            super(64);
        }
    }

    public static class DHA256 extends HMacKeyGenerator {
        public DHA256() {
            super(64);
        }
    }

    public static class FORK256 extends HMacKeyGenerator {
        public FORK256() {
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
