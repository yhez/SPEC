package de.flexiprovider.my;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.io.StreamCorruptedException;



public final class MathContext implements Serializable {

    /* ----- Constants ----- */

    // Smallest values for digits (Maximum is Integer.MAX_VALUE)
    private static final int MIN_DIGITS = 0;

    // Serialization version
    private static final long serialVersionUID = 5579720004786848255L;

    final int precision;


    final RoundingMode roundingMode;

    /* ----- Constructors ----- */


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

    // Private methods


    private void readObject(ObjectInputStream s)
            throws IOException, ClassNotFoundException {
        s.defaultReadObject();     // read in all fields
        // validate possibly bad fields
        if (precision < MIN_DIGITS) {
            String message = "MathContext: invalid digits in stream";
            throw new StreamCorruptedException(message);
        }
        if (roundingMode == null) {
            String message = "MathContext: null roundingMode in stream";
            throw new StreamCorruptedException(message);
        }
    }

}
