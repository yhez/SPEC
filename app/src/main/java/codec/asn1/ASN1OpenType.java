package codec.asn1;

import java.io.IOException;

public class ASN1OpenType extends ASN1AbstractType {
    private static final String NO_INNER = "No inner type defined!";

    private ASN1Type inner_;

    protected Resolver resolver_;

    public ASN1OpenType() {
        super();
    }


    public ASN1OpenType(OIDRegistry registry, ASN1ObjectIdentifier oid) {
        resolver_ = new DefinedByResolver(registry, oid);
    }


    public ASN1Type getInnerType() throws ResolverException {
        if (inner_ != null) {
            return inner_;
        }
        if (resolver_ == null) {
            return null;
        }
        inner_ = resolver_.resolve(this);

        return inner_;
    }

    public int getTag() {
        if (inner_ == null) {
            throw new IllegalStateException(NO_INNER);
        }
        return inner_.getTag();
    }

    public int getTagClass() {
        if (inner_ == null) {
            throw new IllegalStateException(NO_INNER);
        }
        return inner_.getTagClass();
    }

    public Object getValue() {
        if (inner_ == null) {
            throw new IllegalStateException(NO_INNER);
        }
        return inner_.getValue();
    }

    public void setExplicit(boolean explicit) {
        super.setExplicit(explicit);

        if (inner_ != null) {
            inner_.setExplicit(explicit);
        }
    }

    public void setConstraint(Constraint constraint) {
        if (inner_ == null) {
            throw new IllegalStateException(NO_INNER);
        }
        inner_.setConstraint(constraint);
    }

    public void checkConstraints() throws ConstraintException {
        if (inner_ == null) {
            throw new IllegalStateException(NO_INNER);
        }
        inner_.checkConstraints();
    }

    public boolean isType(int tag, int tagclass) {
        if (inner_ != null) {
            return inner_.isType(tag, tagclass);
        }
        try {
            if (resolver_ != null) {
                inner_ = resolver_.resolve(this);
            }
        } catch (ResolverException e) {
            return false;
        }

        return inner_ == null || inner_.isType(tag, tagclass);
    }

    public void encode(Encoder enc) throws ASN1Exception, IOException {
        if (inner_ == null) {
            throw new IllegalStateException(NO_INNER);
        }
        enc.writeType(inner_);
    }


    public void decode(Decoder dec) throws ASN1Exception, IOException {
        if (resolver_ != null && inner_ == null) {
            inner_ = resolver_.resolve(this);
        }
        if (inner_ == null) {
            inner_ = dec.readType();
        } else {
            inner_.decode(dec);
        }
        inner_.setExplicit(isExplicit());
    }

    public String toString() {
        if (inner_ == null) {
            return "Open Type <NOT InitializED>";
        }
        return "(Open Type) " + inner_.toString();
    }
}
