package de.flexiprovider.common.math.ellipticcurves;

import de.flexiprovider.common.math.FlexiBigInt;
import de.flexiprovider.common.math.finitefields.GF2nElement;


public class EllipticCurveGF2n extends EllipticCurve {




    public EllipticCurveGF2n(GF2nElement a, GF2nElement b, int deg) {
        super(a, b, (FlexiBigInt.ONE).shiftLeft(deg));
    }

    public String toString() {
        return "y<sup>2</sup> + xy = x<sup>3</sup> +ax<sup>2</sup> + b, where\n"
                + "a = "
                + mA.toString(16)
                + ",\nb = "
                + mB.toString(16)
                + "\n field order = " + mQ.toString(16);
    }

}
