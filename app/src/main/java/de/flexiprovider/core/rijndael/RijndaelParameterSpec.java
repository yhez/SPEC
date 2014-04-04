package de.flexiprovider.core.rijndael;

import java.util.Arrays;

import de.flexiprovider.common.util.ByteUtils;


public class RijndaelParameterSpec implements java.security.spec.AlgorithmParameterSpec {


    public static final int DEFAULT_BLOCK_SIZE = 128;


    private int blockSize;

    private byte[] iv;

    public RijndaelParameterSpec() {
        this(DEFAULT_BLOCK_SIZE);
    }

    public RijndaelParameterSpec(int blockSize) {
        if ((blockSize != 128) && (blockSize != 192) && (blockSize != 256)) {
            this.blockSize = DEFAULT_BLOCK_SIZE;
        } else {
            this.blockSize = blockSize;
        }
    }

    public int getBlockSize() {
        return blockSize;
    }

    public byte[] getIV() {
        return ByteUtils.clone(iv);
    }

    public boolean equals(Object other) {
        if ((other == null) || !(other instanceof RijndaelParameterSpec)) {
            return false;
        }
        RijndaelParameterSpec otherSpec = (RijndaelParameterSpec) other;

        return (blockSize == otherSpec.blockSize)
                && ByteUtils.equals(iv, otherSpec.iv);
    }

    public int hashCode() {
        if (iv == null) {
            return blockSize;
        }
        return blockSize + Arrays.hashCode(iv);
    }

}
