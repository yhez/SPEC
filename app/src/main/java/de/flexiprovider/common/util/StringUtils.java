package de.flexiprovider.common.util;


public final class StringUtils {


    private StringUtils() {
        // empty
    }

    public static String filterSpaces(String str) {
        StringBuffer buf = new StringBuffer(str);

        for (int i = 0; i < buf.length(); i++) {
            if (buf.charAt(i) == ' ') {
                buf = buf.deleteCharAt(i);
                i--;
            }
        }
        return buf.toString();
    }

}
