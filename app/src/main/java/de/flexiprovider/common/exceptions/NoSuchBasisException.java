package de.flexiprovider.common.exceptions;


public class NoSuchBasisException extends GFException {

    private static final String diagnostic = "This extension field does not have a normal basis";

    public NoSuchBasisException(String detail) {
        super(diagnostic + ":\n" + detail);
    }

}
