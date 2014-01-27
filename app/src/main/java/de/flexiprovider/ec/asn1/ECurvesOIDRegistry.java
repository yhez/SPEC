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

    /**
     * The ASN.1 types registered for the OID with the same array index in the
     * <tt>oids</tt> field
     */
    private static final Class[] types = {PrimeField.class,
            CharacteristicTwoField.class, GnBasis.class, TpBasis.class,
            PpBasis.class};

    /**
     * The OID -> ASN.1 types mapping used for decoding of the ASN.1 structure
     * registered types. This field is initialized statically.
     */
    private static Hashtable map;

    /**
     * Construct the "OID -> ASN.1 types" mapping via static initialization.
     */
    static {
        map = new Hashtable(oids.length);
        for (int i = 0; i < oids.length; i++) {
            map.put(oids[i], types[i]);
        }
    }

    /**
     * @return the OID -> ASN.1 types mapping
     */
    protected Map getOIDMap() {
        return map;
    }

    /**
     * Return the prefix that is prepended to strings in the mapping returned by
     * {@link #getOIDMap} in order to form the fully qualified class name. This
     * method is not used for {@link ECurvesOIDRegistry} since only class
     * objects are stored in the mapping.
     *
     * @return the prefix of class names in the mapping
     */
    protected String getPrefix() {
        return "de.flexiprovider.ec.asn1ec";
    }

}
