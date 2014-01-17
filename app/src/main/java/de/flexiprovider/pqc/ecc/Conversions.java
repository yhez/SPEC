package de.flexiprovider.pqc.ecc;

import de.flexiprovider.common.math.FlexiBigInt;
import de.flexiprovider.common.math.IntegerFunctions;
import de.flexiprovider.common.math.linearalgebra.GF2Vector;
import de.flexiprovider.common.util.FlexiBigIntUtils;

/**
 * Provides methods for CCA2-Secure Conversions of McEliece PKCS
 *
 * @author Elena Klintsevich
 */
public final class Conversions {

    /**
     * Default constructor (private).
     */
    private Conversions() {
        // empty
    }
    public static GF2Vector encode(final int n, final int t, final byte[] m) {
        if (n < t) {
            throw new IllegalArgumentException("n < t");
        }

        // compute the binomial c = (n|t)
        FlexiBigInt c = IntegerFunctions.binomial(n, t);
        // get the number encoded in m
        FlexiBigInt i = new FlexiBigInt(1, m);
        // compare
        if (i.compareTo(c) >= 0) {
            throw new IllegalArgumentException("Encoded number too large.");
        }

        GF2Vector result = new GF2Vector(n);

        int nn = n;
        int tt = t;
        for (int j = 0; j < n; j++) {
            c = c.multiply(FlexiBigInt.valueOf(nn - tt)).divide(
                    FlexiBigInt.valueOf(nn));
            nn--;
            if (c.compareTo(i) <= 0) {
                result.setBit(j);
                i = i.subtract(c);
                tt--;
                if (nn == tt) {
                    c = FlexiBigInt.ONE;
                } else {
                    c = (c.multiply(FlexiBigInt.valueOf(tt + 1)))
                            .divide(FlexiBigInt.valueOf(nn - tt));
                }
            }
        }

        return result;
    }

    /**
     * Decode a binary vector of length n and weight t into a number between 0
     * and (n|t) (binomial coefficient). The result is given as a byte array of
     * length floor[(s+7)/8], where s = floor[log(n|t)].
     *
     * @param n   integer
     * @param t   integer
     * @param vec the binary vector
     * @return the decoded vector as a byte array
     */
    public static byte[] decode(int n, int t, GF2Vector vec) {
        if ((vec.getLength() != n) || (vec.getHammingWeight() != t)) {
            throw new IllegalArgumentException(
                    "vector has wrong length or hamming weight");
        }
        int[] vecArray = vec.getVecArray();

        FlexiBigInt bc = IntegerFunctions.binomial(n, t);
        FlexiBigInt d = FlexiBigInt.ZERO;
        int nn = n;
        int tt = t;
        for (int i = 0; i < n; i++) {
            bc = bc.multiply(FlexiBigInt.valueOf(nn - tt)).divide(
                    FlexiBigInt.valueOf(nn));
            nn--;

            int q = i >> 5;
            int e = vecArray[q] & (1 << (i & 0x1f));
            if (e != 0) {
                d = d.add(bc);
                tt--;
                if (nn == tt) {
                    bc = FlexiBigInt.ONE;
                } else {
                    bc = bc.multiply(FlexiBigInt.valueOf(tt + 1)).divide(
                            FlexiBigInt.valueOf(nn - tt));
                }

            }
        }

        return FlexiBigIntUtils.toMinimalByteArray(d);
    }

}
