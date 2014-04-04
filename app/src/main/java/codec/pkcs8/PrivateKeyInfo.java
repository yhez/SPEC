package codec.pkcs8;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import codec.asn1.ASN1Exception;
import codec.asn1.ASN1Integer;
import codec.asn1.ASN1OctetString;
import codec.asn1.ASN1Sequence;
import codec.asn1.ASN1Set;
import codec.asn1.ASN1SetOf;
import codec.asn1.ASN1TaggedType;
import codec.asn1.ASN1Type;
import codec.asn1.DERDecoder;
import codec.x501.Attribute;
import codec.x509.AlgorithmIdentifier;


public class PrivateKeyInfo extends ASN1Sequence {

    public static final int VERSION = 0;

    protected ASN1Integer version_;

    protected AlgorithmIdentifier algorithm_;

    transient private ASN1OctetString encodedKey_;

    protected ASN1Set attributes_;

    public PrivateKeyInfo() {
        version_ = new ASN1Integer(VERSION);
        add(version_);

        algorithm_ = new AlgorithmIdentifier();
        add(algorithm_);

        encodedKey_ = new ASN1OctetString();
        add(encodedKey_);

        attributes_ = new ASN1SetOf(Attribute.class);
        add(new ASN1TaggedType(0, attributes_, false, true));
    }


    public AlgorithmIdentifier getAlgorithmIdentifier() {
        return algorithm_;
    }


    public ASN1Type getDecodedRawKey() throws Exception {
        DERDecoder dec;
        ASN1Type raw;

        try {
            dec = new DERDecoder(new ByteArrayInputStream(encodedKey_
                    .getByteArray()));

            raw = dec.readType();
            dec.close();

            return raw;
        } catch (ASN1Exception e) {
            throw new Exception("Cannot decode raw key!");
        } catch (IOException e) {
            throw new Exception(
                    "Internal, I/O exception caught!");
        }
    }

}
