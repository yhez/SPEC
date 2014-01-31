package de.flexiprovider.api;

import java.security.spec.AlgorithmParameterSpec;

import de.flexiprovider.api.exceptions.BadPaddingException;
import de.flexiprovider.api.exceptions.InvalidAlgorithmParameterException;
import de.flexiprovider.api.exceptions.InvalidKeyException;
import de.flexiprovider.api.exceptions.ShortBufferException;
import de.flexiprovider.api.keys.Key;

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
        return opMode == ENCRYPT_MODE ? encryptOutputSize(inLen)
                : decryptOutputSize(inLen);
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

    protected abstract int encryptOutputSize(int inLen);


    protected abstract int decryptOutputSize(int inLen);

    protected abstract void initCipherEncrypt(Key key,
                                              AlgorithmParameterSpec params, SecureRandom sr)
            throws InvalidKeyException, InvalidAlgorithmParameterException;


    protected abstract void initCipherDecrypt(Key key,
                                              AlgorithmParameterSpec params) throws InvalidKeyException,
            InvalidAlgorithmParameterException;

}
