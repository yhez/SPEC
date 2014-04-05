package codec.asn1;

public class ASN1TaggedType extends ASN1AbstractType {
    private int tag_;
    private int cls_ = ASN1.CLASS_CONTEXT;

    private ASN1Type inner_;


    public ASN1TaggedType(int tag, int cls, ASN1Type inner, boolean explicit) {
        setTag(tag);
        setTagClass(cls);
        setInnerType(inner);
        inner_.setExplicit(explicit);
    }


    public ASN1Type getInnerType() {
        return inner_;
    }

    public void setInnerType(ASN1Type t) {
        if (t == null) {
            throw new NullPointerException("Type is NULL!");
        }
        inner_ = t;
    }

    public Object getValue() {
        return inner_.getValue();
    }

    public int getTag() {
        return tag_;
    }

    public void setTag(int tag) {
        tag_ = tag;
    }

    public int getTagClass() {
        return cls_;
    }

    public void setTagClass(int cls) {
        cls_ = cls;
    }

    public boolean isExplicit() {
        return true;
    }

    public void setExplicit(boolean explicit) {
        if (!explicit)
            throw new IllegalArgumentException(
                    "Tagget types are never IMPLICIT!");
    }

    public void encode(Encoder enc) {
        enc.writeTaggedType(this);
    }

    public void decode(Decoder dec) {
        dec.readTaggedType(this);
    }

    public String toString() {
        StringBuffer buf;

        buf = new StringBuffer();
        buf.append("[");

        switch (cls_) {
            case ASN1.CLASS_CONTEXT:
                buf.append("CONTEXT SPECIFIC ");
                break;
            case ASN1.CLASS_UNIVERSAL:
                buf.append("UNIVERSAL ");
                break;
            case ASN1.CLASS_APPLICATION:
                buf.append("APPLICATION ");
                break;
            case ASN1.CLASS_PRIVATE:
                buf.append("PRIVATE ");
                break;
        }
        buf.append(tag_).append("] ");

        if (inner_.isExplicit())
            buf.append("EXPLICIT ");
        else
            buf.append("IMPLICIT ");

        buf.append(inner_.toString());
        return buf.toString();
    }

}
