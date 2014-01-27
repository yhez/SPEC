package de.flexiprovider.common.math.finitefields;

import de.flexiprovider.common.exceptions.DifferentFieldsException;
import de.flexiprovider.common.exceptions.NoSolutionException;


public abstract class GF2nElement implements GFElement {

    // /////////////////////////////////////////////////////////////////////
    // member variables
    // /////////////////////////////////////////////////////////////////////

    /**
     * holds a pointer to this element's corresponding field.
     */
    protected GF2nField mField;

    /**
     * holds the extension degree <i>n</i> of this element's corresponding
     * field.
     */
    protected int mDegree;

    // /////////////////////////////////////////////////////////////////////
    // pseudo-constructors
    // /////////////////////////////////////////////////////////////////////

    /**
     * @return a copy of this GF2nElement
     */
    public abstract Object clone();

    // /////////////////////////////////////////////////////////////////////
    // assignments
    // /////////////////////////////////////////////////////////////////////

    /**
     * Assign the value 0 to this element.
     */
    abstract void assignZero();

    // /////////////////////////////////////////////////////////////////////
    // access
    // /////////////////////////////////////////////////////////////////////

    /**
     * Returns whether the rightmost bit of the bit representation is set. This
     * is needed for data conversion according to 1363.
     *
     * @return true if the rightmost bit of this element is set
     */
    public abstract boolean testRightmostBit();

    /**
     * Checks whether the indexed bit of the bit representation is set
     *
     * @param index the index of the bit to test
     * @return <tt>true</tt> if the indexed bit is set
     */
    abstract boolean testBit(int index);

    /**
     * Returns the field of this element.
     *
     * @return the field of this element
     */
    public final GF2nField getField() {
        return mField;
    }

    // /////////////////////////////////////////////////////////////////////
    // arithmetic
    // /////////////////////////////////////////////////////////////////////

    /**
     * Increases this element by one.
     */
    public abstract void increaseThis();

    /**
     * Compute the difference of this element and <tt>minuend</tt>.
     *
     * @param minuend the minuend
     * @return <tt>this - minuend</tt> (newly created)
     * @throws DifferentFieldsException if the elements are of different fields.
     */
    public final GFElement subtract(GFElement minuend)
            throws DifferentFieldsException {
        return add(minuend);
    }

    public abstract GF2nElement square();

    /**
     * Squares <tt>this</tt> element.
     */
    public abstract void squareThis();

    /**
     * Compute the square root of this element and return the result in a new
     * {@link GF2nElement}.
     *
     * @return <tt>this<sup>1/2</sup></tt> (newly created)
     */
    public abstract GF2nElement squareRoot();

    /**
     * Solves a quadratic equation.<br>
     * Let z<sup>2</sup> + z = <tt>this</tt>. Then this method returns z.
     *
     * @return z with z<sup>2</sup> + z = <tt>this</tt>
     * @throws NoSolutionException if z<sup>2</sup> + z = <tt>this</tt> does not have a
     *                             solution
     */
    public abstract GF2nElement solveQuadraticEquation()
            throws NoSolutionException;

}
