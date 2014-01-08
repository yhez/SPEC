package de.flexiprovider.core.rsa;

import java.math.BigInteger;

import de.flexiprovider.common.math.FlexiBigInt;

public class RSAPrivateCrtKeySpec extends
        java.security.spec.RSAPrivateCrtKeySpec implements
        RSAPrivKeySpecInterface {

    // ****************************************************
    // JCA adapter methods
    // ****************************************************

    public RSAPrivateCrtKeySpec(BigInteger n, BigInteger e, BigInteger d,
                                BigInteger p, BigInteger q, BigInteger dP, BigInteger dQ,
                                BigInteger crtCoeff) {
        super(n, e, d, p, q, dP, dQ, crtCoeff);
    }

    /**
     * Create a new RSAPrivateCrtKeySpec out of the given
     * {@link java.security.spec.RSAPrivateKeySpec}.
     *
     * @param keySpec the {@link java.security.spec.RSAPrivateKeySpec}
     */
    public RSAPrivateCrtKeySpec(java.security.spec.RSAPrivateCrtKeySpec keySpec) {
        super(keySpec.getModulus(), keySpec.getPublicExponent(), keySpec
                .getPrivateExponent(), keySpec.getPrimeP(),
                keySpec.getPrimeQ(), keySpec.getPrimeExponentP(), keySpec
                .getPrimeExponentQ(), keySpec.getCrtCoefficient());
    }

    // ****************************************************
    // FlexiAPI methods
    // ****************************************************

    public RSAPrivateCrtKeySpec(FlexiBigInt n, FlexiBigInt e, FlexiBigInt d,
                                FlexiBigInt p, FlexiBigInt q, FlexiBigInt dP, FlexiBigInt dQ,
                                FlexiBigInt crtCoeff) {
        super(my.BigInteger.get(n.bigInt), my.BigInteger.get(e.bigInt), my.BigInteger.get(d.bigInt), my.BigInteger.get(p.bigInt), my.BigInteger.get(q.bigInt), my.BigInteger.get(dP.bigInt),
                my.BigInteger.get(dQ.bigInt), my.BigInteger.get(crtCoeff.bigInt));
    }

    /**
     * @return the modulus n
     */
    public FlexiBigInt getN() {
        return new FlexiBigInt(getModulus());
    }

    /**
     * @return the public exponent e
     */
    public FlexiBigInt getE() {
        return new FlexiBigInt(getPublicExponent());
    }

    /**
     * @return the private exponent d
     */
    public FlexiBigInt getD() {
        return new FlexiBigInt(getPrivateExponent());
    }

    /**
     * @return the prime p
     */
    public FlexiBigInt getP() {
        return new FlexiBigInt(getPrimeP());
    }

    /**
     * @return the prime q
     */
    public FlexiBigInt getQ() {
        return new FlexiBigInt(getPrimeQ());
    }

    /**
     * @return the private exponent d mod (p-1)
     */
    public FlexiBigInt getDp() {
        return new FlexiBigInt(getPrimeExponentP());
    }

    /**
     * @return the private exponent d mod (q-1)
     */
    public FlexiBigInt getDq() {
        return new FlexiBigInt(new my.BigInteger(getPrimeExponentQ()));
    }

    /**
     * @return the CRT coefficient
     */
    public FlexiBigInt getCRTCoeff() {
        return new FlexiBigInt(new my.BigInteger(getCrtCoefficient()));
    }

}
