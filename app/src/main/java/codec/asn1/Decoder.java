package codec.asn1;

public interface Decoder {

    public void readBoolean(ASN1Boolean t);

    public void readInteger(ASN1Integer t);

    public void readBitString(ASN1BitString t);

    public void readOctetString(ASN1OctetString t);

    public void readNull(ASN1Null t);

    public void readObjectIdentifier(ASN1ObjectIdentifier t);

    public void readString(ASN1String t);

    public void readCollection(ASN1Collection t);

    public void readCollectionOf(ASN1CollectionOf t);

    public void readTime(ASN1Time t);

    public void readTaggedType(ASN1TaggedType t);

    public void readChoice(ASN1Choice t);

}
