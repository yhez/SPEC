package de.flexiprovider.common.exceptions;

import de.flexiprovider.common.math.FlexiBigInt;


public class NoQuadraticResidueException extends ECException {


    public NoQuadraticResidueException(FlexiBigInt a, FlexiBigInt p) {
        super("NoQuadraticResidueException:\na = " + a + " is not"
                + "a quadratic residue mod p = " + p + ".");
    }

}
