package de.flexiprovider.common.exceptions;

/**
 * This exception is thrown, when trying to construct an object of type
 * <tt>GF2nONBField</tt> of an extension-grade, which does not have an optimal
 * normal base.
 *
 * @author Birgit Henhapl
 * @see de.flexiprovider.common.math.finitefields.GF2nONBField
 */
public class NoSuchBasisException extends GFException {

    private static final String diagnostic = "This extension field does not have a normal basis";

    /**
     * Calls the parent-constructor with the message "This extension field does
     * not have a normal basis: <em>detail</em>"
     *
     * @param detail specifies the details of this exception
     */
    public NoSuchBasisException(String detail) {
        super(diagnostic + ":\n" + detail);
    }

}
