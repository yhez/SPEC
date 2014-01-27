package de.flexiprovider.core.rsa;

import de.flexiprovider.api.keys.KeySpec;
import de.flexiprovider.common.math.FlexiBigInt;
import de.flexiprovider.my.BigInteger;

public final class RSAPublicKeySpec extends java.security.spec.RSAPublicKeySpec
        implements KeySpec {

    public RSAPublicKeySpec(java.security.spec.RSAPublicKeySpec keySpec) {
        super(keySpec.getModulus(), keySpec.getPublicExponent());
    }

    public RSAPublicKeySpec(FlexiBigInt n, FlexiBigInt e) {
        super(BigInteger.get(n.bigInt), BigInteger.get(e.bigInt));
    }

    public FlexiBigInt getN() {
        return new FlexiBigInt(new BigInteger(getModulus()));
    }
    public FlexiBigInt getE() {
        return new FlexiBigInt(new BigInteger(getPublicExponent()));
    }

}
