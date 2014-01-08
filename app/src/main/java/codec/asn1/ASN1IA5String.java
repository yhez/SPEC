package codec.asn1;


public class ASN1IA5String extends ASN1AbstractString {

    public ASN1IA5String() {
        super();
    }

    public ASN1IA5String(String s) {
        super(s);
    }


    public int getTag() {
        return ASN1.TAG_IA5STRING;
    }

}
