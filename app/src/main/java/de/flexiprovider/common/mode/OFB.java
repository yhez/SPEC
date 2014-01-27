package de.flexiprovider.common.mode;

import de.flexiprovider.api.Mode;
import de.flexiprovider.api.exceptions.InvalidAlgorithmParameterException;
import de.flexiprovider.api.exceptions.InvalidKeyException;
import de.flexiprovider.api.keys.SecretKey;
import de.flexiprovider.api.parameters.AlgorithmParameterSpec;


public class OFB extends Mode {

    // the output buffer
    private byte[] buf;

    // the feedback block
    private byte[] feedbackBlock;


    protected final void initEncrypt(SecretKey key,
                                     ModeParameterSpec modeParams, AlgorithmParameterSpec cipherParams)
            throws InvalidKeyException, InvalidAlgorithmParameterException {

        initCipherEncrypt(key, cipherParams);
        int cipherBlockSize = getCipherBlockSize();

        iv = new byte[cipherBlockSize];
        if (modeParams != null) {
            // obtain IV from mode parameters
            byte[] iv = modeParams.getIV();

            if (iv.length < cipherBlockSize) {
                // if IV is too short, fill with zeroes
                System.arraycopy(iv, 0, this.iv, 0, iv.length);
            } else if (iv.length > cipherBlockSize) {
                // if IV is too long, use only first bytes
                System.arraycopy(iv, 0, this.iv, 0, cipherBlockSize);
            } else {
                // else, use the IV
                this.iv = iv;
            }
        }

        if (modeParams instanceof OFBParameterSpec) {
            // get block size
            blockSize = ((OFBParameterSpec) modeParams).getBlockSize();
            // check block size
            if (blockSize > cipherBlockSize) {
                blockSize = cipherBlockSize;
            }
        } else {
            // default: set block size to cipher block size
            blockSize = cipherBlockSize;
        }

        feedbackBlock = new byte[cipherBlockSize];
        buf = new byte[cipherBlockSize];
        reset();
    }


    protected final void initDecrypt(SecretKey key,
                                     ModeParameterSpec modeParams, AlgorithmParameterSpec cipherParams)
            throws InvalidKeyException, InvalidAlgorithmParameterException {
        initEncrypt(key, modeParams, cipherParams);
    }


    protected final void nextChunkEncrypt(final byte[] input, final int inOff,
                                          byte[] output, final int outOff) {

        singleBlockEncrypt(buf, 0, feedbackBlock, 0);

        byte[] swap = buf;
        buf = feedbackBlock;
        feedbackBlock = swap;

        for (int i = 0; i < blockSize; i++) {
            output[outOff + i] = (byte) (feedbackBlock[i] ^ input[inOff + i]);
            if (input.length - 1 == (inOff + i))
                break;
        }
    }


    protected final void nextChunkDecrypt(final byte[] input, final int inOff,
                                          byte[] output, final int outOff) {
        nextChunkEncrypt(input, inOff, output, outOff);
    }
    protected final void reset() {
        //singleBlockEncrypt(iv, 0, feedbackBlock, 0);
        //System.arraycopy(iv, 0, buf, 0, iv.length);
        singleBlockEncrypt(iv, 0, buf, 0);
    }

}
