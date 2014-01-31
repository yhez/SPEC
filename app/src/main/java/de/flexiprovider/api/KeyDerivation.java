package de.flexiprovider.api;

import java.security.spec.AlgorithmParameterSpec;

import de.flexiprovider.api.exceptions.InvalidAlgorithmParameterException;
import de.flexiprovider.api.exceptions.InvalidKeyException;


public abstract class KeyDerivation {

    public abstract void init(byte[] secret, AlgorithmParameterSpec params)
            throws InvalidKeyException, InvalidAlgorithmParameterException;

    public abstract byte[] deriveKey(int keySize);

}
