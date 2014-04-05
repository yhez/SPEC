package codec.asn1;

import java.util.ArrayList;
import java.util.Iterator;


public class ASN1Choice extends ASN1AbstractType {
    private static final String NO_INNER = "No inner type defined!";

    private ASN1Type inner_;

    private ArrayList choices_;

    public ASN1Choice(int capacity) {
        if (capacity < 1)
            throw new IllegalArgumentException(
                    "capacity must be greater than zero!");

        choices_ = new ArrayList(capacity);
    }

    public ASN1Type getType(int tag, int tagclass) {
        Iterator i;
        ASN1Type t;

        for (i = choices_.iterator(); i.hasNext(); ) {
            t = (ASN1Type) i.next();
            if (t.getTag() != tag)
                continue;
            if (t.getTagClass() == tagclass)
                return t;
        }
        return null;
    }

    public boolean isType(int tag, int tagclass) {
        return getType(tag, tagclass) != null;
    }


    public void setInnerType(ASN1Type t) {
        if (t == null)
            throw new NullPointerException("No type given!");

        inner_ = t;
    }

    public int getTag() {
        if (inner_ == null)
            throw new IllegalStateException(NO_INNER);

        return inner_.getTag();
    }

    public int getTagClass() {
        if (inner_ == null)
            throw new IllegalStateException(NO_INNER);

        return inner_.getTagClass();
    }

    public Object getValue() {
        if (inner_ == null)
            throw new IllegalStateException(NO_INNER);

        return inner_.getValue();
    }

    public void setExplicit(boolean explicit) {
        if (!explicit)
            throw new IllegalArgumentException(
                    "CHOICE types must be tagged EXPLICIT!");
    }

    public boolean isExplicit() {
        return true;
    }

    public void setConstraint(Constraint constraint) {
        if (inner_ == null)
            throw new IllegalStateException(NO_INNER);

        inner_.setConstraint(constraint);
    }

    public void checkConstraints(){
        if (inner_ == null)
            throw new IllegalStateException(NO_INNER);

        inner_.checkConstraints();
    }

    public void encode(Encoder enc){
        if (inner_ == null)
            throw new IllegalStateException(NO_INNER);

        enc.writeType(inner_);
    }

    public void decode(Decoder dec){
        dec.readChoice(this);
    }

    public String toString() {
        if (inner_ == null)
            return "CHOICE <NOT InitializED>";

        return "(CHOICE) " + inner_.toString();
    }
}
