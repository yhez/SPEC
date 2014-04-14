package de.flexiprovider.ec.keys;

import java.security.PrivateKey;

import de.flexiprovider.common.math.FlexiBigInt;
import de.flexiprovider.ec.parameters.CurveParamsGFP;


public class ECPrivateKey implements PrivateKey {

    // the private key s, 1 < s < r.
    private FlexiBigInt mS;

    private CurveParamsGFP mParams;

    public ECPrivateKey(FlexiBigInt s, CurveParamsGFP params) {
        mS = s;
        mParams = params;
    }

    public FlexiBigInt getS() {
        return mS;
    }

    public String getAlgorithm() {
        return null;
    }

    @Override
    public String getFormat() {
        return null;
    }

    @Override
    public byte[] getEncoded() {
        return new byte[0];
    }

    public CurveParamsGFP getParams() {
        return mParams;
    }
}
