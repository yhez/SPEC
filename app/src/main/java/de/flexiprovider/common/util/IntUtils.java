package de.flexiprovider.common.util;

public final class IntUtils {

    /**
     * Default constructor (private).
     */
    private IntUtils() {
        // empty
    }


    public static boolean equals(int[] left, int[] right) {
        if (left.length != right.length) {
            return false;
        }
        boolean result = true;
        for (int i = left.length - 1; i >= 0; i--) {
            result &= left[i] == right[i];
        }
        return result;
    }
    public static int[] clone(int[] array) {
        int[] result = new int[array.length];
        System.arraycopy(array, 0, result, 0, array.length);
        return result;
    }


    public static void fill(int[] array, int value) {
        for (int i = array.length - 1; i >= 0; i--) {
            array[i] = value;
        }
    }

    public static String toString(int[] input) {
        String result = "";
        for (int i = 0; i < input.length; i++) {
            result += input[i] + " ";
        }
        return result;
    }

    public static String toHexString(int[] input) {
        return ByteUtils.toHexString(BigEndianConversions.toByteArray(input));
    }

}
