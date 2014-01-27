package de.flexiprovider.common.util;


public final class ByteUtils {

    private static final char[] HEX_CHARS = {'0', '1', '2', '3', '4', '5',
            '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    /**
     * Default constructor (private)
     */
    private ByteUtils() {
        // empty
    }


    public static boolean equals(byte[] left, byte[] right) {
        if (left == null) {
            return right == null;
        }
        if (right == null) {
            return false;
        }

        if (left.length != right.length) {
            return false;
        }
        boolean result = true;
        for (int i = left.length - 1; i >= 0; i--) {
            result &= left[i] == right[i];
        }
        return result;
    }


    public static byte[] clone(byte[] array) {
        if (array == null) {
            return null;
        }
        byte[] result = new byte[array.length];
        System.arraycopy(array, 0, result, 0, array.length);
        return result;
    }

    public static byte[] fromHexString(String s) {
        char[] rawChars = s.toUpperCase().toCharArray();

        int hexChars = 0;
        for (int i = 0; i < rawChars.length; i++) {
            if ((rawChars[i] >= '0' && rawChars[i] <= '9')
                    || (rawChars[i] >= 'A' && rawChars[i] <= 'F')) {
                hexChars++;
            }
        }

        byte[] byteString = new byte[(hexChars + 1) >> 1];

        int pos = hexChars & 1;

        for (int i = 0; i < rawChars.length; i++) {
            if (rawChars[i] >= '0' && rawChars[i] <= '9') {
                byteString[pos >> 1] <<= 4;
                byteString[pos >> 1] |= rawChars[i] - '0';
            } else if (rawChars[i] >= 'A' && rawChars[i] <= 'F') {
                byteString[pos >> 1] <<= 4;
                byteString[pos >> 1] |= rawChars[i] - 'A' + 10;
            } else {
                continue;
            }
            pos++;
        }

        return byteString;
    }


    public static String toHexString(byte[] input) {
        String result = "";
        for (int i = 0; i < input.length; i++) {
            result += HEX_CHARS[(input[i] >>> 4) & 0x0f];
            result += HEX_CHARS[(input[i]) & 0x0f];
        }
        return result;
    }


    public static String toHexString(byte[] input, String prefix,
                                     String seperator) {
        String result = prefix;
        for (int i = 0; i < input.length; i++) {
            result += HEX_CHARS[(input[i] >>> 4) & 0x0f];
            result += HEX_CHARS[(input[i]) & 0x0f];
            if (i < input.length - 1) {
                result += seperator;
            }
        }
        return result;
    }


}
