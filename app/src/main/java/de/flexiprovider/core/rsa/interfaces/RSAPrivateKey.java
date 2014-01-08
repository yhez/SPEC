package de.flexiprovider.core.rsa.interfaces;

import my.BigInteger;

import de.flexiprovider.api.keys.PrivateKey;
import de.flexiprovider.common.math.FlexiBigInt;

public abstract class RSAPrivateKey extends PrivateKey implements RSAKey,
	java.security.interfaces.RSAPrivateKey {

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
     * @return the private exponent d
     */
    public final java.math.BigInteger getPrivateExponent() {
	return BigInteger.get(getD().bigInt);
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
     * @return the private exponent d
     */
    public abstract FlexiBigInt getD();

}
