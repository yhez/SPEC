package de.flexiprovider.common.math.codingtheory;

/**
 * This class describes operations with polynomials over finite field GF(2), i e
 * polynomial ring R = GF(2)[X]. All operations are defined only for polynomials
 * with degree <=32. For the polynomial representation the map f: R->Z,
 * poly(X)->poly(2) is used, where integers have the binary representation. For
 * example: X^7+X^3+X+1 -> (00...0010001011)=139 Also for polynomials type
 * Integer is used.
 *
 * @author Elena Klintsevich
 * @see GF2mField
 */
public final class PolynomialRingGF2 {

    /**
     * Default constructor (private).
     */
    private PolynomialRingGF2() {
        // empty
    }

    /**
     * Return sum of two polyomials
     *
     * @param p polynomial
     * @param q polynomial
     * @return p+q
     */

    public static int add(int p, int q) {
        return p ^ q;
    }

    /**
     * Compute the product of two polynomials modulo a third polynomial.
     *
     * @param a the first polynomial
     * @param b the second polynomial
     * @param r the reduction polynomial
     * @return <tt>a * b mod r</tt>
     */
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

    /**
     * Return the degree of a polynomial
     *
     * @param p polynomial p
     * @return degree(p)
     */

    public static int degree(int p) {
        int result = -1;
        while (p != 0) {
            result++;
            p >>>= 1;
        }
        return result;
    }

    /**
     * Return the remainder of a polynomial division of two polynomials.
     *
     * @param p dividend
     * @param q divisor
     * @return <tt>p mod q</tt>
     */
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
