package codec.pkcs1;

import codec.asn1.ASN1OctetString;
import codec.asn1.ASN1Sequence;
import codec.asn1.ConstraintException;
import codec.x509.AlgorithmIdentifier;

public class DigestInfo extends ASN1Sequence {

    AlgorithmIdentifier aid_ = null;

    ASN1OctetString os_ = null;

    public DigestInfo(AlgorithmIdentifier aid, byte[] digest) {
        super(2);
        aid_ = aid;
        add(aid);
        os_ = new ASN1OctetString();
        try {
            os_.setByteArray(digest);
        } catch (ConstraintException e) {
            System.out.println("internal error:");
            e.printStackTrace();
        }
        add(os_);
    }

    public byte[] getDigest() {
        return os_.getByteArray();
    }
}
