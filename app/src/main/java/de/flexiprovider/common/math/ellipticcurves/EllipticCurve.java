package de.flexiprovider.common.math.ellipticcurves;

import de.flexiprovider.common.math.FlexiBigInt;
import de.flexiprovider.common.math.finitefields.GFElement;


public class EllipticCurve {

    protected FlexiBigInt mQ;
    protected GFElement mA;
    protected GFElement mB;

    public EllipticCurve(GFElement a, GFElement b, FlexiBigInt q) {
        // TODO check whether the parameters match: are a and b are defined over
        // the same field, does parameter q match?
        mQ = q;
        mA = a;
        mB = b;
    }

    public FlexiBigInt getQ() {
        return mQ;
    }

    public GFElement getA() {
        return (GFElement) mA.clone();
    }

    public GFElement getB() {
        return (GFElement) mB.clone();
    }

    public boolean equals(Object other) {
        if (other == null || !(other instanceof EllipticCurve)) {
            return false;
        }

        EllipticCurve otherCurve = (EllipticCurve) other;

        return mQ.equals(otherCurve.mQ) && mA.equals(otherCurve.mA)
                && mB.equals(otherCurve.mB);
    }

    public int hashCode() {
        return mQ.hashCode() + mA.hashCode() + mB.hashCode();
    }

    public final String toString() {
        return "y^2 = x^3 + ax + b, where\n" + "a = " + mA.toString(16)
                + ",\nb = " + mB.toString(16) + "\n field order = "
                + mQ.toString(16);
    }
}
