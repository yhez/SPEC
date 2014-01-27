package codec.x509;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import codec.CorruptedCodeException;
import codec.InconsistentStateException;
import codec.asn1.ASN1BitString;
import codec.asn1.ASN1Exception;
import codec.asn1.ASN1Sequence;
import codec.asn1.ASN1Type;
import codec.asn1.DERDecoder;


public class SubjectPublicKeyInfo extends ASN1Sequence {

    private AlgorithmIdentifier algorithm_;

    private ASN1BitString encodedKey_;

    public SubjectPublicKeyInfo() {
        super(2);

        algorithm_ = new AlgorithmIdentifier();
        add(algorithm_);

        encodedKey_ = new ASN1BitString();
        add(encodedKey_);
    }


    public SubjectPublicKeyInfo(AlgorithmIdentifier aid, byte[] key) {
        super(2);

        if (aid == null || key == null)
            throw new NullPointerException("Some arg is null!");

        algorithm_ = aid;
        add(algorithm_);
        encodedKey_ = new ASN1BitString(key, 0);
        add(encodedKey_);
    }


    public byte[] getRawKey() {
        return encodedKey_.getBytes();
    }


    public ASN1Type getDecodedRawKey() throws CorruptedCodeException {
        DERDecoder dec;
        ASN1Type raw;

        try {
            dec = new DERDecoder(new ByteArrayInputStream(encodedKey_
                    .getBytes()));

            raw = dec.readType();
            dec.close();

            return raw;
        } catch (ASN1Exception e) {
            throw new CorruptedCodeException("Cannot decode raw key!");
        } catch (IOException e) {
            throw new InconsistentStateException(
                    "Internal, I/O exception caught!");
        }
    }

    public AlgorithmIdentifier getAlgorithmIdentifier() {
        return algorithm_;
    }
}
