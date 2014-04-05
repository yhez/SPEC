package de.flexiprovider.ec.keys;

import java.security.InvalidKeyException;
import java.security.PublicKey;

import de.flexiprovider.common.math.ellipticcurves.Point;
import de.flexiprovider.ec.parameters.CurveParams;

public class ECPublicKey implements PublicKey {

    // the public key value w := s * G, 1 < s < r
    private Point mW;

    // the EC domain parameters
    private CurveParams mParams;

    public ECPublicKey(Point w, CurveParams params) {
        mW = w;
        mParams = params;
    }

    public Point getW() throws InvalidKeyException {
        if (mW == null) {
            throw new InvalidKeyException("invalid");
        }
        return mW;
    }

    public CurveParams getParams() {
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
