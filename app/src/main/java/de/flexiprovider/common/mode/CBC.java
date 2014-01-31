package de.flexiprovider.common.mode;

import java.security.spec.AlgorithmParameterSpec;

import de.flexiprovider.api.Mode;
import de.flexiprovider.api.exceptions.InvalidAlgorithmParameterException;
import de.flexiprovider.api.exceptions.InvalidKeyException;
import de.flexiprovider.api.keys.SecretKey;


public class CBC extends Mode {


    private byte[] buf;


    private byte[] chainingBlock;


    protected final void initEncrypt(SecretKey key,
                                     ModeParameterSpec modeParams, AlgorithmParameterSpec cipherParams)
            throws InvalidKeyException, InvalidAlgorithmParameterException {

        initCipherEncrypt(key, cipherParams);
        initCommon(modeParams);
    }


    protected final void initDecrypt(SecretKey key,
                                     ModeParameterSpec modeParams, AlgorithmParameterSpec cipherParams)
            throws InvalidKeyException, InvalidAlgorithmParameterException {

        initCipherDecrypt(key, cipherParams);
        initCommon(modeParams);
    }


    private void initCommon(ModeParameterSpec modeParams) {
        blockSize = getCipherBlockSize();

        iv = new byte[blockSize];
        if (modeParams != null) {
            // obtain IV from mode parameters
            byte[] iv = modeParams.getIV();

            if (iv.length < blockSize) {
                // if IV is too short, fill with zeroes
                System.arraycopy(iv, 0, this.iv, 0, iv.length);
            } else if (iv.length > blockSize) {
                // if IV is too long, use only first bytes
                System.arraycopy(iv, 0, this.iv, 0, blockSize);
            } else {
                // else, use the IV
                this.iv = iv;
            }
        }

        buf = new byte[blockSize];
        chainingBlock = new byte[blockSize];
        reset();
    }


    protected final void nextChunkEncrypt(byte[] input, int inOff,
                                          byte[] output, int outOff) {

        for (int i = blockSize - 1; i >= 0; i--) {
            chainingBlock[i] ^= input[inOff + i];
        }

        singleBlockEncrypt(chainingBlock, 0, output, outOff);
        System.arraycopy(output, outOff, chainingBlock, 0, blockSize);
    }


    protected final void nextChunkDecrypt(byte[] input, int inOff,
                                          byte[] output, int outOff) {

        singleBlockDecrypt(input, inOff, buf, 0);
        for (int i = blockSize - 1; i >= 0; i--) {
            output[outOff + i] = (byte) (chainingBlock[i] ^ buf[i]);
        }

        System.arraycopy(input, inOff, chainingBlock, 0, blockSize);
    }


    protected final void reset() {
        System.arraycopy(iv, 0, chainingBlock, 0, iv.length);
    }

}
