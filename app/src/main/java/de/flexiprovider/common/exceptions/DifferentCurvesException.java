package de.flexiprovider.common.exceptions;


public class DifferentCurvesException extends ECException {

    private static final String diagnostic = "Cannot combine different elliptic curves";

    public DifferentCurvesException() {
        super(diagnostic);
    }

    public DifferentCurvesException(String detail) {
        super(diagnostic + ":\n" + detail);
    }

}
