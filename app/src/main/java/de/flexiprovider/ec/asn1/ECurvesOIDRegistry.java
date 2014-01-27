package de.flexiprovider.ec.asn1;

import java.util.Hashtable;
import java.util.Map;

import codec.asn1.ASN1ObjectIdentifier;
import codec.asn1.AbstractOIDRegistry;


public class ECurvesOIDRegistry extends AbstractOIDRegistry {

    private static final ASN1ObjectIdentifier[] oids = {
            // prime field
            FieldId.PRIME_FIELD,
            // characteristic-two field
            FieldId.CHARACTERISTIC_TWO_FIELD,
            // gaussian normal basis
            FieldId.BASIS_TYPE_ONB,
            // trinomial basis
            FieldId.BASIS_TYPE_TRINOMIAL,
            // pentanomial basis
            FieldId.BASIS_TYPE_PENTANOMIAL};

    private static final Class[] types = {PrimeField.class,
            CharacteristicTwoField.class, GnBasis.class, TpBasis.class,
            PpBasis.class};
    private static Hashtable map;

    static {
        map = new Hashtable(oids.length);
        for (int i = 0; i < oids.length; i++) {
            map.put(oids[i], types[i]);
        }
    }

    protected Map getOIDMap() {
        return map;
    }

    protected String getPrefix() {
        return "de.flexiprovider.ec.asn1ec";
    }
}
