package de.flexiprovider.common.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import codec.asn1.ASN1Exception;
import codec.asn1.ASN1Integer;
import codec.asn1.ASN1Type;
import codec.asn1.DERDecoder;
import codec.asn1.DEREncoder;
import de.flexiprovider.common.math.FlexiBigInt;

public final class ASN1Tools {


    private ASN1Tools() {
        // empty
    }


    public static ASN1Integer createInteger(FlexiBigInt value) {
        return new ASN1Integer(de.flexiprovider.my.BigInteger.get(value.bigInt));
    }

    public static FlexiBigInt getFlexiBigInt(ASN1Integer value) {
        return new FlexiBigInt(value.getBigInteger());
    }


    public static byte[] derEncode(ASN1Type type) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DEREncoder encoder = new DEREncoder(baos);
            type.encode(encoder);
            byte[] result = baos.toByteArray();
            encoder.close();
            return result;
        } catch (ASN1Exception e) {
            throw new RuntimeException("ASN1Exception: " + e.getMessage());
        } catch (IOException e) {
            throw new RuntimeException("IOException: " + e.getMessage());
        }
    }


    public static void derDecode(byte[] encoding, ASN1Type type)
            throws ASN1Exception, IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(encoding);
        DERDecoder decoder = new DERDecoder(bais);
        type.decode(decoder);
        decoder.close();
    }

}
