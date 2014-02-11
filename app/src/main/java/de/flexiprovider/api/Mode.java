package de.flexiprovider.api;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.spec.AlgorithmParameterSpec;

import de.flexiprovider.api.keys.SecretKey;
import de.flexiprovider.common.mode.ModeParameterSpec;


public abstract class Mode {

    private BlockCipher blockCipher;

    protected byte[] iv;

    protected int blockSize;


    final void setBlockCipher(BlockCipher blockCipher) {
        this.blockCipher = blockCipher;
    }


    protected abstract void initEncrypt(SecretKey key,
                                        ModeParameterSpec modeParams, AlgorithmParameterSpec cipherParams)
            throws InvalidKeyException, InvalidAlgorithmParameterException;

    protected abstract void initDecrypt(SecretKey key,
                                        ModeParameterSpec modeParams, AlgorithmParameterSpec cipherParams)
            throws InvalidKeyException, InvalidAlgorithmParameterException;

    protected abstract void nextChunkEncrypt(final byte[] input,
                                             final int inOff, byte[] output, final int outOff);

    protected abstract void nextChunkDecrypt(final byte[] input,
                                             final int inOff, byte[] output, final int outOff);

    protected abstract void reset();

    protected final void initCipherEncrypt(SecretKey key,
                                           AlgorithmParameterSpec cipherParams) throws InvalidKeyException,
            InvalidAlgorithmParameterException {
        blockCipher.initCipherEncrypt(key, cipherParams);
    }


    protected final void initCipherDecrypt(SecretKey key,
                                           AlgorithmParameterSpec cipherParams) throws InvalidKeyException,
            InvalidAlgorithmParameterException {
        blockCipher.initCipherDecrypt(key, cipherParams);
    }

    protected final int getCipherBlockSize() {
        return blockCipher.getCipherBlockSize();
    }


    protected final void singleBlockEncrypt(byte[] input, int inOff,
                                            byte[] output, int outOff) {
        blockCipher.singleBlockEncrypt(input, inOff, output, outOff);
    }

    protected final void singleBlockDecrypt(byte[] input, int inOff,
                                            byte[] output, int outOff) {
        blockCipher.singleBlockDecrypt(input, inOff, output, outOff);
    }

}
