package de.flexiprovider.common.math.ellipticcurves;

import de.flexiprovider.common.math.FlexiBigInt;
import de.flexiprovider.common.math.finitefields.GF2nElement;
import de.flexiprovider.common.math.finitefields.GF2nField;
import de.flexiprovider.common.math.finitefields.GF2nONBElement;
import de.flexiprovider.common.math.finitefields.GF2nONBField;
import de.flexiprovider.common.math.finitefields.GF2nPolynomialElement;
import de.flexiprovider.common.math.finitefields.GF2nPolynomialField;
import de.flexiprovider.common.math.finitefields.GFPElement;

public final class ScalarMult {

    /**
     * Default constructor (private).
     */
    private ScalarMult() {
        // empty
    }

    // ////////////////////////////////////////////////////////////////////
    // multiplications
    // ////////////////////////////////////////////////////////////////////

    /**
     * Multiplies this point with the scalar <tt>b</tt>. Naf-recoding (<tt>w = 4</tt>)
     * and CMO-Precomputation will be used.
     *
     * @param b <tt>FlexiBigInt</tt>
     * @param p base point
     * @return <tt>b*p</tt>
     */
    public static Point multiply(FlexiBigInt b, Point p) {
        int w = 4;
        int[] N = determineNaf(b, w);
        Point[] P = precomputationCMO(p, w + 1, 0);
        return eval_SquareMultiply(N, P);
    }

    public static Point multiply(FlexiBigInt[] b, Point[] p) {
        int w = 5;
        int[] W = new int[b.length];
        for (int i = 0; i < W.length; i++) {
            W[i] = w;
        }
        int[][] N = determineSimultaneousNaf(b, W);
        Point[][] P = new Point[b.length][1 << (w - 1)];
        for (int i = 0; i < b.length; i++) {
            P[i] = precomputationCMO(p[i], w + 1, 0);
        }
        return eval_interleaving(N, P);
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
        if (p instanceof PointGFP) {
            return precomputationCMO((PointGFP) p, w, k);
        } else if (p instanceof PointGF2n) {
            if (w != 0) {
                return precomputationCMO((PointGF2n) p, w);
            }
            throw new RuntimeException(
                    "PrecomputationCMO on EllipticCurveGF2n "
                            + "with k != 0 is not supported.");
        } else {
            throw new RuntimeException(
                    "Point must be an instance of PointGFP / PointGF2n"
                            + " and windowsize must be at least 2.");
        }
    }


    public static Point[] precomputationCMO(PointGFP p, int w, int k) {
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
            P[0] = (PointGFP) p.clone();
            return P;
        }

        P = new Point[length];
        P[0] = (PointGFP) p.clone();

        PointGFP doubleP = (PointGFP) p.multiplyBy2Affine();
        doubleP = (PointGFP) doubleP.getAffin(); // 2P

        // arrays for lambdas denominators and their inverses
        FlexiBigInt[] NennerLambda = new FlexiBigInt[denoms];
        FlexiBigInt[] NennerLambdaInvers = new FlexiBigInt[denoms];
        FlexiBigInt invers;

        FlexiBigInt mP = p.getE().getQ();

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
                                            .toFlexiBigInt());
                    start++;
                }
                NennerLambda[start] = doubleP.getY().toFlexiBigInt().add(
                        doubleP.getY().toFlexiBigInt()).mod(mP);
                NennerLambdaInvers[0] = NennerLambda[0].add(FlexiBigInt.ZERO);

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
                                            .toFlexiBigInt());
                    start++;
                }
                NennerLambdaInvers[0] = NennerLambda[0].add(FlexiBigInt.ZERO);

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
            FlexiBigInt lambda;
            FlexiBigInt temp;
            FlexiBigInt x, y, startX, startY;
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
                P[j] = new PointGFP(gfpx, gfpy, (EllipticCurveGFP) p.getE());
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
                        new FlexiBigInt(Integer.toString(3))).mod(mP);
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
                // doubleP.mZ = new GFPElement(FlexiBigInt.ONE, mP);
                doubleP = new PointGFP(new GFPElement(x, mP), new GFPElement(y,
                        mP), new GFPElement(FlexiBigInt.ONE, mP),
                        (EllipticCurveGFP) doubleP.getE());
            }
        } // end for
        return P;
    }

    /**
     * If k = 0 and w != 0 this method precomputes all odd points 3P, 5P,
     * 7P,..., (2<sup>w-1</sup> -1)P in affine coordinates, whereas P = point.
     * <p/>
     * If k != 0 and w = 0 this method precomputes all odd points 3P, 5P,
     * 7P,..., (2k-1)P in affine coordinates, whereas P = point.
     * <p/>
     * This method computes these points with the algorithm thas was proposed in
     * <i> Cohen, H., Miyaji, A., and Ono, T. Efficient Elliptic Curve
     * Exponentiation Using Mixed Coordinates. 1998</i>
     * <p/>
     * The elements of the returned array are as followed: <br>
     * <br>
     * <table border="0">
     * <tr>
     * <td><b>w != 0, k = 0</b></td>
     * <td>&#160;&#160;</td>
     * <td><b>w = 0, k != 0</b></td>
     * <td>&#160;&#160;</td>
     * <td><b>w != 0, k != 0</b></td>
     * </tr>
     * <tr></tr>
     * <tr>
     * <td>array[0] = P</td>
     * <td>&#160;&#160;</td>
     * <td>array[0] = P</td>
     * <td>&#160;&#160;</td>
     * <td>array[0] = P</td>
     * </tr>
     * <tr>
     * <td>array[1] = 3P</td>
     * <td>&#160;&#160;</td>
     * <td>array[1] = 3P</td>
     * <td>&#160;&#160;</td>
     * <td></td>
     * </tr>
     * <tr>
     * <td>array[2] = 5P</td>
     * <td>&#160;&#160;</td>
     * <td>array[2] = 5P</td>
     * <td>&#160;&#160;</td>
     * <td></td>
     * </tr>
     * <tr>
     * <td>...</td>
     * <td>&#160;&#160;</td>
     * <td>...</td>
     * <td>&#160;&#160;</td>
     * <td></td>
     * </tr>
     * <tr>
     * <td>array[(2<sup>w-2</sup>)-1] = (2<sup>w-1</sup> -1)P</td>
     * <td>&#160;&#160;</td>
     * <td>array[k-1] = (2k-1)P</td>
     * <td>&#160;&#160;</td>
     * <td></td>
     * </tr>
     * </table>
     *
     * @param p the point of the scalar multiplication
     * @param w window size
     * @return Returns an array with the precomputed points in affine
     * coordinates
     */
    public static Point[] precomputationCMO(PointGF2n p, int w) {
        w = w - 1;
        final int length = 1 << (w - 1);
        // length == #precomputed points + 1
        final int denoms = 1 << (w - 2);
        // denoms == #lambdas denominators

        Point[] P = new Point[length];
        P[0] = (PointGF2n) p.clone();
        if (w <= 1) {
            return P;
        }

        PointGF2n doubleP = (PointGF2n) p.multiplyBy2Affine();
        doubleP = (PointGF2n) doubleP.getAffin(); // 2P

        // arrays for lambdas denominators and their inverses
        GF2nElement[] NennerLambda = new GF2nElement[denoms];
        GF2nElement[] NennerLambdaInvers = new GF2nElement[denoms];
        GF2nElement invers;

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
                    NennerLambda[start] = (GF2nElement) doubleP.getX().add(
                            P[start].getX());
                    start++;
                }
                NennerLambda[start] = (GF2nElement) doubleP.getX();
                NennerLambdaInvers[0] = (GF2nElement) NennerLambda[0].clone();

                // example NennerLambdaInvers =
                // |NL(5P) | NL(5P)*NL(7P) | NL(5P)*NL(7P)*NL(8P)| with w>3
                for (int m = 1; m <= begin; m++) {
                    NennerLambdaInvers[m] = (GF2nElement) (NennerLambdaInvers[m - 1]
                            .multiply(NennerLambda[m]));
                }

                invers = (GF2nElement) NennerLambdaInvers[begin].invert();

                // example NennerLambdaInvers = |NL(5P)^-1 | NL(7P)^-1 |
                // NL(8P)^-1| with w>3
                for (int m = begin; m >= 1; m--) {
                    NennerLambdaInvers[m] = (GF2nElement) (NennerLambdaInvers[m - 1]
                            .multiply(invers));
                    invers = (GF2nElement) (invers.multiply(NennerLambda[m]));
                }
                NennerLambdaInvers[0] = invers;
            } else {
                // example NennerLambda = |NL(5P) | NL(7P) | with w==3
                for (int j = begin; j <= end; j++) {
                    NennerLambda[start] = (GF2nElement) doubleP.getX().add(
                            P[start].getX());
                    start++;
                }
                NennerLambdaInvers[0] = (GF2nElement) NennerLambda[0].clone();

                // example NennerLambdaInvers = |NL(5P) | NL(5P)*NL(7P) |
                // with w==3
                for (int m = 1; m < begin; m++) {
                    NennerLambdaInvers[m] = (GF2nElement) (NennerLambdaInvers[m - 1]
                            .multiply(NennerLambda[m]));
                }

                invers = (GF2nElement) NennerLambdaInvers[begin - 1].invert();

                // example NennerLambdaInvers = |NL(5P)^-1 | NL(7P)^-1 |
                // with w==3
                for (int m = begin - 1; m >= 1; m--) {
                    NennerLambdaInvers[m] = (GF2nElement) (NennerLambdaInvers[m - 1]
                            .multiply(invers));
                    invers = (GF2nElement) (invers.multiply(NennerLambda[m]));
                }
                NennerLambdaInvers[0] = invers;
            }

            // compute multiples of point with P[j] = P[start] + doubleP
            GF2nElement lambda;
            GF2nElement tmp;
            GF2nElement x, y, startX, startY;
            start = 0;
            for (int j = begin; j <= end; j++) {
                startX = (GF2nElement) P[start].getX();
                startY = (GF2nElement) P[start].getY();
                lambda = (GF2nElement) (doubleP.getY()).add(startY);
                lambda = (GF2nElement) (NennerLambdaInvers[start])
                        .multiply(lambda);

                // new x-coordinate of point P[j]
                tmp = lambda.square();
                tmp = (GF2nElement) lambda.add(tmp);
                tmp = (GF2nElement) tmp.add(doubleP.getX());
                tmp = (GF2nElement) tmp.add(startX);
                x = (GF2nElement) tmp
                        .add(p.getE().getA());

                // new y-coordinate of Point P[j]
                tmp = (GF2nElement) startX.add(x);
                tmp = (GF2nElement) lambda.multiply(tmp);
                tmp = (GF2nElement) tmp.add(x);
                y = (GF2nElement) tmp.add(startY);

                P[j] = new PointGF2n(x, y, (EllipticCurveGF2n) p.getE());
                start++;
            }

            // compute new 2*doubleP coordinates
            if (notLastStep) {
                lambda = (GF2nElement) doubleP.getY().multiply(
                        NennerLambdaInvers[start]);
                lambda = (GF2nElement) lambda.add(doubleP.getX());

                // new x-coordinate
                tmp = lambda.square();
                tmp = (GF2nElement) tmp.add(lambda);
                x = (GF2nElement) tmp
                        .add(p.getE().getA());

                // new y-coordinate
                GF2nElement element = (GF2nElement) doubleP.getX();
                GF2nField field = element.getField();
                tmp = createGF2nOneElement(field); // temp = 1
                tmp = (GF2nElement) tmp.add(lambda);
                tmp = (GF2nElement) tmp.multiply(x);
                GF2nElement mX = (GF2nElement) doubleP.getX();
                y = (GF2nElement) tmp.add(mX.square());

                // update doubleP
                // doubleP.mX = x;
                // doubleP.mY = y;
                doubleP = new PointGF2n(x, y, (EllipticCurveGF2n) doubleP
                        .getE());
            }
        } // end for
        return P;
    }

    public static int[] determineNaf(FlexiBigInt e, int w, int b) {
        int power2wi = 1 << w;
        int j, u;
        int[] N = new int[b + 1];
        FlexiBigInt c = e.abs();
        int s = e.signum();

        j = 0;
        while (c.compareTo(FlexiBigInt.ZERO) > 0) {
            if (c.testBit(0)) {
                u = (c.intValue()) & ((power2wi << 1) - 1);
                if ((u & power2wi) != 0) {
                    u = u - (power2wi << 1);
                }

                c = c.subtract(FlexiBigInt.valueOf(u));
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
    public static int[] determineNaf(FlexiBigInt e, int w) {
        return determineNaf(e, w, e.bitLength());
    }

    private static void determineNaf(int[] N, FlexiBigInt e, int w) {

        int power2wi = 1 << w;
        int j, u;
        FlexiBigInt c = e.abs();
        int s = e.signum();

        j = 0;
        while (c.compareTo(FlexiBigInt.ZERO) > 0) {
            if (c.testBit(0)) {
                u = (c.intValue()) & ((power2wi << 1) - 1);
                if ((u & power2wi) != 0) {
                    u = u - (power2wi << 1);
                }
                c = c.subtract(FlexiBigInt.valueOf(u));
            } else {
                u = 0;
            }

            N[j++] = (s > 0) ? u : -u;

            c = c.shiftRight(1);
        }

        // fill with zeros
        while (j < N.length) {
            N[j++] = 0;
        }
    }


    public static int[][] determineSimultaneousNaf(FlexiBigInt[] e, int[] w) {
        int b = 0;
        for (int i = 0; i < e.length; i++) {
            b = (b < e[i].bitLength()) ? e[i].bitLength() : b;
        }

        int[][] N = new int[e.length][b + 1];

        for (int i = 0; i < e.length; i++) {
            determineNaf(N[i], e[i], w[i]);
        }
        return N;
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


    public static Point eval_interleaving(int[][] N, Point[][] P) {
        Point r = createZeroPoint(P[0][0], P[0][0], P[0][0].getE());
        int t = N[0].length - 1;
        for (int i = 1; i < N.length; i++) {
            t = (N[i].length - 1 > t) ? N[i].length - 1 : t;
        }

        for (int j = t; j >= 0; j--) {
            r.multiplyThisBy2();
            for (int i = 0; i < N.length; i++) {
                if (j < N[i].length) {
                    if (N[i][j] > 0) {
                        r = P[i][(N[i][j] - 1) >> 1].add(r);
                    } else if (N[i][j] < 0) {
                        r = r.subtract(P[i][(-N[i][j] - 1) >> 1]);
                    }
                }
            }
        }
        return r;
    }

    private static Point createZeroPoint(Object type1, Object type2,
                                         Object curve) {
        if (type1 instanceof PointGFP && type2 instanceof PointGFP
                && curve instanceof EllipticCurveGFP) {
            return new PointGFP((EllipticCurveGFP) curve);
        } else if (type1 instanceof PointGF2n && type2 instanceof PointGF2n
                && curve instanceof EllipticCurveGF2n) {
            return new PointGF2n((EllipticCurveGF2n) curve);
        }
        return null;
    }

    private static GF2nElement createGF2nOneElement(GF2nField gf2n) {
        if (gf2n instanceof GF2nONBField) {
            return GF2nONBElement.ONE((GF2nONBField) gf2n);
        }
        return GF2nPolynomialElement.ONE((GF2nPolynomialField) gf2n);
    }
}
