package codec;
public final class Base64 {

    private Base64() {
    }
    private static final char[] BASE64 = new String(
            "ABCDEFGHIJKLMNOPQRSTUVWXYZ" + "abcdefghijklmnopqrstuvwxyz"
                    + "0123456789+/").toCharArray();

    private static final char PAD_1 = '=';

    private static final String PAD_2 = "==";

    private static final byte F = (byte) 255;

    private static final byte PAD = (byte) 64;

    private static final byte[] REVERSE = {F, F, F, F, F, F, F, F, F, F, F, F,
            F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
            F, F, F, F, F, F, F, F, F, 62, F, F, F, 63, 52, 53, 54, 55, 56, 57,
            58, 59, 60, 61, F, F, F, PAD, F, F, F, 0, 1, 2, 3, 4, 5, 6, 7, 8,
            9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25,
            F, F, F, F, F, F, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37,
            38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, F, F, F, F,
            F};

    private static final String HEX = "0123456789abcdef";

    public static String encode(byte[] input) {
        int i;
        int j;
        int m;
        int a;
        int b;
        int c;
        StringBuffer output;

        if (input.length == 0) {
            return "";
        }

        i = ((input.length + 2) / 3) << 2;
        output = new StringBuffer(i);
        i = input.length / 3;
        j = 0;

        while (i > 0) {
            a = input[j++];
            b = input[j++];
            c = input[j++];

            m = (a >>> 2) & 63;
            output.append(BASE64[m]);

            m = ((a & 3) << 4) | ((b >>> 4) & 15);
            output.append(BASE64[m]);

            m = ((b & 15) << 2) | ((c >>> 6) & 3);
            output.append(BASE64[m]);

            m = c & 63;
            output.append(BASE64[m]);
            i--;
        }

        i = input.length % 3;

        switch (i) {
            case 1:
                a = input[j++];
                m = (a >>> 2) & 63;
                output.append(BASE64[m]);
                m = (a & 3) << 4;
                output.append(BASE64[m]);
                output.append(PAD_2);
                break;

            case 2:
                a = input[j++];
                b = input[j++];
                m = (a >>> 2) & 63;
                output.append(BASE64[m]);

                m = ((a & 3) << 4) | ((b >>> 4) & 15);
                output.append(BASE64[m]);

                m = (b & 15) << 2;
                output.append(BASE64[m]);
                output.append(PAD_1);
                break;
        }
        return output.toString();
    }


    public static byte[] decode(String input) throws CorruptedCodeException {
        int i;
        byte[] b;

        if (input.length() == 0) {
            return new byte[0];
        }
        b = new byte[input.length()];

        for (i = input.length() - 1; i >= 0; i--) {
            b[i] = (byte) input.charAt(i);
        }
        return decode(b);
    }


    public static byte[] decode(byte[] code) throws CorruptedCodeException {
        boolean end;
        byte[] output;
        byte m;
        byte a;
        byte b;
        byte c;
        byte d;
        int i;
        int j;
        int k;
        int l;

        l = code.length;
        end = false;

        for (i = 0, j = 0; i < l; i++) {
            if ((code[i] < 0) || (code[i] >= REVERSE.length)) {
                throw new CorruptedCodeException("Code was not Base64 encoded");
            }
            m = REVERSE[code[i]];

            if (m == PAD) {
                if (end) {
                    break;
                }
                end = true;
                continue;
            }
            if (end) {
                throw new CorruptedCodeException(
                        "Second pad character missing!");
            }
            if (m == F) {
                continue;
            }
            code[j++] = m;
        }
        l = j >> 2;
        i = l * 3;
        k = j & 3;

        if (k == 1) {
            throw new CorruptedCodeException("One character is missing!");
        }
        if (k > 0) {
            i = (i + k) - 1;
        }
        output = new byte[i];

        i = 0;
        j = 0;
        b = 0;

        while (l > 0) {
            a = code[i++];
            b = code[i++];
            c = code[i++];
            d = code[i++];

            output[j++] = (byte) ((a << 2) | ((b >>> 4) & 3));
            output[j++] = (byte) (((b & 15) << 4) | ((c >>> 2) & 15));
            output[j++] = (byte) (((c & 3) << 6) | d);
            l--;
        }
        if (k >= 2) {
            a = code[i++];
            b = code[i++];
            output[j++] = (byte) ((a << 2) | ((b >>> 4) & 3));
        }
        if (k >= 3) {
            c = code[i++];
            output[j++] = (byte) (((b & 15) << 4) | ((c >>> 2) & 15));
        }
        return output;
    }


    public static String toHex(byte[] b) {
        StringBuffer buf;
        int i;

        buf = new StringBuffer(b.length * 2);

        for (i = 0; i < b.length; i++) {
            buf.append(HEX.charAt((b[i] >> 4) & 15));
            buf.append(HEX.charAt(b[i] & 15));
        }
        return buf.toString();
    }
}
