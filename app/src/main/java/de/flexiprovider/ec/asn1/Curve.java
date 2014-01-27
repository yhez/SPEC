package de.flexiprovider.ec.asn1;

import codec.asn1.ASN1OctetString;
import codec.asn1.ASN1Sequence;
import de.flexiprovider.common.math.finitefields.GFElement;


public class Curve extends ASN1Sequence {


    public Curve() {
        super(3);
        add(new ASN1OctetString());
        add(new ASN1OctetString());
    }


    public Curve(GFElement a, GFElement b) {
        super(2);
        add(new ASN1OctetString(filterByteArray(a.toByteArray())));
        add(new ASN1OctetString(filterByteArray(b.toByteArray())));
    }

    byte[] getA() {
        return ((ASN1OctetString) get(0)).getByteArray();
    }

    byte[] getB() {
        return ((ASN1OctetString) get(1)).getByteArray();
    }

    private static byte[] filterByteArray(byte[] array) {
        if ((array[0] == 0) && (array.length > 1)) {
            if (array[1] < 0) {
                int n = array.length - 1;
                byte[] erg = new byte[n];
                System.arraycopy(array, 1, erg, 0, erg.length);
                return erg;
            }
            return array;
        }
        return array;
    }

}
