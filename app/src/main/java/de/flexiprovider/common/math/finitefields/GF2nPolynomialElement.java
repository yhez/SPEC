package de.flexiprovider.common.math.finitefields;

import java.util.Random;

import de.flexiprovider.common.exceptions.DegreeIsEvenException;
import de.flexiprovider.common.exceptions.DifferentFieldsException;
import de.flexiprovider.common.exceptions.GFException;
import de.flexiprovider.common.exceptions.NoSolutionException;
import de.flexiprovider.common.math.FlexiBigInt;


public class GF2nPolynomialElement extends GF2nElement {

    // the used GF2Polynomial which stores the coefficients
    private GF2Polynomial polynomial;

    public GF2nPolynomialElement(GF2nPolynomialField f, Random rand) {
        mField = f;
        mDegree = mField.getDegree();
        polynomial = new GF2Polynomial(mDegree);
        randomize(rand);
    }


    public GF2nPolynomialElement(GF2nPolynomialField f, GF2Polynomial bs) {
        mField = f;
        mDegree = mField.getDegree();
        polynomial = new GF2Polynomial(bs);
        polynomial.expandN(mDegree);
    }


    public GF2nPolynomialElement(GF2nPolynomialField f, byte[] os) {
        mField = f;
        mDegree = mField.getDegree();
        polynomial = new GF2Polynomial(mDegree, os);
        polynomial.expandN(mDegree);
    }


    public GF2nPolynomialElement(GF2nPolynomialElement other) {
        mField = other.mField;
        mDegree = other.mDegree;
        polynomial = new GF2Polynomial(other.polynomial);
    }


    public Object clone() {
        return new GF2nPolynomialElement(this);
    }


    void assignZero() {
        polynomial.assignZero();
    }


    public static GF2nPolynomialElement ZERO(GF2nPolynomialField f) {
        GF2Polynomial polynomial = new GF2Polynomial(f.getDegree());
        return new GF2nPolynomialElement(f, polynomial);
    }


    public static GF2nPolynomialElement ONE(GF2nPolynomialField f) {
        GF2Polynomial polynomial = new GF2Polynomial(f.getDegree(),
                new int[]{1});
        return new GF2nPolynomialElement(f, polynomial);
    }


    private void randomize(Random rand) {
        polynomial.expandN(mDegree);
        polynomial.randomize(rand);
    }


    public boolean isZero() {
        return polynomial.isZero();
    }

    public boolean isOne() {
        return polynomial.isOne();
    }


    public boolean equals(Object other) {
        if (other == null || !(other instanceof GF2nPolynomialElement)) {
            return false;
        }
        GF2nPolynomialElement otherElem = (GF2nPolynomialElement) other;

        if (mField != otherElem.mField) {
            if (!mField.getFieldPolynomial().equals(
                    otherElem.mField.getFieldPolynomial())) {
                return false;
            }
        }

        return polynomial.equals(otherElem.polynomial);
    }

    /**
     * @return the hash code of this element
     */
    public int hashCode() {
        return mField.hashCode() + polynomial.hashCode();
    }


    private GF2Polynomial getGF2Polynomial() {
        return new GF2Polynomial(polynomial);
    }

    /**
     * Checks whether the indexed bit of the bit representation is set.
     *
     * @param index the index of the bit to test
     * @return <tt>true</tt> if the indexed bit is set
     */
    boolean testBit(int index) {
        return polynomial.testBit(index);
    }

    /**
     * Returns whether the rightmost bit of the bit representation is set. This
     * is needed for data conversion according to 1363.
     *
     * @return true if the rightmost bit of this element is set
     */
    public boolean testRightmostBit() {
        return polynomial.testBit(0);
    }

    /**
     * Compute the sum of this element and <tt>addend</tt>.
     *
     * @param addend the addend
     * @return <tt>this + other</tt> (newly created)
     * @throws DifferentFieldsException if the elements are of different fields.
     */
    public GFElement add(GFElement addend) throws DifferentFieldsException {
        GF2nPolynomialElement result = new GF2nPolynomialElement(this);
        result.addToThis(addend);
        return result;
    }

    /**
     * Compute <tt>this + addend</tt> (overwrite <tt>this</tt>).
     *
     * @param addend the addend
     * @throws DifferentFieldsException if the elements are of different fields.
     */
    public void addToThis(GFElement addend) throws DifferentFieldsException {
        if (!(addend instanceof GF2nPolynomialElement)) {
            throw new DifferentFieldsException();
        }
        if (!mField.equals(((GF2nPolynomialElement) addend).mField)) {
            throw new DifferentFieldsException();
        }
        polynomial.addToThis(((GF2nPolynomialElement) addend).polynomial);
    }

    /**
     * Returns <tt>this</tt> element + 'one".
     *
     * @return <tt>this</tt> + 'one'
     */
    public GF2nElement increase() {
        GF2nPolynomialElement result = new GF2nPolynomialElement(this);
        result.increaseThis();
        return result;
    }

    /**
     * Increases this element by 'one'.
     */
    public void increaseThis() {
        polynomial.increaseThis();
    }

    /**
     * Compute the product of this element and <tt>factor</tt>.
     *
     * @param factor the factor
     * @return <tt>this * factor</tt> (newly created)
     * @throws DifferentFieldsException if the elements are of different fields.
     */
    public GFElement multiply(GFElement factor) throws DifferentFieldsException {
        GF2nPolynomialElement result = new GF2nPolynomialElement(this);
        result.multiplyThisBy(factor);
        return result;
    }

    /**
     * Compute <tt>this * factor</tt> (overwrite <tt>this</tt>).
     *
     * @param factor the factor
     * @throws DifferentFieldsException if the elements are of different fields.
     */
    public void multiplyThisBy(GFElement factor)
            throws DifferentFieldsException {
        if (!(factor instanceof GF2nPolynomialElement)) {
            throw new DifferentFieldsException();
        }
        if (!mField.equals(((GF2nPolynomialElement) factor).mField)) {
            throw new DifferentFieldsException();
        }
        if (equals(factor)) {
            squareThis();
            return;
        }
        polynomial = polynomial
                .multiply(((GF2nPolynomialElement) factor).polynomial);
        reduceThis();
    }

    public GFElement invert() throws ArithmeticException {
        return invertMAIA();
    }

    public GF2nPolynomialElement invertMAIA() throws ArithmeticException {
        if (isZero()) {
            throw new ArithmeticException();
        }
        GF2Polynomial b = new GF2Polynomial(mDegree, "ONE");
        GF2Polynomial c = new GF2Polynomial(mDegree);
        GF2Polynomial u = getGF2Polynomial();
        GF2Polynomial v = mField.getFieldPolynomial();
        GF2Polynomial h;
        while (true) {
            while (!u.testBit(0)) { // x|u (x divides u)
                u.shiftRightThis(); // u = u / x
                if (!b.testBit(0)) {
                    b.shiftRightThis();
                } else {
                    b.addToThis(mField.getFieldPolynomial());
                    b.shiftRightThis();
                }
            }
            if (u.isOne()) {
                return new GF2nPolynomialElement((GF2nPolynomialField) mField,
                        b);
            }
            u.reduceN();
            v.reduceN();
            if (u.getLength() < v.getLength()) {
                h = u;
                u = v;
                v = h;
                h = b;
                b = c;
                c = h;
            }
            u.addToThis(v);
            b.addToThis(c);
        }
    }

    public GF2nElement square() {
        return squarePreCalc();
    }


    public void squareThis() {
        squareThisPreCalc();
    }


    public GF2nPolynomialElement squarePreCalc() {
        GF2nPolynomialElement result = new GF2nPolynomialElement(this);
        result.squareThisPreCalc();
        result.reduceThis();
        return result;
    }


    public void squareThisPreCalc() {
        polynomial.squareThisPreCalc();
        reduceThis();
    }

    public GF2nPolynomialElement power(int k) {
        if (k == 1) {
            return new GF2nPolynomialElement(this);
        }

        GF2nPolynomialElement result = GF2nPolynomialElement
                .ONE((GF2nPolynomialField) mField);
        if (k == 0) {
            return result;
        }

        GF2nPolynomialElement x = new GF2nPolynomialElement(this);
        x.polynomial.expandN((x.mDegree << 1) + 32); // increase performance
        x.polynomial.reduceN();

        for (int i = 0; i < mDegree; i++) {
            if ((k & (1 << i)) != 0) {
                result.multiplyThisBy(x);
            }
            x.square();
        }

        return result;
    }

    /**
     * Compute the square root of this element and return the result in a new
     * {@link GF2nPolynomialElement}.
     *
     * @return <tt>this<sup>1/2</sup></tt> (newly created)
     */
    public GF2nElement squareRoot() {
        GF2nPolynomialElement result = new GF2nPolynomialElement(this);
        result.squareRootThis();
        return result;
    }

    /**
     * Compute the square root of this element.
     */
    public void squareRootThis() {
        // increase performance
        polynomial.expandN((mDegree << 1) + 32);
        polynomial.reduceN();
        for (int i = 0; i < mField.getDegree() - 1; i++) {
            squareThis();
        }
    }

    public GF2nElement solveQuadraticEquation() throws NoSolutionException {
        if (isZero()) {
            return ZERO((GF2nPolynomialField) mField);
        }

        if ((mDegree & 1) == 1) {
            return halfTrace();
        }

        GF2nPolynomialElement z, w;
        do {
            // step 1.
            GF2nPolynomialElement p = new GF2nPolynomialElement(
                    (GF2nPolynomialField) mField, new Random());
            // step 2.
            z = ZERO((GF2nPolynomialField) mField);
            w = (GF2nPolynomialElement) p.clone();
            // step 3.
            for (int i = 1; i < mDegree; i++) {
                // compute z = z^2 + w^2 * this
                // and w = w^2 + p
                z.squareThis();
                w.squareThis();
                z.addToThis(w.multiply(this));
                w.addToThis(p);
            }
        } while (w.isZero()); // step 4.

        if (!equals(z.square().add(z))) {
            throw new NoSolutionException();
        }

        // step 5.
        return z;
    }

    public int trace() {
        GF2nPolynomialElement t = new GF2nPolynomialElement(this);
        int i;

        for (i = 1; i < mDegree; i++) {
            t.squareThis();
            t.addToThis(this);
        }

        if (t.isOne()) {
            return 1;
        }
        return 0;
    }
    private GF2nPolynomialElement halfTrace() throws DegreeIsEvenException {
        if ((mDegree & 0x01) == 0) {
            throw new DegreeIsEvenException();
        }
        int i;
        GF2nPolynomialElement h = new GF2nPolynomialElement(this);

        for (i = 1; i <= ((mDegree - 1) >> 1); i++) {
            h.squareThis();
            h.squareThis();
            h.addToThis(this);
        }

        return h;
    }

    private void reduceThis() {
        if (polynomial.getLength() > mDegree) { // really reduce ?
            if (((GF2nPolynomialField) mField).isTrinomial()) { // fieldpolonomial
                // is trinomial
                int tc;
                try {
                    tc = ((GF2nPolynomialField) mField).getTc();
                } catch (GFException NATExc) {
                    throw new GFException(
                            "GF2nPolynomialElement.reduce: the field"
                                    + " polynomial is not a trinomial");
                }
                if (((mDegree - tc) <= 32) // do we have to use slow
                        // bitwise reduction ?
                        || (polynomial.getLength() > (mDegree << 1))) {
                    reduceTrinomialBitwise(tc);
                    return;
                }
                polynomial.reduceTrinomial(mDegree, tc);
                return;
            } else if (((GF2nPolynomialField) mField).isPentanomial()) { // fieldpolynomial
                // is
                // pentanomial
                int[] pc;
                try {
                    pc = ((GF2nPolynomialField) mField).getPc();
                } catch (GFException NATExc) {
                    throw new GFException(
                            "GF2nPolynomialElement.reduce: the field"
                                    + " polynomial is not a pentanomial");
                }
                if (((mDegree - pc[2]) <= 32) // do we have to use slow
                        // bitwise reduction ?
                        || (polynomial.getLength() > (mDegree << 1))) {
                    reducePentanomialBitwise(pc);
                    return;
                }
                polynomial.reducePentanomial(mDegree, pc);
                return;
            } else { // fieldpolynomial is something else
                polynomial = polynomial.remainder(mField.getFieldPolynomial());
                polynomial.expandN(mDegree);
                return;
            }
        }
        if (polynomial.getLength() < mDegree) {
            polynomial.expandN(mDegree);
        }
    }

    private void reduceTrinomialBitwise(int tc) {
        int i;
        int k = mDegree - tc;
        for (i = polynomial.getLength() - 1; i >= mDegree; i--) {
            if (polynomial.testBit(i)) {

                polynomial.xorBit(i);
                polynomial.xorBit(i - k);
                polynomial.xorBit(i - mDegree);

            }
        }
        polynomial.reduceN();
        polynomial.expandN(mDegree);
    }

    private void reducePentanomialBitwise(int[] pc) {
        int i;
        int k = mDegree - pc[2];
        int l = mDegree - pc[1];
        int m = mDegree - pc[0];
        for (i = polynomial.getLength() - 1; i >= mDegree; i--) {
            if (polynomial.testBit(i)) {
                polynomial.xorBit(i);
                polynomial.xorBit(i - k);
                polynomial.xorBit(i - l);
                polynomial.xorBit(i - m);
                polynomial.xorBit(i - mDegree);

            }
        }
        polynomial.reduceN();
        polynomial.expandN(mDegree);
    }

    // /////////////////////////////////////////////////////////////////////
    // conversion
    // /////////////////////////////////////////////////////////////////////

    /**
     * Returns a string representing this Bitstrings value using hexadecimal
     * radix in MSB-first order.
     *
     * @return a String representing this Bitstrings value.
     */
    public String toString() {
        return polynomial.toString(16);
    }

    /**
     * Returns a string representing this Bitstrings value using hexadecimal or
     * binary radix in MSB-first order.
     *
     * @param radix the radix to use (2 or 16, otherwise 2 is used)
     * @return a String representing this Bitstrings value.
     */
    public String toString(int radix) {
        return polynomial.toString(radix);
    }

    public byte[] toByteArray() {
        return polynomial.toByteArray();
    }

    public FlexiBigInt toFlexiBigInt() {
        return polynomial.toFlexiBigInt();
    }

}
