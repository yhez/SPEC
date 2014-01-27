package de.flexiprovider.common.math.codingtheory;

import de.flexiprovider.common.util.LittleEndianConversions;


public class GF2mField {


    private int degree = 0;

    private int polynomial;


    public int getDegree() {
        return degree;
    }


    public int getPolynomial() {
        return polynomial;
    }


    public byte[] getEncoded() {
        return LittleEndianConversions.I2OSP(polynomial);
    }

    public int add(int a, int b) {
        return a ^ b;
    }

    public int mult(int a, int b) {
        return PolynomialRingGF2.modMultiply(a, b, polynomial);
    }
    public int exp(int a, int k) {
        if (a == 0) {
            return 0;
        }
        if (a == 1) {
            return 1;
        }
        int result = 1;
        if (k < 0) {
            a = inverse(a);
            k = -k;
        }
        while (k != 0) {
            if ((k & 1) == 1) {
                result = mult(result, a);
            }
            a = mult(a, a);
            k >>>= 1;
        }
        return result;
    }


    public int inverse(int a) {
        int d = (1 << degree) - 2;

        return exp(a, d);
    }


    public boolean isElementOfThisField(int e) {
        // e is encoded element of this field iff 0<= e < |2^m|
        if (degree == 31) {
            return e >= 0;
        }
        return e >= 0 && e < (1 << degree);
    }


    public boolean equals(Object other) {
        if ((other == null) || !(other instanceof GF2mField)) {
            return false;
        }

        GF2mField otherField = (GF2mField) other;

        return (degree == otherField.degree)
                && (polynomial == otherField.polynomial);

    }

    public int hashCode() {
        return polynomial;
    }


    public String toString() {
        return "Finite Field GF(2^" + degree + ") = " + "GF(2)[X]/<"
                + polyToString(polynomial) + "> ";
    }

    private static String polyToString(int p) {
        String str = "";
        if (p == 0) {
            str = "0";
        } else {
            byte b = (byte) (p & 0x01);
            if (b == 1) {
                str = "1";
            }
            p >>>= 1;
            int i = 1;
            while (p != 0) {
                b = (byte) (p & 0x01);
                if (b == 1) {
                    str = str + "+x^" + i;
                }
                p >>>= 1;
                i++;
            }
        }
        return str;
    }

}
