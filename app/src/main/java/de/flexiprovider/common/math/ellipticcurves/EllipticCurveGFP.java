package de.flexiprovider.common.math.ellipticcurves;

import de.flexiprovider.common.math.FlexiBigInt;
import de.flexiprovider.common.math.finitefields.GFPElement;


public class EllipticCurveGFP extends EllipticCurve {




    public EllipticCurveGFP(GFPElement a, GFPElement b, FlexiBigInt p) {
        super(a, b, p);
    }

    public final String toString() {
        return "y^2 = x^3 + ax + b, where\n" + "a = " + mA.toString(16)
                + ",\nb = " + mB.toString(16) + "\n field order = "
                + mQ.toString(16);
    }

}
