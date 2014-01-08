package codec.asn1;

import java.io.IOException;

public interface Decoder {

    public ASN1Type readType() throws ASN1Exception, IOException;

    public void readBoolean(ASN1Boolean t) throws ASN1Exception, IOException;

    public void readInteger(ASN1Integer t) throws ASN1Exception, IOException;

    public void readBitString(ASN1BitString t) throws ASN1Exception,
            IOException;

    public void readOctetString(ASN1OctetString t) throws ASN1Exception,
            IOException;

    public void readNull(ASN1Null t) throws ASN1Exception, IOException;

    public void readObjectIdentifier(ASN1ObjectIdentifier t)
            throws ASN1Exception, IOException;

    public void readString(ASN1String t) throws ASN1Exception, IOException;

    public void readCollection(ASN1Collection t) throws ASN1Exception,
            IOException;

    public void readCollectionOf(ASN1CollectionOf t) throws ASN1Exception,
            IOException;

    public void readTime(ASN1Time t) throws ASN1Exception, IOException;

    public void readTaggedType(ASN1TaggedType t) throws ASN1Exception,
            IOException;

    public void readChoice(ASN1Choice t) throws ASN1Exception, IOException;

}
