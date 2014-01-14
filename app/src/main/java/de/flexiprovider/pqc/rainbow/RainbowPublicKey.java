package de.flexiprovider.pqc.rainbow;

import codec.asn1.ASN1Integer;
import codec.asn1.ASN1Null;
import codec.asn1.ASN1ObjectIdentifier;
import codec.asn1.ASN1OctetString;
import codec.asn1.ASN1Sequence;
import codec.asn1.ASN1SequenceOf;
import codec.asn1.ASN1Type;
import de.flexiprovider.api.keys.PublicKey;
import de.flexiprovider.common.util.ASN1Tools;
import de.flexiprovider.pqc.rainbow.util.RainbowUtil;


public class RainbowPublicKey extends PublicKey {

    private String oid;

    private short[][] coeffquadratic;
    private short[][] coeffsingular;
    private short[] coeffscalar;
    private int docLength; // length of possible document to sign

    public RainbowPublicKey(String oid, int docLength,
                            short[][] coeffQuadratic, short[][] coeffSingular,
                            short[] coeffScalar) {
        this.oid = oid;
        this.docLength = docLength;
        this.coeffquadratic = coeffQuadratic;
        this.coeffsingular = coeffSingular;
        this.coeffscalar = coeffScalar;

    }

    protected RainbowPublicKey(RainbowPublicKeySpec keySpec) {
        this(keySpec.getOIDString(), keySpec.getDocLength(), keySpec.getCoeffquadratic(), keySpec
                .getCoeffsingular(), keySpec.getCoeffscalar());
    }
    public int getDocLength() {
        return this.docLength;
    }

    protected short[][] getCoeffquadratic() {
        return coeffquadratic;
    }

    protected short[][] getCoeffsingular() {
        return coeffsingular;
    }

    protected short[] getCoeffscalar() {
        return coeffscalar;
    }

    public boolean equals(Object other) {
        if (other == null || !(other instanceof RainbowPublicKey)) {
            return false;
        }
        RainbowPublicKey otherKey = (RainbowPublicKey) other;

        boolean eq;
        // compare using shortcut rule ( && instead of &)
        eq = oid.equals(otherKey.getOIDString());
        eq = docLength == otherKey.getDocLength();
        eq = eq
                && RainbowUtil.equals(coeffquadratic, otherKey
                .getCoeffquadratic());
        eq = eq
                && RainbowUtil.equals(coeffsingular, otherKey
                .getCoeffsingular());
        eq = eq && RainbowUtil.equals(coeffscalar, otherKey.getCoeffscalar());
        return eq;
    }

    /**
     * @return name of the algorithm - "Rainbow"
     */
    public final String getAlgorithm() {
        return "Rainbow";
    }

    /**
     * @return the OID of the algorithm
     */
    protected String getOIDString() {
        return oid;
    }

    /**
     * @return the OID to encode in the SubjectPublicKeyInfo structure
     */
    protected ASN1ObjectIdentifier getOID() {
        return new ASN1ObjectIdentifier(RainbowKeyFactory.OID);
    }

    /**
     * @return the algorithm parameters to encode in the SubjectPublicKeyInfo
     * structure
     */
    protected ASN1Type getAlgParams() {
        return new ASN1Null();
    }


    protected byte[] getKeyData() {
        ASN1Sequence keyData = new ASN1Sequence();

        // encode <oidString>
        keyData.add(new ASN1ObjectIdentifier(oid));

        // encode <docLength>
        keyData.add(new ASN1Integer(docLength));

        // encode <coeffQuadratic>
        ASN1SequenceOf asnCoeffQuad = new ASN1SequenceOf(ASN1OctetString.class);
        for (int i = 0; i < coeffquadratic.length; i++) {
            asnCoeffQuad.add(new ASN1OctetString(RainbowUtil
                    .convertArray(coeffquadratic[i])));
        }
        keyData.add(asnCoeffQuad);

        // encode <coeffSingular>
        ASN1SequenceOf asnCoeffSing = new ASN1SequenceOf(ASN1OctetString.class);
        for (int i = 0; i < coeffsingular.length; i++) {
            asnCoeffSing.add(new ASN1OctetString(RainbowUtil
                    .convertArray(coeffsingular[i])));
        }
        keyData.add(asnCoeffSing);

        // encode <coeffScalar>
        keyData.add(new ASN1OctetString(RainbowUtil.convertArray(coeffscalar)));

        return ASN1Tools.derEncode(keyData);
    }

}
