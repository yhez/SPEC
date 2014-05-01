package de.flexiprovider.common.exceptions;

import java.math.BigInteger;


public class NoQuadraticResidueException extends ECException {


    public NoQuadraticResidueException(BigInteger a, BigInteger p) {
        super("NoQuadraticResidueException:\na = " + a + " is not"
                + "a quadratic residue mod p = " + p + ".");
    }

}
