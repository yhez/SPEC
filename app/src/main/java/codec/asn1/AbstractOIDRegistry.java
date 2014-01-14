package codec.asn1;

import java.util.Map;


public abstract class AbstractOIDRegistry extends OIDRegistry {

    protected abstract String getPrefix();

    public AbstractOIDRegistry() {
        this(null);
    }

    public AbstractOIDRegistry(OIDRegistry parent) {
        super(parent);
    }

    protected abstract Map getOIDMap();

    protected ASN1Type getLocalASN1Type(ASN1ObjectIdentifier oid) {
        Object o;
        Class c;
        Map map;
        map = getOIDMap();
        o = map.get(oid);
        if (o == null) {
            return null;
        }
        try {
            if (o instanceof String) {
                c = Class.forName(getPrefix() + o);
                map.put(new ASN1ObjectIdentifier(oid.getOID()), c);
                o = c;
            }
            c = (Class) o;

            return (ASN1Type) c.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
