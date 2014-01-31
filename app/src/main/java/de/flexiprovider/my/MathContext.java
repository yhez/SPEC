package de.flexiprovider.my;

import java.io.Serializable;



public final class MathContext implements Serializable {


    private static final int MIN_DIGITS = 0;

    final int precision;


    final RoundingMode roundingMode;


    public MathContext(int setPrecision,
                       RoundingMode setRoundingMode) {
        if (setPrecision < MIN_DIGITS)
            throw new IllegalArgumentException("Digits < 0");
        if (setRoundingMode == null)
            throw new NullPointerException("null RoundingMode");

        precision = setPrecision;
        roundingMode = setRoundingMode;
    }


    public boolean equals(Object x) {
        MathContext mc;
        if (!(x instanceof MathContext))
            return false;
        mc = (MathContext) x;
        return mc.precision == this.precision
                && mc.roundingMode == this.roundingMode; // no need for .equals()
    }


    public int hashCode() {
        return this.precision + roundingMode.hashCode() * 59;
    }


    public String toString() {
        return "precision=" + precision + " " +
                "roundingMode=" + roundingMode.toString();
    }


}
