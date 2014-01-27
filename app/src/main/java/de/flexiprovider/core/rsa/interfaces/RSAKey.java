package de.flexiprovider.core.rsa.interfaces;

import de.flexiprovider.common.math.FlexiBigInt;

public interface RSAKey extends java.security.interfaces.RSAKey {

    FlexiBigInt getN();

}
