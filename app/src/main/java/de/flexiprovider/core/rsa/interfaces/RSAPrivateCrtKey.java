package de.flexiprovider.core.rsa.interfaces;

import java.math.BigInteger;

import de.flexiprovider.common.math.FlexiBigInt;

public abstract class RSAPrivateCrtKey extends RSAPrivateKey implements
        java.security.interfaces.RSAPrivateCrtKey {

    // ****************************************************
    // JCA adapter methods
    // ****************************************************

    /**
     * @return the public exponent e
     */
    public final BigInteger getPublicExponent() {
        return de.flexiprovider.my.BigInteger.get(getE().bigInt);
    }

    /**
     * @return the prime p
     */
    public final BigInteger getPrimeP() {
        return de.flexiprovider.my.BigInteger.get(getP().bigInt);
    }

    /**
     * @return the prime q
     */
    public final BigInteger getPrimeQ() {
        return de.flexiprovider.my.BigInteger.get(getQ().bigInt);
    }

    /**
     * @return the private exponent d mod (p-1)
     */
    public final BigInteger getPrimeExponentP() {
        return de.flexiprovider.my.BigInteger.get(getDp().bigInt);
    }

    /**
     * @return the private exponent d mod (q-1)
     */
    public final BigInteger getPrimeExponentQ() {
        return de.flexiprovider.my.BigInteger.get(getDq().bigInt);
    }

    /**
     * @return the CRT coefficient
     */
    public final BigInteger getCrtCoefficient() {
        return de.flexiprovider.my.BigInteger.get(getCRTCoeff().bigInt);
    }

    // ****************************************************
    // FlexiAPI methods
    // ****************************************************

    /**
     * @return the public exponent e
     */
    public abstract FlexiBigInt getE();

    /**
     * @return the prime p
     */
    public abstract FlexiBigInt getP();

    /**
     * @return the prime q
     */
    public abstract FlexiBigInt getQ();

    /**
     * @return the private exponent d mod (p-1)
     */
    public abstract FlexiBigInt getDp();

    /**
     * @return the private exponent d mod (q-1)
     */
    public abstract FlexiBigInt getDq();

    /**
     * @return the CRT coefficient
     */
    public abstract FlexiBigInt getCRTCoeff();

}
