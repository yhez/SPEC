package codec.asn1;

public interface Encoder {

    public void writeType(ASN1Type t);

    public void writeBoolean(ASN1Boolean t);

    public void writeInteger(ASN1Integer t);

    public void writeBitString(ASN1BitString t);

    public void writeOctetString(ASN1OctetString t);

    public void writeNull(ASN1Null t);

    public void writeObjectIdentifier(ASN1ObjectIdentifier t);

    public void writeString(ASN1String t);

    public void writeCollection(ASN1Collection t);

    public void writeTime(ASN1Time t);

    public void writeTaggedType(ASN1TaggedType t);

}
