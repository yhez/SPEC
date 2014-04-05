package de.flexiprovider.pki;

import codec.asn1.ASN1;
import codec.asn1.ASN1ObjectIdentifier;
import de.flexiprovider.api.parameters.AlgorithmParameters;
import de.flexiprovider.ec.parameters.ECParameters;


public class AlgorithmIdentifier extends codec.x509.AlgorithmIdentifier {


    public AlgorithmIdentifier(ASN1ObjectIdentifier oid, byte[] b){
        super(oid, b);
    }


    public AlgorithmParameters getParams() {
        AlgorithmParameters params;

        if (parameters_.isOptional()) {
            return null;
        }

        if (parameters_.getTag() == ASN1.TAG_NULL
                && parameters_.getTagClass() == ASN1.CLASS_UNIVERSAL) {
            return null;
        }

        params = new ECParameters();
        return params;
    }

}
