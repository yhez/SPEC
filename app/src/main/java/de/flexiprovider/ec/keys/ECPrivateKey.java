package de.flexiprovider.ec.keys;

import java.math.BigInteger;
import java.security.PrivateKey;

import de.flexiprovider.ec.parameters.CurveParamsGFP;


public class ECPrivateKey implements PrivateKey {

    // the private key s, 1 < s < r.
    private BigInteger mS;

    private CurveParamsGFP mParams;

    public ECPrivateKey(BigInteger s, CurveParamsGFP params) {
        mS = s;
        mParams = params;
    }

    public BigInteger getS() {
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
