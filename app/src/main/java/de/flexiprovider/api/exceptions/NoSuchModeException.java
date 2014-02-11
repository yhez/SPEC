package de.flexiprovider.api.exceptions;

import java.security.NoSuchAlgorithmException;

/**
 * Exception used to indicate that a mode of operation cannot be found.
 * 
 * @author Martin D�ring
 */
public class NoSuchModeException extends NoSuchAlgorithmException {

    /**
     * Constructor.
     * 
     * @param msg
     *                the error message
     */
    public NoSuchModeException(String msg) {
	super(msg);
    }

}
