package de.flexiprovider.core.rsa;

import de.flexiprovider.api.keys.KeySpec;
import de.flexiprovider.common.math.FlexiBigInt;

interface RSAPrivKeySpecInterface extends KeySpec {

    FlexiBigInt getN();

    FlexiBigInt getD();

}
