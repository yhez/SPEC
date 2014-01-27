package de.flexiprovider.common.mode;


public class CFBParameterSpec extends ModeParameterSpec {

    // the block size
    private int blockSize;


    public CFBParameterSpec(byte[] iv, int blockSize) {
        super(iv);
        this.blockSize = blockSize;
    }


}
