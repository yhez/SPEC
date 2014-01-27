package codec.asn1;

import java.io.IOException;


public abstract class ASN1AbstractString extends ASN1AbstractType implements
        ASN1String {

    private static final String DEFAULT_VALUE = "";

    private String value_ = DEFAULT_VALUE;

    public ASN1AbstractString() {
        super();
    }

    public Object getValue() {
        return value_;
    }

    public String getString() {
        return value_;
    }


    public void setString(String s) throws ConstraintException {
        setString0(s);
        checkConstraints();
    }

    protected void setString0(String s) {
        if (s == null)
            throw new NullPointerException("Need a string!");

        value_ = s;
    }

    public void encode(Encoder enc) throws ASN1Exception, IOException {
        enc.writeString(this);
    }

    public void decode(Decoder enc) throws ASN1Exception, IOException {
        enc.readString(this);
        checkConstraints();
    }

    public String convert(byte[] b) throws ASN1Exception {
        if (b == null)
            throw new NullPointerException("Cannot convert null array!");

        char[] c = new char[b.length];
        for (int i = 0; i < b.length; i++)
            c[i] = (char) (b[i] & 0xff);

        return String.valueOf(c);
    }

    public byte[] convert(String s) throws ASN1Exception {
        if (s == null)
            throw new NullPointerException("Cannot convert null string!");

        char[] c = s.toCharArray();
        byte[] b = new byte[c.length];

        for (int i = 0; i < c.length; i++)
            b[i] = (byte) (c[i] & 0xff);

        return b;
    }

    public int convertedLength(String s) throws ASN1Exception {
        return s.length();
    }

    public String toString() {
        String s;
        int n;

        s = ((Object)this).getClass().getName();
        n = s.lastIndexOf('.');

        if (n < 0)
            n = -1;

        s = s.substring(n + 1);
        if (s.startsWith("ASN1"))
            s = s.substring(4);

        return s + " \"" + value_ + "\"";
    }

    public boolean equals(Object s) {
        return ((Object)this).getClass().equals(s.getClass()) && value_.equals(((ASN1AbstractString) s).getString());
    }

    public int hashCode() {
        return (((Object)this).getClass().hashCode() + value_.hashCode()) / 2;
    }

}
