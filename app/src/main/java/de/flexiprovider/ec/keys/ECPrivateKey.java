package de.flexiprovider.ec.keys;

import java.security.PrivateKey;

import de.flexiprovider.common.math.FlexiBigInt;
import de.flexiprovider.ec.parameters.CurveParams;


public class ECPrivateKey implements PrivateKey {

    // the private key s, 1 < s < r.
    private FlexiBigInt mS;

    private CurveParams mParams;

    public ECPrivateKey(FlexiBigInt s, CurveParams params) {
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

    public CurveParams getParams() {
        return mParams;
    }
}
