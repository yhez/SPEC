package de.flexiprovider.core.rsa.interfaces;

import de.flexiprovider.api.keys.PublicKey;
import de.flexiprovider.common.math.FlexiBigInt;
import de.flexiprovider.my.BigInteger;

public abstract class RSAPublicKey extends PublicKey implements RSAKey,
        java.security.interfaces.RSAPublicKey {

    // ****************************************************
    // JCA adapter methods
    // ****************************************************

    /**
     * @return the modulus n
     */
    public final java.math.BigInteger getModulus() {
        return BigInteger.get(getN().bigInt);
    }

    /**
     * @return the public exponent e
     */
    public final java.math.BigInteger getPublicExponent() {
        return BigInteger.get(getE().bigInt);
    }

    // ****************************************************
    // FlexiAPI methods
    // ****************************************************

    /**
     * @return name of the algorithm - "RSA"
     */
    public final String getAlgorithm() {
        return "RSA";
    }

    /**
     * @return the public exponent e
     */
    public abstract FlexiBigInt getE();

}
