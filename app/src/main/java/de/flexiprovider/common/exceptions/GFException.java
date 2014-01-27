package de.flexiprovider.common.exceptions;


public class GFException extends RuntimeException {

    private static final String DIAGNOSTIC = "A field-specific exception was thrown";

    public GFException() {
        super(DIAGNOSTIC);
    }


    public GFException(String details) {
        super(DIAGNOSTIC + ": " + details);
    }

}
