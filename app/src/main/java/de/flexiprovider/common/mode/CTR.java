package de.flexiprovider.common.mode;

import de.flexiprovider.api.Mode;
import de.flexiprovider.api.exceptions.InvalidAlgorithmParameterException;
import de.flexiprovider.api.exceptions.InvalidKeyException;
import de.flexiprovider.api.keys.SecretKey;
import de.flexiprovider.api.parameters.AlgorithmParameterSpec;


public class CTR extends Mode {

    // the counter value
    private byte[] counter;

    // the feedback block
    private byte[] feedbackBlock;


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

        feedbackBlock = new byte[blockSize];
        counter = new byte[blockSize];
        reset();
    }


    protected final void nextChunkEncrypt(final byte[] input, final int inOff,
                                          byte[] output, final int outOff) {

        singleBlockEncrypt(counter, 0, feedbackBlock, 0);

        int inCarry = 1;
        for (int i = blockSize - 1; i >= 0; i--) {
            output[outOff + i] = (byte) (feedbackBlock[i] ^ input[inOff + i]);
            int x = (counter[i] & 0xff) + inCarry;
            counter[i] = (byte) x;
            inCarry = (x > 255) ? 1 : 0;
        }
    }


    protected final void nextChunkDecrypt(final byte[] input, final int inOff,
                                          byte[] output, final int outOff) {
        nextChunkEncrypt(input, inOff, output, outOff);
    }


    protected final void reset() {
        System.arraycopy(iv, 0, counter, 0, blockSize);
    }

}
