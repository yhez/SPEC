package de.flexiprovider.core.rsa;

import codec.asn1.ASN1Integer;
import codec.asn1.ASN1Null;
import codec.asn1.ASN1ObjectIdentifier;
import codec.asn1.ASN1Sequence;
import codec.asn1.ASN1Type;
import de.flexiprovider.common.math.FlexiBigInt;
import de.flexiprovider.common.util.ASN1Tools;


public class RSAPrivateCrtKey extends
        de.flexiprovider.core.rsa.interfaces.RSAPrivateCrtKey {


    private static final String RSA_OID_STRING = "1.2.840.113549.1.1.1";


    protected FlexiBigInt n;


    protected FlexiBigInt d;


    protected FlexiBigInt e;


    protected FlexiBigInt p;


    protected FlexiBigInt q;


    protected FlexiBigInt dP;


    protected FlexiBigInt dQ;


    protected FlexiBigInt crtCoeff;


    public RSAPrivateCrtKey(FlexiBigInt n, FlexiBigInt e, FlexiBigInt d,
                            FlexiBigInt p, FlexiBigInt q, FlexiBigInt dP, FlexiBigInt dQ,
                            FlexiBigInt crtCoeff) {
        this.n = n;
        this.d = d;
        this.e = e;
        this.p = p;
        this.q = q;
        this.dP = dP;
        this.dQ = dQ;
        this.crtCoeff = crtCoeff;
    }
    protected RSAPrivateCrtKey(RSAPrivateCrtKeySpec keySpec) {
        this(keySpec.getN(), keySpec.getE(), keySpec.getD(), keySpec.getP(),
                keySpec.getQ(), keySpec.getDp(), keySpec.getDq(), keySpec
                .getCRTCoeff());
    }
    public FlexiBigInt getN() {
        return n;
    }
    public FlexiBigInt getE() {
        return e;
    }
    public FlexiBigInt getD() {
        return d;
    }
    public FlexiBigInt getP() {
        return p;
    }


    public FlexiBigInt getQ() {
        return q;
    }
    public FlexiBigInt getDp() {
        return dP;
    }
    public FlexiBigInt getDq() {
        return dQ;
    }
    public FlexiBigInt getCRTCoeff() {
        return crtCoeff;
    }
    public boolean equals(Object other) {
        if (other == null || !(other instanceof RSAPrivateCrtKey)) {
            return false;
        }

        RSAPrivateCrtKey otherKey = (RSAPrivateCrtKey) other;

        return n.equals(otherKey.n) && p.equals(otherKey.p)
                && q.equals(otherKey.q) && d.equals(otherKey.d)
                && e.equals(otherKey.e) && dP.equals(otherKey.dP)
                && dQ.equals(otherKey.dQ) && crtCoeff.equals(otherKey.crtCoeff);

    }
    public String toString() {
        String out = "";
        out += "modulus n:          0x" + n.toString(16) + "\n";
        out += "public exponent e:  0x" + e.toString(16) + "\n";
        out += "private exponent d: 0x" + d.toString(16) + "\n";
        out += "prime P:            0x" + p.toString(16) + "\n";
        out += "prime Q:            0x" + q.toString(16) + "\n";
        out += "prime exponent P:   0x" + dP.toString(16) + "\n";
        out += "prime exponent Q:   0x" + dQ.toString(16) + "\n";
        out += "crt coefficient:    0x" + crtCoeff.toString(16) + "\n";
        return out;
    }

    public int hashCode() {
        return n.hashCode() + d.hashCode() + e.hashCode() + p.hashCode()
                + q.hashCode() + dP.hashCode() + dQ.hashCode()
                + crtCoeff.hashCode();

    }

    protected ASN1ObjectIdentifier getOID() {
        return new ASN1ObjectIdentifier(RSA_OID_STRING);
    }

    protected ASN1Type getAlgParams() {
        return new ASN1Null();
    }
    protected byte[] getKeyData() {
        ASN1Sequence keyData = new ASN1Sequence();
        keyData.add(new ASN1Integer(0));
        keyData.add(ASN1Tools.createInteger(n));
        keyData.add(ASN1Tools.createInteger(e));
        keyData.add(ASN1Tools.createInteger(d));
        keyData.add(ASN1Tools.createInteger(p));
        keyData.add(ASN1Tools.createInteger(q));
        keyData.add(ASN1Tools.createInteger(dP));
        keyData.add(ASN1Tools.createInteger(dQ));
        keyData.add(ASN1Tools.createInteger(crtCoeff));
        return ASN1Tools.derEncode(keyData);
    }

}
