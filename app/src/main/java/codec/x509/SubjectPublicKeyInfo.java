package codec.x509;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.PublicKey;

import codec.CorruptedCodeException;
import codec.InconsistentStateException;
import codec.asn1.ASN1BitString;
import codec.asn1.ASN1Exception;
import codec.asn1.ASN1Sequence;
import codec.asn1.ASN1Type;
import codec.asn1.DERDecoder;
import codec.asn1.DEREncoder;


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


    public SubjectPublicKeyInfo(AlgorithmIdentifier aid, ASN1Type key) {
        super(2);

        algorithm_ = aid;
        add(algorithm_);
        add(null);
        setRawKey(key);
    }


    public void setPublicKey(PublicKey key) throws InvalidKeyException {
        if (key == null)
            throw new NullPointerException("Key is null!");

        DERDecoder dec;

        clear();

        algorithm_ = new AlgorithmIdentifier();
        add(algorithm_);
        encodedKey_ = new ASN1BitString();
        add(encodedKey_);

        try {
            dec = new DERDecoder(new ByteArrayInputStream(key.getEncoded()));

            decode(dec);
            dec.close();
        } catch (IOException e) {
            throw new InvalidKeyException("Caught IOException!");
        } catch (ASN1Exception e) {
            throw new InvalidKeyException("Bad encoding!");
        }
    }
    protected void setRawKey(ASN1Type key) {
        ByteArrayOutputStream bos;
        DEREncoder enc;

        try {
            bos = new ByteArrayOutputStream();
            enc = new DEREncoder(bos);
            key.encode(enc);
            encodedKey_ = new ASN1BitString(bos.toByteArray(), 0);
            enc.close();
            set(1, encodedKey_);
        } catch (ASN1Exception e) {
            throw new InconsistentStateException("Internal, encoding error!");
        } catch (IOException e) {
            throw new InconsistentStateException(
                    "Internal, I/O exception caught!");
        }
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
