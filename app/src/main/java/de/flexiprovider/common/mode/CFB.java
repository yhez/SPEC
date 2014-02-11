package de.flexiprovider.common.mode;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.spec.AlgorithmParameterSpec;

import de.flexiprovider.api.Mode;
import de.flexiprovider.api.keys.SecretKey;


public class CFB extends Mode {


    private byte[] buf;


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

        if (modeParams instanceof CFBParameterSpec) {
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

        buf = new byte[cipherBlockSize];
        feedbackBlock = new byte[cipherBlockSize];
        reset();
    }


    protected final void initDecrypt(SecretKey key,
                                     ModeParameterSpec modeParams, AlgorithmParameterSpec paramSpec)
            throws InvalidKeyException, InvalidAlgorithmParameterException {
        initEncrypt(key, modeParams, paramSpec);
    }
    protected final void nextChunkEncrypt(final byte[] input, final int inOff,
                                          byte[] output, final int outOff) {
        nextChunk(input, inOff, output, outOff);
        // fill feedback block with ciphertext
        System.arraycopy(output, outOff, feedbackBlock, 0, blockSize);
    }


    protected final void nextChunkDecrypt(byte[] input, int inOff,
                                          byte[] output, int outOff) {
        nextChunk(input, inOff, output, outOff);
        // fill feedback block with ciphertext
        System.arraycopy(input, inOff, feedbackBlock, 0, blockSize);
    }

    private void nextChunk(byte[] input, int inOff, byte[] output, int outOff) {

        // encrypt feedback block
        singleBlockEncrypt(feedbackBlock, 0, buf, 0);

        // compute ciphertext block
        for (int i = 0; i < blockSize; i++) {
            output[outOff + i] = (byte) (buf[i] ^ input[inOff + i]);
        }

        // shift feedback block
        System.arraycopy(feedbackBlock, 0, feedbackBlock, blockSize,
                feedbackBlock.length - blockSize);
    }


    protected final void reset() {
        System.arraycopy(iv, 0, feedbackBlock, 0, iv.length);
    }

}
