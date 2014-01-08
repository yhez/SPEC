package codec.asn1;

import java.io.IOException;


public class ASN1Enumerated extends ASN1Integer {

    public ASN1Enumerated(int n) {
        super(n);
    }

    public void decode(Decoder dec) throws ASN1Exception, IOException {
        dec.readInteger(this);
        checkConstraints();
    }

    public void encode(Encoder enc) throws ASN1Exception, IOException {
        enc.writeInteger(this);
    }

    public int getTag() {
        return ASN1.TAG_ENUMERATED;
    }

    public String toString() {
        return "Enumerated " + super.getValue().toString();
    }
}
