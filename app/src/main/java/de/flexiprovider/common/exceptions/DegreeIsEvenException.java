package de.flexiprovider.common.exceptions;


public class DegreeIsEvenException extends GFException {

    private static final String diagnostic = "The degree of the used field is even. Cannot compute halftrace.";

    public DegreeIsEvenException() {
        super(diagnostic);
    }

}
