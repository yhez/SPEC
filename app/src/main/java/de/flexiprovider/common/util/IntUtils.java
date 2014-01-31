package de.flexiprovider.common.util;

public final class IntUtils {

    private IntUtils() {
        // empty
    }


    public static int[] clone(int[] array) {
        int[] result = new int[array.length];
        System.arraycopy(array, 0, result, 0, array.length);
        return result;
    }


}
