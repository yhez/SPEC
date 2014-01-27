package de.flexiprovider.common.math.quadraticfields;

import de.flexiprovider.api.Registry;
import de.flexiprovider.api.SecureRandom;
import de.flexiprovider.api.exceptions.InvalidParameterException;
import de.flexiprovider.common.exceptions.NoQuadraticResidueException;
import de.flexiprovider.common.math.FlexiBigInt;
import de.flexiprovider.common.math.IntegerFunctions;

public class IQClassGroup {
    /*
     * Constants
     */

    /*
     * Member variables
     */

    private FlexiBigInt discriminant;

    public IQClassGroup(FlexiBigInt discriminant, SecureRandom prng) {
        SecureRandom prng1 = (prng == null) ? Registry.getSecureRandom() : prng;
        this.discriminant = discriminant;
    }
    public IQClassGroup(FlexiBigInt discriminant) {
        this(discriminant, null);
    }

    public FlexiBigInt getDiscriminant() {
        return discriminant;
    }

    // /////////////////////////////////////////////////////////////////////////

    private FlexiBigInt sqrtDeltaThirds = null;

    private FlexiBigInt sqrtDeltaHalves = null;

    /**
     * Check whether we have a reduced ideal already.
     *
     * @return <tt>true</tt> if ideal is reduced, <tt>false</tt> otherwise.
     */
    public boolean isReduced(QuadraticIdeal I) {
        if (sqrtDeltaThirds == null) {
            sqrtDeltaThirds = IntegerFunctions.squareRoot(discriminant.abs()
                    .divide(FlexiBigInt.valueOf(3)));
        }
        if (sqrtDeltaHalves == null) {
            // divide by two done for reasons of clarity. speed impact compared
            // to bit
            // shifting is minimal.
            sqrtDeltaHalves = IntegerFunctions.squareRoot(discriminant.abs()
                    .divide(FlexiBigInt.valueOf(2)));
        }

        if (I.a.signum() <= 0 || I.a.compareTo(sqrtDeltaThirds) > 0) {
            return false;
        }
        if (I.b.compareTo(I.a.negate()) < 0 || I.b.compareTo(I.a) > 0) {
            return false;
        }
        if (I.a.compareTo(sqrtDeltaHalves) <= 0) {
            return true;
        }

        // divide by four done for reasons of clarity. speed impact compared to
        // bit
        // shifting is minimal.
        FlexiBigInt c = I.b.multiply(I.b).subtract(discriminant);
        c = c.divide(I.a).divide(FlexiBigInt.valueOf(4));

        return I.a.compareTo(c) <= 0 && !(I.a.compareTo(c) == 0 && I.b.signum() < 0);

    }

    /**
     * Reduce a quadratic ideal.
     */
    private QuadraticIdeal reduce(FlexiBigInt a, FlexiBigInt b) {
        int sign = 1;
        FlexiBigInt c, t1, t;
        FlexiBigInt q, r;
        FlexiBigInt[] qr;

        // check whether ideal is normal
        if ((a.compareTo(b) < 0) || (a.negate().compareTo(b) >= 0)) {
            // we need to normalize
            // b = a - ((a - b) mod (2a))
            b = a.subtract(a.subtract(b).mod(a.shiftLeft(1)));
        }
        if (b.signum() < 0) {
            b = b.abs();
            sign = -1;
        }

        // compute c
        // c = (b^2 - Delta) / (4*a)
        c = b.multiply(b).subtract(discriminant);

        if (c.remainder(a.shiftLeft(2)).signum() != 0) {
            throw new InvalidParameterException("invalid ideal");
        }

        c = c.divide(a.shiftLeft(2));

        while (a.compareTo(c) > 0) {
            // swap a and c
            t = a;
            a = c;

            // t1 = 2 * a
            t1 = a.shiftLeft(1);

            if ((b.bitLength() - t1.bitLength()) > 2) {
                qr = b.divideAndRemainder(t1);
                q = qr[0];
                r = qr[1];
            } else {
                int q_int;
                r = b;

                for (q_int = 0; r.compareTo(t1) > 0; ) {
                    r = r.subtract(t1);
                    q_int++;
                }
                q = FlexiBigInt.valueOf(q_int);
            }

            // c = t - q * (r + b) >> 1
            c = t.subtract(q.multiply(r.add(b)).shiftRight(1));

            // a < r
            if (a.compareTo(r) < 0) {
                // b = 2 * a - r
                b = t1.subtract(r);
                // c' = c + a - r
                c = c.add(a).subtract(r);
            } else {
        /* a >= r */
                b = r;
                sign = -sign;
            }
        }

        if (sign < 0) {
            b = b.negate();
        }

        // check whether ideal is normal
        if ((a.compareTo(b) < 0) || (a.negate().compareTo(b) >= 0)) {
            // we need to normalize
            // b = a - ((a - b) mod (2a))
            b = a.subtract(a.subtract(b).remainder(a.shiftLeft(1)));
        }

        if (a.equals(c) && b.signum() < 0) {
            b = b.negate();
        }

        return new QuadraticIdeal(a, b);
    }

    /**
     * Reduce a quadratic ideal of the class group.
     *
     * @param I ideal to be reduced
     * @return reduced ideal equivalent to I
     */
    public QuadraticIdeal reduce(QuadraticIdeal I) {
        return reduce(I.a, I.b);
    }

    /**
     * Invert a quadratic ideal of the class group.
     *
     * @return the inverse ideal
     */
    public QuadraticIdeal invert(QuadraticIdeal I) {
        return new QuadraticIdeal(I.a, I.b.negate());
    }

    /**
     * Multiply two quadratic ideals of the class group.
     *
     * @return the product of the two ideals
     */
    public QuadraticIdeal multiply(FlexiBigInt a1, FlexiBigInt b1,
                                   FlexiBigInt a2, FlexiBigInt b2) {
        FlexiBigInt[] temp;
        FlexiBigInt t1, tb;
        FlexiBigInt d1, d2, v, w;
        FlexiBigInt a3, b3;

        // d1 = gcd(abs(a1),abs(a2)) = v * a1 + w * a2
        temp = IntegerFunctions.extgcd(a1, a2);
        d1 = temp[0];
        v = temp[1];

        // tb = a1 * v * (b2 - b1)
        tb = a1.multiply(v).multiply(b2.subtract(b1));

        // a3 = a1 * a2
        a3 = a1.multiply(a2);

        // gcd(a1, a2) ?= 1
        if (d1.compareTo(FlexiBigInt.ONE) != 0) { // gcd(a1,a2) != 1
            // t1 = (b1 + b2) >> 1
            t1 = b1.add(b2).shiftRight(1);
            // d2 = gcd(abs(d1),abs(t1)) = v * d1 + w * t1
            temp = IntegerFunctions.extgcd(d1, t1);
            d2 = temp[0];
            v = temp[1];
            w = temp[2];
            // t1 = (w * ((Delta - b1^2) >> 1) + v * tb) / d2
            t1 = discriminant.subtract(b1.multiply(b1)).multiply(w);
            tb = t1.shiftRight(1).add(v.multiply(tb)).divide(d2);
            // a3 = a3 / (d2^2)
            a3 = a3.divide(d2.multiply(d2));
        }

        // b3 = (b1 + tb) mod (2 * a3)
        b3 = b1.add(tb).mod(a3.shiftLeft(1));

        return reduce(a3, b3);
    }

    /**
     * Multiply two quadratic ideals.
     *
     * @return the product of the two ideals
     */
    public QuadraticIdeal multiply(QuadraticIdeal I1, QuadraticIdeal I2) {
        return multiply(I1.a, I1.b, I2.a, I2.b);
    }

    /**
     * Divide a quadratic ideal by another.
     *
     * @param I1 the first ideal (dividend)
     * @param I2 the second ideal (divisor)
     * @return the remainder of the division
     */
    public QuadraticIdeal divide(QuadraticIdeal I1, QuadraticIdeal I2) {
        return multiply(I1.a, I1.b, I2.a, I2.b.negate());
    }

    /**
     * Square a quadratic ideal.
     *
     * @return the squared ideal
     */
    public QuadraticIdeal square(FlexiBigInt a, FlexiBigInt b) {
        FlexiBigInt[] temp;
        FlexiBigInt t1;
        FlexiBigInt d1;
        FlexiBigInt a3, b3;

        // d1 = gcd(abs(a),abs(b)) = v * a + w * b

        temp = IntegerFunctions.extgcd(a, b);
        d1 = temp[0];
        // FlexiBigInt v = temp[1];
        FlexiBigInt w = temp[2];

        // a3 = (a / d1)^2
        a3 = a.divide(d1);
        a3 = a3.multiply(a3);

        // t1 = (Delta - b^2) / (2 * d1) * w + b
        t1 = discriminant.subtract(b.multiply(b)).divide(d1.shiftLeft(1));
        t1 = t1.multiply(w).add(b);

        b3 = t1.mod(a3.shiftLeft(1));

        return reduce(a3, b3);
    }

    /**
     * Square a quadratic ideal.
     *
     * @return the sqared ideal
     */
    public QuadraticIdeal square(QuadraticIdeal I) {
        return square(I.a, I.b);
    }

    /**
     * @return the neutral element of the class group
     */
    public QuadraticIdeal one() {
        return new QuadraticIdeal(1, discriminant.testBit(0) ? 1 : 0);
    }

    // /////////////////////////////////////////////////////////////////////////

    public QuadraticIdeal primePowerIdeal(FlexiBigInt p, int e)
            throws NoQuadraticResidueException {
        FlexiBigInt a, b, s, t1;

        a = p;
        // b is the quadratic root modulo p of
        // the discriminant reduced modulo p
        b = IntegerFunctions.ressol(discriminant.mod(p), p);

        // delta and b have different parity
        if (discriminant.testBit(0) != b.testBit(0)) {
            b = b.add(p);
        }

        // b^2 = Delta (mod 4 p)

        for (int j = 2; j <= e; j++) {
            // r s = 1 (mod p)
            s = b.modInverse(p);
            t1 = discriminant.subtract(b.multiply(b));
            t1 = t1.shiftRight(2).divide(a).multiply(s).mod(p);
            b = b.add(t1.multiply(a).shiftLeft(1));
            a = a.multiply(p);
        }

        return reduce(a, b);
    }

    // ////////////////////////////////////////////////////////////////////////
}
