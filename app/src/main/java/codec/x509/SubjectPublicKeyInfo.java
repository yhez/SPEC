package codec.x509;

import codec.asn1.ASN1BitString;
import codec.asn1.ASN1Sequence;


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


    public AlgorithmIdentifier getAlgorithmIdentifier() {
        return algorithm_;
    }
}
