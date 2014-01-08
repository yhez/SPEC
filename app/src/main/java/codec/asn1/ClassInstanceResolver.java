package codec.asn1;


public class ClassInstanceResolver implements Resolver {

    private Class factory_;


    public ClassInstanceResolver(Class factory) {
        if (factory == null) {
            throw new NullPointerException("Need a factory class!");
        }
        try {
            factory.getConstructor(new Class[0]);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("Class " + factory.getName()
                    + " has no default constructor!");
        }
        if (!ASN1Type.class.isAssignableFrom(factory)) {
            throw new IllegalArgumentException("Class " + factory.getName()
                    + " is not an ASN1Type!");
        }
        factory_ = factory;
    }

    public Class getFactoryClass() {
        return factory_;
    }

    public ASN1Type resolve(ASN1Type caller) throws ResolverException {
        try {
            return (ASN1Type) factory_.newInstance();
        } catch (Exception e) {
            throw new ResolverException("Caught " + e.getClass().getName()
                    + "(\"" + e.getMessage() + "\")");
        }
    }

}
