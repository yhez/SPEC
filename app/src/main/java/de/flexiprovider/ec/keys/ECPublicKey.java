package de.flexiprovider.ec.keys;

import java.security.InvalidKeyException;
import java.security.spec.InvalidParameterSpecException;

import codec.asn1.ASN1Null;
import codec.asn1.ASN1ObjectIdentifier;
import codec.asn1.ASN1Type;
import de.flexiprovider.api.keys.PublicKey;
import de.flexiprovider.common.math.ellipticcurves.Point;
import de.flexiprovider.common.util.ByteUtils;
import de.flexiprovider.ec.parameters.CurveParams;
import de.flexiprovider.ec.parameters.ECParameters;

public class ECPublicKey extends PublicKey {

    // the public key value w := s * G, 1 < s < r
    private Point mW;

    // the encoded public key w in case no EC domain parameters were
    // available at construction time
    private byte[] mEncodedW;

    // the EC domain parameters
    private CurveParams mParams;

    protected ECPublicKey(Point w, CurveParams params) {
        mW = w;
        mParams = params;
    }

    protected ECPublicKey(byte[] encodedW) {
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


    protected ASN1ObjectIdentifier getOID() {
        return new ASN1ObjectIdentifier(ECKeyFactory.OID);
    }


    protected ASN1Type getAlgParams() {
        if (mParams == null) {
            // If no parameters are specified, encode NULL.
            return new ASN1Null();
        }
        // get the OID of the parameters
        ASN1Type algParams = mParams.getOID();
        if (algParams == null) {
            // If no OID is given, the parameters are specified explicitly. In
            // this case, use the corresponding AlgorithmParameters class to get
            // the ASN.1 encoded parameters.
            ECParameters ecParams = new ECParameters();
            try {
                ecParams.init(mParams);
            } catch (InvalidParameterSpecException e) {
                // the parameters are correct and must be accepted
                throw new RuntimeException("internal error");
            }
            algParams = ecParams.getASN1Params();
        }
        return algParams;
    }


    protected byte[] getKeyData() {
        byte[] keyBytes;
        if (mEncodedW == null) {
            keyBytes = mW.EC2OSP(Point.ENCODING_TYPE_UNCOMPRESSED);
        } else {
            keyBytes = mEncodedW;
        }
        return keyBytes;
    }

}
