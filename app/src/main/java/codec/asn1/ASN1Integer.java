package codec.asn1;

import java.math.BigInteger;


public class ASN1Integer extends ASN1AbstractType {

    private static int opi_;


    private BigInteger value_;


    static {
        int n;
        int i;

        for (n = 1, i = 0; n != 0; n = n << 8) {
            i++;
        }
        opi_ = i;
    }


    public ASN1Integer(int n) {
        byte[] b;
        int i;
        int m;

        b = new byte[opi_];
        m = n;

        for (i = opi_ - 1; i >= 0; i--) {
            b[i] = (byte) (m & 0xff);
            m = m >>> 8;
        }
        value_ = new BigInteger(b);
    }

    public Object getValue() {
        return value_;
    }

    public BigInteger getBigInteger() {
        return value_;
    }

    public void setBigInteger(BigInteger n) {
        value_ = n;
        checkConstraints();
    }

    public int getTag() {
        return ASN1.TAG_INTEGER;
    }

    public void encode(Encoder enc){
        enc.writeInteger(this);
    }

    public void decode(Decoder dec){
        dec.readInteger(this);
        checkConstraints();
    }

    public String toString() {
        return "Integer " + value_.toString();
    }
}
