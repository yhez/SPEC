package codec.asn1;

public class ASN1VisibleString extends ASN1AbstractString {
    public ASN1VisibleString() {
        super();
    }

    public int getTag() {
        return ASN1.TAG_VISIBLESTRING;
    }
}
