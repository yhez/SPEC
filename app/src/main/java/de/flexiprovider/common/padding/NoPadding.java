package de.flexiprovider.common.padding;

import de.flexiprovider.api.PaddingScheme;

public class NoPadding extends PaddingScheme {

    protected int padLength(int inLen) {
        return 0;
    }

    protected void pad(byte[] input, int inOff, int inLen) {

    }

    protected int unpad(byte[] input, int inOff, int inLen) {
        return input.length;
    }

}
