package de.flexiprovider.common.exceptions;

public class InvalidPointException extends ECException {


    public InvalidPointException() {
        super("InvalidPointException");
    }
    public InvalidPointException(String msg) {
        super(msg);
    }


}
