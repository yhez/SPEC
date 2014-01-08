package codec.asn1;


public class ASN1Sequence extends ASN1AbstractCollection {

    public ASN1Sequence() {
        super();
    }


    public ASN1Sequence(int capacity) {
        super(capacity);
    }


    public int getTag() {
        return ASN1.TAG_SEQUENCE;
    }

}
