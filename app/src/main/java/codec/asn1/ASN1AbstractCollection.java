package codec.asn1;

import java.io.ByteArrayOutputStream;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;


public abstract class ASN1AbstractCollection extends ArrayList implements
        ASN1Collection, Cloneable, Externalizable {
    private boolean optional_ = false;
    private boolean explicit_ = true;
    private Constraint constraint_;

    public abstract int getTag();

    public ASN1AbstractCollection() {
        super();
    }

    public ASN1AbstractCollection(int capacity) {
        super(capacity);
    }

    public Object getValue() {
        return this;
    }

    public Collection getCollection() {
        return this;
    }

    public void setOptional(boolean optional) {
        optional_ = optional;
    }

    public boolean isOptional() {
        return optional_;
    }

    public int getTagClass() {
        return ASN1.CLASS_UNIVERSAL;
    }

    public void setExplicit(boolean explicit) {
        explicit_ = explicit;
    }

    public boolean isExplicit() {
        return explicit_;
    }


    public void setConstraint(Constraint constraint) {
        constraint_ = constraint;
    }


    public void checkConstraints() throws ConstraintException {
        if (constraint_ != null) {
            constraint_.constrain(this);
        }
    }


    public boolean isType(int tag, int tagclass) {
        return (getTag() == tag) && (getTagClass() == tagclass);
    }

    public void encode(Encoder enc) throws ASN1Exception, IOException {
        checkConstraints();
        enc.writeCollection(this);
    }


    public void decode(Decoder dec) throws ASN1Exception, IOException {
        dec.readCollection(this);
        checkConstraints();
    }

    public String toString() {
        StringBuffer buf;
        Iterator i;
        String s;

        s = removePackageName(((Object)this).getClass());

        buf = new StringBuffer();
        buf.append(s);

        if (isOptional()) {
            buf.append(" OPTIONAL");
        }

        if (this instanceof ASN1CollectionOf) {
            buf.append(" SEQUENCE OF ").append(removePackageName(((ASN1CollectionOf) this)
                    .getElementType()));
        } else {
            buf.append(" SEQUENCE ");
        }
        buf.append(" {\n");

        for (i = iterator(); i.hasNext(); ) {
            buf.append(i.next().toString());
            buf.append("\n");
        }
        buf.append("}");
        return buf.toString();
    }

    private String removePackageName(Class clazz) {
        String s = clazz.getName();
        int n = s.lastIndexOf('.');

        if (n < 0) {
            n = -1;
        }

        s = s.substring(n + 1);
        if (s.startsWith("ASN1")) {
            s = s.substring(4);
        }
        return s;
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        byte[] res;

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            encode(new DEREncoder(baos));
            res = baos.toByteArray();
            baos.close();
            out.write(res);
        } catch (ASN1Exception e) {
            throw new RuntimeException(e.toString());
        }
    }

    public void readExternal(ObjectInput in) throws IOException {
        try {
            decode(new DERDecoder((ObjectInputStream) in));
        } catch (ASN1Exception e) {
            throw new RuntimeException(e.toString());
        }
    }
}
