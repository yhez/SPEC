package de.flexiprovider.common.math.finitefields;

public abstract class GF2nField {
    protected int mDegree;
    protected GF2Polynomial fieldPolynomial;

    public final boolean equals(Object other) {
        if (other == null || !(other instanceof GF2nField)) {
            return false;
        }
        GF2nField otherField = (GF2nField) other;

        return otherField.mDegree == mDegree && fieldPolynomial.equals(otherField.fieldPolynomial) && !((this instanceof GF2nPolynomialField) && !(otherField instanceof GF2nPolynomialField));
    }

    public int hashCode() {
        return mDegree + fieldPolynomial.hashCode();
    }

}
