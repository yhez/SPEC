package de.flexiprovider.api;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.spec.AlgorithmParameterSpec;


public abstract class KeyDerivation {

    public abstract void init(byte[] secret, AlgorithmParameterSpec params)
            throws InvalidKeyException, InvalidAlgorithmParameterException;

    public abstract byte[] deriveKey(int keySize);

}
