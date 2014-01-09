package codec.asn1;

public interface ASN1CollectionOf extends ASN1Collection {
    public Class getElementType();

    public ASN1Type newElement();

}
