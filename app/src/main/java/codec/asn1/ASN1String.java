package codec.asn1;


public interface ASN1String extends ASN1Type {

    public String getString();

    public void setString(String s);


    public String convert(byte[] b);

    public byte[] convert(String s);


    public int convertedLength(String s);

}
