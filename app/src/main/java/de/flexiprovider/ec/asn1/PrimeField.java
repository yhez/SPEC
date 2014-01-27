package de.flexiprovider.ec.asn1;

import codec.asn1.ASN1Integer;
import de.flexiprovider.common.math.FlexiBigInt;
import de.flexiprovider.common.util.ASN1Tools;

public class PrimeField extends ASN1Integer {

    public PrimeField(FlexiBigInt order) {
        super(order.toByteArray());
    }
    public FlexiBigInt getQ() {
        return ASN1Tools.getFlexiBigInt(this);
    }

}
