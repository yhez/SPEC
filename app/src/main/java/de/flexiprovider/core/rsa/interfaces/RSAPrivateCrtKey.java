package de.flexiprovider.core.rsa.interfaces;

import java.math.BigInteger;

import de.flexiprovider.common.math.FlexiBigInt;

public abstract class RSAPrivateCrtKey extends RSAPrivateKey implements
        java.security.interfaces.RSAPrivateCrtKey {

    public final BigInteger getPublicExponent() {
        return de.flexiprovider.my.BigInteger.get(getE().bigInt);
    }

    public final BigInteger getPrimeP() {
        return de.flexiprovider.my.BigInteger.get(getP().bigInt);
    }

    public final BigInteger getPrimeQ() {
        return de.flexiprovider.my.BigInteger.get(getQ().bigInt);
    }

    public final BigInteger getPrimeExponentP() {
        return de.flexiprovider.my.BigInteger.get(getDp().bigInt);
    }

    public final BigInteger getPrimeExponentQ() {
        return de.flexiprovider.my.BigInteger.get(getDq().bigInt);
    }

    public final BigInteger getCrtCoefficient() {
        return de.flexiprovider.my.BigInteger.get(getCRTCoeff().bigInt);
    }

    public abstract FlexiBigInt getE();


    public abstract FlexiBigInt getP();


    public abstract FlexiBigInt getQ();


    public abstract FlexiBigInt getDp();


    public abstract FlexiBigInt getDq();


    public abstract FlexiBigInt getCRTCoeff();

}
