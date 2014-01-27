package de.flexiprovider.common.exceptions;


public class PolynomialIsNotIrreducibleException extends GFException {

    private static final String diagnostic = "The given fieldpolynomial is not irreducible.";


    public PolynomialIsNotIrreducibleException() {
        super(diagnostic);
    }

}
