package de.flexiprovider.common.exceptions;

public class BitDoesNotExistException extends GFException {

    private static final String DIAGNOSTIC = "The given Bit does not exist and thus cannot be modified";

    public BitDoesNotExistException() {
        super(DIAGNOSTIC);
    }
    public BitDoesNotExistException(int pos) {
        super(DIAGNOSTIC + ": " + pos);
    }

}
