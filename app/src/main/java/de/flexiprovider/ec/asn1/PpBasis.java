package de.flexiprovider.ec.asn1;

import codec.asn1.ASN1Integer;
import codec.asn1.ASN1Sequence;
import de.flexiprovider.common.util.ASN1Tools;


public class PpBasis extends ASN1Sequence {

    /**
     * Constructor used for decoding.
     */
    public PpBasis() {
        super(3);
        add(new ASN1Integer());
        add(new ASN1Integer());
        add(new ASN1Integer());
    }

    /**
     * Constructor used for encoding.
     *
     * @param pc1 the first value for pentanomial representation
     * @param pc2 the second value for pentanomial representation
     * @param pc3 the third value for pentanomial representation
     */
    public PpBasis(int pc1, int pc2, int pc3) {
        super(3);
        add(new ASN1Integer(pc1));
        add(new ASN1Integer(pc2));
        add(new ASN1Integer(pc3));
    }

    public int getPC1() {
        return ASN1Tools.getFlexiBigInt((ASN1Integer) get(0)).intValue();
    }

    public int getPC2() {
        return ASN1Tools.getFlexiBigInt((ASN1Integer) get(1)).intValue();
    }

    public int getPC3() {
        return ASN1Tools.getFlexiBigInt((ASN1Integer) get(2)).intValue();
    }

}
