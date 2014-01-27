package de.flexiprovider.common.exceptions;

import de.flexiprovider.common.math.finitefields.GF2nElement;


public class NoSolutionException extends GFException {

    private static final String diagnostic = "The equation z^2 + z = b has no solution z for b";


    public NoSolutionException() {
        super(diagnostic);
    }

    public NoSolutionException(GF2nElement b) {
        super(diagnostic + b.toString(16));
    }

}
