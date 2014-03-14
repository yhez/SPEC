package de.flexiprovider.api;

import javax.crypto.BadPaddingException;


public abstract class PaddingScheme {

    protected int blockSize = -1;

    final void setBlockSize(int blockSize) {
        if (blockSize > 0) {
            this.blockSize = blockSize;
        }
    }

    protected abstract int padLength(int inLen);

    protected abstract void pad(byte[] input, int inOff, int inLen)
            ;

    protected abstract int unpad(byte[] input, int inOff, int inLen)
            throws BadPaddingException;

}
