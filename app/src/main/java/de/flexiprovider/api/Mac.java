package de.flexiprovider.api;

import java.security.InvalidKeyException;
import java.security.spec.AlgorithmParameterSpec;

import de.flexiprovider.api.keys.SecretKey;


public abstract class Mac extends javax.crypto.MacSpi {

    protected final int engineGetMacLength() {
        return getMacLength();
    }

    protected void engineInit(java.security.Key key,
                              java.security.spec.AlgorithmParameterSpec params)
            throws java.security.InvalidKeyException,
            java.security.InvalidAlgorithmParameterException {

        if (!(key instanceof SecretKey)) {
            throw new java.security.InvalidKeyException();
        }
        init((SecretKey) key, params);
    }

    protected final void engineUpdate(byte input) {
        update(input);
    }

    protected final void engineUpdate(byte[] input, int offset, int len) {
        update(input, offset, len);
    }

    protected final byte[] engineDoFinal() {
        return doFinal();
    }

    protected final void engineReset() {
        reset();
    }

    public abstract int getMacLength();

    public final void init(SecretKey key) throws InvalidKeyException {
        try {
            init(key, null);
        } catch (Exception iape) {
            throw new IllegalArgumentException("This MAC needs parameters");
        }
    }

    public abstract void init(SecretKey key, AlgorithmParameterSpec params)
            throws InvalidKeyException;

    public abstract void update(byte input);

    public final void update(byte[] input) {
        update(input, 0, input.length);
    }

    public abstract void update(byte[] input, int offset, int length);

    public abstract byte[] doFinal();

    public final byte[] doFinal(byte[] input) {
        update(input);
        return doFinal();
    }
    public abstract void reset();

}
