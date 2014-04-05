package de.flexiprovider.common.math.finitefields;

public class GF2nONBField extends GF2nField {
    private int mLength;
    private int mBit;
    private int mType;
    int[][] mMult;

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
}
