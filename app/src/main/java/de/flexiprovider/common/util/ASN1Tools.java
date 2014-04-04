package de.flexiprovider.common.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import codec.asn1.ASN1Exception;
import codec.asn1.ASN1Integer;
import codec.asn1.ASN1Type;
import codec.asn1.DERDecoder;
import de.flexiprovider.common.math.FlexiBigInt;

public final class ASN1Tools {


    private ASN1Tools() {
        // empty
    }


    public static FlexiBigInt getFlexiBigInt(ASN1Integer value) {
        return new FlexiBigInt(value.getBigInteger());
    }


    public static void derDecode(byte[] encoding, ASN1Type type)
            throws ASN1Exception, IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(encoding);
        DERDecoder decoder = new DERDecoder(bais);
        type.decode(decoder);
        decoder.close();
    }

}
