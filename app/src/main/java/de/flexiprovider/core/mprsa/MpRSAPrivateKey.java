package de.flexiprovider.core.mprsa;

import codec.asn1.ASN1Integer;
import codec.asn1.ASN1Null;
import codec.asn1.ASN1ObjectIdentifier;
import codec.asn1.ASN1Sequence;
import codec.asn1.ASN1Type;
import de.flexiprovider.common.math.FlexiBigInt;
import de.flexiprovider.common.util.ASN1Tools;
import de.flexiprovider.core.rsa.RSAKeyFactory;
import de.flexiprovider.core.rsa.RSAPrivateCrtKey;


public class MpRSAPrivateKey extends RSAPrivateCrtKey {

    private RSAOtherPrimeInfo[] otherPrimeInfo;

    public MpRSAPrivateKey(FlexiBigInt n, FlexiBigInt e, FlexiBigInt d,
                           FlexiBigInt p, FlexiBigInt q, FlexiBigInt dP, FlexiBigInt dQ,
                           FlexiBigInt crtCoeff, RSAOtherPrimeInfo[] otherPrimeInfo) {
        super(n, d, e, p, q, dP, dQ, crtCoeff);
        this.otherPrimeInfo = otherPrimeInfo;
    }

    public MpRSAPrivateKey(MpRSAPrivateKeySpec keySpec) {
        this(keySpec.getN(), keySpec.getE(), keySpec.getD(), keySpec.getP(),
                keySpec.getQ(), keySpec.getDp(), keySpec.getDq(), keySpec
                .getCRTCoeff(), keySpec.getOtherPrimeInfo());
    }

    public RSAOtherPrimeInfo[] getOtherPrimeInfo() {
        return this.otherPrimeInfo;
    }

    public boolean equals(Object other) {
        if (other == null || !(other instanceof MpRSAPrivateKey)) {
            return false;
        }

        MpRSAPrivateKey otherKey = (MpRSAPrivateKey) other;

        if (n.equals(otherKey.n) && p.equals(otherKey.p)
                && q.equals(otherKey.q) && d.equals(otherKey.d)
                && e.equals(otherKey.e) && dP.equals(otherKey.dP)
                && dQ.equals(otherKey.dQ) && crtCoeff.equals(otherKey.crtCoeff)) {
            RSAOtherPrimeInfo[] otherP = otherKey.getOtherPrimeInfo();
            boolean b = true;
            for (int i = 0; (i < otherP.length) && b; i++) {
                b = b
                        & otherPrimeInfo[i].getPrime().equals(
                        otherP[i].getPrime());
                b = b
                        & otherPrimeInfo[i].getExponent().equals(
                        otherP[i].getExponent());
                b = b
                        & otherPrimeInfo[i].getCrtCoefficient().equals(
                        otherP[i].getCrtCoefficient());
            }
            return b;
        }

        return false;
    }

    public String toString() {
        String out = "";
        out += "modulus n:           0x" + n.toString(16) + "\n";
        out += "public exponent e:   0x" + e.toString(16) + "\n";
        out += "private exponent d:  0x" + d.toString(16) + "\n";
        out += "prime P:             0x" + p.toString(16) + "\n";
        out += "prime Q:             0x" + q.toString(16) + "\n";
        out += "prime exponent P:    0x" + dP.toString(16) + "\n";
        out += "prime exponent Q:    0x" + dQ.toString(16) + "\n";
        out += "crt coefficient:     0x" + crtCoeff.toString(16) + "\n";

        for (int i = 1; i <= otherPrimeInfo.length; i++) {
            out += "prime r" + i + ":            0x"
                    + otherPrimeInfo[i - 1].getPrime().toString(16) + "\n"
                    + "prime exponent d" + i + ":   0x"
                    + otherPrimeInfo[i - 1].getExponent().toString(16) + "\n"
                    + "crt coefficient t" + i + ":  0x"
                    + otherPrimeInfo[i - 1].getCrtCoefficient().toString(16)
                    + "\n";
        }
        return out;
    }

    public int hashCode() {
        int h = n.hashCode() + d.hashCode() + e.hashCode() + p.hashCode()
                + q.hashCode() + dP.hashCode() + dQ.hashCode()
                + crtCoeff.hashCode();

        for (int i = 0; i < otherPrimeInfo.length; i++) {
            h = otherPrimeInfo[i].getPrime().hashCode()
                    + otherPrimeInfo[i].getExponent().hashCode()
                    + otherPrimeInfo[i].getCrtCoefficient().hashCode();
        }
        return h;
    }

    protected ASN1ObjectIdentifier getOID() {
        return new ASN1ObjectIdentifier(RSAKeyFactory.OID);
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

        ASN1Sequence otherPrimesSeq = new ASN1Sequence();
        for (int i = 0; i < otherPrimeInfo.length; i++) {

            ASN1Integer prime_ = ASN1Tools.createInteger(otherPrimeInfo[i]
                    .getPrime());
            ASN1Integer exponent_ = ASN1Tools.createInteger(otherPrimeInfo[i]
                    .getExponent());
            ASN1Integer crtCoefficient_ = ASN1Tools
                    .createInteger(otherPrimeInfo[i].getCrtCoefficient());

            ASN1Sequence seq_i = new ASN1Sequence();
            seq_i.add(prime_);
            seq_i.add(exponent_);
            seq_i.add(crtCoefficient_);

            otherPrimesSeq.add(seq_i);
        }

        keyData.add(otherPrimesSeq);

        return ASN1Tools.derEncode(keyData);
    }

}
