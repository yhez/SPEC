package de.flexiprovider.ec.keys;

import java.security.InvalidKeyException;
import java.security.PublicKey;

import de.flexiprovider.common.math.ellipticcurves.Point;
import de.flexiprovider.ec.parameters.CurveParamsGFP;

public class ECPublicKey implements PublicKey {

    // the public key value w := s * G, 1 < s < r
    private Point mW;

    // the EC domain parameters
    private CurveParamsGFP mParams;

    public ECPublicKey(Point w, CurveParamsGFP params) {
        mW = w;
        mParams = params;
    }

    public Point getW() throws InvalidKeyException {
        if (mW == null) {
            throw new InvalidKeyException("invalid");
        }
        return mW;
    }

    public CurveParamsGFP getParams() {
        return mParams;
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
}
