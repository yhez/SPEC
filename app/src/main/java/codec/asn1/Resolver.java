package codec.asn1;


public interface Resolver {
    public ASN1Type resolve(ASN1Type caller);
}
