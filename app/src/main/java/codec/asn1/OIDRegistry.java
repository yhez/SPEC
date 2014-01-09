package codec.asn1;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;


public class OIDRegistry {


    private static Set registries_ = Collections.synchronizedSet(new HashSet());

    private static OIDRegistry global_ = new OIDRegistry();

    private OIDRegistry parent_ = null;

    private OIDRegistry() {
    }

    static public OIDRegistry getGlobalOIDRegistry() {
        return global_;
    }


    public OIDRegistry(OIDRegistry parent) {
        parent_ = parent;
    }

    final public ASN1Type getASN1Type(ASN1ObjectIdentifier oid) {
        ASN1Type o;

        o = getLocalASN1Type(oid);

        if (o == null && parent_ != null) {
            return parent_.getASN1Type(oid);
        }
        return o;
    }

    protected ASN1Type getLocalASN1Type(ASN1ObjectIdentifier oid) {
        Iterator i;
        ASN1Type o;
        OIDRegistry r;

        for (i = registries_.iterator(); i.hasNext(); ) {
            r = (OIDRegistry) i.next();
            o = r.getASN1Type(oid);

            if (o != null) {
                return o;
            }
        }
        return null;
    }


    protected ASN1Type getLocalASN1Type(ASN1ObjectIdentifier oid, Map map) {
        Object o;
        Class c;

        if (oid == null || map == null) {
            throw new NullPointerException("oid or map");
        }
        o = map.get(oid);

        if (o == null) {
            return null;
        }
        try {
            if (o instanceof String) {
                c = Class.forName((String) o);

                map.put(new ASN1ObjectIdentifier(oid.getOID()), c);

                o = c;
            }
            c = (Class) o;

            return (ASN1Type) c.newInstance();
        } catch (Exception e) {
            e.printStackTrace(System.err);
            return null;
        }
    }

    public boolean equals(Object o) {
        return getClass() == o.getClass();
    }

    public int hashCode() {
        return getClass().hashCode();
    }

}
