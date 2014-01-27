package de.flexiprovider.ec.keys;

import de.flexiprovider.api.keys.KeySpec;
import de.flexiprovider.common.math.FlexiBigInt;
import de.flexiprovider.ec.parameters.CurveParams;


public final class ECPrivateKeySpec implements KeySpec {


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
