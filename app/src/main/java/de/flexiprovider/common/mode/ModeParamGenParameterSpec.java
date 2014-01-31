package de.flexiprovider.common.mode;


public class ModeParamGenParameterSpec implements java.security.spec.AlgorithmParameterSpec {


    public static final int DEFAULT_LENGTH = 8;

    private int ivLength;


    public ModeParamGenParameterSpec() {
        this(DEFAULT_LENGTH);
    }


    public ModeParamGenParameterSpec(int ivLength) {
        this.ivLength = ivLength;
    }


    public int getIVLength() {
        return ivLength;
    }

}
