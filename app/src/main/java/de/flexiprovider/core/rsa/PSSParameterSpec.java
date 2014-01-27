package de.flexiprovider.core.rsa;

import de.flexiprovider.api.parameters.AlgorithmParameterSpec;

/**
 * This class specifies parameters for the RSASSA-PSS signature algorithm
 * (implemented by {@link RSASignaturePSS}). The parameters consist of the OIDs
 * of a hash function and a mask generation function, a salt length, and a
 * trailer field value.
 * <p/>
 * The default hash function is SHA1 (1.3.14.3.2.26). The only supported mask
 * generation function is MGF1 (1.2.840.113549.1.1.8). The default salt length
 * is 20 bytes. The only supported trailer field value is
 * <tt>trailerFieldBC(1)</tt>.
 *
 * @author Martin D�ring
 */
public class PSSParameterSpec implements AlgorithmParameterSpec {

    /**
     * The OID of the default message digest (SHA1, 1.3.14.3.2.26)
     */
    public static final String DEFAULT_MD = "1.3.14.3.2.26";

    /**
     * The OID of the default mask generation function (MGF1)
     */
    public static final String DEFAULT_MGF = "1.2.840.113549.1.1.8";

    /**
     * The default salt length (20 bytes)
     */
    public static final int DEFAULT_SALT_LENGTH = 20;

    /**
     * The default trailer field (<tt>trailerFieldBC</tt>, value 1)
     */
    public static final int DEFAULT_TRAILER_FIELD = 1;

    /**
     * The name of the message digest
     */
    private String md;

    /**
     * The salt length in bytes
     */
    private int saltLength;

    /*
     * Inner classes providing concrete parameter sets
     */

    /**
     * Parameter set (md = "1.3.14.3.2.26" (SHA1), saltLen = 20)
     */
    public static final class SHA1 extends PSSParameterSpec {
    }

    /**
     * Parameter set (md = "2.16.840.1.101.3.4.2.1" (SHA256), saltLen = 32)
     */
    public static final class SHA256 extends PSSParameterSpec {
    }

    /**
     * @return the OID of the message digest
     */
    public String getMD() {
        return md;
    }

    /**
     * @return the salt length in bytes
     */
    public int getSaltLength() {
        return saltLength;
    }

    /**
     * Compare the parameters with another object
     *
     * @param other the other object
     * @return the result of the comparison
     */
    public boolean equals(Object other) {
        if ((other == null) || (!(other instanceof PSSParameterSpec))) {
            return false;
        }
        PSSParameterSpec otherSpec = (PSSParameterSpec) other;

        return md.equals(otherSpec.md) && (saltLength == otherSpec.saltLength);
    }

    /**
     * @return the hash code of the parameters
     */
    public int hashCode() {
        return md.hashCode() + DEFAULT_MGF.hashCode() + saltLength
                + DEFAULT_TRAILER_FIELD;
    }

    /**
     * @return a human readable form of the parameters
     */
    public String toString() {
        String result = "PSS parameters:\n";
        result += "MD OID       : " + md + "\n";
        result += "MGF OID      : " + DEFAULT_MGF + "\n";
        result += "salt length  : " + saltLength + "\n";
        result += "trailer field: " + DEFAULT_TRAILER_FIELD + "\n";
        return result;
    }

}
