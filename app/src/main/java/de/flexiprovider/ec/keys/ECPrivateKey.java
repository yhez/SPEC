package de.flexiprovider.ec.keys;

import codec.asn1.ASN1Integer;
import codec.asn1.ASN1ObjectIdentifier;
import codec.asn1.ASN1OctetString;
import codec.asn1.ASN1Sequence;
import codec.asn1.ASN1Type;
import de.flexiprovider.api.exceptions.InvalidParameterSpecException;
import de.flexiprovider.api.keys.PrivateKey;
import de.flexiprovider.common.math.FlexiBigInt;
import de.flexiprovider.common.util.ASN1Tools;
import de.flexiprovider.ec.parameters.CurveParams;
import de.flexiprovider.ec.parameters.ECParameters;


public class ECPrivateKey extends PrivateKey {

    // the private key s, 1 < s < r.
    private FlexiBigInt mS;

    private CurveParams mParams;

    private static class ECASN1PrivateKey extends ASN1Sequence {

        private ASN1Integer version;

        private ASN1OctetString privKey;

        public ECASN1PrivateKey(int version, ASN1OctetString privKey) {
            super(2);
            this.version = new ASN1Integer(version);
            this.privKey = privKey;
            add(this.version);
            add(privKey);
        }

    }
    protected ECPrivateKey(FlexiBigInt s, CurveParams params) {
        mS = s;
        mParams = params;
    }


    protected ECPrivateKey(ECPrivateKeySpec keySpec) {
        this(keySpec.getS(), keySpec.getParams());
    }


    public FlexiBigInt getS() {
        return mS;
    }

    public String getAlgorithm() {
        return "EC";
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

    /**
     * @return the OID to encode in the SubjectPublicKeyInfo structure
     */
    protected ASN1ObjectIdentifier getOID() {
        return new ASN1ObjectIdentifier(ECKeyFactory.OID);
    }

    protected ASN1Type getAlgParams() {
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
        byte[] keyBytes = mS.toByteArray();
        ECASN1PrivateKey keyData = new ECASN1PrivateKey(1, new ASN1OctetString(
                keyBytes));
        return ASN1Tools.derEncode(keyData);
    }
}
