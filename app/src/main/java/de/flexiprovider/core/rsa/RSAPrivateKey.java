package de.flexiprovider.core.rsa;

import codec.asn1.ASN1Null;
import codec.asn1.ASN1ObjectIdentifier;
import codec.asn1.ASN1Sequence;
import codec.asn1.ASN1Type;
import de.flexiprovider.common.math.FlexiBigInt;
import de.flexiprovider.common.util.ASN1Tools;

public class RSAPrivateKey extends
        de.flexiprovider.core.rsa.interfaces.RSAPrivateKey {

    private FlexiBigInt n;


    private FlexiBigInt d;


    protected RSAPrivateKey(FlexiBigInt n, FlexiBigInt d) {
        this.n = n;
        this.d = d;
    }


    protected RSAPrivateKey(RSAPrivateKeySpec keySpec) {
        this(keySpec.getN(), keySpec.getD());
    }


    public FlexiBigInt getN() {
        return n;
    }

    public FlexiBigInt getD() {
        return d;
    }


    public boolean equals(Object other) {
        if (other == null || !(other instanceof RSAPrivateKey)) {
            return false;
        }

        RSAPrivateKey otherKey = (RSAPrivateKey) other;

        return n.equals(otherKey.n) && d.equals(otherKey.d);

    }

    public int hashCode() {
        return n.hashCode() + d.hashCode();
    }


    public String toString() {
        return "n = 0x" + n.toString(16) + "\n" + "d = 0x" + d.toString(16)
                + "\n";
    }

    protected ASN1ObjectIdentifier getOID() {
        return new ASN1ObjectIdentifier(RSAKeyFactory.OID);
    }

    protected ASN1Type getAlgParams() {
        return new ASN1Null();
    }

    protected byte[] getKeyData() {
        ASN1Sequence keyData = new ASN1Sequence();
        keyData.add(ASN1Tools.createInteger(n));
        keyData.add(ASN1Tools.createInteger(d));
        return ASN1Tools.derEncode(keyData);
    }

}
