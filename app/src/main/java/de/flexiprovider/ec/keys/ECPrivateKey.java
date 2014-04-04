package de.flexiprovider.ec.keys;

import java.security.PrivateKey;

import de.flexiprovider.common.math.FlexiBigInt;
import de.flexiprovider.ec.parameters.CurveParams;


public class ECPrivateKey implements PrivateKey {

    // the private key s, 1 < s < r.
    private FlexiBigInt mS;

    private CurveParams mParams;

    protected ECPrivateKey(FlexiBigInt s, CurveParams params) {
        mS = s;
        mParams = params;
    }


    public ECPrivateKey(ECPrivateKeySpec keySpec) {
        this(keySpec.getS(), keySpec.getParams());
    }


    public FlexiBigInt getS() {
        return mS;
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
        return "s = " + mS.toString(16) + "\n" + ((Object)mParams).toString();
    }
    public CurveParams getParams() {
        return mParams;
    }

    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof ECPrivateKey)) {
            return false;
        }

        ECPrivateKey oKey = (ECPrivateKey) obj;
        boolean value = oKey.mS.equals(mS);
        value &= mParams.equals(oKey.mParams);

        return value;
    }

    public int hashCode() {
        return mS.hashCode() + mParams.getR().hashCode()
                + mParams.getQ().hashCode();
    }

}
