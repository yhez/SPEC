package de.flexiprovider.common.math.codingtheory;
public final class PolynomialRingGF2 {

    private PolynomialRingGF2() {
        // empty
    }

    public static int modMultiply(int a, int b, int r) {
        int result = 0;
        int p = remainder(a, r);
        int q = remainder(b, r);
        if (q != 0) {
            int d = 1 << degree(r);

            while (p != 0) {
                byte pMod2 = (byte) (p & 0x01);
                if (pMod2 == 1) {
                    result ^= q;
                }
                p >>>= 1;
                q <<= 1;
                if (q >= d) {
                    q ^= r;
                }
            }
        }
        return result;
    }



    public static int degree(int p) {
        int result = -1;
        while (p != 0) {
            result++;
            p >>>= 1;
        }
        return result;
    }


    public static int remainder(int p, int q) {
        int result = p;

        if (q == 0) {
            System.err.println("Error: to be divided by 0");
            return 0;
        }

        while (degree(result) >= degree(q)) {
            result ^= q << (degree(result) - degree(q));
        }

        return result;
    }

}
