package de.flexiprovider.common.util;

public final class IntUtils {

    public static int[] clone(int[] array) {
        int[] result = new int[array.length];
        System.arraycopy(array, 0, result, 0, array.length);
        return result;
    }


}
