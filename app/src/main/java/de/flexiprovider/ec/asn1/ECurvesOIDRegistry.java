package de.flexiprovider.ec.asn1;

import java.util.Hashtable;
import java.util.Map;

import codec.asn1.ASN1ObjectIdentifier;
import codec.asn1.AbstractOIDRegistry;


public class ECurvesOIDRegistry extends AbstractOIDRegistry {
    static final ASN1ObjectIdentifier PRIME_FIELD = new ASN1ObjectIdentifier(
            "1.2.840.10045.1.1");
    static final ASN1ObjectIdentifier CHARACTERISTIC_TWO_FIELD = new ASN1ObjectIdentifier(
            "1.2.840.10045.1.2");
    public static final ASN1ObjectIdentifier BASIS_TYPE_ONB = new ASN1ObjectIdentifier(
            "1.2.840.10045.1.2.3.1");
    public static final ASN1ObjectIdentifier BASIS_TYPE_TRINOMIAL = new ASN1ObjectIdentifier(
            "1.2.840.10045.1.2.3.2");
    public static final ASN1ObjectIdentifier BASIS_TYPE_PENTANOMIAL = new ASN1ObjectIdentifier(
            "1.2.840.10045.1.2.3.3");
    private static final ASN1ObjectIdentifier[] oids = {
            // prime field
            PRIME_FIELD,
            // characteristic-two field
            CHARACTERISTIC_TWO_FIELD,
            // gaussian normal basis
            BASIS_TYPE_ONB,
            // trinomial basis
            BASIS_TYPE_TRINOMIAL,
            // pentanomial basis
            BASIS_TYPE_PENTANOMIAL};

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
