package de.flexiprovider.ec.asn1;

import codec.asn1.ASN1Integer;
import codec.asn1.ASN1Sequence;


public class PpBasis extends ASN1Sequence {

    public PpBasis(int pc1, int pc2, int pc3) {
        super(3);
        add(new ASN1Integer(pc1));
        add(new ASN1Integer(pc2));
        add(new ASN1Integer(pc3));
    }

}
