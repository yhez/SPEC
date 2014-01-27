package de.flexiprovider.core.dsa.interfaces;

import de.flexiprovider.common.math.FlexiBigInt;


public interface DSAParams extends java.security.interfaces.DSAParams {


    FlexiBigInt getPrimeP();

    FlexiBigInt getPrimeQ();

    FlexiBigInt getBaseG();

}
