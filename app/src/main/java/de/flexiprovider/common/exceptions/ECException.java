package de.flexiprovider.common.exceptions;


public class ECException extends RuntimeException {

    private static final String diagnostic = "An ec-specific exception was thrown";

    public ECException(String cause) {
        super(diagnostic + ": " + cause);
    }

}
