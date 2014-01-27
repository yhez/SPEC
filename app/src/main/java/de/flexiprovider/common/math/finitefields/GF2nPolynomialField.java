package de.flexiprovider.common.math.finitefields;

import java.util.Vector;

import de.flexiprovider.common.exceptions.GFException;
import de.flexiprovider.common.exceptions.PolynomialIsNotIrreducibleException;

public class GF2nPolynomialField extends GF2nField {


    GF2Polynomial[] squaringMatrix;

    // field polynomial is a trinomial
    private boolean isTrinomial = false;

    // field polynomial is a pentanomial
    private boolean isPentanomial = false;

    // middle coefficient of the field polynomial in case it is a trinomial
    private int tc;

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
                if (k == 3) {
                    tc = j;
                }
                if (k <= 5) {
                    pc[k - 3] = j;
                }
            }
        }
        if (k == 3) {
            isTrinomial = true;
        }
        if (k == 5) {
            isPentanomial = true;
        }
        fields = new Vector();
        matrices = new Vector();
    }


    public boolean isTrinomial() {
        return isTrinomial;
    }


    public boolean isPentanomial() {
        return isPentanomial;
    }


    public int getTc() throws GFException {
        if (!isTrinomial) {
            throw new GFException();
        }
        return tc;
    }


    public int[] getPc() throws GFException {
        if (!isPentanomial) {
            throw new GFException();
        }
        int[] result = new int[3];
        System.arraycopy(pc, 0, result, 0, 3);
        return result;
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


    protected void computeFieldPolynomial() {
        if (testTrinomials()) {
            return;
        }
        if (testPentanomials()) {
            return;
        }
        testRandom();
    }


    private boolean testTrinomials() {
        int i;
        boolean done = false;

        fieldPolynomial = new GF2Polynomial(mDegree + 1);
        fieldPolynomial.setBit(0);
        fieldPolynomial.setBit(mDegree);
        for (i = 1; (i < mDegree) && !done; i++) {
            fieldPolynomial.setBit(i);
            done = fieldPolynomial.isIrreducible();
            if (done) {
                isTrinomial = true;
                tc = i;
                return done;
            }
            fieldPolynomial.resetBit(i);
            done = fieldPolynomial.isIrreducible();
        }

        return done;
    }


    private boolean testPentanomials() {
        int i, j, k;
        boolean done = false;

        fieldPolynomial = new GF2Polynomial(mDegree + 1);
        fieldPolynomial.setBit(0);
        fieldPolynomial.setBit(mDegree);
        for (i = 1; (i <= (mDegree - 3)) && !done; i++) {
            fieldPolynomial.setBit(i);
            for (j = i + 1; (j <= (mDegree - 2)) && !done; j++) {
                fieldPolynomial.setBit(j);
                for (k = j + 1; (k <= (mDegree - 1)) && !done; k++) {
                    fieldPolynomial.setBit(k);
                    if (((mDegree & 1) != 0) | ((i & 1) != 0) | ((j & 1) != 0)
                            | ((k & 1) != 0)) {
                        done = fieldPolynomial.isIrreducible();
                        if (done) {
                            isPentanomial = true;
                            pc[0] = i;
                            pc[1] = j;
                            pc[2] = k;
                            return done;
                        }
                    }
                    fieldPolynomial.resetBit(k);
                }
                fieldPolynomial.resetBit(j);
            }
            fieldPolynomial.resetBit(i);
        }

        return done;
    }


    private boolean testRandom() {
        boolean done = false;

        fieldPolynomial = new GF2Polynomial(mDegree + 1);
        while (!done) {
            fieldPolynomial.randomize();
            fieldPolynomial.setBit(mDegree);
            fieldPolynomial.setBit(0);
            if (fieldPolynomial.isIrreducible()) {
                done = true;
                return done;
            }
        }

        return done;
    }

}
