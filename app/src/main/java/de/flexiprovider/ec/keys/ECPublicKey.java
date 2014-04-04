package de.flexiprovider.ec.keys;

import java.security.InvalidKeyException;
import java.security.PublicKey;

import de.flexiprovider.common.math.ellipticcurves.Point;
import de.flexiprovider.common.util.ByteUtils;
import de.flexiprovider.ec.parameters.CurveParams;

public class ECPublicKey implements PublicKey {

    // the public key value w := s * G, 1 < s < r
    private Point mW;

    // the encoded public key w in case no EC domain parameters were
    // available at construction time
    private byte[] mEncodedW;

    // the EC domain parameters
    private CurveParams mParams;

    public ECPublicKey(Point w, CurveParams params) {
        mW = w;
        mParams = params;
    }

    public ECPublicKey(byte[] encodedW) {
        mEncodedW = encodedW;
    }

    public Point getW() throws InvalidKeyException {
        if (mW == null) {
            throw new InvalidKeyException(
                    "No ecdomain parameters defined for the public point");
        }
        return mW;
    }

    public CurveParams getParams() {
        return mParams;
    }

    public String getAlgorithm() {
        return "EC";
    }

    @Override
    public String getFormat() {
        return null;
    }

    @Override
    public byte[] getEncoded() {
        return new byte[0];
    }


    public String toString() {
        StringBuilder result = new StringBuilder();
        if (mEncodedW != null) {
            result.append("W= (encoded)\n");
            result.append(ByteUtils.toHexString(mEncodedW, "0x", ""));
        } else {
            result.append("W =\n").append(mW.toString());
            if (mParams != null) {
                result.append("\n").append(((Object)mParams).toString());
            }
        }
        return result.toString();
    }


    public boolean equals(Object other) {
        if (other == null || !(other instanceof ECPublicKey)) {
            return false;
        }

        ECPublicKey otherKey = (ECPublicKey) other;

        return ByteUtils.equals(getEncoded(), otherKey.getEncoded());
    }


    public int hashCode() {
        return mW.hashCode() + mParams.getR().hashCode()
                + mParams.getQ().hashCode();
    }


}
