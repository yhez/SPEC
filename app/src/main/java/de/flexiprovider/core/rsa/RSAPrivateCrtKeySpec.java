package de.flexiprovider.core.rsa;

import de.flexiprovider.common.math.FlexiBigInt;

public class RSAPrivateCrtKeySpec extends
        java.security.spec.RSAPrivateCrtKeySpec implements
        RSAPrivKeySpecInterface {

    public RSAPrivateCrtKeySpec(java.security.spec.RSAPrivateCrtKeySpec keySpec) {
        super(keySpec.getModulus(), keySpec.getPublicExponent(), keySpec
                .getPrivateExponent(), keySpec.getPrimeP(),
                keySpec.getPrimeQ(), keySpec.getPrimeExponentP(), keySpec
                .getPrimeExponentQ(), keySpec.getCrtCoefficient());
    }
    public RSAPrivateCrtKeySpec(FlexiBigInt n, FlexiBigInt e, FlexiBigInt d,
                                FlexiBigInt p, FlexiBigInt q, FlexiBigInt dP, FlexiBigInt dQ,
                                FlexiBigInt crtCoeff) {
        super(de.flexiprovider.my.BigInteger.get(n.bigInt), de.flexiprovider.my.BigInteger.get(e.bigInt), de.flexiprovider.my.BigInteger.get(d.bigInt), de.flexiprovider.my.BigInteger.get(p.bigInt), de.flexiprovider.my.BigInteger.get(q.bigInt), de.flexiprovider.my.BigInteger.get(dP.bigInt),
                de.flexiprovider.my.BigInteger.get(dQ.bigInt), de.flexiprovider.my.BigInteger.get(crtCoeff.bigInt));
    }

    public FlexiBigInt getN() {
        return new FlexiBigInt(getModulus());
    }
    public FlexiBigInt getE() {
        return new FlexiBigInt(getPublicExponent());
    }
    public FlexiBigInt getD() {
        return new FlexiBigInt(getPrivateExponent());
    }
    public FlexiBigInt getP() {
        return new FlexiBigInt(getPrimeP());
    }

    public FlexiBigInt getQ() {
        return new FlexiBigInt(getPrimeQ());
    }
    public FlexiBigInt getDp() {
        return new FlexiBigInt(getPrimeExponentP());
    }
    public FlexiBigInt getDq() {
        return new FlexiBigInt(new de.flexiprovider.my.BigInteger(getPrimeExponentQ()));
    }
    public FlexiBigInt getCRTCoeff() {
        return new FlexiBigInt(new de.flexiprovider.my.BigInteger(getCrtCoefficient()));
    }

}
