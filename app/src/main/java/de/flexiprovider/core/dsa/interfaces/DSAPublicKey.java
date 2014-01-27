package de.flexiprovider.core.dsa.interfaces;

import de.flexiprovider.api.keys.PublicKey;
import de.flexiprovider.common.math.FlexiBigInt;
import de.flexiprovider.my.BigInteger;


public abstract class DSAPublicKey extends PublicKey implements DSAKey,
        java.security.interfaces.DSAPublicKey {


    public final java.math.BigInteger getY() {
        return BigInteger.get(getValueY().bigInt);
    }

    public final java.security.interfaces.DSAParams getParams() {
        return getParameters();
    }

    public abstract FlexiBigInt getValueY();

}
