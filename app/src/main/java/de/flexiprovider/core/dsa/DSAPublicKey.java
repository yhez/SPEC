package de.flexiprovider.core.dsa;

import codec.asn1.ASN1Integer;
import codec.asn1.ASN1ObjectIdentifier;
import codec.asn1.ASN1Type;
import de.flexiprovider.api.exceptions.InvalidParameterSpecException;
import de.flexiprovider.common.math.FlexiBigInt;
import de.flexiprovider.common.util.ASN1Tools;
import de.flexiprovider.core.dsa.interfaces.DSAKey;
import de.flexiprovider.core.dsa.interfaces.DSAParams;


public class DSAPublicKey extends
        de.flexiprovider.core.dsa.interfaces.DSAPublicKey implements DSAKey {


    private FlexiBigInt y;


    private DSAParams params;


    public String getAlgorithm() {
        return "DSA";
    }

    public DSAParams getParameters() {
        return params;
    }


    public FlexiBigInt getValueY() {
        return y;
    }


    public String toString() {

        return "public y:  0x" + y.toString(16) + "\n" + "p:         0x"
                + params.getPrimeP().toString(16) + "\n" + "q:         0x"
                + params.getPrimeQ().toString(16) + "\n" + "g:         0x"
                + params.getBaseG().toString(16) + "\n";
    }

    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof DSAPublicKey)) {
            return false;
        }

        DSAPublicKey otherKey = (DSAPublicKey) obj;

        boolean value = y.equals(otherKey.y);
        value &= params.getBaseG().equals(otherKey.params.getBaseG());
        value &= params.getPrimeP().equals(otherKey.params.getPrimeP());
        value &= params.getPrimeQ().equals(otherKey.params.getPrimeQ());

        return value;
    }

    public int hashCode() {
        return y.hashCode() + params.getBaseG().hashCode()
                + params.getPrimeP().hashCode() + params.getPrimeQ().hashCode();
    }

    public static final String OID = "1.2.840.10040.4.1";

    protected ASN1ObjectIdentifier getOID() {
        return new ASN1ObjectIdentifier(OID);
    }


    protected ASN1Type getAlgParams() {
        DSAParameters dsaParams = new DSAParameters();
        DSAParameterSpec dsaParamSpec = new DSAParameterSpec(
                params.getPrimeP(), params.getPrimeQ(), params.getBaseG());
        try {
            dsaParams.init(dsaParamSpec);
        } catch (InvalidParameterSpecException e) {
            // the parameters are correct and must be accepted
            throw new RuntimeException("internal error");
        }
        return dsaParams.getASN1Parameters();
    }

    protected byte[] getKeyData() {
        ASN1Integer keyData = new ASN1Integer(y.toByteArray());
        return ASN1Tools.derEncode(keyData);
    }

}
