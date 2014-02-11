package de.flexiprovider.pki;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;

import codec.asn1.ASN1;
import codec.asn1.ASN1Exception;
import codec.asn1.ASN1ObjectIdentifier;
import codec.asn1.ASN1Type;
import de.flexiprovider.api.Registry;
import de.flexiprovider.api.parameters.AlgorithmParameters;


public class AlgorithmIdentifier extends codec.x509.AlgorithmIdentifier {


    public AlgorithmIdentifier(ASN1ObjectIdentifier oid, byte[] b)
            throws ASN1Exception {
        super(oid, b);
    }


    public AlgorithmIdentifier(ASN1ObjectIdentifier oid, ASN1Type params)
            throws ASN1Exception {
        super(oid, params);
    }


    public AlgorithmParameters getParams() throws NoSuchAlgorithmException,
            InvalidAlgorithmParameterException {
        AlgorithmParameters params;

        if (parameters_.isOptional()) {
            return null;
        }

        if (parameters_.getTag() == ASN1.TAG_NULL
                && parameters_.getTagClass() == ASN1.CLASS_UNIVERSAL) {
            return null;
        }

        params = Registry.getAlgParams(algorithm_.toString());

        try {
            params.init(parameters_.getEncoded());
        } catch (IOException e) {
            throw new InvalidAlgorithmParameterException(
                    "Caught IOException(\"" + e.getMessage() + "\")");
        } catch (ASN1Exception e) {
            throw new InvalidAlgorithmParameterException(
                    "Caught ASN1Exception(\"" + e.getMessage() + "\")");
        }
        return params;
    }

}
