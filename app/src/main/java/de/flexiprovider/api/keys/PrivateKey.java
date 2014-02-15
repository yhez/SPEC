package de.flexiprovider.api.keys;

import codec.asn1.ASN1ObjectIdentifier;
import codec.asn1.ASN1Type;
import codec.pkcs8.PrivateKeyInfo;
import de.flexiprovider.common.util.ASN1Tools;
import de.flexiprovider.pki.AlgorithmIdentifier;

public abstract class PrivateKey implements Key, java.security.PrivateKey {


    public final String getFormat() {
        return "PKCS#8";
    }

    public final byte[] getEncoded() {
        AlgorithmIdentifier aid;
        try {
            aid = new AlgorithmIdentifier(getOID(), getAlgParams());
        } catch (Exception ignored) {
            return null;
        }
        PrivateKeyInfo spki = new PrivateKeyInfo(aid, getKeyData());
        return ASN1Tools.derEncode(spki);
    }


    protected abstract ASN1ObjectIdentifier getOID();

    protected abstract ASN1Type getAlgParams();

    protected abstract byte[] getKeyData();

}
