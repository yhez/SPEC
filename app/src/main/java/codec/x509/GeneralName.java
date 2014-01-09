package codec.x509;

import codec.asn1.ASN1Choice;
import codec.asn1.ASN1IA5String;
import codec.asn1.ASN1ObjectIdentifier;
import codec.asn1.ASN1OctetString;
import codec.asn1.ASN1OpenType;
import codec.asn1.ASN1Sequence;
import codec.asn1.ASN1TaggedType;
import codec.asn1.ASN1Type;
import codec.x501.Name;


public class GeneralName extends ASN1Choice {

    public static final int TYPE_OTHER_NAME = 0;

    public static final int TYPE_RFC822_NAME = 1;

    public static final int TYPE_DNS_NAME = 2;

    public static final int TYPE_X400_ADDRESS = 3;

    public static final int TYPE_DIRECTORY_NAME = 4;

    public static final int TYPE_EDI_PARTY_NAME = 5;

    public static final int TYPE_UNIFORM_RESOURCE_IDENTIFIER = 6;
    private ASN1IA5String uniformResourceId_;
    private ASN1TaggedType uniformResourceIdentifier_;

    public static final int TYPE_IP_ADDRESS = 7;

    public static final int TYPE_REGISTERED_ID = 8;

    public GeneralName() {
        super();

        ASN1ObjectIdentifier otherNameID_ = new ASN1ObjectIdentifier();
        ASN1OpenType otherNameValue_ = new ASN1OpenType();

        ASN1TaggedType otherNameValueTag_ = new ASN1TaggedType(0, otherNameValue_, true, false);

        ASN1Sequence otherNameSequence_ = new ASN1Sequence();
        otherNameSequence_.add(otherNameID_);
        otherNameSequence_.add(otherNameValueTag_);

        ASN1TaggedType otherName_ = new ASN1TaggedType(TYPE_OTHER_NAME, otherNameSequence_,
                false, false);
        addType(otherName_);

        ASN1IA5String rfc822N_ = new ASN1IA5String();
        ASN1TaggedType rfc822Name_ = new ASN1TaggedType(TYPE_RFC822_NAME, rfc822N_, false,
                false);
        addType(rfc822Name_);

        ASN1IA5String dNSN_ = new ASN1IA5String();
        ASN1TaggedType dNSName_ = new ASN1TaggedType(TYPE_DNS_NAME, dNSN_, false, false);
        addType(dNSName_);
        Name dirN_ = new Name();
        ASN1TaggedType directoryName_ = new ASN1TaggedType(TYPE_DIRECTORY_NAME, dirN_, true,
                false);
        addType(directoryName_);
        uniformResourceId_ = new ASN1IA5String();
        uniformResourceIdentifier_ = new ASN1TaggedType(
                TYPE_UNIFORM_RESOURCE_IDENTIFIER, uniformResourceId_, false,
                false);
        addType(uniformResourceIdentifier_);

        ASN1OctetString iPAdr_ = new ASN1OctetString();

        ASN1TaggedType iPAddress_ = new ASN1TaggedType(TYPE_IP_ADDRESS, iPAdr_, false, false);
        addType(iPAddress_);

        ASN1ObjectIdentifier regID_ = new ASN1ObjectIdentifier();

        ASN1TaggedType registeredID_ = new ASN1TaggedType(TYPE_REGISTERED_ID, regID_, false,
                false);
        addType(registeredID_);
    }


    public ASN1Type getGeneralName() throws X509Exception {
        int tag = getTag();
        ASN1TaggedType inner = (ASN1TaggedType) getInnerType();
        switch (tag) {
            case TYPE_OTHER_NAME:
                return inner.getInnerType();
            case TYPE_RFC822_NAME:
                return inner.getInnerType();
            case TYPE_DNS_NAME:
                return inner.getInnerType();
            case TYPE_X400_ADDRESS:
                throw new X509Exception("x400Address not yet supported!");
            case TYPE_DIRECTORY_NAME:
                return inner.getInnerType();
            case TYPE_EDI_PARTY_NAME:
                throw new X509Exception("ediPartyName not yet supported!");
            case TYPE_UNIFORM_RESOURCE_IDENTIFIER:
                return inner.getInnerType();
            case TYPE_IP_ADDRESS:
                return inner.getInnerType();
            case TYPE_REGISTERED_ID:
                return inner.getInnerType();
            default:
                throw new X509Exception("Tag not supported for GeneralName: " + tag);
        }
    }


    public void setUniformResourceIdentifier(ASN1IA5String unirid) {
        uniformResourceId_ = new ASN1IA5String(unirid.getString());
        uniformResourceId_.setExplicit(false);
        uniformResourceIdentifier_.setInnerType(uniformResourceId_);
        setInnerType(uniformResourceIdentifier_);
    }


    public String toString() {

        StringBuilder res = new StringBuilder("GeneralName {\n");

        try {
            res.append(getGeneralName().toString());
        } catch (Exception e) {
            res.append("Caught Exception ").append(e.getMessage());
            e.printStackTrace();
        }
        res.append("\n}");

        return res.toString();
    }
}
