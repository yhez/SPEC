package codec.asn1;

public class DefinedByResolver implements Resolver {
    private OIDRegistry registry_;
    private ASN1ObjectIdentifier oid_;


    public DefinedByResolver(OIDRegistry registry, ASN1ObjectIdentifier oid) {
        if (registry == null || oid == null)
            throw new NullPointerException("Registry or OID is null!");

        registry_ = registry;
        oid_ = oid;
    }


    public ASN1Type resolve(ASN1Type caller) throws ResolverException {
        ASN1Type t;

        t = registry_.getASN1Type(oid_);
        if (t == null) {
            throw new ResolverException("Cannot resolve " + oid_);
        }
        return t;
    }
}
