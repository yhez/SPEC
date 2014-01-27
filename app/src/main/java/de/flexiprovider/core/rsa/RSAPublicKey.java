package de.flexiprovider.core.rsa;

import codec.asn1.ASN1Null;
import codec.asn1.ASN1ObjectIdentifier;
import codec.asn1.ASN1Sequence;
import codec.asn1.ASN1Type;
import de.flexiprovider.common.math.FlexiBigInt;
import de.flexiprovider.common.util.ASN1Tools;


public final class RSAPublicKey extends
        de.flexiprovider.core.rsa.interfaces.RSAPublicKey {

    private FlexiBigInt n;

    private FlexiBigInt e;

    public RSAPublicKey(FlexiBigInt n, FlexiBigInt e) {
        this.n = n;
        this.e = e;
    }

    public RSAPublicKey(RSAPublicKeySpec keySpec) {
        this(keySpec.getN(), keySpec.getE());
    }

    public FlexiBigInt getN() {
        return n;
    }

    public FlexiBigInt getE() {
        return e;
    }

    public String toString() {
        String result;
        result = "modulus n = 0x" + n.toString(16) + "\n";
        result += "public exponent e = 0x" + e.toString(16);
        return result;
    }

    public boolean equals(Object other) {
        if (other == null || !(other instanceof RSAPublicKey)) {
            return false;
        }

        RSAPublicKey otherKey = (RSAPublicKey) other;

        return n.equals(otherKey.n) && e.equals(otherKey.e);

    }

    public int hashCode() {
        return n.hashCode() + e.hashCode();
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
        keyData.add(ASN1Tools.createInteger(e));
        return ASN1Tools.derEncode(keyData);
    }

}
