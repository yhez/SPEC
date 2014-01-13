package de.flexiprovider.pqc.pflash;

import codec.asn1.ASN1Null;
import codec.asn1.ASN1ObjectIdentifier;
import codec.asn1.ASN1OctetString;
import codec.asn1.ASN1Sequence;
import codec.asn1.ASN1SequenceOf;
import codec.asn1.ASN1Type;
import de.flexiprovider.api.keys.PublicKey;
import de.flexiprovider.common.util.ASN1Tools;


public class PFlashPublicKey extends PublicKey {

    // the OID of the algorithm
    private final String oid = "pFLASH";

    PFlashPublicKeyElement[] ppke;

    private int arraycounter;


    protected PFlashPublicKey(int size) {
        ppke = new PFlashPublicKeyElement[size];
        arraycounter = 0;
    }
    protected PFlashPublicKey(PFlashPublicKeySpec keySpec) {
    }

    /**
     * @return the OID of the algorithm
     */
    public String getAlgorithm() {
        return oid;
    }

    // FIXME getter-Methoden f�r "n-r quadrat.polynomials in n-s variables"

    public void addElement(PFlashPublicKeyElement addppke) {
        if (arraycounter < ppke.length) {
            this.ppke[arraycounter] = addppke;
            arraycounter++;
        }
    }

    public PFlashPublicKeyElement getElement(int i) {
        return ppke[i];
    }

    /**
     * Compare this key with another object.
     *
     * @param other the other object
     * @return true if both are equal
     */
    public boolean equals(Object other) {
        if (other == null || !(other instanceof PFlashPublicKey)) {
            return false;
        }
        PFlashPublicKey otherKey = (PFlashPublicKey) other;
        return oid.equals(otherKey.oid);
    }

    protected ASN1ObjectIdentifier getOID() {
        return new ASN1ObjectIdentifier(PFlashKeyFactory.OID);
    }

    protected String getOIDString() {
        return oid;
    }

    public int hashCode() {
        // FIXME
        return 0;
    }

    /**
     * Not supported
     */
    public String toString() {
        return "";
    }

    /**
     * @return the algorithm parameters to encode in the SubjectPublicKeyInfo
     * structure
     */
    protected ASN1Type getAlgParams() {
        return new ASN1Null();
    }

    /**
     * @return the keyData to encode in the SubjectKeyInfo structure
     */
    protected byte[] getKeyData() {
        ASN1Sequence keyData = new ASN1Sequence();

        // encode OID string
        keyData.add(new ASN1ObjectIdentifier(oid));

        // FIXME
        // encode private key bytes
        ASN1SequenceOf keySequence = new ASN1SequenceOf(ASN1OctetString.class);
        keyData.add(keySequence);

        return ASN1Tools.derEncode(keyData);
    }
}