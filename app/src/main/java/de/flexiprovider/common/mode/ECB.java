package de.flexiprovider.common.mode;

import java.security.spec.AlgorithmParameterSpec;

import de.flexiprovider.api.Mode;
import de.flexiprovider.api.exceptions.InvalidAlgorithmParameterException;
import de.flexiprovider.api.exceptions.InvalidKeyException;
import de.flexiprovider.api.keys.SecretKey;


public class ECB extends Mode {


    protected final void initEncrypt(SecretKey key,
                                     ModeParameterSpec modeParams, AlgorithmParameterSpec cipherParams)
            throws InvalidKeyException, InvalidAlgorithmParameterException {

        initCipherEncrypt(key, cipherParams);
        blockSize = getCipherBlockSize();
    }


    protected final void initDecrypt(SecretKey key,
                                     ModeParameterSpec modeParams, AlgorithmParameterSpec cipherParams)
            throws InvalidKeyException, InvalidAlgorithmParameterException {

        initCipherDecrypt(key, cipherParams);
        blockSize = getCipherBlockSize();
    }


    protected final void nextChunkEncrypt(final byte[] input, final int inOff,
                                          byte[] output, final int outOff) {
        singleBlockEncrypt(input, inOff, output, outOff);
    }


    protected final void nextChunkDecrypt(final byte[] input, final int inOff,
                                          byte[] output, final int outOff) {
        singleBlockDecrypt(input, inOff, output, outOff);
    }

    protected final void reset() {
        // empty
    }

}
