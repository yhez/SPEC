package de.flexiprovider.common.math.finitefields;

import de.flexiprovider.common.exceptions.PolynomialIsNotIrreducibleException;

public class GF2nPolynomialField extends GF2nField {

    GF2Polynomial[] squaringMatrix;
    // middle 3 coefficients of the field polynomial in case it is a pentanomial
    private int[] pc = new int[3];

    public GF2nPolynomialField(int deg, GF2Polynomial polynomial)
            throws PolynomialIsNotIrreducibleException {
        if (deg < 3) {
            throw new IllegalArgumentException("degree must be at least 3");
        }
        if (polynomial.getLength() != deg + 1) {
            throw new PolynomialIsNotIrreducibleException();
        }
        if (!polynomial.isIrreducible()) {
            throw new PolynomialIsNotIrreducibleException();
        }
        mDegree = deg;
        // fieldPolynomial = new Bitstring(polynomial);
        fieldPolynomial = polynomial;
        computeSquaringMatrix();
        int k = 2; // check if the polynomial is a trinomial or pentanomial
        for (int j = 1; j < fieldPolynomial.getLength() - 1; j++) {
            if (fieldPolynomial.testBit(j)) {
                k++;
                if (k <= 5) {
                    pc[k - 3] = j;
                }
            }
        }
    }

    private void computeSquaringMatrix() {
        GF2Polynomial[] d = new GF2Polynomial[mDegree - 1];
        int i, j;
        squaringMatrix = new GF2Polynomial[mDegree];
        for (i = 0; i < squaringMatrix.length; i++) {
            squaringMatrix[i] = new GF2Polynomial(mDegree, "ZERO");
        }

        for (i = 0; i < mDegree - 1; i++) {
            d[i] = new GF2Polynomial(1, "ONE").shiftLeft(mDegree + i)
                    .remainder(fieldPolynomial);
        }
        for (i = 1; i <= Math.abs(mDegree >> 1); i++) {
            for (j = 1; j <= mDegree; j++) {
                if (d[mDegree - (i << 1)].testBit(mDegree - j)) {
                    squaringMatrix[j - 1].setBit(mDegree - i);
                }
            }
        }
        for (i = Math.abs(mDegree >> 1) + 1; i <= mDegree; i++) {
            squaringMatrix[(i << 1) - mDegree - 1].setBit(mDegree - i);
        }

    }

}
