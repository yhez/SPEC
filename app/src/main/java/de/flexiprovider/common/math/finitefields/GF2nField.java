package de.flexiprovider.common.math.finitefields;

import java.util.Vector;


public abstract class GF2nField {

    /**
     * the degree of this field
     */
    protected int mDegree;

    /**
     * the irreducible fieldPolynomial stored in normal order (also for ONB)
     */
    protected GF2Polynomial fieldPolynomial;

    /**
     * holds a list of GF2nFields to which elements have been converted and thus
     * a COB-Matrix exists
     */
    protected Vector fields;

    /**
     * the COB matrices
     */
    protected Vector matrices;

    /**
     * Returns the degree <i>n</i> of this field.
     *
     * @return the degree <i>n</i> of this field
     */
    public final int getDegree() {
        return mDegree;
    }

    /**
     * Returns the fieldpolynomial as a new Bitstring.
     *
     * @return a copy of the fieldpolynomial as a new Bitstring
     */
    public final GF2Polynomial getFieldPolynomial() {
        if (fieldPolynomial == null) {
            computeFieldPolynomial();
        }
        return new GF2Polynomial(fieldPolynomial);
    }

    public final boolean equals(Object other) {
        if (other == null || !(other instanceof GF2nField)) {
            return false;
        }

        GF2nField otherField = (GF2nField) other;

        return otherField.mDegree == mDegree && fieldPolynomial.equals(otherField.fieldPolynomial) && !((this instanceof GF2nPolynomialField) && !(otherField instanceof GF2nPolynomialField)) && !((this instanceof GF2nONBField) && !(otherField instanceof GF2nONBField));
    }

    /**
     * @return the hash code of this field
     */
    public int hashCode() {
        return mDegree + fieldPolynomial.hashCode();
    }

    protected abstract void computeFieldPolynomial();

}
