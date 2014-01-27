package de.flexiprovider.ec.asn1;

import codec.asn1.ASN1Choice;
import codec.asn1.ASN1Null;
import codec.asn1.ASN1ObjectIdentifier;


public class Parameters extends ASN1Choice {

    public Parameters() {
        // create an instance of ASN1Choice with capacity 3.
        super(3);
        addType(new ASN1ObjectIdentifier());
        addType(new ECDomainParameters());
        addType(new ASN1Null());
    }

}
