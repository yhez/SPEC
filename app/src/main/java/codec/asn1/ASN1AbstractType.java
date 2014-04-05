package codec.asn1;

import java.io.ByteArrayOutputStream;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;


public abstract class ASN1AbstractType implements ASN1Type,
        Cloneable, Externalizable {

    private boolean optional_ = false;


    private boolean explicit_ = true;


    private Constraint constraint_;


    public abstract Object getValue();

    public abstract int getTag();

    public abstract void encode(Encoder enc);

    public abstract void decode(Decoder dec);


    public ASN1AbstractType() {
        super();
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


    public boolean isType(int tag, int tagclass) {
        return (getTag() == tag) && (getTagClass() == tagclass);
    }

    public void setConstraint(Constraint constraint) {
        constraint_ = constraint;
    }


    public Constraint getConstraint() {
        return constraint_;
    }


    public void checkConstraints() {
        if (constraint_ != null) {
            constraint_.constrain(this);
        }
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        byte[] res;

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
            encode(new DEREncoder(baos));
            res = baos.toByteArray();
            baos.close();
            out.write(res);
    }

    public void readExternal(ObjectInput in) {
            decode(new DERDecoder((ObjectInputStream) in));
    }
}
