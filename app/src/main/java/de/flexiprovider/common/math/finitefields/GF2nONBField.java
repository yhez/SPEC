/*
 * Copyright (c) 1998-2003 by The FlexiProvider Group,
 *                            Technische Universitaet Darmstadt 
 *
 * For conditions of usage and distribution please refer to the
 * file COPYING in the root directory of this package.
 *
 */
package de.flexiprovider.common.math.finitefields;

import java.util.Random;
import java.util.Vector;

import de.flexiprovider.common.exceptions.NoSuchBasisException;
import de.flexiprovider.common.math.IntegerFunctions;

/**
 * This class implements the abstract class <tt>GF2nField</tt> for ONB
 * representation. It computes the fieldpolynomial, multiplication matrix and
 * one of its roots mONBRoot, (see for example <a
 * href=http://www2.certicom.com/ecc/intro.htm>Certicoms Whitepapers</a>).
 * GF2nField is used by GF2nONBElement which implements the elements of this
 * field.
 *
 * @author Birgit Henhapl
 * @author Oliver Seiler
 * @see GF2nField
 * @see GF2nONBElement
 */
public class GF2nONBField extends GF2nField {

    // ///////////////////////////////////////////////////////////////////
    // Hashtable for irreducible normal polynomials //
    // ///////////////////////////////////////////////////////////////////

    // i*5 + 0 i*5 + 1 i*5 + 2 i*5 + 3 i*5 + 4
    /*
     * private static int[][] mNB = {{0, 0, 0}, {0, 0, 0}, {1, 0, 0}, {1, 0, 0},
     * {1, 0, 0}, // i = 0 {2, 0, 0}, {1, 0, 0}, {1, 0, 0}, {4, 3, 1}, {1, 0,
     * 0}, // i = 1 {3, 0, 0}, {2, 0, 0}, {3, 0, 0}, {4, 3, 1}, {5, 0, 0}, // i =
     * 2 {1, 0, 0}, {5, 3, 1}, {3, 0, 0}, {3, 0, 0}, {5, 2, 1}, // i = 3 {3, 0,
     * 0}, {2, 0, 0}, {1, 0, 0}, {5, 0, 0}, {4, 3, 1}, // i = 4 {3, 0, 0}, {4,
     * 3, 1}, {5, 2, 1}, {1, 0, 0}, {2, 0, 0}, // i = 5 {1, 0, 0}, {3, 0, 0},
     * {7, 3, 2}, {10, 0, 0}, {7, 0, 0}, // i = 6 {2, 0, 0}, {9, 0, 0}, {6, 4,
     * 1}, {6, 5, 1}, {4, 0, 0}, // i = 7 {5, 4, 3}, {3, 0, 0}, {7, 0, 0}, {6,
     * 4, 3}, {5, 0, 0}, // i = 8 {4, 3, 1}, {1, 0, 0}, {5, 0, 0}, {5, 3, 2},
     * {9, 0, 0}, // i = 9 {4, 3, 2}, {6, 3, 1}, {3, 0, 0}, {6, 2, 1}, {9, 0,
     * 0}, // i = 10 {7, 0, 0}, {7, 4, 2}, {4, 0, 0}, {19, 0, 0}, {7, 4, 2}, //
     * i = 11 {1, 0, 0}, {5, 2, 1}, {29, 0, 0}, {1, 0, 0}, {4, 3, 1}, // i = 12
     * {18, 0, 0}, {3, 0, 0}, {5, 2, 1}, {9, 0, 0}, {6, 5, 2}, // i = 13 {5, 3,
     * 1}, {6, 0, 0}, {10, 9, 3}, {25, 0, 0}, {35, 0, 0}, // i = 14 {6, 3, 1},
     * {21, 0, 0}, {6, 5, 2}, {6, 5, 3}, {9, 0, 0}, // i = 15 {9, 4, 2}, {4, 0,
     * 0}, {8, 3, 1}, {7, 4, 2}, {5, 0, 0}, // i = 16 {8, 2, 1}, {21, 0, 0},
     * {13, 0, 0}, {7, 6, 2}, {38, 0, 0}, // i = 17 {27, 0, 0}, {8, 5, 1}, {21,
     * 0, 0}, {2, 0, 0}, {21, 0, 0}, // i = 18 {11, 0, 0}, {10, 9, 6}, {6, 0,
     * 0}, {11, 0, 0}, {6, 3, 1}, // i = 19 {15, 0, 0}, {7, 6, 1}, {29, 0, 0},
     * {9, 0, 0}, {4, 3, 1}, // i = 20 {4, 0, 0}, {15, 0, 0}, {9, 7, 4}, {17, 0,
     * 0}, {5, 4, 2}, // i = 21 {33, 0, 0}, {10, 0, 0}, {5, 4, 3}, {9, 0, 0},
     * {5, 3, 2}, // i = 22 {8, 7, 5}, {4, 2, 1}, {5, 2, 1}, {33, 0, 0}, {8, 0,
     * 0}, // i = 23 {4, 3, 1}, {18, 0, 0}, {6, 2, 1}, {2, 0, 0}, {19, 0, 0}, //
     * i = 24 {7, 6, 5}, {21, 0, 0}, {1, 0, 0}, {7, 2, 1}, {5, 0, 0}, // i = 25
     * {3, 0, 0}, {8, 3, 2}, {17, 0, 0}, {9, 8, 2}, {57, 0, 0}, // i = 26 {11,
     * 0, 0}, {5, 3, 2}, {21, 0, 0}, {8, 7, 1}, {8, 5, 3}, // i = 27 {15, 0, 0},
     * {10, 4, 1}, {21, 0, 0}, {5, 3, 2}, {7, 4, 2}, // i = 28 {52, 0, 0}, {71,
     * 0, 0}, {14, 0, 0}, {27, 0, 0}, {10, 9, 7}, // i = 29 {53, 0, 0}, {3, 0,
     * 0}, {6, 3, 2}, {1, 0, 0}, {15, 0, 0}, // i = 30 {62, 0, 0}, {9, 0, 0},
     * {6, 5, 2}, {8, 6, 5}, {31, 0, 0}, // i = 31 {5, 3, 2}, {18, 0, 0 }, {27,
     * 0, 0}, {7, 6, 3}, {10, 8, 7}, // i = 32 {9, 8, 3}, {37, 0, 0}, {6, 0, 0},
     * {15, 3, 2}, {34, 0, 0}, // i = 33 {11, 0, 0}, {6, 5, 2}, {1, 0, 0}, {8,
     * 5, 2}, {13, 0, 0}, // i = 34 {6, 0, 0}, {11, 3, 2}, {8, 0, 0}, {31, 0,
     * 0}, {4, 2, 1}, // i = 35 {3, 0, 0}, {7, 6, 1}, {81, 0, 0}, {56, 0, 0},
     * {9, 8, 7}, // i = 36 {24, 0, 0}, {11, 0, 0}, {7, 6, 5}, {6, 5, 2}, {6, 5,
     * 2}, // i = 37 {8, 7, 6}, {9, 0, 0}, {7, 2, 1}, {15, 0, 0}, {87, 0, 0}, //
     * i = 38 {8, 3, 2}, {3, 0, 0}, {9, 4, 2}, {9, 0, 0}, {34, 0, 0}, // i = 39
     * {5, 3, 2}, {14, 0, 0}, {55, 0, 0}, {8, 7, 1}, {27, 0, 0}, // i = 40 {9,
     * 5, 2}, {10, 9, 5}, {43, 0, 0}, {8, 6, 2}, {6, 0, 0}, // i = 41 {7, 0, 0},
     * {11, 10, 8}, {105, 0, 0}, {6, 5, 2}, {73, 0, 0}}; // i = 42
     */
    // /////////////////////////////////////////////////////////////////////
    // member variables
    // /////////////////////////////////////////////////////////////////////
    private static final int MAXLONG = 64;

    /**
     * holds the length of the array-representation of degree mDegree.
     */
    private int mLength;

    /**
     * holds the number of relevant bits in mONBPol[mLength-1].
     */
    private int mBit;

    /**
     * holds the type of mONB
     */
    private int mType;

    /**
     * holds the multiplication matrix
     */
    int[][] mMult;

    // /////////////////////////////////////////////////////////////////////
    // constructors
    // /////////////////////////////////////////////////////////////////////

    /**
     * constructs an instance of the finite field with 2<sup>deg</sup>
     * elements and characteristic 2.
     *
     * @param deg -
     *            the extention degree of this field
     * @throws NoSuchBasisException if an ONB-implementation other than type 1 or type 2 is
     *                              requested.
     */
    public GF2nONBField(int deg) throws NoSuchBasisException {
        if (deg < 3) {
            throw new IllegalArgumentException("k must be at least 3");
        }

        mDegree = deg;
        mLength = mDegree / MAXLONG;
        mBit = mDegree & (MAXLONG - 1);
        if (mBit == 0) {
            mBit = MAXLONG;
        } else {
            mLength++;
        }

        computeType();

        // only ONB-implementations for type 1 and type 2
        //
        if (mType < 3) {
            mMult = new int[mDegree][2];
            for (int i = 0; i < mDegree; i++) {
                mMult[i][0] = -1;
                mMult[i][1] = -1;
            }
            computeMultMatrix();
        } else {
            throw new NoSuchBasisException("\nThe type of this field is "
                    + mType);
        }
        computeFieldPolynomial();
        fields = new Vector();
        matrices = new Vector();
    }

    // /////////////////////////////////////////////////////////////////////
    // access
    // /////////////////////////////////////////////////////////////////////

    int getONBLength() {
        return mLength;
    }

    int getONBBit() {
        return mBit;
    }

    // /////////////////////////////////////////////////////////////////////
    // arithmetic
    // /////////////////////////////////////////////////////////////////////

    /**
     * Computes the field polynomial for a ONB according to IEEE 1363 A.7.2
     * (p110f).
     *
     * @see "P1363 A.7.2, p110f"
     */
    protected void computeFieldPolynomial() {
        if (mType == 1) {
            fieldPolynomial = new GF2Polynomial(mDegree + 1, "ALL");
        } else if (mType == 2) {
            // 1. q = 1
            GF2Polynomial q = new GF2Polynomial(mDegree + 1, "ONE");
            // 2. p = t+1
            GF2Polynomial p = new GF2Polynomial(mDegree + 1, "X");
            p.addToThis(q);
            GF2Polynomial r;
            int i;
            // 3. for i = 1 to (m-1) do
            for (i = 1; i < mDegree; i++) {
                // r <- q
                r = q;
                // q <- p
                q = p;
                // p = tq+r
                p = q.shiftLeft();
                p.addToThis(r);
            }
            fieldPolynomial = p;
        }
    }

    private void computeType() throws NoSuchBasisException {
        if ((mDegree & 7) == 0) {
            throw new NoSuchBasisException(
                    "The extension degree is divisible by 8!");
        }
        // checking for the type
        int s;
        int k;
        mType = 1;
        for (int d = 0; d != 1; mType++) {
            s = mType * mDegree + 1;
            if (IntegerFunctions.isPrime(s)) {
                k = IntegerFunctions.order(2, s);
                d = IntegerFunctions.gcd(mType * mDegree / k, mDegree);
            }
        }
        mType--;
        if (mType == 1) {
            s = (mDegree << 1) + 1;
            if (IntegerFunctions.isPrime(s)) {
                k = IntegerFunctions.order(2, s);
                int d = IntegerFunctions.gcd((mDegree << 1) / k, mDegree);
                if (d == 1) {
                    mType++;
                }
            }
        }
    }

    private void computeMultMatrix() {

        if ((mType & 7) != 0) {
            int p = mType * mDegree + 1;

            // compute sequence F[1] ... F[p-1] via A.3.7. of 1363.
            // F[0] will not be filled!
            //
            int[] F = new int[p];

            int u;
            if (mType == 1) {
                u = 1;
            } else if (mType == 2) {
                u = p - 1;
            } else {
                u = elementOfOrder(mType, p);
            }

            int w = 1;
            int n;
            for (int j = 0; j < mType; j++) {
                n = w;

                for (int i = 0; i < mDegree; i++) {
                    F[n] = i;
                    n = (n << 1) % p;
                    if (n < 0) {
                        n += p;
                    }
                }
                w = u * w % p;
                if (w < 0) {
                    w += p;
                }
            }

            // building the matrix (mDegree * 2)
            //
            if (mType == 1) {
                for (int k = 1; k < p - 1; k++) {
                    if (mMult[F[k + 1]][0] == -1) {
                        mMult[F[k + 1]][0] = F[p - k];
                    } else {
                        mMult[F[k + 1]][1] = F[p - k];
                    }
                }

                int m_2 = mDegree >> 1;
                for (int k = 1; k <= m_2; k++) {

                    if (mMult[k - 1][0] == -1) {
                        mMult[k - 1][0] = m_2 + k - 1;
                    } else {
                        mMult[k - 1][1] = m_2 + k - 1;
                    }

                    if (mMult[m_2 + k - 1][0] == -1) {
                        mMult[m_2 + k - 1][0] = k - 1;
                    } else {
                        mMult[m_2 + k - 1][1] = k - 1;
                    }
                }
            } else if (mType == 2) {
                for (int k = 1; k < p - 1; k++) {
                    if (mMult[F[k + 1]][0] == -1) {
                        mMult[F[k + 1]][0] = F[p - k];
                    } else {
                        mMult[F[k + 1]][1] = F[p - k];
                    }
                }
            } else {
                throw new RuntimeException("only type 1 or type 2 implemented");
            }
        } else {
            throw new RuntimeException("bisher nur fuer Gausssche Normalbasen"
                    + " implementiert");
        }
    }

    private int elementOfOrder(int k, int p) {
        Random random = new Random();
        int m = 0;
        while (m == 0) {
            m = random.nextInt();
            m %= p - 1;
            if (m < 0) {
                m += p - 1;
            }
        }

        int l = IntegerFunctions.order(m, p);

        while (l % k != 0 || l == 0) {
            while (m == 0) {
                m = random.nextInt();
                m %= p - 1;
                if (m < 0) {
                    m += p - 1;
                }
            }
            l = IntegerFunctions.order(m, p);
        }
        int r = m;

        l = k / l;

        for (int i = 2; i <= l; i++) {
            r *= m;
        }

        return r;
    }

}
