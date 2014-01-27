package de.flexiprovider.common.padding;

import de.flexiprovider.api.PaddingScheme;
import de.flexiprovider.api.exceptions.BadPaddingException;

public class NoPadding extends PaddingScheme {

    protected int padLength(int inLen) {
        return 0;
    }

    protected void pad(byte[] input, int inOff, int inLen)
            throws BadPaddingException {

    }

    protected int unpad(byte[] input, int inOff, int inLen) {
        return input.length;
    }

}
