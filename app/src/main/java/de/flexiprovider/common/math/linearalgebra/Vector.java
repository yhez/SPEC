package de.flexiprovider.common.math.linearalgebra;


public abstract class Vector {


    protected int length;


    public final int getLength() {
        return length;
    }


    public abstract byte[] getEncoded();


    public abstract boolean isZero();
    public abstract Vector add(Vector addend);


    public abstract Vector multiply(Permutation p);


    public abstract boolean equals(Object other);


    public abstract int hashCode();


    public abstract String toString();

}
