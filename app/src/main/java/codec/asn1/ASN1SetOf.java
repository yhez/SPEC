package codec.asn1;

import java.io.IOException;


public class ASN1SetOf extends ASN1Set implements ASN1CollectionOf {

    private Resolver resolver_;


    public ASN1SetOf(Class type) {
        if (type == null)
            throw new NullPointerException("Need a class!");

        resolver_ = new ClassInstanceResolver(type);
    }

    public ASN1SetOf(Resolver resolver, int capacity) {

        super(capacity);

        if (resolver == null) {
            throw new NullPointerException("Need a resolver!");
        }
        resolver_ = resolver;
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
