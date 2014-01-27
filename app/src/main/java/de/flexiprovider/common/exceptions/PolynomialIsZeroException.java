package de.flexiprovider.common.exceptions;

public class PolynomialIsZeroException extends GFException {

    private static final String diagnostic = "This element is Zero!";

    public PolynomialIsZeroException() {
        super(diagnostic);
    }

}
