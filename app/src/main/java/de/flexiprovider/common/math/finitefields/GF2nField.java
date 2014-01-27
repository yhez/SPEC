package de.flexiprovider.common.math.finitefields;

import java.util.Vector;


public abstract class GF2nField {


    protected int mDegree;


    protected GF2Polynomial fieldPolynomial;


    protected Vector fields;


    protected Vector matrices;


    public final int getDegree() {
        return mDegree;
    }


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


    public int hashCode() {
        return mDegree + fieldPolynomial.hashCode();
    }

    protected abstract void computeFieldPolynomial();

}
