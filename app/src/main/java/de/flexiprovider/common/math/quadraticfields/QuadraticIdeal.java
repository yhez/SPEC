package de.flexiprovider.common.math.quadraticfields;

import de.flexiprovider.common.math.FlexiBigInt;

public class QuadraticIdeal {

    protected FlexiBigInt a;

    protected FlexiBigInt b;

    public QuadraticIdeal(FlexiBigInt a, FlexiBigInt b) {
        this.a = a;
        this.b = b;
    }

    public QuadraticIdeal(int a, int b) {
        this.a = FlexiBigInt.valueOf(a);
        this.b = FlexiBigInt.valueOf(b);
    }

    public boolean equals(Object other) {
        if (!(other instanceof QuadraticIdeal)) {
            return false;
        }
        QuadraticIdeal otherIdeal = (QuadraticIdeal) other;

        return a.equals(otherIdeal.a) && b.equals(otherIdeal.b);
    }

    public String toString() {
        return "(" + a + ", " + b + ")";
    }

    public FlexiBigInt getA() {
        return a;
    }

    public FlexiBigInt getB() {
        return b;
    }

    public int hashCode() {
        return a.hashCode() + b.hashCode();
    }

}
