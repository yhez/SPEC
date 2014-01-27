package de.flexiprovider.core.rsa;

import de.flexiprovider.common.math.FlexiBigInt;
import de.flexiprovider.my.BigInteger;

public class RSAPrivateKeySpec extends java.security.spec.RSAPrivateKeySpec
        implements RSAPrivKeySpecInterface {


    public RSAPrivateKeySpec(java.security.spec.RSAPrivateKeySpec keySpec) {
        super(keySpec.getModulus(), keySpec.getPrivateExponent());
    }

    public RSAPrivateKeySpec(FlexiBigInt n, FlexiBigInt d) {
        super(BigInteger.get(n.bigInt), BigInteger.get(d.bigInt));
    }

    public FlexiBigInt getN() {
        return new FlexiBigInt(new BigInteger(getModulus()));
    }
    public FlexiBigInt getD() {
        return new FlexiBigInt(new BigInteger(getPrivateExponent()));
    }

}
