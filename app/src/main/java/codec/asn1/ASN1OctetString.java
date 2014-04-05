package codec.asn1;


public class ASN1OctetString extends ASN1AbstractType {
    private static final byte[] DEFAULT_VALUE = new byte[0];

    private byte[] value_ = DEFAULT_VALUE;

    public ASN1OctetString() {
    }


    public ASN1OctetString(byte[] b) {
        setByteArray0(b);
    }

    public Object getValue() {
        return value_;
    }


    public byte[] getByteArray() {
        return value_.clone();
    }


    public void setByteArray(byte[] b){
        setByteArray0(b);
        checkConstraints();
    }


    private void setByteArray0(byte[] b) {
        if (b == null)
            value_ = DEFAULT_VALUE;
        else
            value_ = b;
    }

    public int byteCount() {
        return value_.length;
    }

    public int getTag() {
        return ASN1.TAG_OCTETSTRING;
    }

    public void encode(Encoder enc){
        enc.writeOctetString(this);
    }

    public void decode(Decoder dec){
        dec.readOctetString(this);
        checkConstraints();
    }

    public String toString() {
        StringBuffer buf;
        String octet;
        int i;

        buf = new StringBuffer("Octet String");

        for (i = 0; i < value_.length; i++) {
            octet = Integer.toHexString(value_[i] & 0xff);

            buf.append(' ');

            if (octet.length() == 1) {
                buf.append('0');
            }
            buf.append(octet);
        }
        return buf.toString();
    }

    public Object clone() {
        ASN1OctetString o;

        try {
            o = (ASN1OctetString) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new Error("Internal, clone support mismatch!");
        }
        o.value_ = value_.clone();

        return o;
    }
}
