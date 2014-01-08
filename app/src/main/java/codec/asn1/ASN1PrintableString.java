package codec.asn1;

public class ASN1PrintableString extends ASN1AbstractString {

    public ASN1PrintableString(String s) {
        super(s);
    }

    public int getTag() {
        return ASN1.TAG_PRINTABLESTRING;
    }
}
