package codec.asn1;

import java.io.IOException;


public class ASN1Boolean extends ASN1AbstractType {

    private boolean value_ = true;

    public ASN1Boolean(boolean t) {
        setTrue(t);
    }

    public Object getValue() {
        return value_;
    }

    public boolean isTrue() {
        return value_;
    }

    public void setTrue(boolean b) {
        value_ = b;
    }

    public int getTag() {
        return ASN1.TAG_BOOLEAN;
    }

    public void encode(Encoder enc) throws ASN1Exception, IOException {
        enc.writeBoolean(this);
    }

    public void decode(Decoder dec) throws ASN1Exception, IOException {
        dec.readBoolean(this);
    }

    public String toString() {
        return "BOOLEAN " + value_;
    }
}
