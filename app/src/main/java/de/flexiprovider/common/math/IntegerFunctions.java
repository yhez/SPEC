package de.flexiprovider.common.math;

import de.flexiprovider.common.exceptions.NoQuadraticResidueException;

public final class IntegerFunctions {

    private static final FlexiBigInt ZERO = FlexiBigInt.ZERO;

    private static final FlexiBigInt ONE = FlexiBigInt.ONE;

    private static final FlexiBigInt TWO = FlexiBigInt.valueOf(2);

    private static final int[] SMALL_PRIMES = {3, 5, 7, 11, 13, 17, 19, 23,
            29, 31, 37, 41};

    // the jacobi function uses this lookup table
    private static final int[] jacobiTable = {0, 1, 0, -1, 0, -1, 0, 1};

    private IntegerFunctions() {
        // empty
    }


    public static int jacobi(FlexiBigInt A, FlexiBigInt B) {
        FlexiBigInt a, b, v;
        long k;

        k = 1;

        // test trivial cases
        if (B.equals(ZERO)) {
            a = A.abs();
            return a.equals(ONE) ? 1 : 0;
        }

        if (!A.testBit(0) && !B.testBit(0)) {
            return 0;
        }

        a = A;
        b = B;

        if (b.signum() == -1) { // b < 0
            b = b.negate(); // b = -b
            if (a.signum() == -1) {
                k = -1;
            }
        }

        v = ZERO;
        while (!b.testBit(0)) {
            v = v.add(ONE); // v = v + 1
            b = b.divide(TWO); // b = b/2
        }

        if (v.testBit(0)) {
            k = k * jacobiTable[a.intValue() & 7];
        }

        if (a.signum() < 0) { // a < 0
            if (b.testBit(1)) {
                k = -k; // k = -k
            }
            a = a.negate(); // a = -a
        }

        // main loop
        while (a.signum() != 0) {
            v = ZERO;
            while (!a.testBit(0)) { // a is even
                v = v.add(ONE);
                a = a.divide(TWO);
            }
            if (v.testBit(0)) {
                k = k * jacobiTable[b.intValue() & 7];
            }

            if (a.compareTo(b) < 0) { // a < b
                // swap and correct intermediate result
                FlexiBigInt x = a;
                a = b;
                b = x;
                if (a.testBit(1) && b.testBit(1)) {
                    k = -k;
                }
            }
            a = a.subtract(b);
        }

        return b.equals(ONE) ? (int) k : 0;
    }


    public static FlexiBigInt ressol(FlexiBigInt a, FlexiBigInt p)
            throws NoQuadraticResidueException {

        FlexiBigInt v;

        if (a.compareTo(ZERO) < 0) {
            a = a.add(p);
        }

        if (a.equals(ZERO)) {
            return ZERO;
        }

        if (p.equals(TWO)) {
            return a;
        }

        // p = 3 mod 4
        if (p.testBit(0) && p.testBit(1)) {
            if (jacobi(a, p) == 1) { // a quadr. residue mod p
                v = p.add(ONE); // v = p+1
                v = v.shiftRight(2); // v = v/4
                return a.modPow(v, p); // return a^v mod p
                // return --> a^((p+1)/4) mod p
            }
            throw new NoQuadraticResidueException(a, p);
        }

        long t;

        // initialization
        // compute k and s, where p = 2^s (2k+1) +1

        FlexiBigInt k = p.subtract(ONE); // k = p-1
        long s = 0;
        while (!k.testBit(0)) { // while k is even
            s++; // s = s+1
            k = k.shiftRight(1); // k = k/2
        }

        k = k.subtract(ONE); // k = k - 1
        k = k.shiftRight(1); // k = k/2

        // initial values
        FlexiBigInt r = a.modPow(k, p); // r = a^k mod p

        FlexiBigInt n = r.multiply(r).remainder(p); // n = r^2 % p
        n = n.multiply(a).remainder(p); // n = n * a % p
        r = r.multiply(a).remainder(p); // r = r * a %p

        if (n.equals(ONE)) {
            return r;
        }

        // non-quadratic residue
        FlexiBigInt z = TWO; // z = 2
        while (jacobi(z, p) == 1) {
            // while z quadratic residue
            z = z.add(ONE); // z = z + 1
        }

        v = k;
        v = v.multiply(TWO); // v = 2k
        v = v.add(ONE); // v = 2k + 1
        FlexiBigInt c = z.modPow(v, p); // c = z^v mod p

        // iteration
        while (n.compareTo(ONE) == 1) { // n > 1
            k = n; // k = n
            t = s; // t = s
            s = 0;

            while (!k.equals(ONE)) { // k != 1
                k = k.multiply(k).mod(p); // k = k^2 % p
                s++; // s = s + 1
            }

            t -= s; // t = t - s
            if (t == 0) {
                throw new NoQuadraticResidueException(a, p);
            }

            v = ONE;
            for (long i = 0; i < t - 1; i++) {
                v = v.shiftLeft(1); // v = 1 * 2^(t - 1)
            }
            c = c.modPow(v, p); // c = c^v mod p
            r = r.multiply(c).remainder(p); // r = r * c % p
            c = c.multiply(c).remainder(p); // c = c^2 % p
            n = n.multiply(c).mod(p); // n = n * c % p
        }
        return r;
    }


    public static int gcd(int u, int v) {
        return FlexiBigInt.valueOf(u).gcd(FlexiBigInt.valueOf(v)).intValue();
    }

    public static int ceilLog256(int n) {
        if (n == 0) {
            return 1;
        }
        int m;
        if (n < 0) {
            m = -n;
        } else {
            m = n;
        }

        int d = 0;
        while (m > 0) {
            d++;
            m >>>= 8;
        }
        return d;
    }

    public static int floorLog(int a) {
        int h = 0;
        if (a <= 0) {
            return -1;
        }
        int p = a >>> 1;
        while (p > 0) {
            h++;
            p >>>= 1;
        }

        return h;
    }

    public static int order(int g, int p) {
        int b, j;

        b = g % p; // Reduce g mod p first.
        j = 1;

        // Check whether g == 0 mod p (avoiding endless loop).
        if (b == 0) {
            throw new IllegalArgumentException(g + " is not an element of Z/("
                    + p + "Z)^*; it is not meaningful to compute its order.");
        }

        // Compute the order of g mod p:
        while (b != 1) {
            b *= g;
            b %= p;
            if (b < 0) {
                b += p;
            }
            j++;
        }

        return j;
    }

    public static boolean isPrime(int n) {
        if (n < 2) {
            return false;
        }
        if (n == 2) {
            return true;
        }
        if ((n & 1) == 0) {
            return false;
        }
        if (n < 42) {
            for (int SMALL_PRIME : SMALL_PRIMES) {
                if (n == SMALL_PRIME) {
                    return true;
                }
            }
        }

        return !((n % 3 == 0) || (n % 5 == 0) || (n % 7 == 0) || (n % 11 == 0) || (n % 13 == 0) || (n % 17 == 0) || (n % 19 == 0) || (n % 23 == 0) || (n % 29 == 0) || (n % 31 == 0) || (n % 37 == 0) || (n % 41 == 0)) && FlexiBigInt.valueOf(n).isProbablePrime(20);

    }


}
