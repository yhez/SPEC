package de.flexiprovider.core.dsa;

import de.flexiprovider.api.parameters.AlgorithmParameterSpec;
import de.flexiprovider.common.math.FlexiBigInt;
import de.flexiprovider.core.dsa.interfaces.DSAParams;
import de.flexiprovider.my.BigInteger;



public final class DSAParameterSpec extends java.security.spec.DSAParameterSpec
        implements DSAParams, AlgorithmParameterSpec {




    public DSAParameterSpec(FlexiBigInt p, FlexiBigInt q, FlexiBigInt g) {
        super(BigInteger.get(p.bigInt), BigInteger.get(q.bigInt), BigInteger.get(g.bigInt));
    }


    public FlexiBigInt getPrimeP() {
        return new FlexiBigInt(new BigInteger(getP()));
    }


    public FlexiBigInt getPrimeQ() {
        return new FlexiBigInt(new BigInteger(getQ()));
    }


    public FlexiBigInt getBaseG() {
        return new FlexiBigInt(new BigInteger(getG()));
    }

}
