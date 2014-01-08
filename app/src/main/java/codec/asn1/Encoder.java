package codec.asn1;

import java.io.IOException;


public interface Encoder {

    public void writeType(ASN1Type t) throws ASN1Exception, IOException;

    public void writeBoolean(ASN1Boolean t) throws ASN1Exception, IOException;

    public void writeInteger(ASN1Integer t) throws ASN1Exception, IOException;

    public void writeBitString(ASN1BitString t) throws ASN1Exception,
            IOException;

    public void writeOctetString(ASN1OctetString t) throws ASN1Exception,
            IOException;

    public void writeNull(ASN1Null t) throws ASN1Exception, IOException;

    public void writeObjectIdentifier(ASN1ObjectIdentifier t)
            throws ASN1Exception, IOException;

    public void writeString(ASN1String t) throws ASN1Exception, IOException;

    public void writeCollection(ASN1Collection t) throws ASN1Exception,
            IOException;

    public void writeTime(ASN1Time t) throws ASN1Exception, IOException;

    public void writeTaggedType(ASN1TaggedType t) throws ASN1Exception,
            IOException;

}
