package de.flexiprovider.api.exceptions;

/**
 * Exception used to indicate registration errors (used by the
 * {@link de.flexiprovider.api.Registry Registry} class). Since this exception
 * is thrown during static initialization, it extends {@link RuntimeException}.
 *
 * @author Martin Dring
 */
public class RegistrationException extends RuntimeException {

    public RegistrationException(String s) {
        super(s);
    }

}
