package de.flexiprovider.api;

import java.lang.reflect.Method;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.InvalidParameterException;
import java.security.Key;
import java.security.spec.AlgorithmParameterSpec;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SecretKey;
import javax.crypto.ShortBufferException;

import de.flexiprovider.common.mode.CBC;
import de.flexiprovider.common.mode.ModeParameterSpec;
import de.flexiprovider.common.mode.OFB;
import de.flexiprovider.common.padding.PKCS5Padding;


public abstract class BlockCipher extends Cipher {


    private Mode mode;

    private PaddingScheme paddingScheme;

    private AlgorithmParameterSpec paramSpec;


    private byte[] buffer = null;

    private int modeBlockSize;

    private boolean initialized = false;

    protected final void engineInit(int opmode, java.security.Key key,
                                    java.security.spec.AlgorithmParameterSpec paramSpec,
                                    java.security.SecureRandom javaRand)
            throws java.security.InvalidKeyException,
            java.security.InvalidAlgorithmParameterException {

        initModeAndPadding();
        opMode = opmode;

        buffer = new byte[0];
        ModeParameterSpec modeParams;
        AlgorithmParameterSpec cipherParams;

        if (paramSpec == null) {
            modeParams = null;
            cipherParams = null;
        } else if (paramSpec instanceof javax.crypto.spec.IvParameterSpec) {
            modeParams = new ModeParameterSpec(
                    (javax.crypto.spec.IvParameterSpec) paramSpec);
            cipherParams = null;

        } else {
            cipherParams = paramSpec;

            byte[] iv;
            Method getIV;
            try {
                getIV = cipherParams.getClass().getMethod("getIV", null);
                iv = (byte[]) getIV.invoke(cipherParams, null);
            } catch (Exception ex) {
                // if no getIV() method is found, iv remains null
                iv = null;
            }

            if (iv == null) {
                modeParams = null;
            } else {
                modeParams = new ModeParameterSpec(iv);
            }
        }

        if (!(key instanceof SecretKey)) {
            throw new java.security.InvalidKeyException("unsupported type");
        }

        if (opmode == ENCRYPT_MODE) {
            mode.initEncrypt((SecretKey) key, modeParams, cipherParams);
        } else if (opmode == DECRYPT_MODE) {
            mode.initDecrypt((SecretKey) key, modeParams, cipherParams);
        }
        modeBlockSize = mode.blockSize;
        paddingScheme.setBlockSize(modeBlockSize);

        initialized = true;
    }


    public final void setMode(String modeName) {
        if (mode != null) {
            return;
        }
        mode = new CBC();
        mode.setBlockCipher(this);
    }

    public final void setPadding(String paddingName) {
        if (paddingScheme != null) {
            return;
        }
        paddingScheme = new PKCS5Padding();
    }


    public final byte[] getIV() {
        return initialized ? mode.iv : null;
    }

    protected abstract int getCipherBlockSize();

    public final int getBlockSize() {
        return initialized ? modeBlockSize : -1;
    }


    public final int getOutputSize(int inLen) {
        if (!initialized) {
            return -1;
        }
        final int newInLen = inLen + (buffer == null ? 0 : buffer.length);
        return newInLen + paddingScheme.padLength(newInLen);
    }


    public final AlgorithmParameterSpec getParameters() {
        return initialized ? paramSpec : null;
    }


    public final void initEncrypt(Key key)
            throws InvalidKeyException, InvalidParameterException {

        try {
            initEncrypt(key, null,
                    (SecureRandom)null);
        } catch (InvalidAlgorithmParameterException e) {
            throw new InvalidParameterException(
                    "This cipher needs algorithm parameters for initialization (cannot be null).");
        }
    }


    public final void initEncrypt(Key key, AlgorithmParameterSpec cipherParams,
                                  SecureRandom random) throws InvalidKeyException,
            InvalidAlgorithmParameterException {
        initEncrypt(key, null, cipherParams);
    }


    public final void initEncrypt(Key key, ModeParameterSpec modeParams,
                                  AlgorithmParameterSpec cipherParams)
            throws InvalidKeyException, InvalidAlgorithmParameterException {

        initModeAndPadding();
        opMode = ENCRYPT_MODE;

        buffer = new byte[0];
        paramSpec = cipherParams;

        if (!(key instanceof SecretKey)) {
            throw new InvalidKeyException("unsupported type");
        }

        mode.initEncrypt((SecretKey) key, modeParams, cipherParams);
        modeBlockSize = mode.blockSize;
        paddingScheme.setBlockSize(modeBlockSize);

        initialized = true;
    }

    public final void initDecrypt(Key key) throws InvalidKeyException,
            InvalidParameterException {
        try {
            initDecrypt(key, null,
                    null);
        } catch (InvalidAlgorithmParameterException e) {
            throw new InvalidParameterException(
                    "This cipher needs algorithm parameters for initialization (cannot be null).");
        }
    }


    public final void initDecrypt(Key key, AlgorithmParameterSpec cipherParams)
            throws InvalidKeyException, InvalidAlgorithmParameterException {
        initDecrypt(key, null, cipherParams);
    }


    public final void initDecrypt(Key key, ModeParameterSpec modeParams,
                                  AlgorithmParameterSpec cipherParams) throws InvalidKeyException,
            InvalidAlgorithmParameterException {

        initModeAndPadding();
        opMode = DECRYPT_MODE;

        buffer = new byte[0];
        paramSpec = cipherParams;

        if (!(key instanceof SecretKey)) {
            throw new InvalidKeyException("unsupported type");
        }

        mode.initDecrypt((SecretKey) key, modeParams, cipherParams);
        modeBlockSize = mode.blockSize;
        paddingScheme.setBlockSize(modeBlockSize);

        initialized = true;
    }


    public final byte[] update(byte[] input, int inOff, int inLen) {

        if (!initialized || input == null || inLen <= 0) {
            return new byte[0];
        }

        int bufLen = buffer.length;
        int newInLen, newInOff;
        byte[] newInput;
        if (bufLen == 0) {
            newInOff = inOff;
            newInLen = inLen;
            newInput = input;
        } else {
            newInLen = bufLen + inLen;
            newInOff = 0;
            newInput = new byte[newInLen];
            System.arraycopy(buffer, 0, newInput, 0, bufLen);
            System.arraycopy(input, inOff, newInput, bufLen, inLen);
        }

        // compute number of blocks to process and remaining bytes
        int numBlocks = newInLen / modeBlockSize;
        int numBytes = numBlocks * modeBlockSize;
        int remaining = newInLen - numBytes;
        if (opMode == DECRYPT_MODE && remaining == 0) {
            remaining = modeBlockSize;
            numBlocks--;
            numBytes -= modeBlockSize;
        }

        byte[] output = new byte[numBytes];
        int outOff = 0;

        // process whole blocks
        for (int block = 0; block < numBlocks; block++) {
            if (opMode == ENCRYPT_MODE) {
                mode.nextChunkEncrypt(newInput, newInOff, output, outOff);
            } else if (opMode == DECRYPT_MODE) {
                mode.nextChunkDecrypt(newInput, newInOff, output, outOff);
            }
            newInOff += modeBlockSize;
            outOff += modeBlockSize;
        }

        // copy unprocessed bytes to buffer
        buffer = new byte[remaining];
        System.arraycopy(newInput, newInOff, buffer, 0, remaining);

        return output;
    }

    public final int update(byte[] input, int inOff, int inLen, byte[] output,
                            int outOff) throws ShortBufferException {

        // if the cipher is not initialized or input is empty return 0
        if (!initialized || input == null || inLen <= 0) {
            return 0;
        }

        // compute number of bytes to process
        int newInLen = buffer.length + inLen;
        int remaining = newInLen % modeBlockSize;
        if (opMode == DECRYPT_MODE && remaining == 0) {
            remaining = modeBlockSize;
        }
        int numBytes = newInLen - remaining;

        if (output.length - outOff < numBytes) {
            throw new ShortBufferException("output");
        }

        byte[] update = update(input, inOff, inLen);
        System.arraycopy(update, 0, output, outOff, update.length);

        return update.length;
    }

    public final byte[] doFinal(byte[] input, int inOff, int inLen)
            throws IllegalBlockSizeException, BadPaddingException {

        byte[] output = new byte[0];

        if (input == null && buffer == null) {
            return output;
        }

        byte[] update = update(input, inOff, inLen);
        int updLen = update.length;
        int bufLen = buffer.length;

        if (opMode == ENCRYPT_MODE) {
            int padLen = paddingScheme.padLength(bufLen);
            if (padLen == 0) {
                if (!this.mode.getClass().equals(OFB.class))
                    return update;
            }
            output = new byte[updLen + bufLen + padLen];
            System.arraycopy(update, 0, output, 0, updLen);
            System.arraycopy(buffer, 0, output, updLen, bufLen);
            paddingScheme.pad(output, updLen, bufLen);
            mode.nextChunkEncrypt(output, updLen, output, updLen);
        } else if (opMode == DECRYPT_MODE) {
            // if (bufLen != modeBlockSize) {
            if (bufLen != modeBlockSize && !this.mode.getClass().equals(OFB.class)) {
                throw new IllegalBlockSizeException(
                        "ciphertext length is not a multiple of block size");
            }
            mode.nextChunkDecrypt(buffer, 0, buffer, 0);
            int padOffset = paddingScheme.unpad(buffer, 0, modeBlockSize);
            output = new byte[updLen + padOffset];
            System.arraycopy(update, 0, output, 0, updLen);
            System.arraycopy(buffer, 0, output, updLen, padOffset);
        }

        buffer = null;
        mode.reset();

        return output;
    }


    public final int doFinal(byte[] input, int inOff, int inLen, byte[] output,
                             int outOff) throws ShortBufferException, IllegalBlockSizeException,
            BadPaddingException {

        byte[] doFinal = doFinal(input, inOff, inLen);
        int outLen = doFinal.length;
        if (outLen == 0) {
            return 0;
        }
        if (output.length - outOff < outLen) {
            throw new ShortBufferException("output");
        }
        System.arraycopy(doFinal, 0, output, outOff, outLen);
        return outLen;
    }

    protected abstract void initCipherEncrypt(SecretKey key,
                                              AlgorithmParameterSpec params) throws InvalidKeyException,
            InvalidAlgorithmParameterException;

    protected abstract void initCipherDecrypt(SecretKey key,
                                              AlgorithmParameterSpec params) throws InvalidKeyException,
            InvalidAlgorithmParameterException;


    protected abstract void singleBlockEncrypt(byte[] input, int inOff,
                                               byte[] output, int outOff);


    protected abstract void singleBlockDecrypt(byte[] input, int inOff,
                                               byte[] output, int outOff);

    private void initModeAndPadding() {
        if (mode == null) {
            mode = new CBC();
            mode.setBlockCipher(this);
        }

        if (paddingScheme == null) {
            paddingScheme = new PKCS5Padding();
        }
    }

}
