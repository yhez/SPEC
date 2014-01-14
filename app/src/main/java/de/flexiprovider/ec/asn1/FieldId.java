package de.flexiprovider.ec.asn1;

import codec.asn1.ASN1ObjectIdentifier;
import codec.asn1.ASN1OpenType;
import codec.asn1.ASN1Sequence;


public class FieldId extends ASN1Sequence {


    static final ASN1ObjectIdentifier PRIME_FIELD = new ASN1ObjectIdentifier(
            "1.2.840.10045.1.1");
    static final ASN1ObjectIdentifier CHARACTERISTIC_TWO_FIELD = new ASN1ObjectIdentifier(
            "1.2.840.10045.1.2");
    public static final ASN1ObjectIdentifier BASIS_TYPE_ONB = new ASN1ObjectIdentifier(
            "1.2.840.10045.1.2.3.1");
    public static final ASN1ObjectIdentifier BASIS_TYPE_TRINOMIAL = new ASN1ObjectIdentifier(
            "1.2.840.10045.1.2.3.2");
    public static final ASN1ObjectIdentifier BASIS_TYPE_PENTANOMIAL = new ASN1ObjectIdentifier(
            "1.2.840.10045.1.2.3.3");
    public FieldId() {
        super(2);
        ASN1ObjectIdentifier fieldType = new ASN1ObjectIdentifier();
        add(fieldType);
        add(new ASN1OpenType(new ECurvesOIDRegistry(), fieldType));
    }
    public FieldId(PrimeField pF) {
        super(2);
        add(PRIME_FIELD);
        add(pF);
    }
    public FieldId(CharacteristicTwoField cTF) {
        super(2);
        add(CHARACTERISTIC_TWO_FIELD);
        add(cTF);
    }
    public ASN1OpenType getField() {
        return (ASN1OpenType) get(1);
    }

}
