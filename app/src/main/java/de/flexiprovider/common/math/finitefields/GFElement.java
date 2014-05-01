package de.flexiprovider.common.math.finitefields;

import java.math.BigInteger;

import de.flexiprovider.common.exceptions.DifferentFieldsException;

public interface GFElement {


    Object clone();


    boolean equals(Object other);


    int hashCode();


    boolean isZero();


    boolean isOne();


    GFElement add(GFElement addend) throws DifferentFieldsException;


    GFElement subtract(GFElement minuend) throws DifferentFieldsException;


    GFElement multiply(GFElement factor) throws DifferentFieldsException;


    void multiplyThisBy(GFElement factor) throws DifferentFieldsException;


    BigInteger toFlexiBigInt();

    String toString();


    String toString(int radix);

}
