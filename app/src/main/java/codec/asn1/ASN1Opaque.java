package codec.asn1;


public class ASN1Opaque extends ASN1TaggedType {


    public ASN1Opaque(int tag, int tagclass, byte[] b) {
        super(tag, tagclass, new ASN1OctetString(b.clone()), false);
    }

    public boolean isType(int tag, int tagclass) {
        if (tagclass != ASN1.CLASS_UNIVERSAL)
            return false;

        if (getTag() == -1) {
            setTag(tag);
            return true;
        }
        return super.isType(tag, tagclass);
    }


    public void setInnerType(ASN1Type t) {
        super.setInnerType(t);
    }


    public Object clone() {
        ASN1OctetString b;
        ASN1Opaque o;

        try {
            o = (ASN1Opaque) super.clone();
            b = (ASN1OctetString) o.getInnerType();

            o.setInnerType((ASN1OctetString) b.clone());
        } catch (CloneNotSupportedException e) {

            throw new Error("Internal, clone support mismatch!");
        }
        return o;
    }

}
