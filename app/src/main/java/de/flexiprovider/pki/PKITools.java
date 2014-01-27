package de.flexiprovider.pki;

import codec.asn1.ASN1ObjectIdentifier;
import codec.pkcs8.PrivateKeyInfo;
import codec.x509.SubjectPublicKeyInfo;


public final class PKITools {


    private PKITools() {
        // empty
    }

    public static AlgorithmIdentifier getAlgorithmIdentifier(PrivateKeyInfo pki) {
        codec.x509.AlgorithmIdentifier algId = pki.getAlgorithmIdentifier();
        ASN1ObjectIdentifier algOID = algId.getAlgorithmOID();
        try {
            java.security.AlgorithmParameters algParams = algId.getParameters();
            if (algParams == null) {
                return new AlgorithmIdentifier(algOID, (byte[]) null);
            }
            byte[] encParams = algParams.getEncoded();
            return new AlgorithmIdentifier(algOID, encParams);
        } catch (Exception e) {
            throw new RuntimeException(e.getClass().getName() + ": "
                    + e.getMessage());
        }
    }


    public static AlgorithmIdentifier getAlgorithmIdentifier(
            SubjectPublicKeyInfo spki) {
        codec.x509.AlgorithmIdentifier algId = spki.getAlgorithmIdentifier();
        ASN1ObjectIdentifier algOID = algId.getAlgorithmOID();
        try {
            java.security.AlgorithmParameters algParams = algId.getParameters();
            if (algParams == null) {
                return new AlgorithmIdentifier(algOID, (byte[]) null);
            }
            byte[] encParams = algParams.getEncoded();
            return new AlgorithmIdentifier(algOID, encParams);
        } catch (Exception e) {
            throw new RuntimeException(e.getClass().getName() + ": "
                    + e.getMessage());
        }
    }

}
