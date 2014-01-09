package codec.asn1;


public class ASN1T61String extends ASN1AbstractString {


    public ASN1T61String(String s) {
        super(s);
    }

    public int getTag() {
        return ASN1.TAG_T61STRING;
    }
}
