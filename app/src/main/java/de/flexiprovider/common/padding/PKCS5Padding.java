package de.flexiprovider.common.padding;

import de.flexiprovider.api.PaddingScheme;
import de.flexiprovider.api.exceptions.BadPaddingException;

public class PKCS5Padding extends PaddingScheme {

    protected int padLength(int inLen) {
        return blockSize - (inLen % blockSize);
    }

    protected void pad(byte[] input, int inOff, int inLen) {
        // compute the pad length
        int padLength = padLength(inLen);

        // pad the input
        int index = inOff + inLen;
        for (int i = 0; i < padLength; i++) {
            input[index++] = (byte) padLength;
        }
    }

    protected int unpad(byte[] input, int inOff, int inLen)
            throws BadPaddingException {
        // the pad length is stored in last byte of the input
        int last = inOff + inLen - 1;
        byte padLength = input[last--];

        // check correctness
        if (padLength < 0 || padLength > inLen) {
            throw new BadPaddingException("unpadding failed");
        }
        for (int i = 1; i < padLength; i++) {
            if (input[last--] != padLength) {
                throw new BadPaddingException("unpadding failed");
            }
        }

        // return start index of padding bytes
        return ++last;
    }

}
