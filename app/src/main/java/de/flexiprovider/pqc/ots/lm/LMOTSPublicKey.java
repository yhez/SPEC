package de.flexiprovider.pqc.ots.lm;

import codec.asn1.ASN1Null;
import codec.asn1.ASN1ObjectIdentifier;
import codec.asn1.ASN1OctetString;
import codec.asn1.ASN1Sequence;
import codec.asn1.ASN1Type;
import de.flexiprovider.api.keys.PublicKey;
import de.flexiprovider.common.math.polynomials.GFP32Polynomial;
import de.flexiprovider.common.util.ASN1Tools;


public class LMOTSPublicKey extends PublicKey {


    private static final long serialVersionUID = 1L;
    private GFP32Polynomial hashedK;
    private GFP32Polynomial hashedL;

    private static final String OID = "1.3.6.1.4.1.8301.3.1.3.1.4";

    private LMOTSHash hFunction;

    public LMOTSPublicKey(LMOTSHash hFunction, GFP32Polynomial hk,
                          GFP32Polynomial hl) {
        hashedK = hk;
        hashedL = hl;

        this.hFunction = hFunction;
    }

    public String getAlgorithm() {
        return null;
    }

    protected ASN1Type getAlgParams() {
        return new ASN1Null();
    }


    protected byte[] getKeyData() {
        ASN1Sequence keyData = new ASN1Sequence();

        // encode public key bytes
        byte[] hk = null;
        byte[] hl = null;
        byte[] hf = null;
        try {
            hk = hashedK.getEncoded();
            hl = hashedL.getEncoded();
            hf = hFunction.getEncoded();
        } catch (Exception e) {
            e.printStackTrace();
        }
        keyData.add(new ASN1OctetString(hk));
        keyData.add(new ASN1OctetString(hl));
        keyData.add(new ASN1OctetString(hf));

        return ASN1Tools.derEncode(keyData);
    }

    protected ASN1ObjectIdentifier getOID() {
        return new ASN1ObjectIdentifier(OID);
    }

}
