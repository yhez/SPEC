package de.flexiprovider.api;

import java.io.ByteArrayOutputStream;
import java.security.spec.AlgorithmParameterSpec;

import de.flexiprovider.api.exceptions.BadPaddingException;
import de.flexiprovider.api.exceptions.IllegalBlockSizeException;
import de.flexiprovider.api.exceptions.InvalidAlgorithmParameterException;
import de.flexiprovider.api.exceptions.InvalidKeyException;
import de.flexiprovider.api.exceptions.ShortBufferException;
import de.flexiprovider.api.keys.Key;


public abstract class AsymmetricBlockCipher extends Cipher {

    protected ByteArrayOutputStream buf;

    protected int maxPlainTextSize;

    protected int cipherTextSize;

    public AsymmetricBlockCipher() {
        buf = new ByteArrayOutputStream();
    }


    public final int getBlockSize() {
        return opMode == ENCRYPT_MODE ? maxPlainTextSize : cipherTextSize;
    }
    public final byte[] getIV() {
        return null;
    }


    public final int getOutputSize(int inLen) {

        int totalLen = inLen + buf.size();

        int maxLen = getBlockSize();

        if (totalLen > maxLen) {
            return 0;
        }

        return maxLen;
    }


    public final AlgorithmParameterSpec getParameters() {
        return null;
    }


    public final void initEncrypt(Key key, AlgorithmParameterSpec params,
                                  SecureRandom secureRandom) throws InvalidKeyException,
            InvalidAlgorithmParameterException {
        opMode = ENCRYPT_MODE;
        initCipherEncrypt(key, params, secureRandom);
    }


    public final void initDecrypt(Key key, AlgorithmParameterSpec params)
            throws InvalidKeyException, InvalidAlgorithmParameterException {
        opMode = DECRYPT_MODE;
        initCipherDecrypt(key, params);
    }


    public final byte[] update(byte[] input, int inOff, int inLen) {
        if (inLen != 0) {
            buf.write(input, inOff, inLen);
        }
        return new byte[0];
    }


    public final int update(byte[] input, int inOff, int inLen, byte[] output,
                            int outOff) {
        update(input, inOff, inLen);
        return 0;
    }


    public final byte[] doFinal(byte[] input, int inOff, int inLen)
            throws IllegalBlockSizeException, BadPaddingException {

        checkLength(inLen);
        update(input, inOff, inLen);
        byte[] mBytes = buf.toByteArray();
        buf.reset();

        switch (opMode) {
            case ENCRYPT_MODE:
                return messageEncrypt(mBytes);

            case DECRYPT_MODE:
                return messageDecrypt(mBytes);

            default:
                return null;

        }
    }


    public final int doFinal(byte[] input, int inOff, int inLen, byte[] output,
                             int outOff) throws ShortBufferException, IllegalBlockSizeException,
            BadPaddingException {

        if (output.length < getOutputSize(inLen)) {
            throw new ShortBufferException("Output buffer too short.");
        }

        byte[] out = doFinal(input, inOff, inLen);
        System.arraycopy(out, 0, output, outOff, out.length);
        return out.length;
    }


    protected final void setMode(String modeName) {
        // empty
    }


    protected final void setPadding(String paddingName) {
        // empty
    }


    protected void checkLength(int inLen) throws IllegalBlockSizeException {

        int inLength = inLen + buf.size();

        if (opMode == ENCRYPT_MODE) {
            if (inLength > maxPlainTextSize) {
                throw new IllegalBlockSizeException(
                        "The length of the plaintext (" + inLength
                                + " bytes) is not supported by "
                                + "the cipher (max. " + maxPlainTextSize
                                + " bytes).");
            }
        } else if (opMode == DECRYPT_MODE) {
            if (inLength != cipherTextSize) {
                throw new IllegalBlockSizeException(
                        "Illegal ciphertext length (expected " + cipherTextSize
                                + " bytes, was " + inLength + " bytes).");
            }
        }

    }


    protected abstract void initCipherEncrypt(Key key,
                                              AlgorithmParameterSpec params, SecureRandom sr)
            throws InvalidKeyException, InvalidAlgorithmParameterException;


    protected abstract void initCipherDecrypt(Key key,
                                              AlgorithmParameterSpec params) throws InvalidKeyException,
            InvalidAlgorithmParameterException;


    protected abstract byte[] messageEncrypt(byte[] input)
            throws IllegalBlockSizeException, BadPaddingException;


    protected abstract byte[] messageDecrypt(byte[] input)
            throws IllegalBlockSizeException, BadPaddingException;

}
