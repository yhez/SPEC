package de.flexiprovider.common.math.finitefields;

import de.flexiprovider.common.exceptions.DifferentFieldsException;
import de.flexiprovider.common.math.FlexiBigInt;

public interface GFElement {


    Object clone();




    boolean equals(Object other);


    int hashCode();


    boolean isZero();


    boolean isOne();




    GFElement add(GFElement addend) throws DifferentFieldsException;


    void addToThis(GFElement addend) throws DifferentFieldsException;


    GFElement subtract(GFElement minuend) throws DifferentFieldsException;


    GFElement multiply(GFElement factor) throws DifferentFieldsException;


    void multiplyThisBy(GFElement factor) throws DifferentFieldsException;


    GFElement invert() throws ArithmeticException;




    FlexiBigInt toFlexiBigInt();


    byte[] toByteArray();


    String toString();


    String toString(int radix);

}
