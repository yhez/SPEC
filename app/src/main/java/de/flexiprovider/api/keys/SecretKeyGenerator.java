package de.flexiprovider.api.keys;

import de.flexiprovider.api.SecureRandom;
import de.flexiprovider.api.exceptions.InvalidAlgorithmParameterException;
import de.flexiprovider.api.parameters.AlgorithmParameterSpec;
import de.flexiprovider.common.util.JavaSecureRandomWrapper;

public abstract class SecretKeyGenerator extends javax.crypto.KeyGeneratorSpi {

    protected final javax.crypto.SecretKey engineGenerateKey() {
        return generateKey();
    }

    protected final void engineInit(java.security.SecureRandom javaRand) {
        init(new JavaSecureRandomWrapper(javaRand));
    }

    protected final void engineInit(int keysize,
                                    java.security.SecureRandom javaRand) {
        init(keysize, new JavaSecureRandomWrapper(javaRand));
    }

    protected void engineInit(java.security.spec.AlgorithmParameterSpec params,
                              java.security.SecureRandom javaRand)
            throws java.security.InvalidAlgorithmParameterException {
        if (params != null && !(params instanceof AlgorithmParameterSpec)) {
            throw new java.security.InvalidAlgorithmParameterException();
        }
        init((AlgorithmParameterSpec) params, new JavaSecureRandomWrapper(
                javaRand));
    }

    public abstract SecretKey generateKey();

    public abstract void init(SecureRandom random);

    public abstract void init(int keySize, SecureRandom random);

    public abstract void init(AlgorithmParameterSpec params, SecureRandom random)
            throws InvalidAlgorithmParameterException;

}
