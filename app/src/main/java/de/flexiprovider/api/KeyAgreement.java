package de.flexiprovider.api;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;

import javax.crypto.KeyAgreementSpi;
import javax.crypto.ShortBufferException;

import de.flexiprovider.api.keys.Key;
import de.flexiprovider.api.keys.PrivateKey;
import de.flexiprovider.api.keys.PublicKey;
import de.flexiprovider.api.keys.SecretKey;
import de.flexiprovider.common.util.JavaSecureRandomWrapper;

public abstract class KeyAgreement extends KeyAgreementSpi {

    protected final void engineInit(java.security.Key key,
                                    java.security.SecureRandom javaRand)
            throws java.security.InvalidKeyException {

        if (!(key instanceof PrivateKey)) {
            throw new java.security.InvalidKeyException();
        }

        try {
            SecureRandom flexiRand = new JavaSecureRandomWrapper(javaRand);
            init((PrivateKey) key, null, flexiRand);
        } catch (InvalidAlgorithmParameterException e) {
            throw new RuntimeException("algorithm parameters required");
        }
    }

    protected final void engineInit(java.security.Key key,
                                    java.security.spec.AlgorithmParameterSpec params,
                                    java.security.SecureRandom javaRand)
            throws java.security.InvalidKeyException,
            java.security.InvalidAlgorithmParameterException {

        if (!(key instanceof PrivateKey)) {
            throw new java.security.InvalidKeyException();
        }

        SecureRandom flexiRand = new JavaSecureRandomWrapper(javaRand);
        init((PrivateKey) key, params, flexiRand);
    }

    protected final byte[] engineGenerateSecret() throws IllegalStateException {
        return generateSecret();
    }
    protected final int engineGenerateSecret(byte[] sharedSecret, int offset)
            throws IllegalStateException, javax.crypto.ShortBufferException {

        return generateSecret(sharedSecret, offset);
    }
    protected final javax.crypto.SecretKey engineGenerateSecret(String algorithm)
            throws IllegalStateException,
            java.security.NoSuchAlgorithmException {

        return generateSecret(algorithm);
    }
    protected final java.security.Key engineDoPhase(java.security.Key key,
                                                    boolean lastPhase) throws java.security.InvalidKeyException,
            IllegalStateException {

        if (!(key instanceof PublicKey)) {
            throw new java.security.InvalidKeyException();
        }

        return doPhase((PublicKey) key, lastPhase);
    }
    public abstract void init(PrivateKey privKey,
                              AlgorithmParameterSpec params, SecureRandom random)
            throws InvalidKeyException, InvalidAlgorithmParameterException;

    public abstract byte[] generateSecret() throws IllegalStateException;

    public abstract int generateSecret(byte[] sharedSecret, int offset)
            throws IllegalStateException, ShortBufferException;

    public abstract SecretKey generateSecret(String algorithm)
            throws IllegalStateException, NoSuchAlgorithmException;

    public abstract Key doPhase(PublicKey pubKey, boolean lastPhase)
            throws InvalidKeyException, IllegalStateException;

}
