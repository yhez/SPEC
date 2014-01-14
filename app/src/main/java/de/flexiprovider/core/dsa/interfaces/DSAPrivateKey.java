package de.flexiprovider.core.dsa.interfaces;

import de.flexiprovider.api.keys.PrivateKey;
import de.flexiprovider.common.math.FlexiBigInt;
import de.flexiprovider.my.BigInteger;

/**
 * The interface to a DSA private key. DSA (Digital Signature Algorithm) is
 * defined in NIST's FIPS-186.
 *
 * @see java.security.Signature
 * @see DSAKey
 * @see DSAPublicKey
 */
public abstract class DSAPrivateKey extends PrivateKey implements DSAKey,
        java.security.interfaces.DSAPrivateKey {

    // ****************************************************
    // JCA adapter methods
    // ****************************************************

    /**
     * @return the value <tt>x</tt> of the private key
     */
    public final java.math.BigInteger getX() {
        return BigInteger.get(getValueX().bigInt);
    }

    /**
     * @return the DSA parameters
     */
    public final java.security.interfaces.DSAParams getParams() {
        return getParameters();
    }

    // ****************************************************
    // FlexiAPI methods
    // ****************************************************

    /**
     * @return the value <tt>x</tt> of the private key
     */
    public abstract FlexiBigInt getValueX();

}
