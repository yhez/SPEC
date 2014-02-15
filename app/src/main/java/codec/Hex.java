package codec;


public final class Hex {

    private Hex() {
    }

    private static final char[] HEX_ = "0123456789abcdef"
            .toCharArray();

    public static String encode(byte[] in) {
        StringBuffer out;
        int m;
        int n;
        int k;

        if (in.length == 0) {
            return "";
        }
        out = new StringBuffer(in.length * 2);

        for (n = 0; n < in.length; n++) {
            m = in[n];
            k = (m >>> 4) & 0x0f;

            out.append(HEX_[k]);

            k = (m & 0x0f);

            out.append(HEX_[k]);
        }
        return out.toString();
    }

    public static byte[] decode(String in) throws Exception {
        byte[] buf;
        int a;
        int b;
        int j;
        int n;

        if (in.length() == 0) {
            return new byte[0];
        }
        n = in.length();

        if ((n % 2) == 1) {
            throw new Exception("uneven input length");
        }
        n = n / 2;
        buf = new byte[n];

        for (j = 0, n = 0; n < buf.length; n++) {
            a = in.charAt(j++);
            b = in.charAt(j++);

            if (('0' <= a) && (a <= '9')) {
                a = a - '0';
            } else if (('a' <= a) && (a <= 'f')) {
                a = a - 'a' + 10;
            } else if (('A' <= a) && (a <= 'F')) {
                a = a - 'A' + 10;
            } else {
                throw new Exception("Illegal char: '" + a + "'");
            }
            if (('0' <= b) && (b <= '9')) {
                b = b - '0';
            } else if (('a' <= b) && (b <= 'f')) {
                b = b - 'a' + 10;
            } else if (('A' <= b) && (b <= 'F')) {
                b = b - 'A' + 10;
            } else {
                throw new Exception("Illegal char: '" + b + "'");
            }
            buf[n] = (byte) ((a << 4) | b);
        }
        return buf;
    }
}
