package de.flexiprovider.common.math.ellipticcurves;

import java.math.BigInteger;

import de.flexiprovider.common.math.finitefields.GFPElement;

public final class ScalarMult {

    /**
     * Default constructor (private).
     */
    private ScalarMult() {
        // empty
    }

    public static Point multiply(BigInteger b, Point p) {
        int w = 4;
        int[] N = determineNaf(b, w);
        Point[] P = precomputationCMO(p, w + 1, 0);
        return eval_SquareMultiply(N, P);
    }


    public static Point[] pre_oddpowers(Point p, int w) {
        final int l = (1 << w) - 1;
        Point[] P = new Point[l];
        P[0] = (Point) p.clone();
        Point tmp = p.multiplyBy2();

        for (int i = 1; i < l; i++) {
            P[i] = P[i - 1].add(tmp);
        }
        return P;
    }


    public static Point[] precomputationCMO(Point p, int w, int k) {
        int length, denoms;
        Point[] P;
        if (w > 2 && k == 0) {
            w = w - 1;
            length = 1 << (w - 1); // length == #precomputed points + 1
            denoms = 1 << (w - 2); // denoms == #lambdas denominators
        } else if (w == 0 && k > 1) {
            length = k; // length == #precomputed points + 1
            int bits = (Integer.toBinaryString(k - 1)).length();
            // numbers of bits of k-1
            w = bits + 1;
            denoms = 1 << (bits - 1); // denoms == #lambdas denominators
        } else {
            P = new Point[1];
            P[0] = (Point) p.clone();
            return P;
        }

        P = new Point[length];
        P[0] = (Point) p.clone();

        Point doubleP = p.multiplyBy2Affine();
        doubleP = doubleP.getAffin(); // 2P

        // arrays for lambdas denominators and their inverses
        BigInteger[] NennerLambda = new BigInteger[denoms];
        BigInteger[] NennerLambdaInvers = new BigInteger[denoms];
        BigInteger invers;

        BigInteger mP = p.getE().getQ();

        for (int i = 1; i < w; i++) {
            final int begin = 1 << i - 1; // startposition
            final int end = (1 << i) - 1; // endposition
            boolean notLastStep = (i + 1) != w;
            // in last step you can save some computations
            int start = 0;

            // compute lambdas denominators
            if (notLastStep) {
                // example NennerLambda = |NL(5P) | NL(7P) | NL(8P)| with
                // w>3
                for (int j = begin; j <= end; j++) {
                    NennerLambda[start] = doubleP.getX().toFlexiBigInt()
                            .subtract(
                                    P[start].getX()
                                            .toFlexiBigInt()
                            );
                    start++;
                }
                NennerLambda[start] = doubleP.getY().toFlexiBigInt().add(
                        doubleP.getY().toFlexiBigInt()).mod(mP);
                NennerLambdaInvers[0] = NennerLambda[0].add(BigInteger.ZERO);

                // example NennerLambdaInvers =
                // |NL(5P) | NL(5P)*NL(7P) | NL(5P)*NL(7P)*NL(8P)| with w>3
                for (int m = 1; m <= begin; m++) {
                    NennerLambdaInvers[m] = (NennerLambdaInvers[m - 1]
                            .multiply(NennerLambda[m])).mod(mP);
                }

                invers = NennerLambdaInvers[begin].modInverse(mP);

                // example NennerLambdaInvers = |NL(5P)^-1 | NL(7P)^-1 |
                // NL(8P)^-1| with w>3
                for (int m = begin; m >= 1; m--) {
                    NennerLambdaInvers[m] = (NennerLambdaInvers[m - 1]
                            .multiply(invers)).mod(mP);
                    invers = (invers.multiply(NennerLambda[m])).mod(mP);
                }
                NennerLambdaInvers[0] = invers;
            } else {
                // example NennerLambda = |NL(5P) | NL(7P) | with w==3
                for (int j = begin; j <= end; j++) {
                    NennerLambda[start] = doubleP.getX().toFlexiBigInt()
                            .subtract(
                                    P[start].getX()
                                            .toFlexiBigInt()
                            );
                    start++;
                }
                NennerLambdaInvers[0] = NennerLambda[0].add(BigInteger.ZERO);

                // example NennerLambdaInvers = |NL(5P) | NL(5P)*NL(7P) |
                // with w==3
                for (int m = 1; m < begin; m++) {
                    NennerLambdaInvers[m] = (NennerLambdaInvers[m - 1]
                            .multiply(NennerLambda[m])).mod(mP);
                }

                invers = NennerLambdaInvers[begin - 1].modInverse(mP);

                // example NennerLambdaInvers = |NL(5P)^-1 | NL(7P)^-1 |
                // with w==3
                for (int m = begin - 1; m >= 1; m--) {
                    NennerLambdaInvers[m] = (NennerLambdaInvers[m - 1]
                            .multiply(invers)).mod(mP);
                    invers = (invers.multiply(NennerLambda[m])).mod(mP);
                }
                NennerLambdaInvers[0] = invers;
            }

            // compute multiples of point with P[j] = P[start] + doubleP
            BigInteger lambda;
            BigInteger temp;
            BigInteger x, y, startX, startY;
            start = 0;
            for (int j = begin; j <= end; j++) {
                startX = P[start].getX().toFlexiBigInt();
                startY = P[start].getY().toFlexiBigInt();
                lambda = (doubleP.getY().toFlexiBigInt()).subtract(startY);
                lambda = (NennerLambdaInvers[start]).multiply(lambda).mod(mP);

                // new x-coordinate of point P[j]
                temp = lambda.multiply(lambda).mod(mP);
                temp = temp.subtract(startX);
                x = (temp.subtract(doubleP.getX().toFlexiBigInt())).mod(mP);

                // new y-coordinate of Point P[j]
                temp = startX.subtract(x);
                temp = lambda.multiply(temp).mod(mP);
                y = (temp.subtract(startY)).mod(mP);

                GFPElement gfpx = new GFPElement(x, mP);
                GFPElement gfpy = new GFPElement(y, mP);
                P[j] = new Point(gfpx, gfpy, p.getE());
                if (k == j + 1) {
                    return P;
                }
                start++;
            }

            // compute new 2*doubleP coordinates
            if (notLastStep) {
                lambda = doubleP.getX().toFlexiBigInt().multiply(
                        doubleP.getX().toFlexiBigInt()).mod(mP);
                lambda = lambda.multiply(
                        new BigInteger(Integer.toString(3))).mod(mP);
                lambda = lambda.add(doubleP.getE().getA().toFlexiBigInt());
                lambda = lambda.multiply(NennerLambdaInvers[start]).mod(mP);

                // new x-coordinate
                temp = doubleP.getX().toFlexiBigInt().add(
                        doubleP.getX().toFlexiBigInt()).mod(mP);
                x = lambda.multiply(lambda).mod(mP).subtract(temp);

                // new y-coordinate
                temp = doubleP.getX().toFlexiBigInt().subtract(x);
                temp = lambda.multiply(temp).mod(mP);
                y = temp.subtract(doubleP.getY().toFlexiBigInt());

                // update doubleP
                // doubleP.mX = new GFPElement(x, mP);
                // doubleP.mY = new GFPElement(y, mP);
                // doubleP.mZ = new GFPElement(BigInteger.ONE, mP);
                doubleP = new Point(new GFPElement(x, mP), new GFPElement(y,
                        mP), new GFPElement(BigInteger.ONE, mP),
                        doubleP.getE()
                );
            }
        } // end for
        return P;
    }


    public static int[] determineNaf(BigInteger e, int w, int b) {
        int power2wi = 1 << w;
        int j, u;
        int[] N = new int[b + 1];
        BigInteger c = e.abs();
        int s = e.signum();

        j = 0;
        while (c.compareTo(BigInteger.ZERO) > 0) {
            if (c.testBit(0)) {
                u = (c.intValue()) & ((power2wi << 1) - 1);
                if ((u & power2wi) != 0) {
                    u = u - (power2wi << 1);
                }

                c = c.subtract(BigInteger.valueOf(u));
            } else {
                u = 0;
            }

            N[j++] = (s > 0) ? u : -u;
            c = c.shiftRight(1);
        }

        // fill with zeros
        while (j <= b) {
            N[j++] = 0;
        }

        return N;
    }

    /**
     * Returns <tt>e</tt> as int-array in <i>non-adjacent-form (Naf)</i>.
     *
     * @param e the FlexiBigInt that is to be converted
     * @param w the entries <i>n</i> of the Nafs are smaller than 2<sup>w</sup>
     * @return <tt>e</tt> in non-adjacent-form as int-array
     */
    public static int[] determineNaf(BigInteger e, int w) {
        return determineNaf(e, w, e.bitLength());
    }


    public static Point eval_SquareMultiply(int[] N, Point[] P) {
        Point r = createZeroPoint(P[0], P[0], P[0].getE());
        int l = N.length - 1;
        for (int i = l; i >= 0; i--) {
            r.multiplyThisBy2();
            int index = N[i];
            if (index > 0) {
                r.addToThis(P[(index - 1) >> 1]);
            } else if (index < 0) {
                r.subtractFromThis(P[(-index - 1) >> 1]);
            }
        }
        return r.getAffin();
    }


    private static Point createZeroPoint(Object type1, Object type2,
                                         Object curve) {
        if (type1 instanceof Point && type2 instanceof Point
                && curve instanceof EllipticCurve) {
            return new Point((EllipticCurve) curve);
        }
        return null;
    }

}
