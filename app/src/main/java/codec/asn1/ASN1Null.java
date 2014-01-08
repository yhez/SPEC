package codec.asn1;

import java.io.IOException;


public class ASN1Null extends ASN1AbstractType implements Cloneable {

    public Object getValue() {
        return null;
    }

    public int getTag() {
        return ASN1.TAG_NULL;
    }

    public void encode(Encoder enc) throws ASN1Exception, IOException {
        enc.writeNull(this);
    }

    public void decode(Decoder dec) throws ASN1Exception, IOException {
        dec.readNull(this);
    }

    public String toString() {
        return "NULL";
    }
}
