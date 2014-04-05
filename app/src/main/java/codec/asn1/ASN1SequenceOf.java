package codec.asn1;

public class ASN1SequenceOf extends ASN1Sequence implements ASN1CollectionOf {

    private Resolver resolver_;

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

    public void decode(Decoder dec){
        dec.readCollectionOf(this);
        checkConstraints();
    }
}
