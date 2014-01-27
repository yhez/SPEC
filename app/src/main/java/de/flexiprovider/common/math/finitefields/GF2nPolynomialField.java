package de.flexiprovider.common.math.finitefields;

import java.util.Vector;

import de.flexiprovider.common.exceptions.GFException;
import de.flexiprovider.common.exceptions.PolynomialIsNotIrreducibleException;

public class GF2nPolynomialField extends GF2nField {

    /**
     * Matrix used for fast squaring
     */
    GF2Polynomial[] squaringMatrix;

    // field polynomial is a trinomial
    private boolean isTrinomial = false;

    // field polynomial is a pentanomial
    private boolean isPentanomial = false;

    // middle coefficient of the field polynomial in case it is a trinomial
    private int tc;

    // middle 3 coefficients of the field polynomial in case it is a pentanomial
    private int[] pc = new int[3];

    /**
     * Creates a new GF2nField of degree <i>i</i> and uses the given
     * <i>polynomial</i> as field polynomial. The <i>polynomial</i> is checked
     * whether it is irreducible. This can take some time if <i>i</i> is huge!
     *
     * @param deg        degree of the GF2nField
     * @param polynomial the field polynomial to use
     * @throws PolynomialIsNotIrreducibleException if the given polynomial is not irreducible in GF(2^<i>i</i>)
     */
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

    /**
     * Returns true if the field polynomial is a trinomial. The coefficient can
     * be retrieved using getTc().
     *
     * @return true if the field polynomial is a trinomial
     */
    public boolean isTrinomial() {
        return isTrinomial;
    }

    /**
     * Returns true if the field polynomial is a pentanomial. The coefficients
     * can be retrieved using getPc().
     *
     * @return true if the field polynomial is a pentanomial
     */
    public boolean isPentanomial() {
        return isPentanomial;
    }

    /**
     * Returns the degree of the middle coefficient of the used field trinomial
     * (x^n + x^(getTc()) + 1).
     *
     * @return the middle coefficient of the used field trinomial
     * @throws GFException if the field polynomial is not a trinomial
     */
    public int getTc() throws GFException {
        if (!isTrinomial) {
            throw new GFException();
        }
        return tc;
    }

    /**
     * Returns the degree of the middle coefficients of the used field
     * pentanomial (x^n + x^(getPc()[2]) + x^(getPc()[1]) + x^(getPc()[0]) + 1).
     *
     * @return the middle coefficients of the used field pentanomial
     * @throws GFException if the field polynomial is not a pentanomial
     */
    public int[] getPc() throws GFException {
        if (!isPentanomial) {
            throw new GFException();
        }
        int[] result = new int[3];
        System.arraycopy(pc, 0, result, 0, 3);
        return result;
    }

    /**
     * Computes a new squaring matrix used for fast squaring.
     *
     * @see GF2nPolynomialElement#square
     */
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

    /**
     * Computes the field polynomial. This can take a long time for big degrees.
     */
    protected void computeFieldPolynomial() {
        if (testTrinomials()) {
            return;
        }
        if (testPentanomials()) {
            return;
        }
        testRandom();
    }

    /**
     * Tests all trinomials of degree (n+1) until a irreducible is found and
     * stores the result in <i>field polynomial</i>. Returns false if no
     * irreducible trinomial exists in GF(2^n). This can take very long for huge
     * degrees.
     *
     * @return true if an irreducible trinomial is found
     */
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

    /**
     * Tests all pentanomials of degree (n+1) until a irreducible is found and
     * stores the result in <i>field polynomial</i>. Returns false if no
     * irreducible pentanomial exists in GF(2^n). This can take very long for
     * huge degrees.
     *
     * @return true if an irreducible pentanomial is found
     */
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

    /**
     * Tests random polynomials of degree (n+1) until an irreducible is found
     * and stores the result in <i>field polynomial</i>. This can take very
     * long for huge degrees.
     *
     * @return true
     */
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
