package codec.asn1;

public class ASN1Null extends ASN1AbstractType implements Cloneable {

    public Object getValue() {
        return null;
    }

    public int getTag() {
        return ASN1.TAG_NULL;
    }

    public void encode(Encoder enc){
        enc.writeNull(this);
    }

    public void decode(Decoder dec){
        dec.readNull(this);
    }

    public String toString() {
        return "NULL";
    }
}
