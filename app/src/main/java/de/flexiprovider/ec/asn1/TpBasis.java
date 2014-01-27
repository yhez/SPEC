package de.flexiprovider.ec.asn1;

import codec.asn1.ASN1Integer;
import de.flexiprovider.common.util.ASN1Tools;


public class TpBasis extends ASN1Integer {

    public TpBasis(int tc) {
        super(tc);
    }
    public int getTC() {
        return ASN1Tools.getFlexiBigInt(this).intValue();
    }

}
