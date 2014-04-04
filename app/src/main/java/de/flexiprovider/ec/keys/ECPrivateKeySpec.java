package de.flexiprovider.ec.keys;

import de.flexiprovider.common.math.FlexiBigInt;
import de.flexiprovider.ec.parameters.CurveParams;


public final class ECPrivateKeySpec implements java.security.spec.KeySpec {


    private CurveParams mParams;


    private FlexiBigInt mS;


    public ECPrivateKeySpec(FlexiBigInt s, CurveParams params) {
        mParams = params;
        mS = s;
    }


    public FlexiBigInt getS() {
        return mS;
    }


    public CurveParams getParams() {
        return mParams;
    }

}
