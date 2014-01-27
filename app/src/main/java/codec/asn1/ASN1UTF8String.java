package codec.asn1;

import java.io.UnsupportedEncodingException;

public class ASN1UTF8String extends ASN1AbstractString {


    public ASN1UTF8String(String s) {
        super.setString0(s);
    }

    public int getTag() {
        return ASN1.TAG_UTF8STRING;
    }

    protected void setString0(String s) {
        try {
            convert(s);
        } catch (ASN1Exception e) {
            throw new IllegalArgumentException(e.getMessage());
        }
        super.setString0(s);
    }


    public String convert(byte[] b) throws ASN1Exception {
        if (b == null) {
            throw new NullPointerException("Cannot convert null array!");
        }

        try {
            return new String(b, 0, b.length, "UTF8");
        } catch (UnsupportedEncodingException e) {
            throw new ASN1Exception("no UTF8");
        }
    }


    public byte[] convert(String s) throws ASN1Exception {
        if (s == null) {
            throw new NullPointerException("Cannot convert null string!");
        }

        try {
            return s.getBytes("UTF8");
        } catch (UnsupportedEncodingException e) {
            throw new ASN1Exception("no UTF8");
        }
    }

    public int convertedLength(String s) throws ASN1Exception {
        return convert(s).length;
    }

}
