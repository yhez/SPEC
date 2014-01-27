package de.flexiprovider.api.keys;

import codec.asn1.ASN1Exception;
import codec.asn1.ASN1ObjectIdentifier;
import codec.asn1.ASN1Type;
import codec.x509.SubjectPublicKeyInfo;
import de.flexiprovider.common.util.ASN1Tools;
import de.flexiprovider.pki.AlgorithmIdentifier;

public abstract class PublicKey implements Key, java.security.PublicKey {


    public final String getFormat() {
        return "X.509";
    }

    public final byte[] getEncoded() {
        AlgorithmIdentifier aid;
        try {
            aid = new AlgorithmIdentifier(getOID(), getAlgParams());
        } catch (ASN1Exception asn1e) {
            throw new RuntimeException("ASN1Exception: " + asn1e.getMessage());
        }
        SubjectPublicKeyInfo spki = new SubjectPublicKeyInfo(aid, getKeyData());
        return ASN1Tools.derEncode(spki);
    }

    protected abstract ASN1ObjectIdentifier getOID();

    protected abstract ASN1Type getAlgParams();

    protected abstract byte[] getKeyData();

}
