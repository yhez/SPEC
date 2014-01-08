package codec.pkcs8;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;

import codec.CorruptedCodeException;
import codec.InconsistentStateException;
import codec.asn1.ASN1Exception;
import codec.asn1.ASN1Integer;
import codec.asn1.ASN1OctetString;
import codec.asn1.ASN1Sequence;
import codec.asn1.ASN1Set;
import codec.asn1.ASN1SetOf;
import codec.asn1.ASN1TaggedType;
import codec.asn1.ASN1Type;
import codec.asn1.DERDecoder;
import codec.asn1.DEREncoder;
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


    public PrivateKeyInfo(AlgorithmIdentifier aid, byte[] key) {
        version_ = new ASN1Integer(VERSION);
        add(version_);

        algorithm_ = aid;
        add(algorithm_);

        encodedKey_ = new ASN1OctetString(key);
        add(encodedKey_);

        attributes_ = new ASN1Set();
        add(new ASN1TaggedType(0, attributes_, false, true));
    }

    public PrivateKeyInfo(AlgorithmIdentifier aid, ASN1Type key) {
        ByteArrayOutputStream bos;
        DEREncoder enc;
        byte[] code;

        version_ = new ASN1Integer(VERSION);
        add(version_);

        algorithm_ = aid;
        add(algorithm_);

        try {
            bos = new ByteArrayOutputStream();
            enc = new DEREncoder(bos);
            key.encode(enc);
            code = bos.toByteArray();
            enc.close();
        } catch (IOException e) {
            throw new InconsistentStateException("Caught IOException!");
        } catch (ASN1Exception e) {
            throw new InconsistentStateException("Caught ASN1Exception!");
        }
        encodedKey_ = new ASN1OctetString(code);
        add(encodedKey_);

        attributes_ = new ASN1Set();
        add(new ASN1TaggedType(0, attributes_, false, true));
    }
    public PrivateKeyInfo(PrivateKey key) throws InvalidKeyException {
        super(2);
        setPrivateKey(key);
    }


    public AlgorithmIdentifier getAlgorithmIdentifier() {
        return algorithm_;
    }


    public PrivateKey getPrivateKey() throws NoSuchAlgorithmException {
        ByteArrayOutputStream bos;
        PKCS8EncodedKeySpec spec;
        DEREncoder enc;
        KeyFactory kf;
        String alg;

        try {
            bos = new ByteArrayOutputStream();
            enc = new DEREncoder(bos);
            encode(enc);
            spec = new PKCS8EncodedKeySpec(bos.toByteArray());
            enc.close();

            alg = algorithm_.getAlgorithmOID().toString();
            kf = KeyFactory.getInstance(alg);

            return kf.generatePrivate(spec);
        } catch (ASN1Exception e) {
            throw new InconsistentStateException("Internal, encoding error!");
        } catch (IOException e) {
            throw new InconsistentStateException(
                    "Internal, I/O exception caught!");
        } catch (InvalidKeySpecException e) {
            throw new InconsistentStateException(
                    "Encoded key spec rejected by key factory!");
        }
    }
    public void setPrivateKey(PrivateKey key) throws InvalidKeyException {
        if (key == null)
            throw new NullPointerException("Key is null!");

        DERDecoder dec;

        clear();

        version_ = new ASN1Integer(VERSION);
        add(version_);

        algorithm_ = new AlgorithmIdentifier();
        add(algorithm_);

        encodedKey_ = new ASN1OctetString();
        add(encodedKey_);

        attributes_ = new ASN1SetOf(Attribute.class);
        add(new ASN1TaggedType(0, attributes_, false, true));

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
    public int getVersion() {
        return version_.getBigInteger().intValue();
    }
    public void setVersion(int version) {
        version_ = new ASN1Integer(version);
        set(0, version_);
    }


    public void setAlgorithm(AlgorithmIdentifier aid) {
        if (aid == null)
            throw new NullPointerException("Algorithm identifier is null!");

        set(1, aid);
        algorithm_ = aid;
    }


    public ASN1Type getDecodedRawKey() throws CorruptedCodeException {
        DERDecoder dec;
        ASN1Type raw;

        try {
            dec = new DERDecoder(new ByteArrayInputStream(encodedKey_
                    .getByteArray()));

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

}
