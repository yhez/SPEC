package de.flexiprovider.api;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;

import javax.crypto.BadPaddingException;
import javax.crypto.ShortBufferException;

public abstract class AsymmetricHybridCipher extends Cipher {

    protected final void setMode(String modeName) {
        // empty
    }

    protected final void setPadding(String paddingName) {
        // empty
    }

    public final byte[] getIV() {
        return null;
    }
    public final int getBlockSize() {
        return 0;
    }


    public final AlgorithmParameterSpec getParameters() {
        return null;
    }


    public final int getOutputSize(int inLen) {
        return opMode == ENCRYPT_MODE ? encryptOutputSize()
                : decryptOutputSize();
    }


    public final void initEncrypt(Key key, AlgorithmParameterSpec params,
                                  SecureRandom random) throws InvalidKeyException,
            InvalidAlgorithmParameterException {
        opMode = ENCRYPT_MODE;
        initCipherEncrypt(key, params, random);
    }


    public final void initDecrypt(Key key, AlgorithmParameterSpec params)
            throws InvalidKeyException, InvalidAlgorithmParameterException {
        opMode = DECRYPT_MODE;
        initCipherDecrypt(key, params);
    }

    public abstract byte[] update(byte[] input, int inOff, int inLen);


    public final int update(byte[] input, int inOff, int inLen, byte[] output,
                            int outOff) throws ShortBufferException {
        if (output.length < getOutputSize(inLen)) {
            throw new ShortBufferException("output");
        }
        byte[] out = update(input, inOff, inLen);
        System.arraycopy(out, 0, output, outOff, out.length);
        return out.length;
    }


    public abstract byte[] doFinal(byte[] input, int inOff, int inLen)
            throws BadPaddingException;


    public final int doFinal(byte[] input, int inOff, int inLen, byte[] output,
                             int outOff) throws ShortBufferException, BadPaddingException {

        if (output.length < getOutputSize(inLen)) {
            throw new ShortBufferException("Output buffer too short.");
        }
        byte[] out = doFinal(input, inOff, inLen);
        System.arraycopy(out, 0, output, outOff, out.length);
        return out.length;
    }

    protected abstract int encryptOutputSize();


    protected abstract int decryptOutputSize();

    protected abstract void initCipherEncrypt(Key key,
                                              AlgorithmParameterSpec params, SecureRandom sr)
            throws InvalidKeyException, InvalidAlgorithmParameterException;


    protected abstract void initCipherDecrypt(Key key,
                                              AlgorithmParameterSpec params) throws InvalidKeyException,
            InvalidAlgorithmParameterException;

}
