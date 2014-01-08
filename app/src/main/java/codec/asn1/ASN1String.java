package codec.asn1;


public interface ASN1String extends ASN1Type {

    public String getString();

    public void setString(String s) throws ConstraintException;


    public String convert(byte[] b) throws ASN1Exception;

    public byte[] convert(String s) throws ASN1Exception;


    public int convertedLength(String s) throws ASN1Exception;

}
