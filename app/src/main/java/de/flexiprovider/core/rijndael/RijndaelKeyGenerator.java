package de.flexiprovider.core.rijndael;

import de.flexiprovider.api.Registry;
import de.flexiprovider.api.SecureRandom;
import de.flexiprovider.api.exceptions.InvalidAlgorithmParameterException;
import de.flexiprovider.api.keys.SecretKey;
import de.flexiprovider.api.keys.SecretKeyGenerator;
import de.flexiprovider.api.parameters.AlgorithmParameterSpec;

public class RijndaelKeyGenerator extends SecretKeyGenerator {

    // the key size in bits
    private int keySize;

    // the source of randomness
    private SecureRandom random;

    // flag indicating whether the key generator has been initialized
    private boolean initialized;


    public void init(AlgorithmParameterSpec params, SecureRandom random)
            throws InvalidAlgorithmParameterException {

        RijndaelKeyGenParameterSpec rijndaelParams;
        if (params == null) {
            rijndaelParams = new RijndaelKeyGenParameterSpec();
        } else if (params instanceof RijndaelKeyGenParameterSpec) {
            rijndaelParams = (RijndaelKeyGenParameterSpec) params;
        } else {
            throw new InvalidAlgorithmParameterException("unsupported type");
        }

        keySize = rijndaelParams.getKeySize() >> 3;
        this.random = random != null ? random : Registry.getSecureRandom();

        initialized = true;
    }

    public void init(int keySize, SecureRandom random) {
        RijndaelKeyGenParameterSpec params = new RijndaelKeyGenParameterSpec(
                keySize);
        try {
            init(params, random);
        } catch (InvalidAlgorithmParameterException e) {
            // the parameters are correct and must be accepted
            throw new RuntimeException("internal error");
        }
    }

    public void init(SecureRandom random) {
        RijndaelKeyGenParameterSpec defaultParams = new RijndaelKeyGenParameterSpec();
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

        return new RijndaelKey(keyBytes);
    }

}
