package de.flexiprovider.api;

import java.security.spec.AlgorithmParameterSpec;

import de.flexiprovider.api.exceptions.InvalidAlgorithmParameterException;
import de.flexiprovider.api.exceptions.InvalidKeyException;
import de.flexiprovider.api.exceptions.SignatureException;
import de.flexiprovider.api.keys.PrivateKey;
import de.flexiprovider.api.keys.PublicKey;
import de.flexiprovider.common.util.JavaSecureRandomWrapper;

public abstract class Signature extends java.security.SignatureSpi {




    protected final Object engineGetParameter(String param)
            throws java.security.InvalidParameterException {
        // method is deprecated
        return null;
    }


    protected final void engineSetParameter(String param, Object value)
            throws java.security.InvalidParameterException {
        // method is deprecated
    }

    protected final void engineInitSign(java.security.PrivateKey privateKey)
            throws java.security.InvalidKeyException {
        if ((privateKey == null) || !(privateKey instanceof PrivateKey)) {
            throw new java.security.InvalidKeyException();
        }
        initSign((PrivateKey) privateKey);
    }

    protected final void engineInitSign(java.security.PrivateKey privateKey,
                                        java.security.SecureRandom javaRand)
            throws java.security.InvalidKeyException {
        if ((privateKey == null) || !(privateKey instanceof PrivateKey)) {
            throw new java.security.InvalidKeyException();
        }
        SecureRandom flexiRand = new JavaSecureRandomWrapper(javaRand);
        initSign((PrivateKey) privateKey, flexiRand);
    }

    protected final void engineInitVerify(java.security.PublicKey publicKey)
            throws java.security.InvalidKeyException {
        if ((publicKey == null) || !(publicKey instanceof PublicKey)) {
            throw new java.security.InvalidKeyException();
        }
        initVerify((PublicKey) publicKey);
    }

    protected void engineSetParameter(
            java.security.spec.AlgorithmParameterSpec params)
            throws java.security.InvalidAlgorithmParameterException {
        if (params != null && !(params instanceof AlgorithmParameterSpec)) {
            throw new java.security.InvalidAlgorithmParameterException();
        }
        setParameters(params);
    }

    protected final void engineUpdate(byte b)
            throws java.security.SignatureException {
        update(b);
    }

    protected final void engineUpdate(byte[] b, int off, int len)
            throws java.security.SignatureException {
        update(b, off, len);
    }

    protected final byte[] engineSign() throws java.security.SignatureException {
        return sign();
    }

    protected final boolean engineVerify(byte[] sigBytes)
            throws java.security.SignatureException {
        return verify(sigBytes);
    }

    protected final boolean engineVerify(byte[] sigBytes, int offset, int length)
            throws java.security.SignatureException {
        return verify(sigBytes, offset, length);
    }




    public final void initSign(PrivateKey privKey) throws InvalidKeyException {
        initSign(privKey, Registry.getSecureRandom());
    }


    public abstract void initSign(PrivateKey privKey, SecureRandom random)
            throws InvalidKeyException;


    public abstract void initVerify(PublicKey pubKey)
            throws InvalidKeyException;


    public abstract void setParameters(AlgorithmParameterSpec params)
            throws InvalidAlgorithmParameterException;


    public abstract void update(byte input) throws SignatureException;


    public abstract void update(byte[] input, int inOff, int inLen)
            throws SignatureException;

    public abstract byte[] sign() throws SignatureException;


    public abstract boolean verify(byte[] signature) throws SignatureException;


    public final boolean verify(byte[] signature, int sigOff, int sigLen)
            throws SignatureException {
        byte[] sig = new byte[sigLen];
        System.arraycopy(signature, sigOff, sig, 0, sigLen);
        return verify(sig);
    }

}
