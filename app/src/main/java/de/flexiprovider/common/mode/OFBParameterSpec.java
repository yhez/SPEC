package de.flexiprovider.common.mode;

public class OFBParameterSpec extends ModeParameterSpec {

    // the block size
    private int blockSize;

    public OFBParameterSpec(byte[] iv, int blockSize) {
        super(iv);
        this.blockSize = blockSize;
    }

    public final int getBlockSize() {
        return blockSize;
    }

}
