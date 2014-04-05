package de.flexiprovider.common.util;

import java.io.ByteArrayInputStream;

import codec.asn1.ASN1Type;
import codec.asn1.DERDecoder;

public final class ASN1Tools {


    private ASN1Tools() {
        // empty
    }


    public static void derDecode(byte[] encoding, ASN1Type type){
        ByteArrayInputStream bais = new ByteArrayInputStream(encoding);
        DERDecoder decoder = new DERDecoder(bais);
        type.decode(decoder);
        decoder.close();
    }

}
