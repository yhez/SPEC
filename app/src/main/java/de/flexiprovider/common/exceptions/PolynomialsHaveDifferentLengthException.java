
package de.flexiprovider.common.exceptions;

public class PolynomialsHaveDifferentLengthException extends GFException {

    private static final String diagnostic = "The two Bitstrings have a different length and thus cannot be"
            + " vector-multiplied.";


    public PolynomialsHaveDifferentLengthException() {
        super(diagnostic);
    }

}
