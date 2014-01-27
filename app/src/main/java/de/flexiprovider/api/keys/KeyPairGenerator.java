package de.flexiprovider.api.keys;

import de.flexiprovider.api.SecureRandom;
import de.flexiprovider.api.exceptions.InvalidAlgorithmParameterException;
import de.flexiprovider.api.exceptions.InvalidParameterException;
import de.flexiprovider.api.parameters.AlgorithmParameterSpec;
import de.flexiprovider.common.util.JavaSecureRandomWrapper;

public abstract class KeyPairGenerator extends
        java.security.KeyPairGeneratorSpi {

    public void initialize(java.security.spec.AlgorithmParameterSpec params,
                           java.security.SecureRandom javaRand)
            throws java.security.InvalidAlgorithmParameterException {

        if (params != null && !(params instanceof AlgorithmParameterSpec)) {
            throw new java.security.InvalidAlgorithmParameterException();
        }
        SecureRandom flexiRand = new JavaSecureRandomWrapper(javaRand);
        initialize((AlgorithmParameterSpec) params, flexiRand);
    }

    public final void initialize(int keysize,
                                 java.security.SecureRandom javaRand)
            throws InvalidParameterException {
        SecureRandom flexiRand = new JavaSecureRandomWrapper(javaRand);
        initialize(keysize, flexiRand);
    }
    public final java.security.KeyPair generateKeyPair() {
        return genKeyPair().pair;
    }

    public abstract void initialize(AlgorithmParameterSpec params,
                                    SecureRandom random) throws InvalidAlgorithmParameterException;

    public abstract void initialize(int keysize, SecureRandom random)
            throws InvalidParameterException;

    public abstract KeyPair genKeyPair();

}
