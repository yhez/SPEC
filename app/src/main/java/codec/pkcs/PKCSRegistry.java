package codec.pkcs;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import codec.asn1.ASN1ObjectIdentifier;
import codec.asn1.ASN1Type;
import codec.asn1.AbstractOIDRegistry;
import codec.asn1.OIDRegistry;


public class PKCSRegistry extends AbstractOIDRegistry {

    public static final String RN = "codec/pkcs";

    static private Map map_ = Collections.synchronizedMap(new HashMap());

    static {
        loadOIDMap(map_, RN);
    }


    static private PKCSRegistry default_ = new PKCSRegistry(OIDRegistry
            .getGlobalOIDRegistry());

    public PKCSRegistry(OIDRegistry parent) {
        super(parent);
    }


    protected Map getOIDMap() {
        return map_;
    }


    protected String getPrefix() {
        return RN;
    }


    protected ASN1Type getLocalASN1Type(ASN1ObjectIdentifier oid) {
        return getLocalASN1Type(oid, map_);
    }


    static public OIDRegistry getDefaultRegistry() {
        return default_;
    }
}
