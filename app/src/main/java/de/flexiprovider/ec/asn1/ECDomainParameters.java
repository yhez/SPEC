package de.flexiprovider.ec.asn1;

import codec.asn1.ASN1Integer;
import codec.asn1.ASN1OctetString;
import codec.asn1.ASN1Sequence;
import de.flexiprovider.common.math.FlexiBigInt;
import de.flexiprovider.common.util.ASN1Tools;


public class ECDomainParameters extends ASN1Sequence {

    /**
     * The version always is 1.
     */
    private static final ASN1Integer version = new ASN1Integer(1);

    /**
     * Constructor used for decoding.
     */
    public ECDomainParameters() {
        super(6);
        add(version);
        add(new FieldId());
        add(new Curve());
        add(new ASN1OctetString());
        add(new ASN1Integer());
        ASN1Integer cofactor = new ASN1Integer();
        cofactor.setOptional(true);
        add(cofactor);
    }


    public ECDomainParameters(FieldId fieldId, Curve curve,
                              ASN1OctetString basePoint, ASN1Integer order, ASN1Integer cofactor) {
        super(6);
        add(version);
        add(fieldId);
        add(curve);
        add(basePoint);
        add(order);
        if (cofactor != null) {
            add(cofactor);
        }
    }

    /**
     * @return the fieldId parameter
     */
    public FieldId getFieldId() {
        return (FieldId) get(1);
    }

    /**
     * @return the encoded curve coefficient a
     */
    public byte[] getA() {
        return ((Curve) get(2)).getA();
    }

    /**
     * @return the encoded curve coefficient b
     */
    public byte[] getB() {
        return ((Curve) get(2)).getB();
    }

    /**
     * @return the basepoint
     */
    public byte[] getG() {
        return ((ASN1OctetString) get(3)).getByteArray();
    }

    /**
     * @return the order of the basepoint
     */
    public FlexiBigInt getR() {
        return ASN1Tools.getFlexiBigInt((ASN1Integer) get(4));
    }

    /**
     * @return the cofactor of the basepoint, or 1 if the cofactor is not
     * present.
     */
    public int getK() {
        ASN1Integer asn1Integer = (ASN1Integer) get(5);

        if (asn1Integer != null) {
            int value = ASN1Tools.getFlexiBigInt(asn1Integer).intValue();
            if (value == 0) { // it was defined as optional therefore return "1"
                return 1;
            }
            return value;
        }
        return 1;
    }

}
