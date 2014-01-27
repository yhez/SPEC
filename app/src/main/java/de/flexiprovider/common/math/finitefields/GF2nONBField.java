package de.flexiprovider.common.math.finitefields;

import java.util.Random;
import java.util.Vector;

import de.flexiprovider.common.exceptions.NoSuchBasisException;
import de.flexiprovider.common.math.IntegerFunctions;


public class GF2nONBField extends GF2nField {

    private static final int MAXLONG = 64;


    private int mLength;


    private int mBit;


    private int mType;


    int[][] mMult;




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





    int getONBLength() {
        return mLength;
    }

    int getONBBit() {
        return mBit;
    }


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
