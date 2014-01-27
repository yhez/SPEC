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

    public static boolean equals(byte[][] left, byte[][] right) {
        if (left.length != right.length) {
            return false;
        }

        boolean result = true;
        for (int i = left.length - 1; i >= 0; i--) {
            result &= ByteUtils.equals(left[i], right[i]);
        }

        return result;
    }


    public static boolean equals(byte[][][] left, byte[][][] right) {
        if (left.length != right.length) {
            return false;
        }

        boolean result = true;
        for (int i = left.length - 1; i >= 0; i--) {
            if (left[i].length != right[i].length) {
                return false;
            }
            for (int j = left[i].length - 1; j >= 0; j--) {
                result &= ByteUtils.equals(left[i][j], right[i][j]);
            }
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


    public static byte[] xor(byte[] x1, byte[] x2) {
        byte[] out = new byte[x1.length];

        for (int i = x1.length - 1; i >= 0; i--) {
            out[i] = (byte) (x1[i] ^ x2[i]);
        }
        return out;
    }


    public static byte[][] split(byte[] input, int index)
            throws ArrayIndexOutOfBoundsException {
        if (index > input.length) {
            throw new ArrayIndexOutOfBoundsException();
        }
        byte[][] result = new byte[2][];
        result[0] = new byte[index];
        result[1] = new byte[input.length - index];
        System.arraycopy(input, 0, result[0], 0, index);
        System.arraycopy(input, index, result[1], 0, input.length - index);
        return result;
    }

}
