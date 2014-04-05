package codec.asn1;

public class ASN1Set extends ASN1AbstractCollection {

    public ASN1Set() {
        super();
    }


    public int getTag() {
        return ASN1.TAG_SET;
    }

}
