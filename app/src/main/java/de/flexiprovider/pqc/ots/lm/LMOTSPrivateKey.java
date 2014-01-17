package de.flexiprovider.pqc.ots.lm;

import java.util.Vector;

import codec.asn1.ASN1ObjectIdentifier;
import codec.asn1.ASN1Type;
import de.flexiprovider.api.keys.PrivateKey;


public class LMOTSPrivateKey extends PrivateKey {


    private static final long serialVersionUID = 1L;
    private Vector k;
    private Vector l;

    public LMOTSPrivateKey(Vector k, Vector l) {
        this.k = k;
        this.l = l;
    }

    /**
     * @return returns the K parameter of this private Key
     */
    public Vector getK() {
        return k;
    }

    /**
     * @return returns the L parameter of this private Key
     */
    public Vector getL() {
        return l;
    }

    /**
     * this method is not used in this implementation
     *
     * @return null
     */
    protected ASN1Type getAlgParams() {
        //not used in this Signature spec
        return null;
    }

    /**
     * this method is not used in this implementation
     *
     * @return null
     */
    protected byte[] getKeyData() {
        //not used in this Signature spec
        return null;
    }

    /**
     * this method is not used in this implementation
     *
     * @return null
     */
    protected ASN1ObjectIdentifier getOID() {
        //not used in this Signature spec
        return null;
    }
    public String getAlgorithm() {
        //not used in this Signature spec
        return null;
    }

}
