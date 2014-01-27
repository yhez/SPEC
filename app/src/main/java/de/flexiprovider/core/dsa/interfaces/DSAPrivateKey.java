package de.flexiprovider.core.dsa.interfaces;

import de.flexiprovider.api.keys.PrivateKey;
import de.flexiprovider.common.math.FlexiBigInt;
import de.flexiprovider.my.BigInteger;


public abstract class DSAPrivateKey extends PrivateKey implements DSAKey,
        java.security.interfaces.DSAPrivateKey {


    public final java.math.BigInteger getX() {
        return BigInteger.get(getValueX().bigInt);
    }

    public final java.security.interfaces.DSAParams getParams() {
        return getParameters();
    }

    public abstract FlexiBigInt getValueX();

}
