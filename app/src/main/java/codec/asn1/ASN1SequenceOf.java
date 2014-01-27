package codec.asn1;

import java.io.IOException;


public class ASN1SequenceOf extends ASN1Sequence implements ASN1CollectionOf {

    private Resolver resolver_;

    protected ASN1SequenceOf(int capacity) {
        super(capacity);
    }

    public ASN1SequenceOf(Class type) {
        if (type == null)
            throw new NullPointerException("Need a class!");

        resolver_ = new ClassInstanceResolver(type);
    }

    public Class getElementType() {
        if (resolver_ instanceof ClassInstanceResolver) {
            return ((ClassInstanceResolver) resolver_).getFactoryClass();
        }
        return ASN1Type.class;
    }


    public ASN1Type newElement() {
        try {
            ASN1Type o;

            o = resolver_.resolve(this);
            add(o);

            return o;
        } catch (Exception e) {
            throw new IllegalStateException("Caught " + e.getClass().getName()
                    + "(\"" + e.getMessage() + "\")");
        }
    }

    public void decode(Decoder dec) throws ASN1Exception, IOException {
        dec.readCollectionOf(this);
        checkConstraints();
    }
}
