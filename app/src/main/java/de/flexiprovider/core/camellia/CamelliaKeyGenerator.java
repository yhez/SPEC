package de.flexiprovider.core.camellia;

import de.flexiprovider.api.Registry;
import de.flexiprovider.api.SecureRandom;
import de.flexiprovider.api.exceptions.InvalidAlgorithmParameterException;
import de.flexiprovider.api.keys.SecretKey;
import de.flexiprovider.api.keys.SecretKeyGenerator;
import de.flexiprovider.api.parameters.AlgorithmParameterSpec;

public class CamelliaKeyGenerator extends SecretKeyGenerator {

    // the key size in bits
    private int keySize;

    // the source of randomness
    private SecureRandom random;

    // flag indicating whether the key generator has been initialized
    private boolean initialized;

    /**
     * Initialize the key generator with the given parameters (which have to be
     * an instance of {@link CamelliaKeyGenParameterSpec}) and a source of
     * randomness. If the parameters are <tt>null</tt>, the
     * {@link CamelliaKeyGenParameterSpec#CamelliaKeyGenParameterSpec() default parameters}
     * are used.
     *
     * @param params the parameters
     * @param random the source of randomness
     * @throws de.flexiprovider.api.exceptions.InvalidAlgorithmParameterException if the parameters are not an instance of
     *                                                                            {@link CamelliaKeyGenParameterSpec}.
     */
    public void init(AlgorithmParameterSpec params, SecureRandom random)
            throws InvalidAlgorithmParameterException {

        CamelliaKeyGenParameterSpec camelliaParams;
        if (params == null) {
            camelliaParams = new CamelliaKeyGenParameterSpec();
        } else if (params instanceof CamelliaKeyGenParameterSpec) {
            camelliaParams = (CamelliaKeyGenParameterSpec) params;
        } else {
            throw new InvalidAlgorithmParameterException("unsupported type");
        }

        keySize = camelliaParams.getKeySize() >> 3;
        this.random = random != null ? random : Registry.getSecureRandom();

        initialized = true;
    }

    /**
     * Initialize the key generator with the given key size and source of
     * randomness.
     *
     * @param keySize the key size
     * @param random  the source of randomness
     */
    public void init(int keySize, SecureRandom random) {
        CamelliaKeyGenParameterSpec params = new CamelliaKeyGenParameterSpec(
                keySize);
        try {
            init(params, random);
        } catch (InvalidAlgorithmParameterException e) {
            // the parameters are correct and must be accepted
            throw new RuntimeException("internal error");
        }
    }

    /**
     * Initialize the key generator with the default parameters and the given
     * source of randomness.
     *
     * @param random the source of randomness
     */
    public void init(SecureRandom random) {
        CamelliaKeyGenParameterSpec defaultParams = new CamelliaKeyGenParameterSpec();
        try {
            init(defaultParams, random);
        } catch (InvalidAlgorithmParameterException e) {
            // the parameters are correct and must be accepted
            throw new RuntimeException("internal error");
        }
    }

    public SecretKey generateKey() {
        if (!initialized) {
            init(Registry.getSecureRandom());
        }

        byte[] keyBytes = new byte[keySize];
        random.nextBytes(keyBytes);

        return new CamelliaKey(keyBytes);
    }

}
