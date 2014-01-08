package codec.asn1;

import java.util.Comparator;


public class DERCodeComparator implements Comparator {
    public int compare(Object o1, Object o2) {
        byte[] a1;
        byte[] a2;
        int n;
        int m;
        int x;
        int y;

        a1 = (byte[]) o1;
        a2 = (byte[]) o2;
        m = Math.min(a1.length, a2.length);

        for (n = 0; n < m; n++) {
            x = a1[n] & 0xff;
            y = a2[n] & 0xff;

            if (x < y) {
                return -1;
            } else if (x > y) {
                return 1;
            }
        }
        if (a1.length == a2.length) {
            return 0;
        } else if (a1.length < a2.length) {
            return -1;
        }
        return 1;
    }

}