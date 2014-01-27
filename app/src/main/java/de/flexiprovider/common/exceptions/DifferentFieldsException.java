package de.flexiprovider.common.exceptions;


public class DifferentFieldsException extends GFException {

    private static final String diagnostic = "Cannot combine elements from different fields";

    public DifferentFieldsException() {
        super(diagnostic);
    }

    public DifferentFieldsException(String details) {
        super(diagnostic + ": " + details);
    }

}
