package de.flexiprovider.ec.asn1;

import codec.asn1.ASN1Integer;
import codec.asn1.ASN1Null;
import codec.asn1.ASN1OpenType;
import codec.asn1.ASN1Sequence;
import codec.asn1.ASN1Type;
import codec.asn1.ResolverException;
import de.flexiprovider.common.util.ASN1Tools;


public class CharacteristicTwoField extends ASN1Sequence {

    public CharacteristicTwoField(int n) {
        super(3);
        add(new ASN1Integer(n));
        add(FieldId.BASIS_TYPE_ONB);
        add(new ASN1Null());
    }

    public CharacteristicTwoField(int n, int tc) {
        super(3);
        add(new ASN1Integer(n));
        add(FieldId.BASIS_TYPE_TRINOMIAL);
        add(new TpBasis(tc));
    }
    public CharacteristicTwoField(int n, int pc1, int pc2, int pc3) {
        super(3);
        add(new ASN1Integer(n));
        add(FieldId.BASIS_TYPE_PENTANOMIAL);
        add(new PpBasis(pc1, pc2, pc3));
    }
    public boolean isONB() {
        return get(1).equals(FieldId.BASIS_TYPE_ONB);
    }
    public boolean isTrinomial() {
        return get(1)
                .equals(FieldId.BASIS_TYPE_TRINOMIAL);
    }
    public boolean isPentanomial() {
        return get(1)
                .equals(FieldId.BASIS_TYPE_PENTANOMIAL);
    }
    public int getN() {
        return ASN1Tools.getFlexiBigInt((ASN1Integer) get(0)).intValue();
    }
    public TpBasis getTrinom() {
        ASN1Type type = (ASN1Type) get(2);
        if (type instanceof TpBasis) {
            return (TpBasis) type;
        }

        try {
            return (TpBasis) ((ASN1OpenType) type).getInnerType();
        } catch (ResolverException re) {
            throw new RuntimeException("ResolverException: " + re.getMessage());
        }
    }
    public PpBasis getPenta() {
        ASN1Type type = (ASN1Type) get(2);
        if (type instanceof PpBasis) {
            return (PpBasis) type;
        }

        try {
            return (PpBasis) ((ASN1OpenType) type).getInnerType();
        } catch (ResolverException re) {
            throw new RuntimeException("ResolverException: " + re.getMessage());
        }
    }

}
