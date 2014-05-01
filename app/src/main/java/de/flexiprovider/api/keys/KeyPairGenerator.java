package de.flexiprovider.api.keys;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidParameterException;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;


public abstract class KeyPairGenerator extends
        java.security.KeyPairGeneratorSpi {



    public final java.security.KeyPair generateKeyPair() {
        return genKeyPair().pair;
    }

    public abstract void initialize(AlgorithmParameterSpec params,
                                    SecureRandom random) throws InvalidAlgorithmParameterException;

    public abstract void initialize(int keysize, SecureRandom random)
            throws InvalidParameterException;

    public abstract KeyPair genKeyPair();

}
