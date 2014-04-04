package de.flexiprovider.ec.asn1;

import codec.asn1.ASN1Integer;
import codec.asn1.ASN1Sequence;
import de.flexiprovider.common.util.ASN1Tools;


public class CharacteristicTwoField extends ASN1Sequence {


    public CharacteristicTwoField(int n, int pc1, int pc2, int pc3) {
        super(3);
        add(new ASN1Integer(n));
        add(ECurvesOIDRegistry.BASIS_TYPE_PENTANOMIAL);
        add(new PpBasis(pc1, pc2, pc3));
    }

    public int getN() {
        return ASN1Tools.getFlexiBigInt((ASN1Integer) get(0)).intValue();
    }

}
