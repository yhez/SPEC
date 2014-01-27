package de.flexiprovider.common.mode;

import de.flexiprovider.api.parameters.AlgorithmParameterSpec;


public class ModeParamGenParameterSpec implements AlgorithmParameterSpec {


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
