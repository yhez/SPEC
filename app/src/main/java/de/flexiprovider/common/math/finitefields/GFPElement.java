package de.flexiprovider.common.math.finitefields;

import java.math.BigInteger;

import de.flexiprovider.common.exceptions.DifferentFieldsException;

public class GFPElement implements GFElement {

    private BigInteger mValue;

    private BigInteger mP;


    public GFPElement(BigInteger value, BigInteger p) {
        mValue = value.mod(p);
        mP = p;
    }

    public GFPElement(byte[] encValue, BigInteger p) {
        mValue = new BigInteger(1, encValue).mod(p);
        mP = p;
    }

    public GFPElement(GFPElement other) {
        mValue = other.mValue;
        mP = other.mP;
    }

    public static GFPElement ZERO(BigInteger p) {
        return new GFPElement(BigInteger.ZERO, p);
    }

    public static GFPElement ONE(BigInteger p) {
        return new GFPElement(BigInteger.ONE, p);
    }

    public Object clone() {
        return new GFPElement(this);
    }

    public boolean isZero() {
        return mValue.equals(BigInteger.ZERO);
    }


    public boolean isOne() {
        return mValue.equals(BigInteger.ONE);
    }


    public boolean equals(Object other) {
        if (other == null || !(other instanceof GFPElement)) {
            return false;
        }

        GFPElement otherElement = (GFPElement) other;

        return mP.equals(otherElement.mP) && mValue.equals(otherElement.mValue);

    }


    public int hashCode() {
        return mP.hashCode() + mValue.hashCode();
    }


    public GFElement add(GFElement addend) throws DifferentFieldsException {
        GFPElement result = new GFPElement(this);
        result.addToThis(addend);
        return result;
    }


    private void addToThis(GFElement addend) throws DifferentFieldsException {
        if (!(addend instanceof GFPElement)) {
            throw new DifferentFieldsException();
        }
        if (!(mP.equals(((GFPElement) addend).mP))) {
            throw new DifferentFieldsException(
                    "Elements are of different fields.");
        }

        mValue = mValue.add(((GFPElement) addend).mValue).mod(mP);
    }


    public GFElement subtract(GFElement minuend)
            throws DifferentFieldsException {
        GFPElement result = new GFPElement(this);
        result.subtractFromThis(minuend);
        return result;
    }


    private void subtractFromThis(GFElement minuend)
            throws DifferentFieldsException {
        if (!(minuend instanceof GFPElement)) {
            throw new DifferentFieldsException();
        }
        mValue = mValue.subtract(((GFPElement) minuend).mValue).mod(mP);
    }


    public GFElement multiply(GFElement factor) throws DifferentFieldsException {
        GFPElement result = new GFPElement(this);
        result.multiplyThisBy(factor);
        return result;
    }


    public void multiplyThisBy(GFElement factor)
            throws DifferentFieldsException {
        if (!(factor instanceof GFPElement)) {
            throw new DifferentFieldsException();
        }
        if (!(mP.equals(((GFPElement) factor).mP))) {
            throw new DifferentFieldsException(
                    "Elements are of different fields.");
        }

        mValue = mValue.multiply(((GFPElement) factor).mValue).mod(mP);
    }


    public GFElement invert() throws ArithmeticException {
        if (isZero()) {
            throw new ArithmeticException();
        }

        return new GFPElement(mValue.modInverse(mP), mP);
    }

    public BigInteger toFlexiBigInt() {
        return mValue;
    }


    public String toString() {
        return mValue.toString(16);
    }

    public String toString(int radix) {
        return mValue.toString(radix);
    }

}
